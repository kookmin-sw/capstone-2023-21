package lastcoder.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lastcoder.model.PEFile;
import lastcoder.model.file_info;
import lastcoder.model.malware_info;
import lastcoder.model.predict_Result;

@Service
public class urlService {

	@Autowired
	private file_info file_info;

	@Autowired
	private fileAnalyze fileAnalyze;

	@Autowired
	private fileToHex fileToHex;

	@Autowired
	private peFile_Body_Extractor peFile_Body_Extractor;

	@Autowired
	private predict_Result predict_Result;

	// 현재 위치 경로 
	private final static String currentDir = System.getProperty("user.dir");
	// 업로드할 파일 경로
	private final static String upload_filePath = currentDir + File.separator + "upload_file_path";
	private final static String unpacking_filePath = currentDir + File.separator + "unpacking_file_Path";
	private final static String file_to_npy = currentDir + File.separator + "file_to_npy";
	private final static String save_file_path = currentDir + File.separator + "save_file_path";
	private final static String file_to_hex = currentDir + File.separator + "file_to_hex";
	private final static String file_to_hex_body = currentDir + File.separator + "file_to_hex_body";

	private final String[] file_list = { upload_filePath, unpacking_filePath, file_to_npy, file_to_hex, save_file_path,
			file_to_hex_body };

	private final static List<String> write_characteristics = Arrays.asList("A0", "C0", "E0");

	private List<file_info> file_info_List;
	private List<predict_Result> predict_Results;

	public List<file_info> get_file_info_List() {
		return file_info_List;
	}

	public List<predict_Result> get_predict_Results() {
		return predict_Results;
	}

