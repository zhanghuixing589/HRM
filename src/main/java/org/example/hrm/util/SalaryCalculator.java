package org.example.hrm.util;

import org.example.hrm.repository.UserRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/* 
薪酬计算器
*/
@Component
@Slf4j
@RequiredArgsConstructor
public class SalaryCalculator {
    private final UserRepository userRepository;

    /* 计算员工薪酬 */
    

}
