package sport.store.thinh.service;

import org.springframework.stereotype.Service;
import sport.store.thinh.domain.Roles;
import sport.store.thinh.domain.dto.request.ReqRoleDTO;
import sport.store.thinh.domain.dto.response.ResRoleDTO;
import sport.store.thinh.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public ResRoleDTO createRole(ReqRoleDTO reqRoleDTO) {
        if (this.roleRepository.existsByRole(reqRoleDTO.getRole())) {
            throw new IllegalArgumentException("Role already exists");
        }
        Roles role = new Roles();
        role.setRole(reqRoleDTO.getRole());
        role.setDescription(reqRoleDTO.getDescription());
        Roles savedRole = roleRepository.save(role);
        return convertToResRoleDTO(savedRole);
    }

    public ResRoleDTO convertToResRoleDTO(Roles role) {
        ResRoleDTO resRoleDTO = new ResRoleDTO();
        resRoleDTO.setId(role.getId());
        resRoleDTO.setRole(role.getRole());
        resRoleDTO.setDescription(role.getDescription());
        resRoleDTO.setCreatedAt(role.getCreatedAt());
        resRoleDTO.setCreatedBy(role.getCreatedBy());
        resRoleDTO.setUpdatedAt(role.getUpdatedAt());
        resRoleDTO.setUpdatedBy(role.getUpdatedBy());
        return resRoleDTO;
    }
}
