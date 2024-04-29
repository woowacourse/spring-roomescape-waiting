package roomescape.domain.member;

import java.util.Objects;

public final class LoginMember {

    private final Long id;
    private final String email;
    private final String name;
    private final Role role;

    public LoginMember(String id, String email, String name, String role) {
        validate(id, email, name, role);
        this.id = Long.valueOf(id);
        this.email = email;
        this.name = name;
        this.role = Role.valueOf(role);
    }

    private void validate(String id, String email, String name, String role) {
        validateNull(id, email, name, role);
        validateNumberFormat(id);
        validateRole(role);
    }

    private void validateNull(String id, String email, String name, String role) {
        if (id == null || email == null || name == null || role == null) {
            throw new IllegalArgumentException("비정상 토큰입니다.");
        }
    }

    private static void validateNumberFormat(String id) {
        try {
            Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("비정상 토큰입니다.");
        }
    }

    private void validateRole(String role) {
        try {
            Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("비정상 토큰입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role.toString();
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isNotRegistered() {
        return role == Role.NOT_REGISTERED;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (LoginMember) obj;
        return Objects.equals(this.id, that.id) &&
            Objects.equals(this.email, that.email) &&
            Objects.equals(this.name, that.name) &&
            Objects.equals(this.role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, name, role);
    }
}
