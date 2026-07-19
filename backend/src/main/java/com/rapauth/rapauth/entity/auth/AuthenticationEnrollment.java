package com.rapauth.rapauth.entity.auth;

import com.rapauth.rapauth.entity.common.BaseEntity;
import com.rapauth.rapauth.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "authentication_enrollments")
@Getter
@Setter
public class AuthenticationEnrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthenticationMethod method;

    @Column(nullable = false)
    private boolean enabled = true;
}