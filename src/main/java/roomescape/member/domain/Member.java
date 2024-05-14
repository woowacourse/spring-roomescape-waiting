package roomescape.member.domain;

import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

public class Member {
    private Long id;
    private MemberName name;
    private Email email;
    private Password password;
    private Role role;

    public Member(final String name, final String email, final String password, final Role role) {
        this(null, new MemberName(name), new Email(email), new Password(password), role);
    }

    public Member(final Long id, final Member member) {
        this(id, member.name, member.email, member.password, member.role);
    }

    public Member(final Long id, final String name, final String email, final String password, final Role role) {
        this(id, new MemberName(name), new Email(email), new Password(password), role);
    }

    public Member(final Long id, final MemberName name, final Email email, final Password password, final Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;

        validateRole();
    }

    private void validateRole() {
        if (role == null) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("회원(Member) 역할(Role)에 유효하지 않은 값(null OR 공백)이 입력되었습니다. [values: %s]", this));
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.value();
    }

    public String getEmail() {
        return email.value();
    }

    public String getPassword() {
        return password.value();
    }

    public Role getRole() {
        return role;
    }

    public boolean isRole(final Role role) {
        return this.role == role;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name=" + name +
                ", email=" + email +
                ", password=" + password +
                ", role=" + role +
                '}';
    }
}
