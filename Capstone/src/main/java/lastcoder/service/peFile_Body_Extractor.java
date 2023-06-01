package lastcoder.service;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;

@Service
public class peFile_Body_Extractor {

	public void extract_body(String before_path, String after_path) {
		File beforeFolder = new File(before_path);
		if (!beforeFolder.isDirectory()) {
			System.err.println("Invalid directory path: " + before_path);
			return;
		}

		File[] files = beforeFolder.listFiles();
		if (files == null || files.length == 0) {
			System.err.println("No files found in the directory: " + before_path);
			return;
		}

		for (File file : files) {
			if (file.isFile()) {
				String outputFilePath = after_path + File.separator + file.getName();
				try {
					extractPEBody(file.getAbsolutePath(), outputFilePath);
					System.out.println("PE body extracted successfully for file: " + file.getName());
				} catch (IOException e) {
					System.err.println("Failed to extract PE body for file: " + file.getName());
					e.printStackTrace();
				}
			}
		}
	}

	private void extractPEBody(String filePath, String outputFilePath) throws IOException {
		File inputFile = new File(filePath);
		File outputFile = new File(outputFilePath);

		try (FileInputStream fis = new FileInputStream(inputFile);
				FileOutputStream fos = new FileOutputStream(outputFile)) {
			// 읽을 바이트 수 계산
			long headerSize = calculateHeaderSize(fis);
			long bodySize = inputFile.length() - headerSize;

			// body 섹션 복사
			byte[] buffer = new byte[4096];
			int bytesRead;
			long bytesRemaining = bodySize;
			while (bytesRemaining > 0 && (bytesRead = fis.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
				fos.write(buffer, 0, bytesRead);
				bytesRemaining -= bytesRead;
			}
		}
	}

	private long calculateHeaderSize(FileInputStream fis) throws IOException {
	    // DOS 헤더 크기는 64바이트
	    long dosHeaderSize = 64;

	    // DOS 헤더 크기 읽기
	    byte[] dosHeaderBytes = new byte[(int) dosHeaderSize];
	    fis.read(dosHeaderBytes, 0, (int) dosHeaderSize);

	    // DOS 헤더 마지막 부분부터 PE 헤더 위치를 찾음
	    int peHeaderOffset = ((dosHeaderBytes[0x3F] & 0xFF) << 24) | ((dosHeaderBytes[0x3E] & 0xFF) << 16) |
	            ((dosHeaderBytes[0x3D] & 0xFF) << 8) | (dosHeaderBytes[0x3C] & 0xFF);

	    // PE 헤더 시작 위치로 이동
	    fis.skip(peHeaderOffset);

	    // 'SizeOfOptionalHeader' 필드는 PE 헤더의 0x16부터 시작하여 2바이트입니다.
	    byte[] sizeOfOptionalHeaderBytes = new byte[2];
	    fis.skip(0x16);
	    fis.read(sizeOfOptionalHeaderBytes, 0, 2);
	    int sizeOfOptionalHeader = bytesToShort(sizeOfOptionalHeaderBytes);

	    // PE 헤더의 크기는 COFF 헤더의 20바이트와 'SizeOfOptionalHeader' 필드의 값을 더한 값입니다.
	    int peHeaderSize = 20 + sizeOfOptionalHeader;

	    // 'NumberOfSections' 필드는 COFF 헤더의 2바이트입니다.
	    byte[] numberOfSectionsBytes = new byte[2];
	    fis.skip(2);  // 'NumberOfSections' 필드로 이동
	    fis.read(numberOfSectionsBytes, 0, 2);
	    int numberOfSections = bytesToShort(numberOfSectionsBytes);

	    // 섹션 헤더의 전체 크기는 섹션 헤더의 크기(40바이트)에 섹션의 개수를 곱한 값입니다.
	    int sectionHeaderSize = 40 * numberOfSections;

	    return dosHeaderSize + peHeaderOffset + peHeaderSize + sectionHeaderSize;
	}


	private long bytesToLong(byte[] bytes) {
		return ((bytes[3] & 0xFFL) << 24) | ((bytes[2] & 0xFFL) << 16) | ((bytes[1] & 0xFFL) << 8) | (bytes[0] & 0xFFL);
	}

	private short bytesToShort(byte[] bytes) {
		return (short) (((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF));
	}
}
