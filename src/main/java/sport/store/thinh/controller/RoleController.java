package sport.store.thinh.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sport.store.thinh.domain.dto.request.ReqRoleDTO;
import sport.store.thinh.domain.dto.response.ResRoleDTO;
import sport.store.thinh.service.RoleService;
import sport.store.thinh.util.annotation.APIMessage;

@RestController
@RequestMapping("/api/v1")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    @APIMessage("Create a new role")
    public ResponseEntity<ResRoleDTO> createRole(@RequestBody ReqRoleDTO reqRoleDTO) {
        ResRoleDTO resRoleDTO = this.roleService.createRole(reqRoleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(resRoleDTO);
    }
}
