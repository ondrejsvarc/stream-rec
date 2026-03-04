package svarcondrej.stream_rec.model;

import jakarta.persistence.*;
import lombok.Data;
import svarcondrej.stream_rec.enums.Role;

import java.util.UUID;

@Entity
@Table(name = "app_users")
@Data
public class User {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
}
