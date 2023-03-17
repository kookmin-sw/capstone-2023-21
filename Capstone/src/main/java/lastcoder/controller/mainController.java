//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package lastcoder.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lastcoder.model.info;
import lastcoder.service.urlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lastcoder.model.info;
import lastcoder.service.urlService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
	public info receiveURL(MultipartHttpServletRequest multipartFile) throws IOException {

		List<MultipartFile> fileinputlist = multipartFile.getFiles("multipartFile");
		List<File> PEfile = new ArrayList<File>();
		String filePath = "C:\\Users\\82109\\Desktop\\real\\capstone-2023-21\\Capstone\\quarantine";

		for(int i =0; i< fileinputlist.size(); i++){

			String fileRealName = fileinputlist.get(i).getOriginalFilename();
			System.out.println(fileRealName);
			File saveFile = new File(filePath + File.separator + fileRealName);


			String[] extension = fileRealName.split("\\.");
			System.out.println(extension[extension.length-1]);
			if(extension[extension.length-1].equals("exe") || extension[extension.length-1].equals("src") || extension[extension.length-1].equals("dll") || extension[extension.length-1].equals("ocx") || extension[extension.length-1].equals("cpl") || extension[extension.length-1].equals("drv") || extension[extension.length-1].equals("sys") || extension[extension.length-1].equals("vxd") || extension[extension.length-1].equals("obj")){
				PEfile.add(saveFile);
			}

			try{
				fileinputlist.get(i).transferTo(saveFile);
			}catch (IllegalStateException e){
				e.printStackTrace();
			}catch (IOException e){
				e.printStackTrace();
			}

		}

		return urlService.byteArrayToBinary(PEfile);
	}
	
	
}
