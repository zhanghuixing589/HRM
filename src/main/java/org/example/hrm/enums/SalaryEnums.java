package org.example.hrm.enums;
/**
 * 薪酬管理枚举类
 */

import lombok.Getter;

public class SalaryEnums {
    //项目类型枚举
    @Getter
    public enum ProjectType{
        INCOME(1,"增项"),
        DEDUCTION(2,"减项");

        private final int code;
        private final String name;

        ProjectType(int code, String name){
            this.code = code;
            this.name = name;
        }

        
    }

}
