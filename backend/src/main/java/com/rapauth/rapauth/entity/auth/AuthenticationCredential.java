package com.rapauth.rapauth.entity.auth;

import com.rapauth.rapauth.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "authentication_credentials")
@Getter
@Setter
public class AuthenticationCredential extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "authentication_enrollment_id",
            nullable = false,
            unique = true
    )
    private AuthenticationEnrollment authenticationEnrollment;

    @Column(name = "credential_value", nullable = false, length = 1000)
    private String credentialValue;

    @Column(name = "metadata", length = 2000)
    private String metadata;
}