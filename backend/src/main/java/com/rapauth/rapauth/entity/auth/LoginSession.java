package com.rapauth.rapauth.entity.auth;

import com.rapauth.rapauth.entity.common.BaseEntity;
import com.rapauth.rapauth.entity.user.User;
import com.rapauth.rapauth.enums.LoginSessionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "login_sessions")
public class LoginSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_method", nullable = false)
    private AuthenticationMethod selectedMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginSessionStatus status;

    @Column(name = "regeneration_count", nullable = false)
    private Integer regenerationCount = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
private Integer failedAttempts = 0;

@Column
private LocalDateTime lockedUntil;
}