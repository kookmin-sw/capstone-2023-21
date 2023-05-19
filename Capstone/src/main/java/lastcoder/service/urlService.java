package lastcoder.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import lastcoder.model.PEFile;
import lastcoder.model.file_Name;
import lastcoder.model.file_info;

@Service
public class urlService {

	@Autowired
	private file_info file_info;

	@Autowired
	private fileAnalyze fileAnalyze;

	@Autowired
	private PEFile PEFile;
	
	@Autowired
	private file_Name file_Name;
	
	private List<String> file_Name_List;
	

	// 현재 위치 경로
	private final static String currentDir = System.getProperty("user.dir");
	// 업로드할 파일 경로
	private final static String upload_filePath = currentDir + File.separator + "Capstone\\quarantine";

    private final static List<String> write_characteristics = Arrays.asList("A0", "C0", "E0");

	
	// PE파일 분류 함수
	public List<File> checked_PEfile(List<MultipartFile> multiFile) throws IOException {

		// PE파일 확장자들을 저장한 리스트
		List<PEFile> peList = PEFile.getPEList();

		List<File> PEfile_list = new ArrayList<>();
		
		File uploadFile;

		for (MultipartFile file : multiFile) {
			String fileName = file.getOriginalFilename();
			String[] extension = fileName.split("\\.");

			if (peList.stream().anyMatch(ext -> ext.getExtension().equals(extension[extension.length - 1]))) {
				// 업로드할 경로에 파일 생성
				uploadFile = new File(upload_filePath + File.separator + fileName);
				try {
					// 입력 받은 파일을 지정한 경로(upload)에 저장
					file.transferTo(uploadFile);
					PEfile_list.add(uploadFile);
					file_Name.set_List(fileName);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return PEfile_list;

	}

	public List<byte[]> convertPEFileToBytes(List<File> PEfile_list) throws IOException {

		List<byte[]> dataList = new ArrayList<>();
		for (File PEfile : PEfile_list) {
			try (FileInputStream fis = new FileInputStream(PEfile)) {
				byte[] data = new byte[(int) PEfile.length()];
				fis.read(data);
				dataList.add(data);
			}
		}
		return dataList;
	}

	// 바이트 배열을 바이너리로 변환
	public List<String> binaryEnc(List<byte[]> Byte_list) {
		List<String> binaryStr_List = new ArrayList<>();
		for (byte[] b : Byte_list) {
			String tmp_str = new BigInteger(1, b).toString(2);
			binaryStr_List.add(tmp_str);
		}
		return binaryStr_List;
	}

	// 바이너리 파일 16진수 변환
	public List<String> BinaryToHxd(List<String> binaryStr_List) {
		List<String> hxd_list = new ArrayList<>();

		for (String str : binaryStr_List) {
			int padding = 8 - str.length() % 8;
			String paddedBinaryString = str;
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
			hxd_list.add(hexString.toString());
		}
		return hxd_list;
	}

	// 16진수 데이터 이중배열로 저장
	public List<String[][]> HxdresultToArray(List<String> hxd_list) {

		List<String[][]> hxdArray = new ArrayList<>();

		for (String str : hxd_list) {
			String hexarray[] = str.split(" ");
			String hxdarray[][] = new String[hexarray.length / 16][16];

			for (int row = 0; row < hxdarray.length; row++) {
				for (int col = 0; col < hxdarray[row].length; col++) {
					hxdarray[row][col] = hexarray[row * 16 + col];
				}
			}
			hxdArray.add(hxdarray);
		}
		return hxdArray;
	}

	// 파일을 분석하여 패킹 결과와 언패킹 결과를 알아내는 함수
	public void detectPackAndUnpack(List<String[][]> hxdarray) throws IOException {
		// Image_dos_header
		// e_magic 2byte로 "MZ" PE파일 확인
		for (String[][] str : hxdarray) {
			fileAnalyze.isPEFile(str);

			// e_lfanew로 IMAGE_NT_HEADERS 위치 찾기
			int INH_location_index = fileAnalyze.findINHLocation(str);
			// IMAGE_NT_HEADERS 위치 확인
			fileAnalyze.checkINH(str, INH_location_index);

			// INH_location_index 끝위치
			int INH_finish_location_index = INH_location_index + 3;

			// PE파일 섹션 개수
			int numberOfSection_location = INH_finish_location_index + 4;
			int numberOfSection = fileAnalyze.getFieldData(str, numberOfSection_location, 2);
			System.out.println("섹션 개수 : " + numberOfSection);

			// Optional header 크기
			int sizeOfOptionalHeader_location = INH_finish_location_index + 18;
			int sizeOfOptionalHeader = fileAnalyze.getFieldData(str, sizeOfOptionalHeader_location, 2);
			System.out.println("optionalheader 크기 : " + sizeOfOptionalHeader);

			// Image_file_header 끝나는 지점
			int Image_file_header_finish_location = INH_finish_location_index + 20;

			// BaseOfCode
			int baseofcode_location = Image_file_header_finish_location + 24;
			int baseofcode = fileAnalyze.getFieldData(str, baseofcode_location, 4);
			System.out.println("base of code : " + baseofcode);

			// ImageBase
			int image_location = Image_file_header_finish_location + 32;
			int imagebase = fileAnalyze.getFieldData(str, image_location, 4);
			System.out.println("Imagebase : " + imagebase);

			// Image_optional_header 끝나는 지점
			int image_optional_header_finish = Image_file_header_finish_location + sizeOfOptionalHeader;

			// basecode와 virtualAddress 같은 섹션 찾기
			int section_number = fileAnalyze.findSection(baseofcode, numberOfSection, image_optional_header_finish,
					str);

			// sectiontable_name 확인하기
			int section_table_name = image_optional_header_finish + (section_number * 40 + 8);
			fileAnalyze.getSectionName(section_table_name, str);

			// section_table_offset
			int section_table_offset_location = image_optional_header_finish + (section_number * 40 + 24);
			int section_table_offset = fileAnalyze.getFieldData(str, section_table_offset_location, 4);
			System.out.println("section table offset : " + section_table_offset);

			// section_table_size
			int section_table_size_location = image_optional_header_finish + (section_number * 40 + 20);
			int section_table_size = fileAnalyze.getFieldData(str, section_table_size_location, 4);
			System.out.println("section table size : " + section_table_size);

			// characteristics
			int characteristics_location = image_optional_header_finish + (section_number * 40 + 40);
			String characteristics = fileAnalyze.getCharacteristics(str, characteristics_location);
			System.out.println("파일 속성 : " + characteristics);

			// 엔트로피
			double entropy = EntryPointEntropy(section_table_offset, section_table_size);

			// 패킹 파일 탐지
			if (write_characteristics.contains(characteristics) && entropy > 6.85 && entropy < 8) {
				file_Name_List = file_Name.get_List();
				fileAnalyze.unPacking(file_Name_List, currentDir, upload_filePath);
			}
		}
	}

	// 진입점 섹션의 엔트로피 계산 함수
	public double EntryPointEntropy(int offset, int size) throws IOException {

		byte[] entryPointData = new byte[size];

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


	
	public void run_inference() {
		 try {
	            // 아나콘다 가상머신 실행 및 현재 위치 변경
	            String[] commandVm = {"conda", "activate", "python_VM"};  // 아나콘다 가상머신 실행 명령어
	            Process processVm = Runtime.getRuntime().exec(commandVm);
	            processVm.waitFor();  // 가상머신 실행 완료까지 대기
	            
	            // 현재 위치 변경
	            String[] commandCd = {"cd", "D:\\Git\\capstone-2023-21\\Capstone"};  // 현재 위치 변경 명령어
	            Process processCd = Runtime.getRuntime().exec(commandCd);
	            processCd.waitFor();  // 현재 위치 변경 완료까지 대기
	            
	            // 실행할 명령어 생성
	            String[] commandPy = {"python", "main.py", "읽어들일 파일 경로", "저장할 파일 경로"};

	            // 명령어 실행
	            Process processPy = Runtime.getRuntime().exec(commandPy);

	            // 출력 읽기
	            BufferedReader reader = new BufferedReader(new InputStreamReader(processPy.getInputStream()));
	            String line;
	            while ((line = reader.readLine()) != null) {
	                System.out.println(line);
	            }
	        } catch (IOException | InterruptedException e) {
	            e.printStackTrace();
	        }
	}
	
	
	
	// deeplearning에 파일경로를 넘겨주는 함수
	public String load_model_from_file(String path) {
		String outputStr = null;

		try {
			System.out.println("path : " + path);

			ProcessBuilder processBuilder = new ProcessBuilder("python",
					"C:\\Users\\82109\\Desktop\\real\\capstone-2023-21\\Capstone\\src\\main\\java\\lastcoder\\service\\file.py",
					path);
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

	// 악성코드 결과를 저장하는 함수
//	public void save_malware_result(String packing_result, String unpacking_result, String malware, List malware_list){
//		// 악성코드 종류를 저장한 리스트
//		List<PEFile> peList
//		// 패킹 탐지결과가 미탐 or 언 패킹이 실패했을 때 악성코드 탐지 결과는 불 필요하다
//		if(packing_result.equals("???") || unpacking_result.equals("Fail")){
//			malware_list.add("???");
//			return;
//		}
//
//		// 악성코드 결과확인
//		if(mw.contains(malware)){
//			malware_list.add(malware);
//		}
//		else{
//			malware_list.add("X");
//		}
//	}

	// 분석결과를 저장하는 함수
	public void save_analysis_result(String packing_result, String unpacking_result, String malware_result,
			List describe_list) {
		// 패킹 결과
		if (packing_result.equals("O")) {
			// 언 패킹 결과
			if (unpacking_result.equals("Sccuess")) {
				// 악성코드 결과
				String describe = malware_describe(malware_result);
				describe_list.add(describe);
			} else {
				describe_list.add("언 패킹이 실패하여 정확한 악성코드 탐지가 어렵습니다.");
			}
		} else if (packing_result.equals("?")) {
			describe_list.add("패킹 파일인지 확인이 어려워 정확한 악성코드 탐지가 어렵습니다.");
		} else {
			String describe = malware_describe(malware_result);
			describe_list.add(describe);
		}
	}

	// 악성코드 결과에 대한 설명을 저장하는 함수
	public String malware_describe(String malware_result) {
		switch (malware_result) {
		case "Ramnit":
			return "백도어를 통해 공격자가 원하는 정보를 전송하고 다수의 파일에 접근하여 악의적인 행위를 하는 악성코드입니다.";
		case "Lollipop":
			return "사용자의 동의 없이 광고를 클릭하도록 유도하여 수익을 얻는 행위를 수행하는 악성코드입니다.";
		case "Kelihos_ver3":
			return "Windows 운영체제를 대상으로 한 좀비 네트워크 구축 및 스팸 메일 전송 등의 악성 행위를 수행하는 트로이목마 바이러스입니다.";
		case "Vundo":
			return "Windows 운영체제에서 동작하는 백도어 트로이목마로, 광고 클릭 유도 및 개인정보 탈취 등의 악성 행위를 수행하는 악성코드입니다.";
		case "Simda":
			return "웹사이트 감염과 악성 파일 다운로드를 통해 컴퓨터에 침투하여 좀비 네트워크를 형성하고, 이를 이용한 악성코드 배포 및 개인정보 탈취 등의 악성 행위를 수행하는 트로이목마 바이러스입니다.";
		case "Tracur":
			return "웹사이트 감염 및 스팸 메일을 통해 사용자의 컴퓨터에 침투하여 온라인 금융 거래 정보를 탈취하거나 악성 광고를 보여주는 등의 악성 행위를 수행하는 트로이목마 바이러스입니다.";
		case "Kelihos_ver1":
			return "Windows 운영체제에서 동작하는 좀비 네트워크를 구축하여 스팸 메일 전송, DDoS 공격 등의 악성 행위를 수행하는 트로이목마 바이러스입니다.";
		case "Obfuscator.ACY":
			return "코드 난독화 기술을 사용하여 악성 코드를 숨기고 탐지를 회피하는 기능을 수행하는 트로이목마 바이러스입니다.";
		case "Gatak":
			return "유저의 웹 브라우저에서 정보를 탈취하여 백도어를 설치하거나 악성 광고를 보여주는 등의 악성 행위를 수행하는 트로이목마 바이러스";
		default:
			return "탐지된 악성코드가 없습니다.";
		}
	}

	// 업로드 파일 삭제 함수
	public void deleteFileUpload(List deletelist) {
		for (int i = 0; i < deletelist.size(); i++) {
			File file = new File(deletelist.get(i).toString());
			file.delete();
		}
	}


}