package roomescape.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.common.DomainAssert;
import roomescape.common.exception.InvalidInputException;
import roomescape.store.Store;

public class Member {
    private static final int NAME_MAX_LENGTH = 20;

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final MemberRole role;
    private final Store store;

    public Member(Long id, String name, String email, String password, MemberRole role) {
        this(id, name, email, password, role, null);
    }

    public Member(Long id, String name, String email, String password, MemberRole role, Store store) {
        validate(name, email, password, role);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.store = store;
    }

    private void validate(String name, String email, String password, MemberRole role) {
        DomainAssert.notNull(name, "이름은 비어 있을 수 없습니다.");
        DomainAssert.notNull(email, "이메일 형식이 올바르지 않습니다.");
        DomainAssert.notNull(password, "비밀번호는 비어 있을 수 없습니다.");
        DomainAssert.notNull(role, "역할은 비어 있을 수 없습니다.");
        if (name.isBlank()) {
            throw new InvalidInputException("이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new InvalidInputException("이름은 " + NAME_MAX_LENGTH + "자 이하여야 합니다.");
        }
        if (email.isBlank() || !email.contains("@")) {
            throw new InvalidInputException("이메일 형식이 올바르지 않습니다.");
        }
        if (password.isBlank()) {
            throw new InvalidInputException("비밀번호는 비어 있을 수 없습니다.");
        }
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

    public Store getStore() {
        return store;
    }

    public Long getStoreId() {
        if (store == null) {
            return null;
        }
        return store.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Member member)) {
            return false;
        }
        return id != null && Objects.equals(id, member.id);
    }
}
