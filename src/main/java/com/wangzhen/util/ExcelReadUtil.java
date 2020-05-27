package com.wangzhen.util;

import com.alibaba.fastjson.JSON;
import com.wangzhen.models.EmailPaper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelReadUtil {
    /**
    * 验证EXCEL文件
    * @param fileType 文件类型
    * @return
    */
    private static boolean validateExcel(String fileType){
        if (fileType == null || !(isExcel_xls(fileType) || isExcel_xlsx(fileType))){
            log.error("文件类型不是excel格式");
            return false;
        }
        return true;
    }
    /**
    * 读EXCEL文件
    */
    private static Workbook getExcelWorkBook(MultipartFile uploadFile) {//
        Workbook wb = null;
        String originalFilename = uploadFile.getOriginalFilename();// 获取文件名，带后缀
        String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();// 获取文件的后缀格式
        InputStream inputStream = null;
        if(!validateExcel(fileType))//验证文件名是否合格
        return null;
        try {
            inputStream = uploadFile.getInputStream();
            if(isExcel_xls(fileType))
                wb = new HSSFWorkbook(inputStream);
            if(isExcel_xlsx(fileType))
                wb = new XSSFWorkbook(inputStream);
        }catch (IOException e) {
            log.error(e.getMessage());
        }
        return wb;
    }

    /**
    * 读取Excel里面sheet
    * @param wb
    * @return
    */
    private static ExcelDetail getExcelInfo(Workbook wb){
        if(wb == null){
            return null;
        }
        //得到第一个shell
        Sheet sheet=wb.getSheetAt(0);
        int totalRows;//Excel的行数 从1开始
        int totalCells = 0;//Excel的列数
        totalRows=sheet.getPhysicalNumberOfRows();//得到Excel的行数
        if(totalRows>=1 && sheet.getRow(0) != null){//得到Excel的列数(前提是有行数)
            totalCells=sheet.getRow(0).getPhysicalNumberOfCells();
        }
        return new ExcelDetail(sheet,totalRows,totalCells);
    }

    private static void initSheet(int totalRows, int totalCells, Sheet sheet){
        Row row = null;
        for(int i=2; i<totalRows; ++i){
            row = sheet.getRow(i);
            for(int j=0; j<totalCells; ++j){
                Cell cell = row.getCell(j);
                if(cell!=null) cell.setCellType(CellType.STRING);
            }
        }
    }
    private static String getCellValue(Row row, int cellNum){
        if(row == null){
            return "";
        }
        Cell cell = row.getCell(cellNum);
        if(cell == null){
            return "";
        }
        return cell.getStringCellValue();
    }
    private static List<String> getCellArrayValue(Row row, int cellNum, String separation){
        if(row == null){
            return null;
        }
        Cell cell = row.getCell(cellNum);
        if(cell == null){
            return null;
        }
        String[] val = cell.getStringCellValue().split(separation);
        List<String> list = new ArrayList<>();
        for(String s : val){
            list.add(s);
        }
        return list;
    }
    private static <T> List<T> getCellArrayValue(Row row, int cellNum, Class<T> cla){
        if(row == null) {
            return null;
        }
        Cell cell = row.getCell(cellNum);
        if(cell == null){
            return null;
        }
        return JSON.parseArray(cell.getStringCellValue(),cla);
    }
    // @描述：是否是xls的excel
    private static boolean isExcel_xls(String fileType)  {
    return fileType.equalsIgnoreCase("xls");
    }

    //@描述：是否是xlsx的excel
    private static boolean isExcel_xlsx(String fileType)  {
    return fileType.equalsIgnoreCase("xlsx");
    }
    private static String getNewProblem(StringBuffer problem, List<String> answerList){
        int beginIdx = -1;
        for (int i = 0; i < problem.length(); i++) {
            if(problem.charAt(i) == '#' && problem.charAt(i+1) == '{'){
                i++;
                beginIdx = i;
                continue;
            }
            if(problem.charAt(i) == '}'){
                if(beginIdx != -1){
                    String answer = problem.substring(beginIdx+1,i);
                    answerList.add(answer);
                    problem.delete(beginIdx+1,i);
                    i = beginIdx+1;
                    beginIdx = -1;
                }
            }
        }
        return problem.toString();//.replace("#{}","___");
    }




    private static class ExcelDetail{
        Sheet sheet = null;
        int totalRows;
        int totalCells;
        public ExcelDetail(Sheet sheet, int totalRows, int totalCells) {
            this.sheet = sheet;
            this.totalRows = totalRows;
            this.totalCells = totalCells;
        }
    }
    private static class Message{
        private Map<Integer, String> errMap = new LinkedHashMap<>();
        public void getErrMsg(StringBuffer errMsg){
            if(errMap.isEmpty()){
                errMsg.append("无错误信息");
                return;
            }
            for (Integer integer : errMap.keySet()) {
                errMsg.append("第"+integer+"行数据错误,错误原因："+errMap.get(integer)+"\n");
            }
        }
        public void addErrMsg(Integer errRow, String errMsg){
            errMap.put(errRow,errMsg);
        }
    }
    public static List<EmailPaper> getEmailPaperList(MultipartFile excelFile, StringBuffer errMsg){
        ExcelDetail excelDetail = getExcelInfo(getExcelWorkBook(excelFile));
        if(excelDetail == null){
            return null;
        }
        Sheet sheet = excelDetail.sheet;//sheet表格
        int totalRows = excelDetail.totalRows;//Excel的行数
        int totalCells = excelDetail.totalCells;//Excel的列数
        int rowStart = 2;
        Message message = new Message();
        Row row = null;
        List<EmailPaper> paperList = new ArrayList<EmailPaper>();
        initSheet(totalRows,totalCells,sheet);//设置 Cells : CellType(CellType.STRING);
        EmailPaper build = null;
        for(int i=rowStart; i<totalRows; ++i){
            row = sheet.getRow(i);
            try{
                build = new EmailPaper();
                build.setPaperName(getCellValue(row,0));
                build.setSendEmail(getCellValue(row,1));
                build.setStudentName(getCellValue(row,2));
                build.setStudentNo(getCellValue(row,3));
            }catch (Exception e){
                message.addErrMsg(i-1,e.getMessage());
                log.error("第{}条记录错误,错误信息：{}",i-1,e.getMessage());
                build = null;
            }
            if(build == null || build.getPaperName().isBlank() || build.getSendEmail().isBlank()){
                build = null;
            }
            log.debug(build.toString());
            if (build != null) {
                paperList.add(build);
            }
        }
        message.getErrMsg(errMsg);
        return  paperList;
    }
}