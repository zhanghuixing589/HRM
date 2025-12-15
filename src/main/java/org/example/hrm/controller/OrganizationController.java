package org.example.hrm.controller;

import org.example.hrm.service.OrganizationService;
import org.example.hrm.entity.Organization;
import org.example.hrm.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/hr/organization")
@CrossOrigin
@Slf4j
@PreAuthorize("hasRole('HR_MANAGER')") // 只有人事经理可以操作
public class OrganizationController {

  @Autowired
  private OrganizationService organizationService;

  // 获取机构树
  @RequestMapping("/tree")
  public Result<List<Organization>> getOrganizationTree() {
    try {
      List<Organization> orgTree = organizationService.getOrganizationTree();
      return Result.success(orgTree);
    } catch (Exception e) {
      log.error("获取机构树失败", e);
      return Result.error("获取机构树失败: " + e.getMessage());
    }
  }

  // 根据级别获取机构
  @RequestMapping("/level/{level}")
  public Result<List<Organization>> getOrganizationsByLevel(@PathVariable Integer level) {
    try {
      List<Organization> orgs = organizationService.getOrgsByLevel(level);
      return Result.success(orgs);
    } catch (Exception e) {
      log.error("获取机构失败", e);
      return Result.error("获取机构失败: " + e.getMessage());
    }
  }

  // 根据父级id获取子机构
  @RequestMapping("/parent/{parentId}")
  public Result<List<Organization>> getOrgsByParentId(@PathVariable Long parentId) {
    try {
      List<Organization> orgs = organizationService.getOrgsByParentId(parentId);
      return Result.success(orgs);
    } catch (Exception e) {
      log.error("获取子机构失败", e);
      return Result.error("获取子机构失败: " + e.getMessage());
    }
  }

  // 获取所有启动的机构
  @RequestMapping("/active")
  public Result<List<Organization>> getActiveOrs() {
    try {
      List<Organization> orgs = organizationService.getActiveOrgs();
      return Result.success(orgs);
    } catch (Exception e) {
      log.error("获取启用的机构失败", e);
      return Result.error("获取启用的机构失败: " + e.getMessage());
    }
  }

  // 创建或更新机构
  @PostMapping("/save")
  public Result<Organization> saveOrganization(@RequestBody Organization organization) { // 添加 @RequestBody
    try {
      Organization savedOrg = organizationService.saveOrganization(organization);
      return Result.success("机构保存成功", savedOrg);
    } catch (Exception e) {
      log.error("保存机构失败", e);
      return Result.error("保存机构失败: " + e.getMessage());
    }
  }

  // 删除机构及其子机构
  @RequestMapping("/delete/{orgId}")
  public Result<String> deleteOrganization(@PathVariable Long orgId) {
    try {
      organizationService.deleteOrganization(orgId);
      return Result.success("机构删除成功");
    } catch (Exception e) {
      log.error("删除机构失败", e);
      return Result.error("删除机构失败: " + e.getMessage());
    }
  }

  // 启用或禁用机构
  @RequestMapping("/{orgId}/status/{status}")
  public Result<Organization> toggleStatus(@PathVariable Long orgId, @PathVariable Integer status) {
    try {
      Organization updatedOrg = organizationService.toggleStatus(orgId, status);
      return Result.success("机构状态更新成功", updatedOrg);
    } catch (Exception e) {
      log.error("更新机构状态失败", e);
      return Result.error("更新机构状态失败: " + e.getMessage());
    }
  }

  /**
   * 获取所有一级机构
   */
  @RequestMapping("/first-level")
  public Result<List<Organization>> getFirstLevelOrgs() {
    try {
      List<Organization> orgs = organizationService.getOrgsByLevel(1);
      return Result.success(orgs);
    } catch (Exception e) {
      log.error("获取一级机构失败", e);
      return Result.error("获取一级机构失败: " + e.getMessage());
    }
  }

  /**
   * 添加二级机构
   * 参数：parentId（一级机构ID）, orgName（二级机构名称）
   */
  @RequestMapping("/add-second-level")
  public Result<Organization> addSecondLevelOrg(Long parentId, String orgName) {
    try {
      // 验证一级机构是否存在
      Organization parentOrg = organizationService.getOrgById(parentId);
      if (parentOrg.getOrgLevel() != 1) {
        return Result.error("指定的父级机构不是一级机构");
      }

      // 创建二级机构
      Organization secondOrg = new Organization();
      secondOrg.setOrgName(orgName);
      secondOrg.setOrgLevel(2); // 二级机构
      secondOrg.setParId(parentId); // 父级是一级机构
      secondOrg.setStatus(1); // 启用

      Organization savedOrg = organizationService.saveOrganization(secondOrg);
      return Result.success("二级机构添加成功", savedOrg);
    } catch (Exception e) {
      log.error("添加二级机构失败", e);
      return Result.error("添加二级机构失败: " + e.getMessage());
    }
  }

