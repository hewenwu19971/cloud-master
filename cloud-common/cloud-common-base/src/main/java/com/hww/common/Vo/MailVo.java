package com.hww.common.Vo;

import lombok.Data;

@Data
public class MailVo {
    private String mail;
    private String fromPeo;
    private static final String TITLE = "验证码";

}
