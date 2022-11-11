package com.softserve.itacademy.controller.rest;

import com.softserve.itacademy.dto.user.UserRequest;
import com.softserve.itacademy.dto.user.UserResponse;
import com.softserve.itacademy.exception.EntityNotCreatedException;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.security.UserDetailsServiceImpl;
import com.softserve.itacademy.service.RoleService;
import com.softserve.itacademy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;
    private final RoleService roleService;
    private final UserDetailsServiceImpl userDetailsService;

    public UserRestController(UserService userService, RoleService roleService, UserDetailsServiceImpl userDetailsService) {
        this.userService = userService;
        this.roleService = roleService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@Valid @RequestBody UserRequest userRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new EntityNotCreatedException(EntityNotCreatedException.errorMessage(bindingResult));
        }
        User newUser = new User();
        newUser.setFirstName(userRequest.getFirstName());
        newUser.setLastName(userRequest.getLastName());
        newUser.setEmail(userRequest.getEmail());
        newUser.setPassword(new BCryptPasswordEncoder().encode(userRequest.getPassword()));
        newUser.setRole(roleService.readById(2L));
        User newUser1 = userService.create(newUser);
        return new ResponseEntity<>(new UserResponse(newUser1), HttpStatus.CREATED);
    }

    @GetMapping("/{user_id}")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.readById(#id).email.equals(authentication.name)")
    public ResponseEntity<?> read(@PathVariable("user_id") long id) {
        return ResponseEntity.ok(new UserResponse(userService.readById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.readById(#id).email.equals(authentication.name)")
    public ResponseEntity<?> update(@PathVariable long id, @Valid @RequestBody UserRequest userRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new EntityNotCreatedException(EntityNotCreatedException.errorMessage(bindingResult));
        }
        User oldUser = userService.readById(id);
        oldUser.setFirstName(userRequest.getFirstName());
        oldUser.setLastName(userRequest.getLastName());
        oldUser.setEmail(userRequest.getEmail());
        oldUser.setPassword(new BCryptPasswordEncoder(10).encode(userRequest.getPassword()));

        if (userDetailsService.getSecurityUser().getAuthorities().iterator().next().toString().equals("ROLE_ADMIN")
                && userRequest.getRole() != null) {
            oldUser.setRole(roleService.getByName(userRequest.getRole()));
        } else {
            oldUser.setRole(oldUser.getRole());
        }
        oldUser = userService.update(oldUser);
        return new ResponseEntity<>(new UserResponse(oldUser), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.readById(#id).email.equals(authentication.name)")
    public ResponseEntity<?> delete(@PathVariable long id) {
        userService.delete(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll() {
        return userService.getAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }
}
