package com.epg.corebank.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "user_roles", schema = "public", indexes = {
        @Index(name = "idx_user_roles_user_id",
                columnList = "user_id"),
        @Index(name = "idx_user_roles_role_id",
                columnList = "role_id")})
public class UserRole {
    @EmbeddedId
    private UserRoleId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    //@JasonIgnoreProperties("userRoles")
    private User user;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


}