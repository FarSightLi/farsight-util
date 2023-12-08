package org.example.performance.component.exception;

public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private CodeMsg codeMsg;

    private String msg;

    public BusinessException() {

    }

    public BusinessException(CodeMsg codeMsg) {
        this(codeMsg, codeMsg.getCodeRemark());
    }

    public BusinessException(CodeMsg codeMsg, String msg) {
        super(codeMsg.toString() + msg);
        this.codeMsg = codeMsg;
        this.msg = msg;
    }

    public CodeMsg getCodeMsg() {
        return codeMsg;
    }
}
