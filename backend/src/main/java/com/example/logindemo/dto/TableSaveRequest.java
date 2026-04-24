package com.example.logindemo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TableSaveRequest {

    @NotBlank(message = "台球桌编号不能为空")
    private String tableNo;

    @NotBlank(message = "台球桌名称不能为空")
    private String tableName;

    private String areaName;

    @NotBlank(message = "台球桌状态不能为空")
    private String status;

    @NotNull(message = "每小时单价不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "每小时单价必须大于 0")
    private BigDecimal hourlyPrice;

    private String imageUrl;

    private String remark;

    public String getTableNo() {
        return tableNo;
    }

    public void setTableNo(String tableNo) {
        this.tableNo = tableNo;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getHourlyPrice() {
        return hourlyPrice;
    }

    public void setHourlyPrice(BigDecimal hourlyPrice) {
        this.hourlyPrice = hourlyPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
