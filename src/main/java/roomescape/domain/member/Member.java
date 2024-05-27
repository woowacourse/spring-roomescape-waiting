package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private PlayerName name;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    protected Member() {
    }

    public Member(Long id, PlayerName name, Email email, Password password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(PlayerName name, Email email, Password password) {
        this(null, name, email, password, Role.MEMBER);
    }

    public Member(String name, String email, String password) {
        this(null, new PlayerName(name), new Email(email), new Password(password), Role.MEMBER);
    }

    public Member(String name, String email, String password, Role role) {
        this(null, new PlayerName(name), new Email(email), new Password(password), role);
    }

    public boolean matchPassword(Password otherPassword) {
        return password.equals(otherPassword);
    }

    public boolean isNotAdmin() {
        return !role.isAdmin();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member other)) {
            return false;
        }
        return Objects.equals(id, other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email.getAddress();
    }

    public String getPassword() {
        return password.getPassword();
    }

    public Role getRole() {
        return role;
    }
}
