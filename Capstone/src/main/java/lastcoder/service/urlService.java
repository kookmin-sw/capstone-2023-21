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

			// 파일 바이너리화 & 16진수 데이터형 변환
			info.setBinary_array("0" + binaryEnc(fileToByteArray(filelocation)));
			String binaryfile = info.getBinary_array();
			String hxdresult = "";
			String hxd = "";
			int count = 0;
			int space = 0;
			System.out.println(binaryfile.length());

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
				System.out.println(j);
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
			String mz = "";
			for(int index = 0; index < 2; index++){
				String component = hxdarray[0][index];
				int decimal = Integer.parseInt(component, 16);
				char c = (char)decimal;
				mz = mz + c;
			}

			if(mz.equals("MZ")){
				PEcheck = true;
				System.out.println(PEcheck + " PE 파일 입니다.");
			}
			else{
				System.out.println(PEcheck + " PE파일이 아님 " + mz);
			}

			// e_lfanew로 IMAGE_NT_HEADERS 위치 찾기
			String INH_location = "";
			for(int index = 1; index <= 4; index++){
				String component = hxdarray[64/16-1][16-index];
				INH_location = INH_location + component;
			}
			System.out.println(INH_location);
			int INH_location_index = Integer.parseInt(INH_location,16);

			int row = INH_location_index/16;
			int col = INH_location_index%16;

			System.out.println("행 : " + row + " " + "열 : " + col);

			//IMAGE_NT_HEADERS 위치 확인
			boolean checkINH = false;
			String checkINH_location = "";
			int increase = 0;
			int INH_row = row;
			int INH_col = col;
			for(int index = 0; index < 4; index++){
				INH_col = INH_col + increase;
				if(INH_col >= 16){
					INH_col = INH_col-16;
					INH_row = INH_row+1;
				}
				checkINH_location = checkINH_location + hxdarray[INH_row][INH_col];
				if(increase < 1){
					increase++;
				}
			}
			System.out.println(checkINH_location);
			if(checkINH_location.equals("50450000")){
				checkINH = true;
			}
			System.out.println(checkINH + " " + "INH 시작위치입니다.");
			System.out.println(INH_location_index);

			// INH_location_index 끝위치
			int INH_finish_location_index = INH_location_index + 3;

			// PE파일 섹션 개수
			int numberOfSection_location = INH_finish_location_index + 4;
			row = numberOfSection_location/16;
			col = numberOfSection_location%16;
			String nos = "";
			for(int index = 0; index < 2; index++){
				if(col - index < 0){
					row = row - 1;
					col = 16;
				}
				nos = nos + hxdarray[row][col - index];
			}
			int numberOfSection = Integer.parseInt(nos, 16);
			System.out.println("PE 파일 섹션 개수 : " + numberOfSection);


			// Optional header 크기
			int sizeOfOptionalHeader_location = INH_finish_location_index + 18;
			row = sizeOfOptionalHeader_location/16;
			col = sizeOfOptionalHeader_location%16;
			String sooh = "";
			for(int index = 0; index < 2; index++){
				if(col - index < 0){
					row = row - 1;
					col = 16;
				}
				sooh = sooh + hxdarray[row][col - index];
			}
			int sizeOfOptionalHeader = Integer.parseInt(sooh, 16);
			System.out.println("optionalheader 크기 : " + sizeOfOptionalHeader);

			// Image_file_header 끝나는 지점
			int Image_file_header_finish_location = INH_finish_location_index + 20;

			// BaseOfCode
			int baseofcode_location = Image_file_header_finish_location + 24;
			row = baseofcode_location/16;
			col = baseofcode_location%16;
			String bc = "";
			for(int index = 0; index < 4; index++){
				if(col - index < 0){
					row = row - 1;
					col = 16;
				}
				bc = bc + hxdarray[row][col - index];
			}
			int baseofcode = Integer.parseInt(bc, 16);
			System.out.println("base of code : " + baseofcode);

			// ImageBase
			int image_location = Image_file_header_finish_location + 32;
			row = image_location/16;
			col = image_location%16;
			String image = "";
			for(int index = 0; index < 4; index++){
				if(col - index < 0){
					row = row - 1;
					col = 16;
				}
				image = image + hxdarray[row][col - index];
			}
			int imagebase = Integer.parseInt(image, 16);
			System.out.println("Imagebase : " + imagebase);

			// Image_optional_header 끝나는 지점
			int image_optional_header_finish = Image_file_header_finish_location + sizeOfOptionalHeader;

			// section of number & virtualAddress
			int section_number = 0;
			for(int index = 0; index < numberOfSection; index++) {
				int virtualAddress_location = image_optional_header_finish + (index * 40 + 16);
				String va = "";
				int section_row = virtualAddress_location / 16;
				int section_col = virtualAddress_location % 16;
				for (int va_index = 0; va_index < 4; va_index++) {
					if (section_col - va_index < 0) {
						section_row = row - 1;
						section_col = 16;
					}
					va = va + hxdarray[section_row][section_col - va_index];
				}
				int virtualAddress = Integer.parseInt(va, 16);
				if(virtualAddress == baseofcode){
					section_number = index;
					break;
				}
			}

			// sectiontable_name
			int section_table_name = image_optional_header_finish + (section_number*40+8);
			String sn = "";
			row = section_table_name/16;
			col = section_table_name%16;
			for(int index = 0; index < 8; index++){
				if(col - index < 0){
					row = row -1;
					col = 16;
				}
				String component = hxdarray[row][col - index];
				int num = Integer.parseInt(component, 16);
				char str = (char)num;
				sn = sn + str;
			}
			System.out.println("section_table name : " + sn);

			// characteristics
			int characteristics_location = image_optional_header_finish + (section_number*40 + 40);
			row = characteristics_location/16;
			col = characteristics_location%16;

			String characteristics = hxdarray[row][col];
			System.out.println("파일 속성 : " + characteristics);

			// first packing file quarantine
			if(characteristics.equals("80") || characteristics.equals("a0") || characteristics.equals("c0") || characteristics.equals("e0")){
				System.out.println("패킹 파일 입니다");
			}
			else{
				System.out.println("패킹 파일이 아닙니다.");
			}


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
