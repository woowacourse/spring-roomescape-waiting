package roomescape.member.domain;

import roomescape.common.exception.BusinessException;

public class Member {

    private final Long id;
    private final Name name;
    private final Email email;
    private final Password password;

    private Member(final Long id, final Name name, final Email email, final Password password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static Member createWithId(final Long id, String name, String email, String password) {
        validateIdIsNonNull(id);
        return new Member(id, new Name(name), new Email(email), new Password(password));
    }

    private static void validateIdIsNonNull(final Long id) {
        if (id == null) {
            throw new BusinessException("회원 id는 null일 수 없습니다.");
        }
    }

    public static Member createWithoutId(String name, String email, String password) {
        return new Member(null, new Name(name), new Email(email), new Password(password));
    }

    public boolean isSamePassword(final String password) {
        return this.password.getPassword().equals(password);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email.getEmail();
    }

    public String getPassword() {
        return password.getPassword();
    }
}
