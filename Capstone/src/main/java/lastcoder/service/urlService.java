package lastcoder.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lastcoder.model.info;


@Service
public class urlService {


	@Autowired
	private info info;
	
	public byte[] base64Enc(byte[] buffer) {
	    return Base64.encodeBase64(buffer, false);
	}
	
	
	public String fileToBinary(File file) {
	    String out = new String();
	    FileInputStream fis = null;
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 
	    try {
	        fis = new FileInputStream(file);
	    } catch (FileNotFoundException e) {
	        System.out.println("Exception position : FileUtil - fileToString(File file)");
	    }
	 
	    int len = 0;
	    byte[] buf = new byte[1024];
	    try {
	        while ((len = fis.read(buf)) != -1) {
	            baos.write(buf, 0, len);
	        }
	 
	        byte[] fileArray = baos.toByteArray();
	        
//			String str = new String(fileArray);
//			System.out.println("file Array : " + fileArray.length);
////			System.out.println("file Array : " + base64Enc(fileArray));
////			System.out.println("file Array : " + str);
			  
	        out = new String(base64Enc(fileArray));
	 
	        fis.close();
	        baos.close();
	    } catch (IOException e) {
	        System.out.println("Exception position : FileUtil - fileToString(File file)");
	    }
	 
	    return out;
	}
	 

	
	public info infoAndBinary(String url_info, String file_loaction) throws IOException {
		
		 File file = new File(file_loaction);		 
//		 System.out.println("byte_encoding : " + fileToBinary(file));
		 info = new info();
		 info.setUrl_info(url_info);
		 info.setFile_location(file_loaction);
		 info.setByte_array(fileToBinary(file));
		 
		 return info;	
		 
	}
	
	
	
}
