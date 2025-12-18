package org.example.hrm.controller;

import java.util.List;

import org.example.hrm.common.Result;
import org.example.hrm.entity.Organization;
import org.example.hrm.repository.OrganizationRepository;
import org.example.hrm.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/salary/payment")
@CrossOrigin(origins = "*")
@Slf4j
@Validated
public class SalaryPaymentController {
    @Autowired
    private OrganizationService organizationService;

    //获取三级机构
    /**
 * 获取所有三级机构（用于薪酬发放页面筛选）
 */
@RequestMapping("/third-level/all")
public Result<List<Organization>> getAllThirdLevelOrganizations() {
    try {
        List<Organization> thirdLevelOrgs = organizationService.getAllThirdLevelOrgs();
        return Result.success(thirdLevelOrgs);
    } catch (Exception e) {
        log.error("获取所有三级机构失败", e);
        return Result.error("获取所有三级机构失败: " + e.getMessage());
    }
}

}
