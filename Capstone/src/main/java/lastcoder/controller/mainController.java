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

import lastcoder.model.info;
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

	// PE파일 분류 함수
	public void checked_PEfile(String fileName, String upload_filePath, List PEfileList, MultipartFile inputFile){
		// PE파일 확장자들을 저장한 리스트
		List<String> pefile_extensions = Arrays.asList("exe", "src", "dll", "ocx", "cpl", "drv", "sys", "vxd", "obj");
		// 경로 split
		String[] extension = fileName.split("\\.");

		// 확장자 검사
		if(pefile_extensions.contains(extension[extension.length-1])){

			// 업로드할 경로에 파일 생성
			File uploadFile = new File(upload_filePath + File.separator + fileName);
			// True일 경우 리스트에 추가
			PEfileList.add(uploadFile);

			try{
				// 입력 받은 파일을 지정한 경로(upload)에 저장
				inputFile.transferTo(uploadFile);
			}catch (IllegalStateException e){
				e.printStackTrace();
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	@RequestMapping("/main")
	@ResponseBody
	public String hello() {
		String hi = "hello";
		return hi;
	}

	@RequestMapping("/input_URL")
	public String inputURL() {

		return "input_URL";
	}	
	
	@PostMapping("/receive_URL")
	@ResponseBody
	public ModelAndView receiveURL(MultipartHttpServletRequest multipartFile) throws IOException {
		// 입력받은 파일들을 저장한 리스트
		List<MultipartFile> fileinputlist = multipartFile.getFiles("multipartFile");
		// PE 파일들을 저장하는 리스트
		List<File> PEfileList = new ArrayList<File>();
		// 현재 위치 경로
		String currentDir = System.getProperty("user.dir");
		// 업로드할 파일 경로
		String upload_filePath = currentDir + File.separator + "Capstone\\quarantine";

		// 입력받은 파일들의 이름 추출 및 PE파일 분류
		for(int i =0; i< fileinputlist.size(); i++){

			// 파일 이름 추출, ex) hello.exe
			String fileName = fileinputlist.get(i).getOriginalFilename();
			// PE파일 분류
			checked_PEfile(fileName, upload_filePath, PEfileList, fileinputlist.get(i));
		}
		// PE파일들 분석
		info d = urlService.byteArrayToBinary(PEfileList);

		// receive_URL에 넘길 데이터 가져오기
		List<String> nList = d.getFilenamelist();
		List<String> mList = d.getMalware_result();
		List<String> pList = d.getPacking_result();
		List<String> uList = d.getUnpacking_result();

		// 클라이언트에게 보여줄 뷰(view) 생성
		ModelAndView modelAndView = new ModelAndView("receive_URL");

		// receive_URL에 데이터 넘기기
		modelAndView.addObject("nList",nList);
		modelAndView.addObject("pList",pList);
		modelAndView.addObject("uList",uList);
		modelAndView.addObject("mList",mList);

		return modelAndView;
	}
}
