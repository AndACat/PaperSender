package com.wangzhen.models;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author wangzhen
 * @Description
 * @CreateDate 2020/5/16 11:07
 */
public class EmailPaper {
    private String paperName;
    private String sendEmail;
    private String studentName;
    private String studentNo;
    private MultipartFile paper;

    public MultipartFile getPaper() {
        return paper;
    }

    public void setPaper(MultipartFile paper) {
        this.paper = paper;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(String sendEmail) {
        this.sendEmail = sendEmail;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }
}
