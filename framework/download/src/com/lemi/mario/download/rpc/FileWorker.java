package com.lemi.mario.download.rpc;

import com.lemi.mario.base.utils.FileNameUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class is used to provider file writer to write file in given file.
 * And if the offset is given, it will write file from the offset in this file.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class FileWorker {

  // the file name to write data.
  private String fileName;

  // file wirter
  private RandomAccessFile randomAccessFile;

  /**
   * Constructor of FileWorker.
   * 
   * @param name need contains the fullPath.
   * @throws java.io.FileNotFoundException if file is not found.
   */
  public FileWorker(String name) throws FileNotFoundException {
    this.fileName = name;
    generateFileFolderIfNeed(fileName);
    randomAccessFile = new RandomAccessFile(fileName, "rw");
  }

  /**
   * This function is used to avoid the case below.
   *
   * <p>
   * fileName is /storage/folderName/A.txt If folderName does not exists, will throw FileNotFound
   * Exception
   * </p>
   *
   * @param fileName
   */
  private void generateFileFolderIfNeed(String fileName) {
    String folderPath = FileNameUtil.getFullPath(fileName);
    File rootFile = new File(folderPath);
    if (!rootFile.exists()) {
      rootFile.mkdirs();
    }
  }

  /**
   * Constructor of FileWorker.
   *
   * Init RandomAccessFile fileWriter, and seek to the given offset.
   *
   * @param name
   * @param startPos
   * @throws java.io.FileNotFoundException if file is not found.
   * @throws IllegalStateException Cann't seek to the given position
   */
  public FileWorker(String name, long startPos) throws IOException {
    this(name);
    randomAccessFile.seek(startPos);
  }

  /**
   * Seek file pointer with given offset.
   *
   * @param offset
   * @return operation result
   * @throws java.io.IOException
   */
  public void seek(long offset) throws IOException {
    randomAccessFile.seek(offset);
  }

  /**
   * Write file at the given offset.
   *
   * @param startPos
   * @param data
   * @throws java.io.IOException
   */
  public synchronized void write(long startPos, byte[] data) throws IOException {
    randomAccessFile.seek(startPos);
    randomAccessFile.write(data);
  }

  /**
   * Write file at the current file pointer.
   *
   * @param data
   * @throws java.io.IOException
   */
  public synchronized void write(byte[] data) throws IOException {
    randomAccessFile.write(data);
  }

  /**
   * Close randomAccessFile writer.
   *
   * @throws java.io.IOException
   */
  public synchronized void close() {
    try {
      randomAccessFile.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Write file
   *
   * @param data
   * @param offset
   * @param bytesRead
   *
   * @throws java.io.IOException
   */
  public void write(byte[] data, int offset, int bytesRead) throws IOException {
    randomAccessFile.write(data, offset, bytesRead);
  }
}
