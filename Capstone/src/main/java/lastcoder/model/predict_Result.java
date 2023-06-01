package lastcoder.model;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class predict_Result {
	
	private String file_Origin_Name = null;
	private String file_name = null;
	private String packing = null;
	private String unpacking = null;
	private String malware_name = null;
	private String malware_info = null;

}
