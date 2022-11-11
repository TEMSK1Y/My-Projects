package com.softserve.itacademy.controller.view;

import com.softserve.itacademy.model.User;
import com.softserve.itacademy.security.UserDetailsServiceImpl;
import com.softserve.itacademy.service.RoleService;
import com.softserve.itacademy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public UserController(UserService userService, RoleService roleService,
                          UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userService = userService;
        this.roleService = roleService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @GetMapping("/**")
    public String error404() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
    }

    @GetMapping("/create")
    public String create(Model model) {
        try {
            model.addAttribute("owner", userDetailsServiceImpl.getSecurityUser());
        } catch (Exception e) {
            throw new UsernameNotFoundException("User does not exist!");
        } finally {
            model.addAttribute("user", new User());
            return "create-user";
        }
    }

    @PostMapping("/create")
    public String create(@Validated @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "create-user";
        }
        user.setPassword(new BCryptPasswordEncoder(10).encode(user.getPassword()));
        user.setRole(roleService.readById(2));
        User newUser = userService.create(user);
        return "redirect:/todos/all/users/" + newUser.getId();
    }

    @GetMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.readById(#id).email.equals(authentication.name)")
    public String read(@PathVariable long id, Model model) {
        model.addAttribute("owner", userDetailsServiceImpl.getSecurityUser());
        User user = userService.readById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    @GetMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.readById(#id).email.equals(authentication.name)")
    public String update(@PathVariable long id, Model model) {
        model.addAttribute("owner", userDetailsServiceImpl.getSecurityUser());
        User user = userService.readById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAll());
        return "update-user";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable long id, Model model, @Validated @ModelAttribute("user") User user, @RequestParam("roleId") long roleId, BindingResult result) {
        User oldUser = userService.readById(id);
        if (result.hasErrors()) {
            user.setRole(oldUser.getRole());
            model.addAttribute("roles", roleService.getAll());
            return "update-user";
        }
        if (userDetailsServiceImpl.getSecurityUser().getAuthorities().iterator().next().toString().equals("ROLE_ADMIN")) {
            user.setRole(roleService.readById(roleId));
        } else {
            user.setRole(oldUser.getRole());
        }
        user.setPassword(new BCryptPasswordEncoder(10).encode(user.getPassword()));
        userService.update(user);
        return "redirect:/users/{id}/read";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.readById(#id).email.equals(authentication.name)")
    public String delete(@PathVariable("id") long id) {
        if (userDetailsServiceImpl.getCurrentUsername().equals(userService.readById(id).getEmail())) {
            userService.delete(id);
            userDetailsServiceImpl.manualLogout();
        } else {
            userService.delete(id);
        }
        return "redirect:/users/all";
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAll(Model model) {
        model.addAttribute("owner", userDetailsServiceImpl.getSecurityUser());
        model.addAttribute("users", userService.getAll());
        return "users-list";
    }
}
