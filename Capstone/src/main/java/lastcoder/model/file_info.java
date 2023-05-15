package lastcoder.model;

import org.springframework.stereotype.Component;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Data
@Component
public class file_info {

	private String url_info;
	private String file_location;
	private MultipartFile multipartFile;
	private File file;

	private List<File> Flist;

	private String base64_array;
	private byte[] byteArray;
	
	private String binary_array;
	private int[][] imageArray;
	private String hex_array;


	// 삭제할 파일 목록
	private String deletelist;
	// 파일 이름을 저장하는 목록
	private String filenamelist;
	// 파일 악성코드 결과 목록
	private String malware_result;
	// 파일 패킹 탐지 결과 목록
	private String packing_result;
	// 언패킹 결과 목록
	private String unpacking_result;
	// 설명에 대한 목록
	private String describelist;

}
