package lastcoder.model;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class file_info {

	private File file;
	private String file_Name;
	private String[][] file_Array;

	
    public void saveFile(String filePath) {
        if (file != null) {
            File destination = new File(filePath);
            try {
                // 파일을 복사하여 저장
                org.apache.commons.io.FileUtils.copyFile(file, destination);
                System.out.println("File saved successfully: " + filePath);
            } catch (IOException e) {
                System.out.println("Failed to save file: " + e.getMessage());
            }
        } else {
            System.out.println("No file to save.");
        }
    }
}
