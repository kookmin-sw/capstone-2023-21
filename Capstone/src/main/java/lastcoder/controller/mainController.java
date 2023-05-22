//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package lastcoder.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lastcoder.model.PEFile;
import lastcoder.model.file_info;
import lastcoder.service.urlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class mainController {



	@Autowired
	private urlService urlService;


	@RequestMapping("/input_URL")
	public String inputURL() {
		return "input_URL";
	}	


	@PostMapping("/receive_URL")
	@ResponseBody
	public void receiveURL(MultipartHttpServletRequest multipartFile) throws IOException {
		// 입력받은 파일들을 저장한 리스트
		List<MultipartFile> multiFile = multipartFile.getFiles("multipartFile");

		List<File> PEfile_list = urlService.checked_PEfile(multiFile);
		
		List<byte[]> Byte_list = urlService.convertPEFileToBytes(PEfile_list);	
		List<String> binaryStr_List = urlService.binaryEnc(Byte_list);
		List<String> hxd_list = urlService.BinaryToHxd(binaryStr_List);
		
		urlService.HxdresultToArray(hxd_list);
		//file_info_List 객체 List 완성
		urlService.detectPackAndUnpack(urlService.get_file_info_List());
//		urlService.run_inference();
		
//		return modelAndView;
	}
}
