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

import lastcoder.model.info;

@Service
public class urlService {

	@Autowired
	private info info;

	public byte[] base64Enc(byte[] buffer) {
		return Base64.encodeBase64(buffer, false);
	}

	public String binaryEnc(byte[] buffer) {
		String binaryStr = new BigInteger(1, buffer).toString(2);
		return binaryStr;
	}

	public byte[] fileTobase64(File file) {
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

	public info base64ToBinary(String url_info, String file_loaction) throws IOException {

		File file = new File(file_loaction);
//		 System.out.println("byte_encoding : " + fileToBinary(file));
		
		String binaryArray = binaryEnc(fileTobase64(file));
		
		info = new info();
		info.setUrl_info(url_info);
		info.setFile_location(file_loaction);
		info.setBase64_array(new String(base64Enc(fileTobase64(file))));
		info.setBinary_arry(binaryArray);

		////////////////////////////
		PythonInterpreter interpreter = new PythonInterpreter();

		interpreter.execfile("D:\\test.py");
		interpreter.exec("print(testFunc(5,10))");

		PyFunction pyFunction = interpreter.get("testFunc", PyFunction.class);

		int a = 10;
		int b = 20;

		PyObject pyObject = pyFunction.__call__(new PyInteger(a), new PyInteger(b));
		System.out.println(pyObject.toString());

		return info;
	}

}
