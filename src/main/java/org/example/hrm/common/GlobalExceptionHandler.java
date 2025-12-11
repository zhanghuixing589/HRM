package org.example.hrm.common;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException ex){
       // 从异常中获取所有字段错误信息，拼接成字符串
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.error("参数校验异常: {}", message);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(),message);
    }

     // 处理业务异常
    @ExceptionHandler(value = BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException ex) {
       log.error("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage());
    }

    //处理绑定异常
     @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException ex) {
         // 获取所有字段错误信息
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数绑定异常: {}", message);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), message);
    }
    
    //处理参数校验异常，单个参数
      @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        log.error("参数校验异常: {}", message);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), message);
    }

    // 处理其他异常
     @ExceptionHandler(Exception.class)
    public Result<?> handleException(HttpServletRequest request, Exception ex) {
        log.error("系统异常: URL={}, Error={}", request.getRequestURL(), ex.getMessage(), ex);
        return Result.error(ResultCode.ERROR.getCode(), "系统繁忙，请稍后再试");
    }

}
