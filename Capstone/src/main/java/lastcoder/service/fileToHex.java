package lastcoder.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

@Service
public class fileToHex {

	public void fileToHexArray(String before_path, String after_path) {
		String directoryPath = before_path;
		String outputDirectoryPath = after_path;

        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isPEFile(file)) {
                    try {
                        byte[] fileBytes = readFileBytes(file.toPath());
                        String formattedString = formatBytesToCustom(fileBytes);
                        String outputFileName = removeFileExtension(file.getName()) + ".bytes";
                        String outputFilePath = outputDirectoryPath + File.separator + outputFileName;
                        saveStringToFile(formattedString, outputFilePath);
                        System.out.println("PE file saved in custom format: " + outputFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
	}
    private boolean isPEFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] dosHeader = new byte[2];
            fis.read(dosHeader);
            return (dosHeader[0] == 'M' && dosHeader[1] == 'Z');
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private byte[] readFileBytes(Path filePath) throws IOException {
        return Files.readAllBytes(filePath);
    }

    private String formatBytesToCustom(byte[] bytes) {
        StringBuilder formattedString = new StringBuilder();
        int offset = 0;

        for (byte b : bytes) {
            String hex = String.format("%02X", b);
            if (offset % 16 == 0) {
                formattedString.append(String.format("%08X ", offset));
            }
            formattedString.append(hex).append(" ");
            offset++;
            if (offset % 16 == 0) {
                formattedString.append("\n");
            }
        }

        return formattedString.toString();
    }

    private void saveStringToFile(String content, String filePath) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(filePath));
        writer.print(content);
        writer.close();
    }

    private String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
}
