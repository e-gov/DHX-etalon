package ee.bpw.dhx.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;

import lombok.extern.slf4j.Slf4j;

import org.apache.axis.encoding.Base64;
import org.springframework.core.io.ClassPathResource;

import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;

@Slf4j
public class FileUtil {
	
	private static final Integer binaryBuffeSize = 100000;
	
	public static File extractAndUnpackAttachment (DataHandler attachment) throws DhxException{
		try{
			File file = createPipelineFile(0, "");
			getDataFromDataSource(attachment.getDataSource(), "", file, false);
			gzipUnpackXML(file, false);
			return file;
		}catch(Exception e) {
			throw new DhxException(DHXExceptionEnum.EXCTRACTION_ERROR,  "Error while extracting and unpacking attachment. " + e.getMessage(), e);
		}
	}
	
	/**
	 * Genereerib operatsioonisüsteemi ajutiste failide kataloogi uue unikaalse nimega ajutise faili.
	 *
	 * @param itemIndex		Faili järjekorranumber. Võimaldab vajadusel eristada näiteks tsüklis loodud ajutisi faile.
	 * @param extension		Faililaiend. Võimaldab ajutisele failile vajadusel ka faililaiendi anda.
	 * @return				Faili nimi (absolute path)
	 */
    public static File createPipelineFile(int itemIndex, String extension) throws IOException{
            if (extension == null) {
            	extension = "";
            }
            if ((extension.length() > 0) && !extension.startsWith(".")) {
            	extension = "." + extension;
            }

        	String tmpDir = System.getProperty("java.io.tmpdir", "");

            String result = tmpDir + File.separator + "dhl_" + String.valueOf((new Date()).getTime()) + ((itemIndex > 0) ? "_item" + String.valueOf(itemIndex) : "") + extension;
            int uniqueCounter = 0;
            while ((new File(result)).exists()) {
                ++uniqueCounter;
                result = tmpDir + File.separator + "dhl_" + String.valueOf((new Date()).getTime()) + ((itemIndex > 0) ? "_item" + String.valueOf(itemIndex) : "") + "_" + String.valueOf(uniqueCounter) + extension;
            }
            File file = new File(result);
            file.createNewFile();
            return file;
    }
    
    public static File getClassPathFile (String path) {
    	File file = null;
    	try {
    		InputStream stream = new ClassPathResource(path).getInputStream();
    		file = createPipelineFile(0, "");
    		writeToFile(stream, file);
		}catch(IOException ex) {
			log.error("Error occured while reading dvk capsule XSD." + ex.getMessage(), ex);
			file = null;
		}
		return file;
    }
    
    public static InputStream getClassPathFileStream (String path) throws IOException{
    	return new ClassPathResource(path).getInputStream();
    }
    
    public static File getFile(String path) throws DhxException{
    	File file = null;
    	try{
	    	
	    	if/*(path.startsWith("http")) {
	            URL url = new URL(path);
	            stream = url.openStream();
	        } else if*/(path.startsWith("jar://")) {
	    		file = new ClassPathResource(path.substring(6)).getFile();
	        } else {
	        	file = new File(path);
	        }
	    	return file;
    	}
    	catch(IOException ex) {
    		throw new DhxException(DHXExceptionEnum.FILE_ERROR, "Error while reading file. path:" + path + " " + ex.getMessage(), ex);
    	}

    }
    
    
    public static InputStream getFileAsStream(String path) throws DhxException{
    	InputStream stream = null;
    	try{
	    	
	    	if(path.startsWith("http")) {
	            URL url = new URL(path);
	            stream = url.openStream();
	        } else if(path.startsWith("jar://")) {
	        	stream = new ClassPathResource(path.substring(6)).getInputStream();
	        } else {
	        	stream = new FileInputStream(path);
	        }
	    	return stream;
    	}
    	catch(IOException ex) {
    		throw new DhxException(DHXExceptionEnum.FILE_ERROR, "Error while reading file. path:" + path + " " + ex.getMessage(), ex);
    	}

    }
    
    protected static InputStream getFileAsStream(File file) throws DhxException{
    	try{

	    	return new FileInputStream(file);
    	}
    	catch(IOException ex) {
    		throw new DhxException(DHXExceptionEnum.FILE_ERROR, "Error while reading file. path:" + file.getPath() + " " + ex.getMessage(), ex);
    	}

    }
	 
    public static File createFileAndWrite (InputStream stream) throws DhxException{
    	try{
	    	File file = createPipelineFile(0, "");
	    	writeToFile(stream, file);
	    	return file;
    	} catch(IOException ex) {
    		throw new DhxException(DHXExceptionEnum.FILE_ERROR, "Error occured while creating file. " + ex.getMessage(), ex);
    	}
    }
    
