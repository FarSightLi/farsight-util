package org.example.performance.component;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class Result<T> implements Serializable {

    //api 状态码
    private Integer code;

    //业务信息
    private String message;

    //数据信息
    private T data;


    public Result(Integer code, String message, T data) {

        this.code = code;
        this.message = message;
        this.data = data;
    }

    private Result(CodeMsg codeMsg) {
        if (codeMsg != null) {
            this.code = codeMsg.getCode();
        }
    }

    private Result(CodeMsg codeMsg, String message, T data) {
        if (codeMsg != null) {
            this.code = codeMsg.getCode();
        }
        this.message = message;
        this.data = data;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Result<T> success(String message, T data) {
        return new Result(CodeMsg.SUCCESS, CodeMsg.SUCCESS.getCodeRemark(), data);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> Result<List<T>> successForPage(List<T> list) {
        return new Result(CodeMsg.SUCCESS, CodeMsg.SUCCESS.getCodeRemark(), list);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Result<T> success(T data) {
        return new Result(CodeMsg.SUCCESS, CodeMsg.SUCCESS.getCodeRemark(), data);
    }

    @SuppressWarnings({"rawtypes"})
    public static Result success() {
        return new Result(CodeMsg.SUCCESS);
    }

    @SuppressWarnings("rawtypes")
    public static Result error(BusinessException e) {
        return error(e.getCodeMsg(), e.getCodeMsg().getCodeRemark());
    }

    @SuppressWarnings({"rawtypes"})
    public static Result error(CodeMsg codeMsg) {
        return error(codeMsg, codeMsg.getCodeRemark());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Result error(CodeMsg codeMsg, String message) {
        return new Result(codeMsg.getCode(), codeMsg.getCodeRemark(), null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Result<T> error(CodeMsg codeMsg, T data) {
        return new Result(codeMsg, null, data);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
