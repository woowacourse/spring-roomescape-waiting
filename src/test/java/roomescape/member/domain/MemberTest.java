package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static roomescape.member.role.Role.ADMIN;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.exception.ReserverNameEmptyException;

class MemberTest {
    @Test
    @DisplayName("이름은 공백이 될 수 없다.")
    void nameNullTest() {
        assertThatThrownBy(() ->
                new Member(1L, new Name(""), new Email("matt.kakao"), new Password("1234"), ADMIN))
                .isInstanceOf(ReserverNameEmptyException.class)
                .hasMessage("[ERROR] 예약자 이름은 필수입니다.");
    }

    @Test
    @DisplayName("이메일은 공백이 될 수 없다.")
    void emailNullTest() {
        assertThatThrownBy(() ->
                new Member(1L, new Name("매트"), new Email(""), new Password("1234"), ADMIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] email 은 공백이 될 수 없습니다.");
    }

    @Test
    @DisplayName("비밀번호는 공백이 될 수 없다.")
    void passwordNullTest() {
        assertThatThrownBy(() ->
                new Member(1L, new Name("매트"), new Email("matt.kakao"), new Password(""), ADMIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 비밀번호는 공백이 될 수 없습니다.");
    }

    @Test
    @DisplayName("회원은 아이디, 이름, 이메일, 비밀번호를 가진다.")
    void memberTest() {
        assertDoesNotThrow(() ->
                new Member(1L, new Name("매트"), new Email("matt.kakao"), new Password("1234"), ADMIN));
    }
}