	 public static boolean writeToFile(InputStream inStream, File targetFile) {
	    	long totalBytesExtracted = 0;
	        byte[] buf = new byte[binaryBuffeSize];
	        int len;
	       // FileInputStream sourceStream = null;
	        BufferedInputStream sourceBuffered = null;
	       // GZIPInputStream in = null;
	        FileOutputStream out = null;

	        try {
	            // Init streams needed for uncompressing data
	            //sourceStream = attachmentStream;
	            sourceBuffered = new BufferedInputStream(inStream);
	          //  in = new GZIPInputStream(sourceBuffered);
	           // String targetFile = "try" + ".out";
	            out = new FileOutputStream(targetFile, false);
	           /* if (appendDocumentHeader) {
	                out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><root>".getBytes("UTF-8"));
	            }*/

	            // Uncompress data in input stream
	            while ((len = sourceBuffered.read(buf)) > 0) {
	                out.write(buf, 0, len);
	                totalBytesExtracted += len;
	            }
	           

	            // Paneme failid kinni, et saaks ülearuse faili maha kustutada ja
	            // vajaliku ümber nimetada.
	            safeCloseStream(inStream);
	            safeCloseStream(sourceBuffered);
	            safeCloseStream(out);

	            // Kustutame esialgse faili ja nimetame uue ümber nii,
	            // et see saaks vana asemele
	          /*  (new File(sourceFile)).delete();
	            (new File(targetFile)).renameTo(new File(sourceFile));*/

	            return true;
	        } catch (Exception ex) {
	        	log.error(ex.getMessage(), ex);
	        	log.error("Initial file length: "+ 0 +", total bytes extracted before error: "+ totalBytesExtracted);
	            return false;
	        } finally {
	            safeCloseStream(inStream);
	            safeCloseStream(sourceBuffered);
	           // safeCloseStream(attachmentStream);
	            safeCloseStream(out);

	          //  attachmentStream = null;
	          //  in = null;
	            out = null;
	            buf = null;
	        }
	    }
	 
	 public static void getDataFromDataSource(DataSource source, String transferEncoding, File targetFile, boolean append) throws IOException, MessagingException, NoSuchAlgorithmException{
	            // Väldime andmete korduvat lugemist ja arvutame andmete esmakordsel
	            // lugemisel ühtlasi ka andmete MD5 kontrollsumma
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            InputStream dataStream = new BufferedInputStream(source.getInputStream());

	            OutputStream outStream = new BufferedOutputStream(new FileOutputStream(targetFile, append));
	            InputStream base64DecoderStream = null;

	            // Puhvri pikkus peab jaguma 4-ga, kuna meil on potentsiaalselt
	            // tegemist Base64 kodeeringus andmetega, mille me tahame kohe
	            // ka dekodeerida.
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

	            byte[] digest = md.digest();
	            md = null;
	    }
	 
	 public static File gzipPackXML(File fileToZip) {
		 	if (!fileToZip.exists()) {
	            log.debug("Input file \""+ fileToZip.getPath() +"\" does not exist!");
	            throw new IllegalArgumentException("Data file does not exist!");
	        }
	        long time = Calendar.getInstance().getTimeInMillis();
	        try {
	        	log.debug("Starting packing file. path:" + fileToZip.getPath());
	        	InputStream stream = new FileInputStream(fileToZip);
	            return gzipPackXML(stream, String.valueOf(time), String.valueOf(time + 1));
	        } catch (IOException e) {
	            log.error("Unable to gzip and encode to base64", e);
	            throw new RuntimeException(e);
	        }
	    }
	 
	 
	 public static File gzipPackXML(InputStream streamtoZip) {
	        long time = Calendar.getInstance().getTimeInMillis();
	        try {
	        	log.debug("Starting packing inputstream.");
	            return gzipPackXML(streamtoZip, String.valueOf(time), String.valueOf(time + 1));
	        } catch (IOException e) {
	            log.error("Unable to gzip and encode to base64", e);
	            throw new RuntimeException(e);
	        }
	    }
	 
	 public static File gzipPackXML(InputStream streamtoZip, String orgCode, String requestName) throws IllegalArgumentException, IOException {
		 	File zipOutFile = gzipFile(streamtoZip);
	        String tmpDir = System.getProperty("java.io.tmpdir", "");

	        // Kodeerime pakitud andmed base64 kujule
	        String base64OutFileName = tmpDir + File.separator + "dhl_" + requestName + "_" + orgCode + "_" + String.valueOf((new Date()).getTime()) + "_base64OutBuffer.dat";
	        File base64File = new File(base64OutFileName); 
	        InputStream in = new BufferedInputStream(new FileInputStream(zipOutFile));
	        OutputStream b64out = new BufferedOutputStream(new FileOutputStream(base64File, false));
	        byte[] buf = new byte[66000];  // Puhvri pikkus peaks jaguma 3-ga
	        int len;
	        try {
		        while ((len = in.read(buf)) > 0) {
		            b64out.write(Base64.encode(buf, 0, len).getBytes());
		        }
	        } finally {
		        in.close();
		        b64out.close();
	        }

	        // Kustutame vaheproduktideks olnud failid ära
	        (zipOutFile).delete();

	        return base64File;
	    }
	 
