package lastcoder.model;

import java.io.File;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class file_info {

	private File file;
	private String file_Name;
	private String[][] file_Array;
	
}
