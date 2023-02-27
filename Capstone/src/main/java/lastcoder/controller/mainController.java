package lastcoder.controller;

import java.io.IOException;

import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import lastcoder.service.urlService;


@Controller
public class mainController {
	

	
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
	public String receiveURL(@RequestParam("url_info") String url_info, @RequestParam("file_location") String file_location) throws IOException {

		urlService urlService = new urlService();
		ModelAndView mv = new ModelAndView();
		
		
		
		PythonInterpreter interpreter = new PythonInterpreter();
		interpreter.execfile("D:\\test.py");
		interpreter.exec("print(testFunc(5,10))");
		
		PyFunction pyFunction = interpreter.get("testFunc", PyFunction.class);
		
		int a = 10;
		int b = 20;
		
		PyObject pyObject = pyFunction.__call__(new PyInteger(a), new PyInteger(b));
		System.out.println(pyObject.toString());
		//test
		return pyObject.toString() + urlService.infoAndBinary(url_info, file_location).toString();
		
	}
	
	
}
