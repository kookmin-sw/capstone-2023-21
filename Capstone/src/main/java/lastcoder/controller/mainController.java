package lastcoder.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import lastcoder.model.info;
import lastcoder.service.urlService;

@Controller
public class mainController {
	
	
	@Autowired
	private urlService urlService;
	

	
	@RequestMapping("/main")
	@ResponseBody
	public String hello() {
		String hi = "hello";
		return hi;
	}
	
	
	
	@GetMapping("/input_URL")
	public String inputURL() {
//		model.addAttribute("url_info", new String());
		return "input_URL";
	}	
	
	@PostMapping("/receive_URL")
	@ResponseBody
	public info receiveURL(@RequestParam("multipartFile") MultipartFile multipartFile) throws IOException {
		System.out.println(multipartFile);
		return urlService.byteArrayToBinary(multipartFile);
	}
	
}
