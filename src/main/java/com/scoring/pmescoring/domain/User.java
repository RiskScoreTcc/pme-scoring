package com.scoring.pmescoring.domain;

import com.scoring.pmescoring.model.TypeUser;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email",nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeUser userType;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "creation_date", updatable = false)
    private LocalDate creationDate;

    public User() {
    }

    public User(String email, String password, TypeUser userType) {
        this.email = email;
        this.password = password;
        this.userType = userType;
    }

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDate.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.userType.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TypeUser getUserType() {
        return userType;
    }

    public void setUserType(TypeUser userType) {
        this.userType = userType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void updateData(String email, String password, TypeUser userType) {
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
        if (password != null && !password.isBlank()) {
            this.password = password;
        }
        if (userType != null) {
            this.userType = userType;
        }
    }

    public void delete(){
     this.active = false;
    }
}

