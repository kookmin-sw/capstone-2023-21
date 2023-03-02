package lastcoder.model;

import org.springframework.stereotype.Component;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Component
public class info {

	private String url_info;
	private String file_location;
	private MultipartFile multipartFile;

	private String base64_array;
	private byte[] byteArray;
	
	private String binary_array;
	private int[][] imageArray;
}
