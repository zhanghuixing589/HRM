package org.example.hrm.service;

import org.example.hrm.entity.Role;
import org.example.hrm.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    public Role findByRoleType(Integer roleType) {
        return roleRepository.findByRoleType(roleType).orElse(null);
    }
    
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
    
    public Role save(Role role) {
        return roleRepository.save(role);
    }
    
    public Role findByRoleCode(String roleCode) {
        return roleRepository.findByRoleCode(roleCode).orElse(null);
    }
}
