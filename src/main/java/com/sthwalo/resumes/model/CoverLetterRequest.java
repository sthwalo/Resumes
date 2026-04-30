package com.sthwalo.resumes.model;

import lombok.Data;
import java.util.List;

@Data
public class CoverLetterRequest {
    private String senderName;
    private String senderEmail;
    private String senderPhone;
    private String senderLocation;
    private String date;
    private String recipientName;
    private String recipientTitle;
    private String companyName;
    private String roleTitle;
    private List<String> paragraphs;
}
