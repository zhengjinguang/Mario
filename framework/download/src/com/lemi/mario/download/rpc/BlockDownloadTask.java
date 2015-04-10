package com.lemi.mario.download.rpc;

import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;

import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.download.http.AndroidHttpClient;
import com.lemi.mario.download.listener.InnerBlockDownloadListener;
import com.lemi.mario.download.listener.NetworkStatusStub;
import com.lemi.mario.download.log.DownloadLog;
import com.lemi.mario.download.utils.CrcCalculator;
import com.lemi.mario.download.utils.Proxy;
import com.lemi.mario.download.utils.StorageUtil;
import com.lemi.mario.download.utils.UrlEncodeUtil;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRouteParams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Sub download task which runs an actual download to get one part of (or the whole) file.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class BlockDownloadTask {
  // buffer size to store the stream
  public static final int BUFFER_SIZE = 4096;
  // buffer size for downloading with speed limit
  public static final int BUFFER_SIZE_WITH_SPEED_LIMIT = 128;
  /**
   * The number of times that the download manager will retry its network
   * operations when no progress is happening before it gives up.
   */
  public static final int MAX_RETRIES = 5;
  /**
   * The maximum number of restarts. More restarts more boring our users
   */
  public static final int MAX_RESTARTS = 2;
  /**
   * The minimum amount of time that the download manager accepts for a
   * Retry-After response header with a parameter in delta-seconds.
   */
  public static final long MIN_RETRY_AFTER = 100L;
  /**
   * The maximum amount of time that the download manager accepts for a
   * Retry-After response header with a parameter in delta-seconds.
   */
  public static final long MAX_RETRY_AFTER = 2000L;
  /**
   * The maximum number of redirects.
   */
  public static final int MAX_REDIRECTS = 5;

  private final Context context;

  // basic info which used in download thread.
  private final InnerBlockDownloadRequest request;

  private final NetworkStatusStub networkStatusStub;

  // listener to notify download status or progress change.
  private final InnerBlockDownloadListener innerBlockDownloadListener;

  private final Executor executor;

  private final Runnable downloadRunnable;

  private final State state;

  private long rangeFrom;

  private final String threadId;

  private CrcCalculator calculator;


  // this is used to prevent checking file existence too much times.
  private static final long MIN_CHECK_FILE_EXISTS_STEP = 1024 * 400L;

  /**
   * modified failed and pending sequence.
   */
  public enum BlockParentStatus {
    SUCCESS(0),
    FAILED(1),
    PENDING(2),
    PAUSED(3),
    RUNNING(4);

    private final int priority;

    private BlockParentStatus(int value) {
      this.priority = value;
    }

    private int getPriority() {
      return priority;
    }
  }

  public enum BlockStatus {
    SUCCESS(BlockParentStatus.SUCCESS),
    PENDING(BlockParentStatus.PAUSED),
    RUNNING(BlockParentStatus.RUNNING),
    QUEUED_FOR_WIFI_OR_USB(BlockParentStatus.PAUSED),
    QUEUED_FOR_MEDIA(BlockParentStatus.PAUSED),
    NO_WRITE_PERMISSION(BlockParentStatus.PAUSED),
    CRC_VERIFY_ERROR(BlockParentStatus.FAILED),
    TOO_MANY_REDIRECTS(BlockParentStatus.FAILED),
    RESOLVE_REDIRECT_URL_FAILED(BlockParentStatus.FAILED),
    EXCEED_MAX_RETRY_TIMES(BlockParentStatus.FAILED),
    DOWNLOAD_SIZE_UNKNOWN(BlockParentStatus.FAILED),
    DOWNLOADED_BYTES_OVERFLOW(BlockParentStatus.FAILED),
    FILE_NOT_FOUND(BlockParentStatus.FAILED),
    STORAGE_NOT_READY(BlockParentStatus.FAILED),
    INSUFFICIENT_STORAGE(BlockParentStatus.FAILED),
    FILE_ERROR(BlockParentStatus.FAILED),
    HTTP_ERROR(BlockParentStatus.FAILED),
    URL_NULL_ERROR(BlockParentStatus.FAILED),
    CONNECTION_TIMEOUT(BlockParentStatus.FAILED),
    UNKNOWN_ERROR(BlockParentStatus.FAILED);

    private final BlockParentStatus parentStatus;

    private BlockStatus(BlockParentStatus parentStatus) {
      this.parentStatus = parentStatus;
    }

    public BlockParentStatus getParentStatus() {
      return parentStatus;
    }

    public int getPriority() {
      return parentStatus.getPriority();
    }
  }

  private enum NetworkType {
    NETWORK_OK /* current network is able to execute download. */,
    NETWORK_NO_CONNECTION /* download can't be executed caused by no network available */,
    NETWORK_WAIT_WIFI_OR_USB /* wifi is disconnected, download has been paused. */
  }


  // TODO(liuchunyu): handle file existing case.
  /**
   * Constructor of download thread.
   * 
   * @param context context
   * @param request block download request
   * @param listener event listener
   * @param networkStatusStub stub to get current network environment
   * @param executor thread pool executor
   */
  public BlockDownloadTask(Context context, InnerBlockDownloadRequest request,
      InnerBlockDownloadListener listener, NetworkStatusStub networkStatusStub,
      Executor executor) {
    this.context = context;
    this.request = request;
    innerBlockDownloadListener = listener;
    this.networkStatusStub = networkStatusStub;
    this.executor = executor;
    this.state = new State(request);
    downloadRunnable = new DownloadRunnable();
    if (request.crcInfos != null && request.crcInfos.size() > 0) {
      calculator = new CrcCalculator(
          request.crcInfos, (request.startPos + request.currentBytes), request.segSize);
    }
    this.threadId = String.valueOf(System.currentTimeMillis());
  }

  public void execute() {
    notifyStatus(BlockStatus.PENDING, state);
    executor.execute(downloadRunnable);
  }

  /**
   * Returns the user agent provided by the initiating app, or use the default
   * one.
   */
  private static String getUserAgent(InnerBlockDownloadRequest mRequest) {
    return mRequest != null && mRequest.userAgent != null
        ? mRequest.userAgent : DownloadConstants.DEFAULT_AGENT;
  }

  /**
   * State to keep download status get from download process.
   */
  private static final class State {
    BlockStatus blockStatus;
    String filePath;
    File destinationChecker;
    String mimeType;
    long retryAfter;
    int redirectCount;
    int restartCount;
    int retriedTimes;
    // Real download url, e.x. origin url -> 302 -> real download url
    String realDownloadUrl;
    // The number of bytes that have been read in this block download
    long bytesRead;
    // Total bytes of this block download
    long expectedTotalBytes;
    String headerETag;
    long checkFileExistBytes;
    long startMoment;
    String headerContentDisposition;
    volatile boolean needToStop;
    int httpStatusCode;
    long contentLength;

    // used to record header log
    String headerStr;

    byte[] fileWorkerLock = new byte[0];
    FileWorker fileWorker;
    byte[] clientLock = new byte[0];
    AndroidHttpClient client;
    byte[] downloadThreadLock = new byte[0];
    Thread downloadThread;

    State(InnerBlockDownloadRequest request) {
      mimeType = sanitizeMimeType(request.mimeType);
      realDownloadUrl = UrlEncodeUtil.encodeURI(request.downloadUrl);
      filePath = request.filePath;
      bytesRead = request.currentBytes;
      if (request.endPos > 0) {
        expectedTotalBytes = request.endPos - request.startPos + 1;
      }
      headerETag = request.eTag;
    }
  }

  private final class DownloadRunnable implements Runnable {

    private Thread runningThread;

    /**
     * Executes the download in a separate thread.
     */
    @Override
    public void run() {
      runningThread = Thread.currentThread();
      PowerManager.WakeLock wakeLock = null;
      BlockStatus blockStatus;
      state.startMoment = System.currentTimeMillis();
      try {

        synchronized (state.downloadThreadLock) {
          checkStopFlag(state);
          state.downloadThread = Thread.currentThread();
        }

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DownloadConstants.TAG);
        wakeLock.acquire();
        synchronized (state.clientLock) {
          checkStopFlag(state);
          state.client = AndroidHttpClient.newInstance(getUserAgent(request), context);
        }
        ConnRouteParams.setDefaultProxy(state.client.getParams(),
            Proxy.getPreferredHttpHost(context, state.realDownloadUrl));
        while (true) {
          try {
            executeDownload(state, state.client);
            break;
          } catch (RetryDownloadException e) {
            e.printStackTrace();
          }
        }
        blockStatus = BlockStatus.SUCCESS;
      } catch (StopDownloadException error) {
        // remove the cause before printing, in case it contains PII
        blockStatus = error.blockStatus;
      } catch (Throwable ex) {
        // sometimes the socket code throws unchecked exceptions
        ex.printStackTrace();
        if (state.needToStop) {
          blockStatus = null;
        } else {
          blockStatus = BlockStatus.UNKNOWN_ERROR;
        }
      } finally {
        if (wakeLock != null) {
          // in some roms, release method maybe throws exception.
          try {
            wakeLock.release();
          } catch (Throwable th) {
            th.printStackTrace();
          }
        }
        synchronized (state.clientLock) {
          if (state.client != null) {
            state.client.close();
          }
        }
        synchronized (state.fileWorkerLock) {
          if (state.fileWorker != null) {
            state.fileWorker.close();
          }
        }
        runningThread = null;
      }
      notifyStatus(blockStatus, state);
    }
  }

  /**
   * it was used when a thread is interrupted or finished.
   * 
   * @param status
   * @param state
   */
  private void notifyStatus(BlockStatus status, State state) {
    if (status == null || state.blockStatus == status || state.needToStop) {
      return;
    }
    state.blockStatus = status;
    innerBlockDownloadListener.onDurationIncreased(request.blockId,
        System.currentTimeMillis() - state.startMoment);
    innerBlockDownloadListener.onBlockStatusChange(request.blockId, status);
  }

  /**
   * Fully execute a single download request - setup and send the request,
   * handle the response, and transfer the data to the destination file.
   */
  private void executeDownload(State state, AndroidHttpClient client)
      throws StopDownloadException, RetryDownloadException {
    if (TextUtils.isEmpty(request.downloadUrl)) {
      // download url is null, give up downloading.
      throw new StopDownloadException(BlockStatus.URL_NULL_ERROR,
          "download url is null");
    }
    resetState();
    HttpGet downloadRequest = createDownloadRequest(state);
    // check just before sending the downloadRequest to avoid using an invalid connection。
    checkConnectivity();

    HttpResponse response;
    InputStream inputStream = null;
    try {
      response = client.execute(downloadRequest);
      // http status code
      handleHttpStatusCode(state, response);
      // handle response header
      processResponseHeaders(state, response);
      inputStream = response.getEntity().getContent();
      // TODO(tangqisen): check all status notification, and make a rule about when to notify.
      // convert status to RUNNING
      notifyStatus(BlockStatus.RUNNING, state);
      transferData(state, inputStream);
    } catch (IOException e) {
      e.printStackTrace();
      handleNetIOException(state, e);
    } finally {
      /**
       * Abort request instead of close stream,
       * see {@link ContentLengthInputStream#close()}, {HttpRequestBase#abort()}.
       */
      downloadRequest.abort();
    }
  }

  private void checkConnectivity() throws StopDownloadException {
    NetworkType networkType = getNetworkType();
    switch (networkType) {
      case NETWORK_NO_CONNECTION:
      case NETWORK_WAIT_WIFI_OR_USB:
        throw new StopDownloadException(BlockStatus.QUEUED_FOR_WIFI_OR_USB,
            "Download can not be executed caused by no wifi or ReverseProxy");
      case NETWORK_OK:
      default:
        // do nothing.
    }
  }

  private NetworkType getNetworkType() {
    NetworkStatusStub.NetworkStatus networkStatus = networkStatusStub.getCurrentNetWorkStatus();
    switch (networkStatus) {
      case NETWORK_USB_CONNECTED:
      case NETWORK_WIFI_CONNECTED:
        return NetworkType.NETWORK_OK;
      case NETWORK_MOBILE_CONNECTED:
        if (request.allowInMobile) {
          return NetworkType.NETWORK_OK;
        } else {
          return NetworkType.NETWORK_WAIT_WIFI_OR_USB;
        }
      case NETWORK_NO_CONNECTION:
      default:
        return NetworkType.NETWORK_NO_CONNECTION;
    }
  }

  /**
   * Transfer as much data as possible from the HTTP response to the
   * destination file.
   * 
   * @param state state
   */
  private void transferData(State state, InputStream inputStream)
      throws StopDownloadException, RetryDownloadException {
    byte[] data;
    if (request.speedLimit > 0) {
      data = new byte[BUFFER_SIZE_WITH_SPEED_LIMIT];
    } else {
      data = new byte[BUFFER_SIZE];
    }
    while (true) {
      long startTime = System.currentTimeMillis();
      int bytesRead = 0;
      try {
        bytesRead = inputStream.read(data);
      } catch (IOException e) {
        e.printStackTrace();
        checkStopFlag(state);
        handleNetIOException(state, e);
      }
      if (bytesRead == -1) {
        assert (state.bytesRead <= state.expectedTotalBytes);
        // input stream eof
        if (state.bytesRead < state.expectedTotalBytes) {
          // Close connect can cause inputstream close and bytesRead = -1
          checkStopFlag(state);
          // Download has been finished, when download task has not reached the end of the file.
          // Download should be retried instantly.
          throw new RetryDownloadException("the state bytesRead is smaller than excepted!");
        }
        break;
      } else {
        if (bytesRead + state.bytesRead > state.expectedTotalBytes) {
          // If received bytes is bigger than expectedTotalBytes, intercept it to make sure OVER
          // FLOW won't happen.
          writeAndUpdateData(state, data, (int) (state.expectedTotalBytes - state.bytesRead));
          break;
        }
        writeAndUpdateData(state, data, bytesRead);
        if (request.speedLimit > 0) {
          long expectedTime = bytesRead * 1000L / request.speedLimit;
          long sleepTime = expectedTime - (System.currentTimeMillis() - startTime);
          if (sleepTime > 0) {
            try {
              Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
              checkStopFlag(state);
            }
          }
        }
      }
    }
  }

  private void writeAndUpdateData(State state, byte[] data, int bytesRead)
      throws StopDownloadException {
    checkCrc(calculator, data, bytesRead);
    writeDataToDestination(state, data, bytesRead);
    checkFileExists(state);
    updateAndReportProgress(data, bytesRead, state);
  }

  private void updateAndReportProgress(byte[] data, int bytesRead, State state) {
    if (state.needToStop) {
      // cancel or pause flag has been set, don't do any work on callback and updating data.
      return;
    }
    state.bytesRead += bytesRead;
    // notify downloadTask that progress has been changed.
    innerBlockDownloadListener.onBlockProgressChange(
        request.blockId, state.bytesRead, data, bytesRead);
  }

  private static void checkCrc(CrcCalculator calculator, byte[] data, int bytesRead)
      throws StopDownloadException {
    if (calculator != null) {
      try {
        calculator.updateAndCheckCrcValues(data, bytesRead);
      } catch (CrcCalculator.CrcVerifiedException e) {
        e.printStackTrace();
        throw new StopDownloadException(BlockStatus.CRC_VERIFY_ERROR, e.getMessage());
      }
    }
  }

  /**
   * Check if the download has been paused or canceled, stopping the request
   * appropriately if it has been.
   */
  private static void checkStopFlag(State state) throws StopDownloadException {
    if (state.needToStop) {
      // stop flag
      throw new StopDownloadException("state is needToStop, let's stop");
    }
  }

  private static void checkExternalMediaMounted(State state) throws StopDownloadException {
    try {
      // unmount media is time-casting process,
      // so pause 0.5 seconds and then check media is mounted
      TimeUnit.MILLISECONDS.sleep(500);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
      checkStopFlag(state);
    }
    if (!StorageUtil.isExternalMediaMounted()) {
      // change exception from STORAGE_NOT_FOUND to QUEUED_FOR_MEDIA
      throw new StopDownloadException(BlockStatus.QUEUED_FOR_MEDIA,
          "external media not mounted while writing destination file");
    }
  }

  /**
   * RandomAccessFile doesn't throw exception when file has been deleted.
   * So here need to check whether file exists manually.
   * 
   * @param state
   * @throws com.lemi.mario.download.rpc.BlockDownloadTask.StopDownloadException
   */
  private static void checkFileExists(State state) throws StopDownloadException {
    if (state.bytesRead - state.checkFileExistBytes > MIN_CHECK_FILE_EXISTS_STEP) {
      if (state.destinationChecker == null) {
        state.destinationChecker = new File(state.filePath);
      }
      if (!state.destinationChecker.exists()) {
        checkStopFlag(state);
        checkExternalMediaMounted(state);
        throw new StopDownloadException(BlockStatus.FILE_NOT_FOUND,
            "download file has been deleted");
      }
      state.checkFileExistBytes = state.bytesRead;
    }
  }

  /**
   * Write a data buffer to the destination file.
   * 
   * @param state download state
   * @param data buffer containing the data to write
   * @param bytesRead how many bytes to write from the buffer
   */
  private void writeDataToDestination(State state, byte[] data, int bytesRead)
      throws StopDownloadException {
    if (TextUtils.isEmpty(state.filePath)) {
      String filePath;
      try {
        filePath =
            StorageUtil.generateSaveFile(request.title, state.realDownloadUrl, request.folderPath,
                state.headerContentDisposition, request.type, state.expectedTotalBytes);
      } catch (StorageUtil.GenerateSaveFileException e) {
        checkStopFlag(state);
        throw new StopDownloadException(BlockStatus.QUEUED_FOR_MEDIA, e.getMessage(), e);
      }
      state.filePath =
          innerBlockDownloadListener.onFilePathDeterminated(request.blockId, filePath);
    }
    try {
      synchronized (state.fileWorkerLock) {
        checkStopFlag(state);
        if (state.fileWorker == null) {
          state.fileWorker = new FileWorker(state.filePath);
          state.fileWorker.seek(rangeFrom);
        }
      }
      state.fileWorker.write(data, 0, bytesRead);
    } catch (IOException e) {
      handleFileIOException(state, e);
    }
  }

  /**
   * handle io exception when writing data to file.
   * 
   * @param state
   * @param e
   * @throws StopDownloadException
   */
  private void handleFileIOException(State state, IOException e) throws StopDownloadException {
    checkStopFlag(state);
    if(!FileUtil.canWrite(state.filePath)){
      throw new StopDownloadException(BlockStatus.NO_WRITE_PERMISSION,
              "no write permission while writing destination file", e);
    }
    long availableBytes = FileUtil.getAvailableBytes(StorageUtil
        .getFilesystemRoot(state.filePath).getAbsolutePath());
    if (availableBytes < request.endPos - (request.startPos + state.bytesRead) + 1) {
      throw new StopDownloadException(BlockStatus.INSUFFICIENT_STORAGE,
          "insufficient space while writing destination file", e);
    }
    checkExternalMediaMounted(state);
    throw new StopDownloadException(BlockStatus.FILE_ERROR, e.getMessage(), e);
  }

  /**
   * This function is used to handle io-exception.
   *
   * <p>
   * If network is available, and not reach MAX_RETRIES, throws RetryDownloadException, else throws
   * StopDownloadException
   * </p>
   *
   * @param state download state
   * @param e IOException
   * @throws com.lemi.mario.download.rpc.BlockDownloadTask.StopDownloadException
   * @throws BlockDownloadTask.RetryDownloadException
   */
  private void handleNetIOException(State state, IOException e)
      throws StopDownloadException, RetryDownloadException {
    checkStopFlag(state);
    NetworkType networkType = getNetworkType();
    switch (networkType) {
      case NETWORK_NO_CONNECTION:
      case NETWORK_WAIT_WIFI_OR_USB:
        throw new StopDownloadException(BlockStatus.QUEUED_FOR_WIFI_OR_USB,
            "WIFI is disconnected, can't getting data", e);
      case NETWORK_OK:
      default:
        if (e instanceof ConnectTimeoutException) {
          throw new StopDownloadException(BlockStatus.CONNECTION_TIMEOUT,
              "unable to connect server.", e);
        }
        if (state.retriedTimes < MAX_RETRIES) {
          int retriedTimes = ++state.retriedTimes;
          try {
            TimeUnit.MILLISECONDS.sleep((1 << retriedTimes) * 500L);
          } catch (InterruptedException exception) {
            checkStopFlag(state);
          }
          throw new RetryDownloadException("meet IO exception, we need retry", e);
        } else {
          // Retried times has been reached.
          // thus give up retrying.
          throw new StopDownloadException(BlockStatus.EXCEED_MAX_RETRY_TIMES,
              "While getting data, IOException has been happens, and retried times has reached"
                  + " MAX_RETRIES");
        }
    }
  }

  /**
   * Read headers from the HTTP response and store them into local state. if the tile and size is
   * unknown, this will try to set tile and size, and then unable to do this, will throw a
   * StopDownloadException.
   */
  private void processResponseHeaders(State state, HttpResponse response)
      throws StopDownloadException {

    // set headerContentDisposition
    Header header = response.getFirstHeader("Content-Disposition");
    if (header != null) {
      state.headerContentDisposition = header.getValue();
    }

    // set mime type
    if (state.mimeType == null) {
      header = response.getFirstHeader("Content-Type");
      if (header != null) {
        state.mimeType = sanitizeMimeType(header.getValue());
      }
      DownloadMessageCenter.getInstance().notifyMimetypeAchieved(request.id, state.mimeType);
    }

    // set and notify etag.
    header = response.getFirstHeader("ETag");
    String etag = (header == null || TextUtils.isEmpty(header.getValue())) ? "" : header.getValue();
    if (header != null && !TextUtils.equals(state.headerETag, etag)) {
      state.headerETag = etag;
      innerBlockDownloadListener.onETagChanged(request.blockId, state.headerETag);
    }

    // set and verify content-length.
    header = response.getFirstHeader("Content-Length");
    if (header != null && !TextUtils.isEmpty(header.getValue())) {
      // put parse process into try-catch block to avoid illegal content-length value.
      try {
        state.contentLength = Long.parseLong(header.getValue());
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    if (state.contentLength > 0 && state.expectedTotalBytes == 0L) {
      // This must be download from the very beginning.
      assert (state.bytesRead == 0);
      state.expectedTotalBytes = state.contentLength;
      if (!request.downloadWithMultiSegments) {
        innerBlockDownloadListener.onTotalSizeConfirmed(request.blockId, state.contentLength);
      }
    }

    // If endPos is unKnown after parsing response header, give up downloading.
    if (state.expectedTotalBytes <= 0) {
      throw new StopDownloadException(BlockStatus.DOWNLOAD_SIZE_UNKNOWN,
          "can't get download size, give up downloading");
    }
  }

  /**
   * Check the HTTP response status and handle anything unusual (e.g. not 200/206).
   */
  private void handleHttpStatusCode(State state, HttpResponse response)
      throws StopDownloadException, RetryDownloadException {
    state.httpStatusCode = response.getStatusLine().getStatusCode();
    if (state.httpStatusCode == HttpStatus.SC_OK && !request.downloadWithMultiSegments) {
      if (state.destinationChecker != null && state.destinationChecker.exists()) {
        state.destinationChecker.delete();
        rangeFrom = 0L;
      }
    } else if (state.httpStatusCode == HttpStatus.SC_PARTIAL_CONTENT) {
      // normal case, do nothing
    } else if (state.httpStatusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
      handleServiceUnavailable(state, response);
    } else if (state.httpStatusCode == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
      handleRestart(state);
    } else if (state.httpStatusCode == HttpStatus.SC_MOVED_PERMANENTLY
        || state.httpStatusCode == HttpStatus.SC_MOVED_TEMPORARILY
        || state.httpStatusCode == HttpStatus.SC_SEE_OTHER
        || state.httpStatusCode == HttpStatus.SC_TEMPORARY_REDIRECT) {
      handleRedirect(state, response);
    } else {
      throw new StopDownloadException(BlockStatus.HTTP_ERROR, "http error " + state.httpStatusCode);
    }
  }

  /**
   * Handle 416 restart download and
   */
  private void handleRestart(State state) throws RetryDownloadException, StopDownloadException {
    if (state.restartCount++ >= MAX_RESTARTS) {
      throw new StopDownloadException(
          BlockStatus.HTTP_ERROR, "http error " + state.httpStatusCode);
    }
    // re-download file.
    state.bytesRead = 0;
    throw new RetryDownloadException("we get '416' response code, which need to restart.");
  }

  /**
   * Handle a 3xx redirect status.
   */
  private void handleRedirect(State state, HttpResponse response)
      throws StopDownloadException, RetryDownloadException {
    if (state.redirectCount++ >= MAX_REDIRECTS) {
      throw new StopDownloadException(BlockStatus.TOO_MANY_REDIRECTS,
          "too many redirects");
    }
    Header header = response.getFirstHeader("Location");
    if (header == null || TextUtils.isEmpty(header.getValue())) {
      throw new StopDownloadException(
          BlockStatus.RESOLVE_REDIRECT_URL_FAILED, "Redirect URI is null");
    }
    state.realDownloadUrl = UrlEncodeUtil.encodeURI(header.getValue());
    throw new RetryDownloadException("we get '" + response.getStatusLine().getStatusCode()
        + "' response code, which need to redirect to " + state.realDownloadUrl);
  }


  /**
   * Handle a 503 Service Unavailable status by processing the Retry-After
   * header.
   */
  private void handleServiceUnavailable(State state, HttpResponse response)
      throws RetryDownloadException, StopDownloadException {
    DownloadLog.v("got HTTP response code 503");
    if (state.retriedTimes++ >= MAX_RETRIES) {
      throw new StopDownloadException(BlockStatus.HTTP_ERROR,
          "http error " + response.getStatusLine().getStatusCode());
    }
    Header header = response.getFirstHeader("Retry-After");
    if (header != null) {
      DownloadLog.v("Retry-After :" + header.getValue());
      try {
        state.retryAfter = Integer.parseInt(header.getValue()) * 1000L;
      } catch (NumberFormatException ex) {
        // ignored - retryAfter stays 0 in this case.
        ex.printStackTrace();
      }
      if (state.retryAfter < MIN_RETRY_AFTER) {
        state.retryAfter = MIN_RETRY_AFTER;
      } else if (state.retryAfter > MAX_RETRY_AFTER) {
        state.retryAfter = MAX_RETRY_AFTER;
      }
    } else {
      state.retryAfter = 1000L;
    }
    try {
      Thread.sleep(state.retryAfter);
    } catch (InterruptedException e) {
      checkStopFlag(state);
    }
    throw new RetryDownloadException(
        "we get '503' response code, which means the server is unavailavle, sleep then retry.");
  }

  private void resetState() {
    state.redirectCount = 0;
    state.startMoment = System.currentTimeMillis();
  }

  /**
   * Stops this block download. Note that this should return in very short time.
   */
  public void stop() {
    if (state.needToStop) {
      return;
    }
    state.needToStop = true;
    synchronized (state.clientLock) {
      if (state.client != null) {
        state.client.close();
      }
    }
    synchronized (state.fileWorkerLock) {
      if (state.fileWorker != null) {
        state.fileWorker.close();
      }
    }
    synchronized (state.downloadThreadLock) {
      if (state.downloadThread != null) {
        state.downloadThread.interrupt();
        state.downloadThread = null;
      }
    }
  }

  public BlockStatus getStatus() {
    return state.blockStatus;
  }

  public int getHttpStatusCode() {
    return state.httpStatusCode;
  }

  public int getBlockId() {
    return request.blockId;
  }

  /**
   * Creates download request.
   * 
   * @throws com.lemi.mario.download.rpc.BlockDownloadTask.StopDownloadException
   */
  private HttpGet createDownloadRequest(State state) {
    HttpGet httpGet = new HttpGet(state.realDownloadUrl);

    if (useRange()) {
      long requestStart = request.startPos + state.bytesRead;
      rangeFrom = requestStart;
      // TODO lantian confirm the logic about totalSize and requestEnd, and the relationship
      // between request.isOscar.
      long totalSize = request.endPos - request.startPos + 1;
      // request.endPos may be the zero, consider using state.expectedTotalBytes instead of
      // request.endPos, because state.expectedTotalBytes can be updated when content-length
      // is fetched.
      long requestEnd = request.endPos;
      if (requestStart > requestEnd) {
        // TODO lantian add log about requestStart > requestEnd bad case.
        httpGet.addHeader("Range", "bytes=" + requestStart + "-");
      } else {
        httpGet.addHeader("Range", "bytes=" + requestStart + "-" + requestEnd);
      }
      // add refer for wdjcdn
      if (state.realDownloadUrl.contains("wdjcdn.com")) {
        httpGet.addHeader("Referer", "http://android.wdjcdn.com/");
      }

      // Multi block may download from different address, etag may be different.
      // But the logic on etag can only deal with one etag currently,
      // so we don't deal with multi-thread downloading case yet.
      if (!request.downloadWithMultiSegments && !TextUtils.isEmpty(state.headerETag)) {
        httpGet.addHeader("If-Match", state.headerETag);
      }
      HeaderIterator iterator = httpGet.headerIterator();
      StringBuilder sb = new StringBuilder();
      while (iterator.hasNext()) {
        Header header = iterator.nextHeader();
        sb.append(header.getName()).append(":");
        sb.append(header.getValue()).append(";");
      }
      state.headerStr = sb.toString();
    }
    return httpGet;
  }

  private boolean useRange() {
    // for breakpoint resume, startPos + currentBytes > 0
    // for using multi blocks.
    return request.startPos + state.bytesRead > 0 || request.downloadWithMultiSegments;
  }

  /**
   * Clean up a mimeType string so it can be used to dispatch an intent to view a downloaded asset.
   * 
   * @param mimeType either null or one or more mime types (semi colon separated)
   */
  private static String sanitizeMimeType(String mimeType) {
    if (TextUtils.isEmpty(mimeType)) {
      return null;
    }
    mimeType = mimeType.trim().toLowerCase(Locale.ENGLISH);
    final int semicolonIndex = mimeType.indexOf(';');
    if (semicolonIndex > 0) {
      mimeType = mimeType.substring(0, semicolonIndex);
    }
    return mimeType;
  }


  /**
   * Raised to indicate that the current request should be stopped immediately.
   * 
   * Note the message passed to this exception will be logged and therefore must
   * be guaranteed not to contain any PII, meaning it generally can't include any
   * information about the request URI, headers, or destination filename.
   */
  private static final class StopDownloadException extends Exception {

    private final BlockStatus blockStatus;

    private StopDownloadException(String message) {
      this(null, message);
    }

    private StopDownloadException(BlockStatus blockStatus, String message) {
      this(blockStatus, message, null);
    }

    private StopDownloadException(BlockStatus blockStatus, String message, Throwable e) {
      super(message, e);
      this.blockStatus = blockStatus;
    }
  }

  /**
   * Raised from methods called by executeDownload() to indicate that the
   * download should be retried immediately.
   */
  private static class RetryDownloadException extends Exception {

    private RetryDownloadException(String detailMessage) {
      super(detailMessage);
    }

    private RetryDownloadException(String detailMessage, Throwable throwable) {
      super(detailMessage, throwable);
    }

    private RetryDownloadException(Throwable throwable) {
      super(throwable);
    }
  }

}
