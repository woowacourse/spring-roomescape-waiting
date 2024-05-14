package roomescape.domain;

import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public class Member {
    private final Long id;
    private final PlayerName name;
    private final Email email;
    private final Password password;
    private final Role role;

    public Member(PlayerName name, Email email, Password password, Role role) {
        this(null, name, email, password, role);
    }

    public Member(Long id, PlayerName name, Email email, Password password, Role role) {
        if (name == null) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "이름은 필수 입력값 입니다.");
        }
        if (email == null) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "이메일은 필수 입력값 입니다.");
        }
        if (password == null) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "비밀번호는 필수 입력값 입니다.");
        }
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean matchPassword(String password) {
        return this.password.matches(password);
    }

    public Member withId(long id) {
        return new Member(id, this.name, this.email, this.password, this.role);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
