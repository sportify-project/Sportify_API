package sport.store.thinh.controller;

import com.turkraft.springfilter.boot.Filter;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sport.store.thinh.domain.Users;
import sport.store.thinh.domain.dto.response.ResUserDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.service.UserService;
import sport.store.thinh.util.annotation.APIMessage;


@RestController
@RequestMapping("/api/v1")
public class UserController {
    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<Users> handleCreateUser(@RequestBody Users user) {
        userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/users/{id}")
    @APIMessage("Get user by id")
    public ResponseEntity<ResUserDTO> handleFindUserById(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findById(id));
    }

    @GetMapping("/users")
    @APIMessage("Get all users")
    public ResponseEntity<ResultPaginationDTO<ResUserDTO>> handleFindAllUsers(
            @Spec(path = "email", spec = Equal.class) Specification<Users> spec,
            Pageable pageable) {
        ResultPaginationDTO<ResUserDTO> users = userService.findAllUsers(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }
}
