package roomescape.domain.member;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
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

    protected Member() {
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

    public boolean isSamePassword(String password) {
        return this.password.isSamePassword(password);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
