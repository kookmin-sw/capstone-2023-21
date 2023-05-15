package lastcoder.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.tomcat.util.codec.binary.Base64;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lastcoder.model.info;

@Service
public class urlService {

	@Autowired
	private info info;
	
//	

	public byte[] base64Enc(byte[] byteArray) {
		return Base64.encodeBase64(byteArray, false);
	}

	public String binaryEnc(byte[] byteArray) {
		String binaryStr = new BigInteger(1, byteArray).toString(2);
		return binaryStr;
	}

	public byte[] fileToByteArray(File file) {
		String out = new String();
		FileInputStream fis = null;
		byte[] fileArray = null;
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

			fileArray = baos.toByteArray();

//			out = new String(base64Enc(fileArray));

			fis.close();
			baos.close();
		} catch (IOException e) {
			System.out.println("Exception position : FileUtil - fileToString(File file)");
		}

//		return out;
		return fileArray;
	}

	public File multipartFileToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		multipartFile.transferTo(file);
		return file;
	}

	public info byteArrayToBinary(MultipartFile multipartFile) throws IOException {

		//File file = new File(file_loaction);
//		 System.out.println("byte_encoding : " + fileToBinary(file));
		info = new info();

		//info.setUrl_info(url_info);
		//info.setFile_location(file_loaction);
		info.setMultipartFile(multipartFile);
		File savefile = multipartFileToFile(multipartFile);
		System.out.println("this is : " + savefile);
		info.setFile(savefile);

		info.setBase64_array(new String(base64Enc(fileToByteArray(savefile))));
		info.setByteArray(fileToByteArray(savefile));

		info.setBinary_array(binaryEnc(fileToByteArray(savefile)));

		byteArrayToImage(info);

		return info;
	}

	public void byteArrayToImage(info info) {

		int[][] imageArray = new int[128][128];
		String binaryArray = info.getBinary_array();
		
		int j, k = 0;
		int tmp = -1;
		System.out.println(binaryArray.length());

		for(int i = 0; 14 <= binaryArray.length() - i ; i += 14){
			System.out.println("i = " + i);
			j = Byte.parseByte(binaryArray.substring(i, i + 7), 2);
			k = Byte.parseByte(binaryArray.substring(i + 7, i + 14), 2);
			 
			System.out.println(j +", " + k);
			if (imageArray[j][k] < 255) {
				imageArray[j][k] += 1;
			}
			tmp = i;
		}
		System.out.println(binaryArray.length() - tmp);

		info.setImageArray(imageArray);
	}

	

	

	public void pythonExec() {

		PythonInterpreter interpreter = new PythonInterpreter();

		interpreter.execfile("D:\\test.py");
		interpreter.exec("print(testFunc(5,10))");

		PyFunction pyFunction = interpreter.get("testFunc", PyFunction.class);

		int a = 10;
		int b = 20;

		PyObject pyObject = pyFunction.__call__(new PyInteger(a), new PyInteger(b));
		System.out.println(pyObject.toString());
	}


}
