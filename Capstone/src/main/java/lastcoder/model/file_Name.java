package lastcoder.model;

import java.util.ArrayList;
import java.util.List;


public class file_Name {

	private List<String> file_Name_List = new ArrayList<>();
	
	public void reset_List() {
		file_Name_List = new ArrayList<>();
	}
	
	public void set_List(String str) {
		file_Name_List.add(str);
	}
	
	public List<String> get_List(){
		return file_Name_List;
	}
	
}
