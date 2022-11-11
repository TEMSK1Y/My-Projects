package com.softserve.itacademy.controller.view;

import com.softserve.itacademy.dto.AuthenticationRequestDTO;
import com.softserve.itacademy.security.SecurityUser;
import com.softserve.itacademy.security.UserDetailsServiceImpl;
import com.softserve.itacademy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/")
public class HomeController {
    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final AuthenticationManager authenticationManager;

    public HomeController(AuthenticationManager authenticationManager, UserService userService, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @GetMapping("/**")
    public String error404() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
    }

    @GetMapping(value = {"/", "home"})
    public String home(Model model) {
        SecurityUser securityUser = userDetailsServiceImpl.getSecurityUser();
        model.addAttribute("owner", securityUser);
        if (securityUser.getAuthorities().iterator().next().toString().equals("ROLE_ADMIN")) {
            model.addAttribute("users", userService.getAll());
            return "home";
        } else return "redirect:/todos/all/users/" + securityUser.getId();
    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String getLogin(AuthenticationRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {
            return "redirect:/login?error";
        }
        return "redirect:/home";
    }
}
