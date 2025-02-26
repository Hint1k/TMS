package com.demo.tms.service;

import com.demo.tms.entity.Role;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.repository.RoleRepository;
import com.demo.tms.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Role saveRole(Role role) {
        validateUser(role);
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role updateRole(Long roleId, Role updatedRole) {
        try {
            Role existingRole = roleRepository.findById(roleId).orElseThrow(() ->
                    new ResourceNotFoundException("Role with ID " + roleId + " not found"));

            // Update role fields
            existingRole.setAuthority(updatedRole.getAuthority());
            existingRole.setUser(updatedRole.getUser());

            return roleRepository.save(existingRole);
        } catch (Exception e) {
            log.error("Error updating role with ID {}: {}", roleId, e.getMessage(), e);
            throw new ResourceNotFoundException("Error updating role with ID " + roleId);
        }
    }

    @Override
    @Transactional
    public boolean deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role != null) {
            roleRepository.deleteById(roleId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Helper method to validate if the assigned user exists
    private void validateUser(Role role) {
        if (role.getUser() != null && !userRepository.existsById(role.getUser().getUserId())) {
            throw new ResourceNotFoundException("User with ID " + role.getUser().getUserId() + " not found");
        }
    }
}