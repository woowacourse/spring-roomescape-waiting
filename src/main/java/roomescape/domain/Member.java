package roomescape.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Entity
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
    @Enumerated(value = EnumType.STRING)
    private Role role;

    public Member() {
    }

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
