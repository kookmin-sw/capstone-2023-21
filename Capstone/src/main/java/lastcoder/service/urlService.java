package lastcoder.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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

	public byte[] base64Enc(byte[] byteArray) {
		return Base64.encodeBase64(byteArray, false);
	}

	public String binaryEnc(byte[] byteArray) {
		String binaryStr = new BigInteger(1, byteArray).toString(2);
		return binaryStr;
	}

	public byte[] fileToByteArray(File file) {
//		String out = new String();
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

	public info byteArrayToBinary(String url_info, String file_loaction) throws IOException {

		File file = new File(file_loaction);
//		 System.out.println("byte_encoding : " + fileToBinary(file));

		info = new info();

		info.setUrl_info(url_info);
		info.setFile_location(file_loaction);

		info.setBase64_array(new String(base64Enc(fileToByteArray(file))));
		info.setByteArray(fileToByteArray(file));

		info.setBinary_array(binaryEnc(fileToByteArray(file)));

		byteArrayToImage(info);

		return info;
	}

	public void byteArrayToImage(info info) {

		int[][] imageArray = new int[128][128];
		String binaryArray = info.getBinary_array();

		int j, k = 0;

		for (int i = 0; 14 <= binaryArray.length() - i; i += 14) {
			j = Byte.parseByte(binaryArray.substring(i, i + 7), 2);
			k = Byte.parseByte(binaryArray.substring(i + 7, i + 14), 2);
			if (imageArray[j][k] < 255) {
				imageArray[j][k] += 1;
			}
		}

		info.setImageArray(imageArray);
		saveCSV(info);
	}

	public void saveCSV(info info) {
		File csv = new File("D:\\image.csv");
		PrintWriter writer;
		StringBuilder sb = new StringBuilder();

		int[][] imageArray = info.getImageArray();

		try {
			writer = new PrintWriter(csv);

			for (int i = 0; i < imageArray.length; i++) {
				for (int k = 0; k < imageArray[0].length; k++) {
					if (i == imageArray.length - 1 & k == imageArray.length -1) {
						sb.append(imageArray[i][k]);
					} else {
						sb.append(imageArray[i][k] + ", ");
					}
				}
				sb.append('\n');
			}
			writer.write(sb.toString());
			writer.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			// TODO: handle exception
		}
	}

	public void pythonExec(int[][] imageArray) {

		PythonInterpreter interpreter = new PythonInterpreter();

		interpreter.execfile("D:\\test.py");
//		interpreter.exec("print(testFunc(5,10))");

		PyFunction pyFunction = interpreter.get("testFunc", PyFunction.class);

		int a = 10;
		int b = 20;
//		PyObject array = Object( 	imageArray);
		PyObject pyObject = pyFunction.__call__(new PyInteger(a), new PyInteger(b));
//		System.out.println(pyObject.toString());
	}

}
