package com.softserve.itacademy.security;

import com.softserve.itacademy.exception.NullEntityReferenceException;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws NullEntityReferenceException {
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst().orElse(null);
        if (user == null) throw new UsernameNotFoundException("User not found!");

        return SecurityUser.fromUser(user);
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    public SecurityUser getSecurityUser() {
        return (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public void manualLogout() {
        SecurityContextHolder.clearContext();
    }

}
