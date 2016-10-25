package ee.bpw.dhx.util;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// import javax.mail.MessagingException;


/**
 * Utility methods related to files. e.g. creating files, reading files, zipping and unzipping,
 * base64 endocding and decoding
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public class FileUtil {

  public static final Integer BINARY_BUFFER_SIZE = 100000;


  /**
   * Generates operationsystem temporary file with unique name. File is written to java.io.tmpdir
   * 
   * @return created file
   * @throws IOException - thrown if error occurs while creating file
   * 
   */
  public static File createPipelineFile() throws IOException {
    String tmpDir = System.getProperty("java.io.tmpdir", "");

    String result = tmpDir + File.separator + "dhx_" + String.valueOf((new Date()).getTime());
    int uniqueCounter = 0;
    while ((new File(result)).exists()) {
      ++uniqueCounter;
      result =
          tmpDir + File.separator + "dhl_" + String.valueOf((new Date()).getTime())
              + String.valueOf(uniqueCounter);
    }
    File file = new File(result);
    file.createNewFile();
    return file;
  }

  /**
   * Gets file from classpath or from filesystem by files path.
   * 
   * @param path - path of the file. if start with jar://, then searches from classpath
   * @return - file
   * @throws DhxException - throws if error occurs while getting file
   */
  public static File getFile(String path) throws DhxException {
    File file = null;
    try {

      if (path.startsWith("jar://")) {
        file = new ClassPathResource(path.substring(6)).getFile();
      } else {
        file = new File(path);
      }
      return file;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, "Error while reading file. path:"
          + path + " " + ex.getMessage(), ex);
    }

  }


  /**
   * Gets file from classpath or from filesystem by files path and returns files stream
   * 
   * @param path - path of the file. if start with jar://, then searches from classpath
   * @return - files stream
   * @throws DhxException - throws if error occurs while getting file
   */
  public static InputStream getFileAsStream(String path) throws DhxException {
    InputStream stream = null;
    try {

      if (path.startsWith("http")) {
        URL url = new URL(path);
        stream = url.openStream();
      } else if (path.startsWith("jar://")) {
        stream = new ClassPathResource(path.substring(6)).getInputStream();
      } else {
        stream = new FileInputStream(path);
      }
      return stream;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, "Error while reading file. path:"
          + path + " " + ex.getMessage(), ex);
    }

  }

  /**
   * Returns files stream.
   * 
   * @param file - file to get its stream
   * @return - files stream
   * @throws DhxException - throws if error occcurs while getting files stream
   */
  public static InputStream getFileAsStream(File file) throws DhxException {
    try {
      log.debug("Creating stream for file {}", file.getAbsolutePath());
      return new FileInputStream(file);
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, "Error while reading file. path:"
          + file.getPath() + " " + ex.getMessage(), ex);
    }

  }

  /**
   * Creates file in temporary directory and writes stream to that file.
   * 
   * @param stream - stream to write to file
   * @return - file containing data from stream
   * @throws DhxException - thrown if error occured while creating or writing file
   */
  public static File createFileAndWrite(InputStream stream) throws DhxException {
    try {
      File file = createPipelineFile();
      writeToFile(stream, file);
      return file;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, "Error occured while creating file. "
          + ex.getMessage(), ex);
    }
  }

  /**
   * Writes stream to file.
   * 
   * @param inStream - stream to write to file
   * @param targetFile - file into which write the stream
   * @throws DhxException - thrown if error occured while writing to file
   */
  private static void writeToFile(InputStream inStream, File targetFile) throws DhxException {
    long totalBytesExtracted = 0;
    byte[] buf = new byte[BINARY_BUFFER_SIZE];
    int len;
    BufferedInputStream sourceBuffered = null;
    FileOutputStream out = null;

    try {
      sourceBuffered = new BufferedInputStream(inStream);
      out = new FileOutputStream(targetFile, false);
      while ((len = sourceBuffered.read(buf)) > 0) {
        out.write(buf, 0, len);
        totalBytesExtracted += len;
      }
      safeCloseStream(inStream);
      safeCloseStream(sourceBuffered);
      safeCloseStream(out);
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      log.error("Initial file length: 0, total bytes extracted before error: {}",
          totalBytesExtracted);
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, "Error occured while writing to file. "
          + ex.getMessage(), ex);
    } finally {
      safeCloseStream(inStream);
      safeCloseStream(sourceBuffered);
      safeCloseStream(out);
      out = null;
      buf = null;
    }
  }

  /**
   * Safely closes inputstream.
   * 
   * @param stream - stream to close
   */
  public static void safeCloseStream(InputStream stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (Exception ex) {
        log.error("Error occured while closing stream." + ex.getMessage(), ex);
      } finally {
        stream = null;
      }
    }
  }

  /**
   * Safely closes outputstream.
   * 
   * @param stream - stream to close
   */
  public static void safeCloseStream(OutputStream stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (Exception ex) {
        log.error("Error occured while closing stream." + ex.getMessage(), ex);
      } finally {
        stream = null;
      }
    }
  }

  /**
   * Safely closes reader.
   * 
   * @param reader - reader to close
   */
  public static void safeCloseReader(Reader reader) {
    if (reader != null) {
      try {
        reader.close();
      } catch (Exception ex) {
        log.error("Error occured while closing stream." + ex.getMessage(), ex);
      } finally {
        reader = null;
      }
    }
  }

  /**
   * Safely closes writer.
   * 
   * @param writer - writer to close
   */
  public static void safeCloseWriter(Writer writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (Exception ex) {
        log.error("Error occured while closing stream." + ex.getMessage(), ex);
      } finally {
        writer = null;
      }
    }
  }


  /**
   * Unpacks ZIP file and find file provided in input.
   * 
   * @param zipStream - stream of ZIP file
   * @param fileToFindInZip - file to find inside ZIP file
   * @return - stream of file found
   * @throws DhxException - throws if error occurs while unzipping of during file search
   */
  public static InputStream zipUnpack(InputStream zipStream, String fileToFindInZip)
      throws DhxException {
    try {
      log.debug("Strating zip unpack. Searching for file: {}", fileToFindInZip);
      ZipInputStream zis = new ZipInputStream(zipStream);
      ZipEntry ze;
      log.debug("Zip inputstream created");
      while ((ze = zis.getNextEntry()) != null) {
        log.debug("Zip entry: {}", ze.getName());
        if (ze.getName().equals(fileToFindInZip)) {
          return zis;
        }
      }
      throw new DhxException(DhxExceptionEnum.EXTRACTION_ERROR,
          "Not found expected file in ZIP archive. FILE:" + fileToFindInZip);
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.EXTRACTION_ERROR,
          "Extracting zipped XML file failed!" + ex.getMessage(), ex);
    }

  }

}
