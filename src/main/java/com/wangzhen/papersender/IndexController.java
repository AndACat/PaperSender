package com.wangzhen.papersender;
import com.wangzhen.models.EmailPaper;
import com.wangzhen.services.IEmailService;
import com.wangzhen.util.ExcelReadUtil;
import com.wangzhen.util.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangzhen
 * @Description
 * @CreateDate 2020/5/16 10:37
 */
@RestController
public class IndexController {

    @Autowired
    private IEmailService emailService;

    @Value("${papersender.subject}")
    private String subject;

    @Value("${papersender.content}")
    private String content;
    /**
     * code:
     *  200：试卷全部发送成功
     *  201:试卷格式不正确
     *  202:excel读取有错误信息
     *  203:excel中指定的pdf没有上传
     *  204:试卷没有全部发送成功
     *
     */

    @PostMapping(value = "/paperSendToEmail", headers = "content-type=multipart/form-data")
    public ResponseMessage paperSendToEmail(@RequestParam("papers") MultipartFile[] papers,@RequestParam("excel")MultipartFile excel){
        Boolean checkFormat = checkFormat(papers);
        if(!checkFormat){
            //格式不正确
            return ResponseMessage.getInstance().setCode(201).setMsg("格式校验未通过");
        }
        StringBuffer errMsg = new StringBuffer();
        List<EmailPaper> emailPaperList = ExcelReadUtil.getEmailPaperList(excel, errMsg);
        if(emailPaperList.size() != papers.length){
            //excel没有读取完
            return ResponseMessage.getInstance().setCode(202).setMsg(errMsg.toString());
        }
        List<String> paperFileNameList = new ArrayList<>();
        for (MultipartFile paper : papers) {
            paperFileNameList.add(paper.getOriginalFilename());
        }

        //为每个实体类设置试卷文件
        for (EmailPaper emailPaper : emailPaperList) {
            String paperName = emailPaper.getPaperName();
            if(!paperFileNameList.contains(paperName)){
                //不包含指定的paper
                //excel中指定的pdf没有上传
                return ResponseMessage.getInstance().setCode(203).setMsg("excel中指定的pdf没有上传");
            }else{
                MultipartFile paper = null;
                for (MultipartFile multipartFile : papers) {
                    if(multipartFile.getOriginalFilename().equals(paperName)){
                        paper = multipartFile;
                        break;
                    }
                }
                emailPaper.setPaper(paper);
            }
        }


//        //获得所有pdf的格式
//        for (EmailPaper emailPaper : emailPaperList) {
//            paperFileNameList.add(emailPaper.getPaperName());
//        }
//        //判定是否有没上传的pdf
//        for (MultipartFile paper : papers) {
//            String paperName = paper.getOriginalFilename();
//            //如果不包含，则发生错误
//            if(!paperFileNameList.contains(paperName)){
//                //excel中指定的pdf没有上传
//                return ResponseMessage.getInstance().setCode(203).setMsg("excel中指定的pdf没有上传");
//            }else{
//
//            }
//        }
        StringBuffer errMessage = new StringBuffer();
        boolean tag = sendEmail(emailPaperList, errMessage);
        if(tag){
            //全部发送成功
            return ResponseMessage.getInstance().setCode(200).setMsg("试卷发送成功");
        }else{
            //没有全部发送成功
            return ResponseMessage.getInstance().setCode(204).setMsg(errMessage.toString());
        }
    }

    private boolean sendEmail(List<EmailPaper> emailPaperList,StringBuffer errMessage) {
        boolean tag = true;
        for (EmailPaper emailPaper : emailPaperList) {
            String to = emailPaper.getSendEmail();
            String studentName = emailPaper.getStudentName();
            String studentNo = emailPaper.getStudentNo();
            String to_subject = subject.replace("$studentName$",studentName).replace("$studentNo$",studentNo);
            String to_content = content.replace("$studentName$",studentName).replace("$studentNo$",studentNo);
            MultipartFile paper = emailPaper.getPaper();
            boolean sendSuccess = emailService.sendAttachmentsMail(to, to_subject, to_content, paper);
            if(!sendSuccess){
                System.out.println(studentName+"学生的邮件未发送成功");
                errMessage.append(studentName+"学生的邮件未发送成功\n");
                tag = false;
            }
        }
        return tag;
    }

    public Boolean checkFormat(MultipartFile[] papers){
        for (MultipartFile paper : papers) {
            //得到原文件名
            String originalFileName = paper.getOriginalFilename();
//            String[] requiredFormat = {"pdf","excel","word","PDF","EXCEL","WORD"};
//            for (String format : requiredFormat) {
//
//            }
            if(!originalFileName.endsWith("pdf")){
                return false;
            }
        }
        return true;
    }
}
