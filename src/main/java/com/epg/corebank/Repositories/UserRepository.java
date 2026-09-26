package com.epg.corebank.Repositories;

import com.epg.corebank.Models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// The save() method inherited from JpaRepository can be used to add new Users
// Usage: userRepository.save(newUser);
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query("""
            select distinct u
            from User u
            left join fetch u.userRoles ur
            left join fetch ur.role
            where u.username = :username
            """)
    Optional<User> findWithRolesByUsername(@Param("username") String username);


}