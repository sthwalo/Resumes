package com.sthwalo.resumes.model;

import lombok.Data;

@Data
public class Certification {

    private String name;
    private String issuer;

    /** MM/YYYY */
    private String date;

    /** MM/YYYY — optional expiry */
    private String expiryDate;
}
