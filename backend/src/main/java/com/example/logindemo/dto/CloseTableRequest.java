package com.example.logindemo.dto;

import jakarta.validation.constraints.NotBlank;

public class CloseTableRequest {

    @NotBlank(message = "请输入会员卡后四位")
    private String memberCardLastFour;

    public String getMemberCardLastFour() {
        return memberCardLastFour;
    }

    public void setMemberCardLastFour(String memberCardLastFour) {
        this.memberCardLastFour = memberCardLastFour;
    }
}
