package org.example.hrm.common;

public class BusinessException  extends RuntimeException {
    private Integer code;

     public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode){
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
    
    public Integer getCode() {
        return code;
    }

     public void setCode(Integer code) {
        this.code = code;
    }
}
