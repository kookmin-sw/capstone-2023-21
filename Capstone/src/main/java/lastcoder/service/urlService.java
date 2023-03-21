package lastcoder.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.tomcat.util.codec.binary.Base64;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lastcoder.model.info;
import org.springframework.web.multipart.MultipartFile;

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

	public byte[] fileToByteArray(String location) {
		String out = new String();
		FileInputStream fis = null;
		byte[] fileArray = null;
		System.out.println(location);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			fis = new FileInputStream(location);
			System.out.println(fis);

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

	public info byteArrayToBinary(List<File> list) throws IOException {

		String location = "C:\\Users\\82109\\Desktop\\real\\capstone-2023-21\\Capstone\\quarantine";
		//File file = new File(file_loaction);
//		 System.out.println("byte_encoding : " + fileToBinary(file));
		info = new info();
		info.setFlist(list);

		for(int i=0; i < list.size(); i++){

			String filelocation = list.get(i).toString();
			//info.setBase64_array(new String(base64Enc(fileToByteArray(filelocation))));
			//info.setByteArray(fileToByteArray(filelocation));
			info.setBinary_array("0" + binaryEnc(fileToByteArray(filelocation)));
			String binaryfile = info.getBinary_array();
			String hxdresult = "";
			String hxd = "";
			int count = 0;
			int space = 0;

			for (int j = 1; j <= binaryfile.length(); j++){

				hxd = hxd + binaryfile.charAt(j-1);
				count++;

				if(count == 4){
					int binaryToHex = Integer.parseInt(hxd,2);
					String hexString = Integer.toHexString(binaryToHex);
					hxdresult = hxdresult + hexString;
					hxd = "";
					space++;
					count = 0;
				}

				if(space == 2){
					hxdresult = hxdresult + " ";
					space = 0;
				}

			}

			info.setHex_array(hxdresult);

			// 이중배열로 파일 16진수 데이터로 출력
			String hexarray[] = hxdresult.split(" ");
			System.out.println("배열크기 : " + hxdresult.split(" ").length);
			System.out.println("마지막 값 : " + hexarray[hexarray.length-1]);
			String hxdarray[][] = new String[hexarray.length/16][16];

			for(int row = 0; row < hxdarray.length; row++){
				for(int col = 0; col < hxdarray[row].length; col++){
					hxdarray[row][col] = hexarray[row*16 + col];
				}
			}

			for(int row = 0; row < hxdarray.length; row++){
				for(int col = 0; col < hxdarray[row].length; col++){
					System.out.print(hxdarray[row][col] + " ");
				}
				System.out.println();
			}



			// Image_dos_header

			// e_magic 2byte로 "MZ" PE파일 확인
			boolean PEcheck = false;


		}


		//info.setUrl_info(url_info);
		//info.setFile_location(file_loaction);


		//byteArrayToImage(info);

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
