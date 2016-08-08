package ee.bpw.dhx.util;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;

import lombok.extern.slf4j.Slf4j;

import org.apache.axis.encoding.Base64;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;


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
   * Method extract attachment and unpacks attachment from datahandler and returns as file.
   * Internally base64 decode and gzip unzip is done.
   * 
   * @param attachment - datahandler of the attachment
   * @return unpacked attachment file
   * @throws DhxException - throws if error occurs while unpacking attachment
   */
  public static File extractAndUnpackAttachment(DataHandler attachment) throws DhxException {
    try {
      File file = createPipelineFile();
      getDataFromDataSource(attachment.getDataSource(), "", file, false);
      gzipUnpackXml(file, false);
      return file;
    } catch (Exception ex) {
      throw new DhxException(DhxExceptionEnum.EXTRACTION_ERROR,
          "Error while extracting and unpacking attachment. " + ex.getMessage(), ex);
    }
  }

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

  private static void getDataFromDataSource(DataSource source, String transferEncoding,
      File targetFile, boolean append) throws IOException, MessagingException,
      NoSuchAlgorithmException {
    // We dont want to read data several times, to do everything on the first read(including md5).
    MessageDigest md = MessageDigest.getInstance("MD5");
    InputStream dataStream = new BufferedInputStream(source.getInputStream());

    OutputStream outStream = new BufferedOutputStream(new FileOutputStream(targetFile, append));
    InputStream base64DecoderStream = null;

    // Buffer must be divisible to 4,
    // because we potentially have base 64 data, which we want to
    // decode
    byte[] buf = new byte[65536];
    int len = 0;
    try {
      if (transferEncoding.equalsIgnoreCase("base64")) {
        while ((len = dataStream.read(buf, 0, buf.length)) > 0) {
          md.update(buf, 0, len);
          outStream.write(buf, 0, len);
        }
      } else {
        base64DecoderStream = javax.mail.internet.MimeUtility.decode(dataStream, "base64");
        while ((len = base64DecoderStream.read(buf, 0, buf.length)) > 0) {
          md.update(buf, 0, len);
          outStream.write(buf, 0, len);
        }
      }
    } finally {
      buf = null;
      safeCloseStream(base64DecoderStream);
      safeCloseStream(dataStream);
      safeCloseStream(outStream);
      base64DecoderStream = null;
      dataStream = null;
      outStream = null;
    }
    md = null;
  }

  /**
   * Packs the file with gzip. Internally creates new file for gzipping and returns it.
   * 
   * @param fileToZip - file which needs to be gzipped and base64 encoded
   * @return - gzipped base64 file
   * @throws DhxException - thrown if error occurs while gzipping or base64 encoding file
   */
  public static File gzipPackXml(File fileToZip) throws DhxException {
    if (!fileToZip.exists()) {
      log.debug("Input file \"{}\" does not exist!", fileToZip.getPath());
      throw new IllegalArgumentException("Data file does not exist!");
    }
    long time = Calendar.getInstance().getTimeInMillis();
    try {
      log.debug("Starting packing file. path: {}", fileToZip.getPath());
      InputStream stream = new FileInputStream(fileToZip);
      File zipPackedFile = gzipPackXml(stream, String.valueOf(time), String.valueOf(time + 1));
      return zipPackedFile;
    } catch (IOException ex) {
      log.error("Unable to gzip and encode to base64", ex);
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error occured while gzip and packing fail. " + ex.getMessage(), ex);
    }
  }

  /**
   * Packs the file with gzip.
   * 
   * @param streamtoZip - stream which need to be gzipped
   * @return - gzipped file
   */

  public static File gzipPackXml(InputStream streamtoZip) {
    long time = Calendar.getInstance().getTimeInMillis();
    try {
      log.debug("Starting packing inputstream.");
      return gzipPackXml(streamtoZip, String.valueOf(time), String.valueOf(time + 1));
    } catch (IOException ex) {
      log.error("Unable to gzip and encode to base64", ex);
      throw new RuntimeException(ex);
    }
  }


  private static File gzipPackXml(InputStream streamtoZip, String orgCode, String requestName)
      throws IllegalArgumentException, IOException {
    File zipOutFile = gzipFile(streamtoZip);
    File base64File = createPipelineFile();
    InputStream in = new BufferedInputStream(new FileInputStream(zipOutFile));
    OutputStream b64out = new BufferedOutputStream(new FileOutputStream(base64File, false));
    byte[] buf = new byte[66000]; // buffer must be divisible by 3
    int len;
    try {
      while ((len = in.read(buf)) > 0) {
        b64out.write(Base64.encode(buf, 0, len).getBytes());
      }
    } finally {
      in.close();
      b64out.close();
    }
    // delete temporary unneeded files
    (zipOutFile).delete();

    return base64File;
  }

  private static File gzipFile(InputStream streamToZip) {
    log.debug("Starting gziping inputstream.");
    try {
      // pack the data
      File zipFile = createPipelineFile();
      InputStream in = new BufferedInputStream(streamToZip);
      OutputStream zipOutFile = new BufferedOutputStream(new FileOutputStream(zipFile));
      GZIPOutputStream out = new GZIPOutputStream(zipOutFile);
      byte[] buf = new byte[BINARY_BUFFER_SIZE];
      int len;
      try {
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      } finally {
        in.close();
        out.finish();
        out.close();
      }

      return zipFile;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }


  private static void gzipUnpackXml(File sourceFile, boolean appendDocumentHeader)
      throws FileNotFoundException, IOException, DhxException {
    if (sourceFile == null) {
      throw new DhxException(DhxExceptionEnum.EXTRACTION_ERROR,
          "Extracting gzipped XML file failed because file was not supplied!");

    }

    File sourceFileAsObject = sourceFile;
    if (!sourceFileAsObject.exists()) {
      throw new DhxException(DhxExceptionEnum.EXTRACTION_ERROR,
          "Extracting gzipped XML file failed because file " + sourceFile + " does not exist!");
    }
    if (sourceFileAsObject.length() < 1) {
      throw new DhxException(DhxExceptionEnum.EXTRACTION_ERROR,
          "Extracting gzipped XML file failed because file " + sourceFile + " is empty!");
    }

    long totalBytesExtracted = 0;
    byte[] buf = new byte[BINARY_BUFFER_SIZE];
    int len;
    FileInputStream sourceStream = null;
    BufferedInputStream sourceBuffered = null;
    GZIPInputStream in = null;
    FileOutputStream out = null;

    try {
      // Init streams needed for uncompressing data
      sourceStream = new FileInputStream(sourceFile);
      sourceBuffered = new BufferedInputStream(sourceStream);
      in = new GZIPInputStream(sourceBuffered);
      String targetFile = sourceFile + ".out";
      out = new FileOutputStream(targetFile, false);
      if (appendDocumentHeader) {
        out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><root>".getBytes("UTF-8"));
      }

      // Uncompress data in input stream
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
        totalBytesExtracted += len;
      }
      if (appendDocumentHeader) {
        out.write("</root>".getBytes("UTF-8"));
      }

      // close stream, so we can delete unnecessary file and rename
      safeCloseStream(in);
      safeCloseStream(sourceBuffered);
      safeCloseStream(sourceStream);
      safeCloseStream(out);

      // delete file and rename so new file is like old one
      sourceFile.delete();
      File target = new File(targetFile);
      target.renameTo(sourceFile);
      sourceFile = target;
    } finally {
      safeCloseStream(in);
      safeCloseStream(sourceBuffered);
      safeCloseStream(sourceStream);
      safeCloseStream(out);

      sourceStream = null;
      in = null;
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
