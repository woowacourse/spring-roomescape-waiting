package roomescape.domain;

import static roomescape.domain.exception.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.exception.DomainErrorCode.UNAUTHORIZED_RESERVATION;
import static roomescape.domain.exception.DomainPreconditions.require;
import static roomescape.domain.exception.DomainPreconditions.requireNonBlank;
import static roomescape.domain.exception.DomainPreconditions.requireNonNull;

import java.util.Objects;

import roomescape.domain.exception.RoomescapeException;

public class Member {

    private static final int MAX_LOGIN_ID_SIZE = 255;
    private static final int MAX_NAME_SIZE = 10;
    private static final int MAX_PASSWORD_SIZE = 255;

    private final Long id;
    private final String loginId;
    private final String name;
    private final String password;
    private final Role role;

    public Member(Long id, String loginId, String name, String password, Role role) {
        String validatedLoginId = requireNonBlank(loginId, INVALID_INPUT, "로그인 ID는 비거나 공백일 수 없습니다.");
        String validatedName = requireNonBlank(name, INVALID_INPUT, "회원 이름은 비거나 공백일 수 없습니다.");
        String validatedPassword = requireNonBlank(password, INVALID_INPUT, "비밀번호는 비거나 공백일 수 없습니다.");

        require(validatedLoginId.length() <= MAX_LOGIN_ID_SIZE, INVALID_INPUT, String.format("로그인 ID는 %d자를 초과할 수 없습니다.", MAX_LOGIN_ID_SIZE));
        require(validatedName.length() <= MAX_NAME_SIZE, INVALID_INPUT, String.format("회원 이름은 %d자를 초과할 수 없습니다.", MAX_NAME_SIZE));
        require(validatedPassword.length() <= MAX_PASSWORD_SIZE, INVALID_INPUT, String.format("비밀번호는 %d자를 초과할 수 없습니다.", MAX_PASSWORD_SIZE));

        this.id = id;
        this.loginId = validatedLoginId;
        this.name = validatedName;
        this.password = validatedPassword;
        this.role = requireNonNull(role, INVALID_INPUT, "권한은 null일 수 없습니다.");
    }

    public boolean matchesPassword(String password) {
        return this.password.equals(password);
    }

    public void validateSameMember(Member member) {
        requireNonNull(member, INVALID_INPUT, "예약자는 null일 수 없습니다.");

        if (!Objects.equals(id, member.id)) {
            throw new RoomescapeException(UNAUTHORIZED_RESERVATION, "본인의 예약만 변경할 수 있습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
