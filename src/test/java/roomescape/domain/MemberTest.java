package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

class MemberTest {

    @DisplayName("이름이 null이면 예외가 발생한다.")
    @Test
    void createMemberExceptionTest() {
        assertThatCode(
                () -> new Member(null, new Email("member@wooteco.com"), new Password("wootecoCrew6!"), Role.BASIC))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("이메일이 null이면 예외가 발생한다.")
    @Test
    void createMemberExceptionTest2() {
        assertThatCode(
                () -> new Member(new PlayerName("member"), null, new Password("wootecoCrew6!"), Role.BASIC))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("비밀번호가 null이면 예외가 발생한다.")
    @Test
    void createMemberExceptionTest3() {
        assertThatCode(
                () -> new Member(new PlayerName("member"), new Email("member@wooteco.com"), null, Role.BASIC))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("비밀번호가 일치하면 true를 반환한다.")
    @Test
    void matchPasswordTrueTest() {
        Member member = new Member(new PlayerName("member"), new Email("member@wooteco.com"), new Password("wootecoCrew6!"), Role.BASIC);

        assertThat(member.matchPassword("wootecoCrew6!")).isTrue();
    }

    @DisplayName("비밀번호가 일치하지 않으면 false를 반환한다.")
    @Test
    void matchPasswordFalseTest() {
        Member member = new Member(new PlayerName("member"), new Email("member@wooteco.com"), new Password("wootecoCrew6!"), Role.BASIC);

        assertThat(member.matchPassword("wrongPassword")).isFalse();
    }
}
