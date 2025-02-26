package com.demo.tms.service;

import com.demo.tms.entity.Role;

import java.util.List;

public interface RoleService {

    Role saveRole(Role role);

    Role updateRole(Long roleId, Role updatedRole);

    boolean deleteRole(Long roleId);

    Role getRoleById(Long roleId);

    List<Role> getAllRoles();
}