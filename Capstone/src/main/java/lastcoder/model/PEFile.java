package lastcoder.model;

import java.util.Arrays;
import java.util.List;

public enum PEFile{
    EXE("exe"),
    SRC("src"),
    DLL("dll"),
    OCX("ocx"),
    CPL("cpl"),
    DRV("drv"),
    SYS("sys"),
    VXD("vxd"),
    OBJ("obj");

    private final String extension;

    PEFile(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
    
    public List<PEFile> getPEList(){
    	PEFile[] peList = PEFile.values();
    	List<PEFile> peFileList = Arrays.asList(peList);

    	return peFileList;
    }
}