	// PE파일 분류 함수
	public List<File> checked_PEfile(List<MultipartFile> multiFile) throws IOException {

		file_info_List = new ArrayList<>();
		predict_Results = new ArrayList<>();

		// PE파일 확장자들을 저장한 리스트
		List<PEFile> peList = PEFile.getPEList();

		List<File> PEfile_list = new ArrayList<>();

		File uploadFile;

		for (MultipartFile file : multiFile) {

			file_info = new file_info();
			predict_Result = new predict_Result();

			String fileName = file.getOriginalFilename();
			String[] extension = fileName.split("\\.");

			if (peList.stream().anyMatch(ext -> ext.getExtension().equals(extension[extension.length - 1]))) {
				// 업로드할 경로에 파일 생성
				uploadFile = new File(upload_filePath + File.separator + fileName);
				file_info.setFile_Origin_Name(fileName);
				predict_Result.setFile_Origin_Name(fileName);
				fileName = fileName.split("\\.")[0];
//				if(fileName.length() >= 15) {
//					fileName = fileName.substring(0, 14);
//				}
				try {
					// 입력 받은 파일을 지정한 경로(upload)에 저장
					file.transferTo(uploadFile);
					PEfile_list.add(uploadFile);
					file_info.setFile(uploadFile);
					file_info.setFile_Name(fileName);
					file_info_List.add(file_info);

					predict_Result.setFile_name(fileName);
					predict_Results.add(predict_Result);

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
	public void HxdresultToArray(List<String> hxd_list) {

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

		int size = hxdArray.size();
		for (int i = 0; i < size; i++) {
			String[][] hxdData = hxdArray.get(i);
			file_info fileInfo = file_info_List.get(i);

			fileInfo.setFile_Array(hxdData);
		}
	}

	// 파일을 분석하여 패킹 결과와 언패킹 결과를 알아내는 함수
	public void detectPackAndUnpack(List<file_info> file_info_List) throws IOException {
		// Image_dos_header
		// e_magic 2byte로 "MZ" PE파일 확인

		for (file_info info : file_info_List) {
			String[][] str = info.getFile_Array();

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
			double entropy = EntryPoint(info.getFile(), section_table_offset, section_table_size);

			// 패킹 파일 탐지
			// write 속성 있고, entropy > 6.85 -> 패킹 파일이다 : 패킹후 저장
			// write 속성 있고, entorpy < 5.05 -> 패킹 파일이 아니다 : 곧바로 저장
//			fileAnalyze.unPacking(info.getFile_Name(), currentDir, upload_filePath, predict_Results);
//			info.saveFile(unpacking_filePath);
			if (write_characteristics.contains(characteristics)) {

				if (entropy >= 5.05) {
					// 패킹후 저장
					fileAnalyze.unPacking(info.getFile_Origin_Name(), currentDir, upload_filePath, predict_Results);
					info.saveFile(unpacking_filePath);
				}
			} else {
				info.saveFile(unpacking_filePath);
			}

		}
	}

	// 진입점 섹션의 엔트로피 계산 함수
	public double EntryPoint(File f, int offset, int size) {
		try {
			FileInputStream fis = new FileInputStream(f);
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
			for (int count : freq) {
				if (count > 0) {
					double p = (double) count / entryPointData.length;
					entropy -= p * Math.log(p) / Math.log(2);
				}
			}

			// 엔트로피 값을 출력합니다.
			System.out.println("Entry point entropy: " + entropy);
			return entropy;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public void fileToHex_Method() {
		fileToHex.fileToHexArray(unpacking_filePath, file_to_hex);
	}

	public void peFile_Body_Extractor_Method() {
		peFile_Body_Extractor.extract_body(file_to_hex, file_to_hex_body);
	}

	public void deleteFilesInFolders(String[] folders) {
		for (String folderPath : folders) {
			File folder = new File(folderPath);
			if (folder.exists() && folder.isDirectory()) {
				File[] files = folder.listFiles();
				if (files != null) {
					for (File file : files) {
						if (file.isFile()) {
							file.delete();
						}
					}
				}
			}
		}
	}

	public Map<String, Integer> readPredictionsFromCSV(String filePath) {
		Map<String, Integer> predictions = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line = reader.readLine(); // 헤더 라인 읽기
			String[] headers = line.split(",");
			int classCount = headers.length - 1;

			while ((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				String id = values[0];
				int predictedClass = -1;

				for (int i = 1; i < values.length; i++) {
					int prediction = Integer.parseInt(values[i]);
					if (prediction == 1) {
						predictedClass = i;
						break;
					}
				}

				if (predictedClass != -1) {
					predictions.put(id, predictedClass);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return predictions;
	}

	public Map<String, Integer> run_inference() {
		try {
			// 아나콘다 가상환경 실행
			ProcessBuilder condaProcessBuilder = new ProcessBuilder("cmd.exe", "/c", "conda", "activate", "python_VM",
					"&&", "cd", currentDir, "&&", "python", "main.py", file_to_hex, file_to_npy, save_file_path);
			Process condaProcess = condaProcessBuilder.start();
			condaProcess.waitFor();
			System.out.println("conda activate");
			deleteFilesInFolders(file_list);

			// main.py의 출력 파일 경로
			String outputFilePath = ".\\output.txt";

			// 출력 파일을 읽어서 출력 확인
			try (BufferedReader reader = new BufferedReader(new FileReader(outputFilePath))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}


		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		String filePath = currentDir + File.separator + "predictions.csv"; // predictions.csv 파일 경로 설정

		Map<String, Integer> predictions = readPredictionsFromCSV(filePath);
//        System.out.println(predictions);
//        output : {EppManifest=6}

		return predictions;

	}

	public void inference_result(Map<String, Integer> predictions) {

		Map<Integer, String> malware_info_List = malware_info.getAllMalwareInfo();

		for (Map.Entry<String, Integer> entry : predictions.entrySet()) {

			String id = entry.getKey();
			int predictedClass = entry.getValue();

			for (predict_Result pr : predict_Results) {
				System.out.println(pr.getFile_name());
				System.out.println(id);
				if (pr.getFile_name().equals(id)) {
					for (Map.Entry<Integer, String> maps : malware_info_List.entrySet()) {
						if (predictedClass == maps.getKey().intValue()) {
							pr.setMalware_info(maps.getValue());
							pr.setMalware_name(malware_info.values()[predictedClass].name());

						}
					}
 
				}
			}

		}
	}

}