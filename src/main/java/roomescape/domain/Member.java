package roomescape.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Member {
    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final MemberRole role;
    private final Long storeId;

    public Member(Long id, String name, String email, String password, MemberRole role) {
        this(id, name, email, password, role, null);
    }

    public Member(Long id, String name, String email, String password, MemberRole role, Long storeId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.storeId = storeId;
    }

    public boolean isAdmin() {
        return role == MemberRole.ADMIN;
    }

    public boolean isManager() {
        return role == MemberRole.MANAGER;
    }

    public boolean matchesPassword(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.password);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }

    public Long getStoreId() {
        return storeId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Member that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
