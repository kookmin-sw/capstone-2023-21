package lastcoder.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class fileAnalyze
{

    // 16진수 값 10진수로 변환 함수
    public int HexToDecimal(int index_location, int byte_size, String hxdarray[][]){
        int row = index_location/16;
        int col = index_location%16;
        String hexresult = "";
        for(int i = 0; i < byte_size; i++){
            if(col - i < 0) {
                row = row - 1;
                col = 16;
            }
            hexresult = hexresult + hxdarray[row][col - i];
        }
        return Integer.parseInt(hexresult, 16);
    }


    // Image_dos_header
    // e_magic 2byte로 "MZ" PE파일 확인
    public void isPEFile(String[][] hxdarray) {

        boolean PEcheck = false;

        String mz = "";

        for (int index = 0; index < 2; index++) {
            String component = hxdarray[0][index];
            int decimal = Integer.parseInt(component, 16);
            char c = (char) decimal;
            mz = mz + c;
        }

        if (mz.equals("MZ")) {
            PEcheck = true;
            System.out.println(PEcheck + " PE 파일 입니다.");
        } else {
            System.out.println(PEcheck + " PE파일이 아님 " + mz);
        }
    }


    // e_lfanew로 IMAGE_NT_HEADERS 위치 찾기
    public int findINHLocation(String[][] hxdarray) {
        String INH_location = "";
        for(int index = 1; index <= 4; index++){
            String component = hxdarray[64/16-1][16-index];
            INH_location = INH_location + component;
        }
        System.out.println(INH_location);
        int INH_location_index = Integer.parseInt(INH_location,16);

        int row = INH_location_index/16;
        int col = INH_location_index%16;

        System.out.println("행 : " + row + " " + "열 : " + col);
        return INH_location_index;
    }


    public void checkINH(String[][] hxdarray, int INH_location_index) {
        boolean check = false;
        String checkINH_location = "";
        int increase = 0;
        int INH_row = INH_location_index / 16;
        int INH_col = INH_location_index % 16;

        for(int index = 0; index < 4; index++) {
            INH_col = INH_col + increase;
            if(INH_col >= 16) {
                INH_col = INH_col - 16;
                INH_row = INH_row + 1;
            }
            checkINH_location = checkINH_location + hxdarray[INH_row][INH_col];
            if(increase < 1) {
                increase++;
            }
        }
        System.out.println(checkINH_location);
        if(checkINH_location.equals("50450000")) {
            check = true;
        }
        System.out.println(check + " " + "INH 시작위치");
        System.out.println(INH_location_index);
    }


    // 해당 필드의 값을 가져오는 함수
    public int getFieldData(String[][] hxdarray, int index, int byte_size) {
        int fieldData = HexToDecimal(index, byte_size, hxdarray);
        return fieldData;
    }


    // basecode와 virtualAddress 같은 섹션 찾기
    public int findSection(int baseofcode, int numberOfSection, int image_optional_header_finish, String[][] hxdarray) {
        int section_number = -1;
        for(int index = 0; index < numberOfSection; index++) {
            int virtualAddress_location = image_optional_header_finish + (index * 40 + 16);
            int virtualAddress = HexToDecimal(virtualAddress_location, 4, hxdarray);
            if(virtualAddress == baseofcode){
                section_number = index;
                break;
            }
        }
        return section_number;
    }


    // sectiontable_name 확인하기
    public void getSectionName(int section_table_name, String[][] hxdarray){
        String sn = "";
        int row = section_table_name/16;
        int col = section_table_name%16;
        for(int index = 0; index < 8; index++){
            if(col - index < 0){
                row = row -1;
                col = 16;
            }
            String component = hxdarray[row][col - index];
            int num = Integer.parseInt(component, 16);
            char str = (char)num;
            sn = sn + str;
        }
        System.out.println("section_table name : " + sn);
    }


    // characteristics
    public String getCharacteristics(String[][] hxdarray, int characteristics_location) {
        int row = characteristics_location/16;
        int col = characteristics_location%16;

        return hxdarray[row][col];
    }


    // 패킹 파일 탐지 및 언 패킹
    public void detectPackedFile(String filelocation, String characteristics, double entropy, List packing_list, List unpacking_list){
        // write 속성을 가진 리스트
        List<String> write_characteristics = Arrays.asList("A0", "C0", "E0");

        // packing file quarantine(write and entropy)
        if(write_characteristics.contains(characteristics)){

            if(entropy > 6.85 && entropy < 8){
                System.out.println("패킹 파일 입니다.");
                packing_list.add("O");
                // 언 패킹하기(UPX)
                unPacking(filelocation, unpacking_list);
            }
            else if(entropy > 5.05 && entropy < 6.69){
                packing_list.add("X");
                unpacking_list.add("-");
                System.out.println("패킹 파일이 아닙니다.");
            }
            else{
                packing_list.add("???");
                unpacking_list.add("-");
                System.out.println("패킹 파일인지 탐지하지 못했습니다.");
            }
        }
        else{
            packing_list.add("X");
            unpacking_list.add("-");
            System.out.println("패킹 파일이 아닙니다.");
        }
    }


    // 언 패킹하기(UPX)
    public void unPacking(String filelocation, List unpacking_list){
        String packedFilePath = filelocation;
        // 현재 위치 경로
        String currentDir = System.getProperty("user.dir");
        String upxPath = currentDir + File.separator + "Capstone\\upx-3.95-win64\\upx.exe";
        ProcessBuilder pb = new ProcessBuilder(upxPath, "-d" ,packedFilePath);

        try{
            Process process = pb.start();
            int exitValue = process.waitFor();
            if(exitValue == 0){
                unpacking_list.add("Sccuess");
            }
            else{
                unpacking_list.add("Fail");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch(IOException e){
            throw  new RuntimeException(e);
        }
    }

}
