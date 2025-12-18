package org.example.hrm.common;

import lombok.Getter;

@Getter
public enum ResultCode {
     /* 通用状态码 */
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源未找到"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

      /* 业务状态码 */
    BUSINESS_ERROR(1000, "业务异常"),
    VALIDATE_FAILED(1001, "参数校验失败"),
    DUPLICATE_KEY(1002, "数据重复"),
    DATA_NOT_EXIST(1003, "数据不存在"),
    DATA_EXIST(1004, "数据已存在"),

    //用户相关
    USER_NOT_EXIST(2001, "用户不存在"),
    USER_DISABLED(2002, "用户被禁用"), //用户离职
    PASSWORD_ERROR(2003, "密码错误"),
    TOKEN_EXPIRED(2004, "Token已过期"),
    TOKEN_INVALID(2005, "无效的Token"),

    //薪酬相关
    SALARY_STANDARD_NOT_EXIST(3001, "薪酬标准不存在"),
    SALARY_STANDARD_APPROVED(3002, "薪酬标准已审核"),
    SALARY_CALCULATE_ERROR(3003, "薪酬计算错误"),
    SALARY_PAYMENT_NOT_EXIST(3004, "薪酬发放记录不存在"),
    SALARY_PAYMENT_SUBMITTED(3005, "薪酬单已提交"),
    EMPLOYEE_RESIGNED(3006, "员工已离职"),
    NO_SALARY_CONFIG(3007, "员工未配置薪资"),
    SALARY_ITEM_NOT_FOUND(3008, "薪酬项目不存在"),

    // 权限相关
    NO_PERMISSION(4001, "无操作权限"),
    ROLE_NOT_EXIST(4002, "角色不存在"),
    
    // 文件操作
    FILE_UPLOAD_ERROR(5001, "文件上传失败"),
    FILE_NOT_EXIST(5002, "文件不存在"),
    FILE_DOWNLOAD_ERROR(5003, "文件下载失败"),

     /* 机构职位相关 */
    ORG_NOT_EXIST(6001, "机构不存在"),
    POSITION_NOT_EXIST(6002, "职位不存在"), 
    POSITION_LESS(6003,"职位没有关联"),
    PARAM_ERROR(3009,"必须为增项或者减项");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

  public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


}
