package lastcoder.model;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class info {

	private String url_info;
	private String file_location;
	private String base64_array;
	private String binary_arry;
}
