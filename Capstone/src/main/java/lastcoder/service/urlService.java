package lastcoder.service;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;

import org.apache.tomcat.util.codec.binary.Base64;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyList;
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


	// 16진수 값 10진수로 변환 함수
	public int HexToDecimal(int index_location, int byte_size, String hxdarray[][]){
		int row = index_location/16;
		int col = index_location%16;
		String hexresult = "";
		for(int i = 0; i < byte_size; i++){
			if(col - i < 0) {
				row = row - 1;
				col = 16;
			}
			hexresult = hexresult + hxdarray[row][col - i];
		}
		return Integer.parseInt(hexresult, 16);
	}

	public String [][] HxdresultToArray(String hxdresult){

		String hexarray[] = hxdresult.split(" ");
		System.out.println("배열크기 : " + hxdresult.split(" ").length);
		System.out.println("마지막 값 : " + hexarray[hexarray.length-1]);
		String hxdarray[][] = new String[hexarray.length/16][16];

		for(int row = 0; row < hxdarray.length; row++){
			for(int col = 0; col < hxdarray[row].length; col++){
				hxdarray[row][col] = hexarray[row*16 + col];
			}
		}

		// 출력
		//for(int row = 0; row < hxdarray.length; row++){
		//	for(int col = 0; col < hxdarray[row].length; col++){
		//		System.out.print(hxdarray[row][col] + " ");
		//	}
		//	System.out.println();
		//}

		return hxdarray;

	}

	// 바이너리 파일 16진수 변환
	public String BinaryToHxd(String binaryarray){
		int padding = 8 - binaryarray.length() % 8;
		String paddedBinaryString = binaryarray;
		for (int i = 0; i < padding; i++) {
			paddedBinaryString = "0" + paddedBinaryString;
		}

		byte[] byteArray = new byte[paddedBinaryString.length() / 8];
		for (int i = 0; i < byteArray.length; i++) {
			byteArray[i] = (byte) Integer.parseInt(paddedBinaryString.substring(i * 8, (i + 1) * 8), 2);
		}

		ByteBuffer buffer = ByteBuffer.wrap(byteArray);
		StringBuilder hexString = new StringBuilder();
		while (buffer.hasRemaining()) {
			hexString.append(String.format("%02X ", buffer.get()));
		}

		return hexString.toString();
	}


	// 진입점 섹션의 엔트로피 계산 함수
	public double EntryPointEntropy(String filelocation, int offset, int size)throws IOException{
		FileInputStream fis = new FileInputStream(filelocation);
		fis.skip(offset);
		byte[] entryPointData = new byte[size];
		fis.read(entryPointData);
		fis.close();

		// Step 2: 분리한 각 바이트 값의 등장 빈도를 계산합니다.
		int[] freq = new int[256];
		for (byte b : entryPointData) {
			freq[b & 0xFF]++;
		}

		// Step 3: 등장 빈도를 확률 분포로 바꾸어 정보 이론(Shannon entropy)의 엔트로피 공식에 따라 계산합니다.
		double entropy = 0;
		for (int f : freq) {
			if (f > 0) {
				double p = (double) f / entryPointData.length;
				entropy -= p * Math.log(p) / Math.log(2);
			}
		}

		// 엔트로피 값을 출력합니다.
		System.out.println("Entry point entropy: " + entropy);

		return entropy;
	}

	//multipartFile 객체를 File 객체로 변환
	public File multipartFileToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		multipartFile.transferTo(file);
		return file;
	}

	// deeplearning에 PE body데이터를 넘겨주는 함수
	public String deeplearning(String path) {
		String outputStr = null;

		try {
			System.out.println("path : " + path);

			ProcessBuilder processBuilder = new ProcessBuilder("python", "C:\\Users\\82109\\Desktop\\real\\capstone-2023-21\\Capstone\\src\\main\\java\\lastcoder\\service\\file.py", path);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			outputStr = reader.readLine();
			reader.close();

			process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return outputStr;


	}

	// 업로드 파일 삭제 함수
	public void deleteFileUpload(List deletelist){
		for(int i = 0; i < deletelist.size(); i++){
			File file = new File(deletelist.get(i).toString());
			file.delete();
		}
	}


	// 파일 바이너리화
	public info byteArrayToBinary(List<File> list) throws IOException {

		String location = "C:\\Users\\82109\\Desktop\\real\\capstone-2023-21\\Capstone\\quarantine";
		//int nfile = 0;
		//File file = new File(file_loaction);
//		 System.out.println("byte_encoding : " + fileToBinary(file));
		info = new info();
		info.setFlist(list);

		// 삭제할 파일들
		List<String> delete_list = new ArrayList<String>();
		// 파일 이름을 저장
		List<String> name_list = new ArrayList<String>();
		// 파일 패킹 결과 저장 결과
		List<String> packing_list = new ArrayList<String>();
		// 언패킹 결과
		List<String> unpacking_list = new ArrayList<String>();
		// 악성코드 결과
		List<String> malware_list = new ArrayList<String>();


		for(int i=0; i < list.size(); i++){

			String filelocation = list.get(i).toString();
			//info.setBase64_array(new String(base64Enc(fileToByteArray(filelocation))));
			//info.setByteArray(fileToByteArray(filelocation));

			// 삭제할 파일 추가
			delete_list.add(filelocation);

			// 이름 저장
			String name_split [] = filelocation.split("\\\\");
			String name = name_split[name_split.length-1];
			name_list.add(name);

			// 파일 바이너리화 & 16진수 데이터형 변환
			String binaryfile = binaryEnc(fileToByteArray(filelocation));
			String hxdresult = BinaryToHxd(binaryfile);

			// 이중배열로 파일 16진수 데이터로 출력
			String hxdarray[][] = HxdresultToArray(hxdresult);

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
			System.out.println(checkINH + " " + "INH 시작위치");
			System.out.println(INH_location_index);

			// INH_location_index 끝위치
			int INH_finish_location_index = INH_location_index + 3;

			// PE파일 섹션 개수
			int numberOfSection_location = INH_finish_location_index + 4;
			int numberOfSection = HexToDecimal(numberOfSection_location, 2, hxdarray);
			System.out.println("PE 파일 섹션 개수 : " + numberOfSection);

			// Optional header 크기
			int sizeOfOptionalHeader_location = INH_finish_location_index + 18;
			int sizeOfOptionalHeader = HexToDecimal(sizeOfOptionalHeader_location, 2, hxdarray);
			System.out.println("optionalheader 크기 : " + sizeOfOptionalHeader);

			// Image_file_header 끝나는 지점
			int Image_file_header_finish_location = INH_finish_location_index + 20;

			// BaseOfCode
			int baseofcode_location = Image_file_header_finish_location + 24;
			int baseofcode = HexToDecimal(baseofcode_location, 4, hxdarray);
			System.out.println("base of code : " + baseofcode);

			// ImageBase
			int image_location = Image_file_header_finish_location + 32;
			int imagebase = HexToDecimal(image_location, 4, hxdarray);
			System.out.println("Imagebase : " + imagebase);

			// Image_optional_header 끝나는 지점
			int image_optional_header_finish = Image_file_header_finish_location + sizeOfOptionalHeader;

			// section of number & virtualAddress
			int section_number = 0;
			for(int index = 0; index < numberOfSection; index++) {
				int virtualAddress_location = image_optional_header_finish + (index * 40 + 16);
				int virtualAddress = HexToDecimal(virtualAddress_location, 4, hxdarray);
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

			//section_table_offset
			int section_table_offset_location = image_optional_header_finish + (section_number*40 + 24);
			int section_table_offset = HexToDecimal(section_table_offset_location, 4, hxdarray);
			System.out.println("section table offset : " + section_table_offset);

			//section_table_size
			int section_table_size_location = image_optional_header_finish + (section_number*40 + 20);
			int section_table_size = HexToDecimal(section_table_size_location, 4,hxdarray);
			System.out.println("section table size : " + section_table_size);

			// characteristics
			int characteristics_location = image_optional_header_finish + (section_number*40 + 40);
			row = characteristics_location/16;
			col = characteristics_location%16;

			String characteristics = hxdarray[row][col];
			System.out.println("파일 속성 : " + characteristics);

			// 엔트로피
			double entropy = EntryPointEntropy(filelocation, section_table_offset, section_table_size);

			// packing file quarantine(write and entropy)
			if(characteristics.equals("80") || characteristics.equals("A0") || characteristics.equals("C0") || characteristics.equals("E0")){

				if(entropy > 6.85 && entropy < 8){
					System.out.println("패킹 파일 입니다.");

					// 패킹파일 추가
					packing_list.add("O");

					// 언 패킹하기(UPX)
					String packedFilePath = filelocation;
					String upxPath = "C:\\Users\\82109\\Desktop\\real\\capstone-2023-21\\Capstone\\upx-3.95-win64\\upx.exe";
					ProcessBuilder pb = new ProcessBuilder(upxPath, "-d" ,packedFilePath);

					try{
						Process process = pb.start();
						int exitValue = process.waitFor();
						if(exitValue == 0){
							unpacking_list.add("Sccuess");
						}
						else{
							unpacking_list.add("Fail");
						}
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch(IOException e){
						throw  new RuntimeException(e);
					}

					//언 패킹 파일 바이너리화
					String upx_binary =  binaryEnc(fileToByteArray(filelocation));
					hxdresult = BinaryToHxd(upx_binary);

					// 이중배열로 파일 16진수 데이터로 출력
					hxdarray = HxdresultToArray(hxdresult);

					//언 패킹하여 PE헤더 정보다 달라질 수 있기 때문에 다시 검사
					// e_lfanew로 IMAGE_NT_HEADERS 위치 찾기
					INH_location = "";
					for(int index = 1; index <= 4; index++){
						String component = hxdarray[64/16-1][16-index];
						INH_location = INH_location + component;
					}
					System.out.println(INH_location);
					INH_location_index = Integer.parseInt(INH_location,16);

					row = INH_location_index/16;
					col = INH_location_index%16;

					System.out.println("행 : " + row + " " + "열 : " + col);

					//IMAGE_NT_HEADERS 위치 확인
					checkINH = false;
					checkINH_location = "";
					increase = 0;
					INH_row = row;
					INH_col = col;
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
					INH_finish_location_index = INH_location_index + 3;

					// PE파일 섹션 개수
					numberOfSection_location = INH_finish_location_index + 4;
					numberOfSection = HexToDecimal(numberOfSection_location, 2, hxdarray);
					System.out.println("PE 파일 섹션 개수 : " + numberOfSection);

					// Optional header 크기
					sizeOfOptionalHeader_location = INH_finish_location_index + 18;
					sizeOfOptionalHeader = HexToDecimal(sizeOfOptionalHeader_location, 2, hxdarray);
					System.out.println("optionalheader 크기 : " + sizeOfOptionalHeader);

					// Image_file_header 끝나는 지점
					Image_file_header_finish_location = INH_finish_location_index + 20;

					// BaseOfCode
					baseofcode_location = Image_file_header_finish_location + 24;
					baseofcode = HexToDecimal(baseofcode_location, 4, hxdarray);
					System.out.println("base of code : " + baseofcode);

					// ImageBase
					image_location = Image_file_header_finish_location + 32;
					imagebase = HexToDecimal(image_location, 4, hxdarray);
					System.out.println("Imagebase : " + imagebase);

					// Image_optional_header 끝나는 지점
					image_optional_header_finish = Image_file_header_finish_location + sizeOfOptionalHeader;

					// section of number & virtualAddress
					section_number = 0;
					for(int index = 0; index < numberOfSection; index++) {
						int virtualAddress_location = image_optional_header_finish + (index * 40 + 16);
						int virtualAddress = HexToDecimal(virtualAddress_location, 4, hxdarray);
						if(virtualAddress == baseofcode){
							section_number = index;
							break;
						}
					}

					// sectiontable_name
					section_table_name = image_optional_header_finish + (section_number*40+8);
					sn = "";
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

					//section_table_offset
					section_table_offset_location = image_optional_header_finish + (section_number*40 + 24);
					section_table_offset = HexToDecimal(section_table_offset_location, 4, hxdarray);
					System.out.println("section table offset : " + section_table_offset);

					//section_table_size
					section_table_size_location = image_optional_header_finish + (section_number*40 + 20);
					section_table_size = HexToDecimal(section_table_size_location, 4,hxdarray);
					System.out.println("section table size : " + section_table_size);

					// characteristics
					characteristics_location = image_optional_header_finish + (section_number*40 + 40);
					row = characteristics_location/16;
					col = characteristics_location%16;

					characteristics = hxdarray[row][col];
					System.out.println("파일 속성 : " + characteristics);
					double entropy2 = EntryPointEntropy(filelocation, section_table_offset, section_table_size);

				}
				else if(entropy > 5.05 && entropy < 6.69){
					packing_list.add("X");
					unpacking_list.add("-");
					System.out.println("패킹 파일이 아닙니다.");
				}
				else{
					packing_list.add("?");
					unpacking_list.add("-");
					System.out.println("패킹 파일인지 탐지하지 못했습니다.");
				}
			}
			else{
				packing_list.add("X");
				unpacking_list.add("-");
				System.out.println("패킹 파일이 아닙니다.");
			}

			// PE 파일의 body 추출(.text 데이터)
			//nfile++;
			//String str_nfile = Integer.toString(nfile);
			//String PEbody_filepath = location + "//" + str_nfile + ".txt";
			//String[] filearray = hxdresult.split(" ");
			//FileOutputStream outputStream = new FileOutputStream(new File(PEbody_filepath));
			//for(int index = section_table_offset; index < section_table_size; index++){
			//	byte data = (byte) Integer.parseInt(filearray[index], 16);
			//	outputStream.write(data);
			//}
			//outputStream.close();
			//delete_list.add(PEbody_filepath);
			//System.out.println();

			// 악성코드 결과 저장
			String malware = deeplearning(filelocation);
			System.out.println("결과 : " + malware);
			malware_list.add(malware);

		}

		//info.setUrl_info(url_info);
		//info.setFile_location(file_loaction);
		//byteArrayToImage(info);

		info.setDeletelist(delete_list);
		info.setFilenamelist(name_list);
		info.setPacking_result(packing_list);
		info.setUnpacking_result(unpacking_list);
		info.setMalware_result(malware_list);

		//업로드 파일 삭제
		deleteFileUpload(delete_list);

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