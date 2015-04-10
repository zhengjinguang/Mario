package com.lemi.mario.externalmanager.socket;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.externalmanager.manager.MountManager;
import com.lemi.mario.externalmanager.manager.MountManager.MountAction;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class MountSocketHelper {

  private static final String SOCKET_NAME = "recreation";
  private static final String MOUNT_COMMAND = "mountpoint:ext:";

  private static final int EXECUTE_THREAD_NUM = 1;

  private CachedThreadPoolExecutorWithCapacity executeThreadPool;

  private final byte[] socketLock = new byte[0];


  private LocalSocket localSocket;

  public MountSocketHelper() {
    executeThreadPool = new CachedThreadPoolExecutorWithCapacity(EXECUTE_THREAD_NUM);
  }

  public void mountSdCard(final MountAction action) {
    executeThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        mountSdCardBySocket(action);
      }
    });
  }

  private void mountSdCardBySocket(MountAction action) {
    String command;
    if (action == MountAction.MOUNT) {
      command = MOUNT_COMMAND + MountManager.MOUNT_ACTION;
    } else {
      command = MOUNT_COMMAND + MountManager.UN_MOUNT_ACTION;
    }
    connectSocket();
    executeCommand(command);
    closeSocket();
  }

  private boolean executeCommand(String command) {
    checkSocketConnectionAndReconnect();
    OutputStream outputStream = null;

    try {
      synchronized (socketLock) {
        if (localSocket != null) {
          outputStream = localSocket.getOutputStream();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    if (outputStream == null) {
      return false;
    }

    try {
      byte[] buf = command.getBytes();
      outputStream.write(buf);
      outputStream.flush();
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  private void closeSocket() {
    synchronized (socketLock) {
      if (localSocket != null) {
        try {
          localSocket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      localSocket = null;
    }
  }


  private void connectSocket() {
    synchronized (socketLock) {
      localSocket = new LocalSocket();
      LocalSocketAddress socketAddress =
          new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.RESERVED);
      try {
        localSocket.connect(socketAddress);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


  private boolean isSocketConnect() {
    synchronized (socketLock) {
      return localSocket != null && localSocket.isConnected();
    }
  }

  private void checkSocketConnectionAndReconnect() {
    if (!isSocketConnect()) {
      connectSocket();
    }
  }

}
