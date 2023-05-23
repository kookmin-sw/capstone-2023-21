package lastcoder.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;

@Service
public class peFile_Body_Extractor {

	public void extract_body(String before_path, String after_path) {
		try {
			extractPEBody(before_path, after_path);
			System.out.println("PE body extracted successfully.");
		} catch (IOException e) {
			e.printStackTrace();
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

			// header 섹션 건너뛰기
			fis.skip(headerSize);

			// body 섹션 복사
			byte[] buffer = new byte[4096];
			int bytesRead;
			long bytesRemaining = bodySize;
			while ((bytesRead = fis.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) > 0) {
				fos.write(buffer, 0, bytesRead);
				bytesRemaining -= bytesRead;
			}
		}
	}

	private long calculateHeaderSize(FileInputStream fis) throws IOException {
		// DOS 헤더 크기는 64바이트
		long headerSize = 64;

		// PE 헤더 크기 읽기
		fis.skip(60); // DOS 헤더의 PE 헤더 위치로 이동
		byte[] peHeaderOffsetBytes = new byte[4];
		fis.read(peHeaderOffsetBytes, 0, 4);
		long peHeaderOffset = bytesToLong(peHeaderOffsetBytes);
		fis.skip(peHeaderOffset + 4); // PE 헤더의 크기 위치로 이동
		byte[] peHeaderSizeBytes = new byte[2];
		fis.read(peHeaderSizeBytes, 0, 2);
		short peHeaderSize = bytesToShort(peHeaderSizeBytes);

		// 섹션 헤더 크기는 40바이트
		long sectionHeaderSize = 40;

		return headerSize + peHeaderOffset + peHeaderSize + sectionHeaderSize;
	}

	private long bytesToLong(byte[] bytes) {
		return ((bytes[3] & 0xFFL) << 24) | ((bytes[2] & 0xFFL) << 16) | ((bytes[1] & 0xFFL) << 8) | (bytes[0] & 0xFFL);
	}

	private short bytesToShort(byte[] bytes) {
		return (short) (((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF));
	}

}
