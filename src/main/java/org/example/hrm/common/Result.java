package org.example.hrm.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

     public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    
    public static <T> Result<T> success() {
       return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }
    
    public static <T> Result<T> success(T data) {
         return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> success(ResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }
    
    public static <T> Result<T> error() {
         return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage());
    }
    
    public static <T> Result<T> error(String message) {
       return new Result<>(ResultCode.ERROR.getCode(), message);
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

     public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }
    
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message);
    }

    //快速方法
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(code);
    }
}