  /**
   * 添加三级机构
   * 参数：parentId（二级机构ID）, orgName（三级机构名称）
   */
  @RequestMapping("/add-third-level")
  public Result<Organization> addThirdLevelOrg(Long parentId, String orgName) {
    try {
      // 验证二级机构是否存在
      Organization parentOrg = organizationService.getOrgById(parentId);
      if (parentOrg.getOrgLevel() != 2) {
        return Result.error("指定的父级机构不是二级机构");
      }

      // 创建三级机构
      Organization thirdOrg = new Organization();
      thirdOrg.setOrgName(orgName);
      thirdOrg.setOrgLevel(3); // 三级机构
      thirdOrg.setParId(parentId); // 父级是二级机构
      thirdOrg.setStatus(1); // 启用

      Organization savedOrg = organizationService.saveOrganization(thirdOrg);
      return Result.success("三级机构添加成功", savedOrg);
    } catch (Exception e) {
      log.error("添加三级机构失败", e);
      return Result.error("添加三级机构失败: " + e.getMessage());
    }
  }

  /**
   * 根据一级机构ID获取所有二级机构
   */
  @RequestMapping("/first/{firstOrgId}/seconds")
  public Result<List<Organization>> getSecondLevelOrgsByFirst(@PathVariable Long firstOrgId) {
    try {
      // 先获取一级机构的所有直接子机构（二级机构）
      List<Organization> seconds = organizationService.getOrgsByParentId(firstOrgId);
      return Result.success(seconds);
    } catch (Exception e) {
      log.error("获取二级机构失败", e);
      return Result.error("获取二级机构失败: " + e.getMessage());
    }
  }

  /**
   * 根据二级机构ID获取所有三级机构
   */
  @RequestMapping("/second/{secondOrgId}/thirds")
  public Result<List<Organization>> getThirdLevelOrgsBySecond(@PathVariable Long secondOrgId) {
    try {
      // 先获取二级机构的所有直接子机构（三级机构）
      List<Organization> thirds = organizationService.getOrgsByParentId(secondOrgId);
      return Result.success(thirds);
    } catch (Exception e) {
      log.error("获取三级机构失败", e);
      return Result.error("获取三级机构失败: " + e.getMessage());
    }
  }

  /**
   * 检查同一父级下机构名称是否重复
   */
  @RequestMapping("/check-name-duplicate")
  public Result<Boolean> checkNameDuplicate(Long parentId, String orgName, Long excludeOrgId) {
    try {
      boolean isDuplicate = organizationService.isNameDuplicate(parentId, orgName, excludeOrgId);
      return Result.success(!isDuplicate);
    } catch (Exception e) {
      log.error("检查机构名称重复失败", e);
      return Result.error("检查机构名称重复失败: " + e.getMessage());
    }
  }

  /**
   * 获取机构的完整层级路径（用于显示）
   */
  @RequestMapping("/path/{orgId}")
  public Result<String> getOrgPath(@PathVariable Long orgId) {
    try {
      String path = organizationService.getOrgPath(orgId);
      return Result.success(path);
    } catch (Exception e) {
      log.error("获取机构路径失败", e);
      return Result.error("获取机构路径失败: " + e.getMessage());
    }
  }

  /**
   * 分页获取机构列表
   */
  @GetMapping("/page")
  public Result<Page<Organization>> getOrganizationsPage(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) Integer level) {
    try {
      Page<Organization> orgs;
      if (level != null) {
        orgs = organizationService.getOrgsByLevelWithPage(level, page, size);
      } else {
        orgs = organizationService.getAllOrgsWithPage(page, size);
      }
      return Result.success(orgs);
    } catch (Exception e) {
      log.error("分页获取机构失败", e);
      return Result.error("分页获取机构失败: " + e.getMessage());
    }
  }

  /**
   * 分页获取机构树（扁平化）
   */
  @GetMapping("/tree/page")
  public Result<Page<Organization>> getOrganizationTreePage(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      Page<Organization> orgs = organizationService.getAllOrgsWithPage(page, size);
      return Result.success(orgs);
    } catch (Exception e) {
      log.error("分页获取机构树失败", e);
      return Result.error("分页获取机构树失败: " + e.getMessage());
    }
  }
}