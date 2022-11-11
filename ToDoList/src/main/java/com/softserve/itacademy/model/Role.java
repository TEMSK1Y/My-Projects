package com.softserve.itacademy.model;

import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "roles")
@NoArgsConstructor
public class Role {

    public Set<SimpleGrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> res = new HashSet<>();
        res.add(name.equals("ADMIN") ? new SimpleGrantedAuthority(Permission.ADMIN.getPermission())
                : new SimpleGrantedAuthority(Permission.USER.getPermission()));
        return res;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "The 'name' cannot be empty")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "role")
    private List<User> users;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "Role {" +
                "id = " + id +
                ", name = '" + name + '\'' +
                "} ";
    }

}
