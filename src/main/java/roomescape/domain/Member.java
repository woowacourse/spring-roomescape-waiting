package roomescape.domain;

import roomescape.auth.Role;

public class Member {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 10;
    private static final String NAME_PATTERN = "^[a-zA-Z가-힣]+$";

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final Role role;
    private final Long storeId;

    public Member(Long id, String email, String password, String name, Role role, Long storeId) {
        validateEmail(email);
        validatePassword(password);
        validateName(name);
        validateRole(role);
        validateStoreId(storeId);

        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.storeId = storeId;
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

    public Role getRole() {
        return role;
    }

    public Long getStoreId() {
        return storeId;
    }

    public boolean matchesPassword(String rawPassword) {
        return this.password.equals(rawPassword);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 비어 있을 수 없습니다.");
        }
        if (!email.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어 있을 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("이름은 2자 이상 10자 이하여야 합니다.");
        }
        if (!name.matches(NAME_PATTERN)) {
            throw new IllegalArgumentException("이름은 한글과 영문만 입력할 수 있습니다.");
        }
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("권한은 비어 있을 수 없습니다.");
        }
    }

    private void validateStoreId(Long storeId) {
        if (storeId != null && storeId <= 0) {
            throw new IllegalArgumentException("매장 ID는 양수여야 합니다.");
        }
    }
}
