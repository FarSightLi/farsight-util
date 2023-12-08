package org.example.performance.component.exception;

public enum CodeMsg {
    //成功
    SUCCESS(200, "success"),
    TOKEN_ERROR(5000, "Token错误"),
    SYSTEM_ERROR(10001, "系统错误"),
    DATABASE_ERROR(10002, "数据库错误"),
    PARAMETER_ERROR(10008, "参数错误，请参考API文档");

    private int code;
    private String codeRemark;

    CodeMsg() {
    }

    CodeMsg(int code, String codeRemark) {
        this.code = code;
        this.codeRemark = codeRemark;
    }

    @Override
    public String toString() {
        return "CodeMsg [code=" + code + ", codeRemark=" + codeRemark + "]";
    }

    public int getCode() {
        return code;
    }

    public String getCodeRemark() {
        return codeRemark;
    }
}
