package com.wangzhen.services;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author wangzhen
 * @Description 封装一个发邮件的接口，后边直接调用即可
 * @CreateDate 2020/5/16 11:39
 */
public interface IEmailService {
    /**
     * 发送文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    boolean sendSimpleMail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    boolean sendHtmlMail(String to, String subject, String content);
    /**
     * 发送带附件的邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @param file 附件
     */
    boolean sendAttachmentsMail(String to, String subject, String content, MultipartFile file);
}