	 public static File gzipFile(InputStream streamToZip) {
	        /*if (!fileToZip.exists()) {
	            log.debug("Input file \""+ fileToZip.getPath() +"\" does not exist!");
	            throw new IllegalArgumentException("Data file does not exist!");
	        }*/
		 log.debug("Starting gziping inputstream.");  
	        String tmpDir = System.getProperty("java.io.tmpdir", "");
	        try {
	            // Pakime andmed kokku
	            String zipOutFileName = tmpDir + File.separator + "dhl_" + UUID.randomUUID().toString() + "_" + String.valueOf((new Date()).getTime()) + "_zipOutBuffer.dat";
	            File zipFile = new File(zipOutFileName);
	            InputStream in = new BufferedInputStream(streamToZip);
	            OutputStream zipOutFile = new BufferedOutputStream(new FileOutputStream(zipFile));
	            GZIPOutputStream out = new GZIPOutputStream(zipOutFile);
	            byte[] buf = new byte[binaryBuffeSize];
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
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	    }
	 
	 public static void gzipUnpackXML(File sourceFile, boolean appendDocumentHeader) throws FileNotFoundException, IOException, DhxException{
	        if (sourceFile == null ) {
	        	throw new DhxException(DHXExceptionEnum.EXCTRACTION_ERROR, "Extracting gzipped XML file failed because file was not supplied!");
	        	
	        }

	        File sourceFileAsObject = sourceFile;
	        if (!sourceFileAsObject.exists()) {
	        	throw new DhxException(DHXExceptionEnum.EXCTRACTION_ERROR, "Extracting gzipped XML file failed because file "+ sourceFile +" does not exist!");
	        }
	        if (sourceFileAsObject.length() < 1) {
	        	throw new DhxException(DHXExceptionEnum.EXCTRACTION_ERROR, "Extracting gzipped XML file failed because file "+ sourceFile +" is empty!");
	        }

	    	long totalBytesExtracted = 0;
	        byte[] buf = new byte[binaryBuffeSize];
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

	            // Paneme failid kinni, et saaks ülearuse faili maha kustutada ja
	            // vajaliku ümber nimetada.
	            safeCloseStream(in);
	            safeCloseStream(sourceBuffered);
	            safeCloseStream(sourceStream);
	            safeCloseStream(out);

	            // Kustutame esialgse faili ja nimetame uue ümber nii,
	            // et see saaks vana asemele
	            sourceFile.delete();
	            File target  = new File(targetFile);
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
	     * Determines if given String is null or empty (zero length).
	     * Whitespace is not treated as empty string.
	     *
	     * @param stringToEvaluate
	     * 		String that will be checked for having NULL or empty value
	     * @return
	     * 		true, if input String is NULL or has zero length
	     */
	    public static boolean isNullOrEmpty(final String stringToEvaluate) {
	    	return ((stringToEvaluate == null) || stringToEvaluate.isEmpty());
	    }
	 
	 public static void safeCloseStream(InputStream s) {
	        if (s != null) {
	            try {
	                s.close();
	            } catch (Exception ex) {
	            } finally {
	                s = null;
	            }
	        }
	    }
	 
	 public static void safeCloseStream(OutputStream s) {
	        if (s != null) {
	            try {
	                s.close();
	            } catch (Exception ex) {
	            } finally {
	                s = null;
	            }
	        }
	    }
	 
	 
	public static InputStream zipUnpack (InputStream zipStream, String fileToFindInZip) throws DhxException{
		try {
		log.debug("Strating zip unpack. Searching for file:"  + fileToFindInZip);
        ZipInputStream zis = 
        		new ZipInputStream(zipStream);
        ZipEntry ze;
        log.debug("Zip inputstream created" );
        ze = zis.getNextEntry();
        while((ze = zis.getNextEntry()) != null) {
        	log.debug("Zip entry:"  + ze.getName());
        	if(ze.getName().equals(fileToFindInZip)) {
        		return zis;
        	//	extractFile(zis, globalConf);
        		//SharedParametersType sharedParameters = XsdUtil.unmarshallCapsule(zis, unmarshaller);
        		//return sharedParameters;
        	} 
        }
        throw new DhxException(DHXExceptionEnum.EXCTRACTION_ERROR, "Not found expected file in ZIP archive. FILE:" + fileToFindInZip);
		} catch(IOException e) {
			throw new DhxException(DHXExceptionEnum.EXCTRACTION_ERROR, "Extracting zipped XML file failed!" + e.getMessage(), e);
		}
        
	}

}
