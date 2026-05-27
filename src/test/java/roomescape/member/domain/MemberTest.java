package roomescape.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.member.exception.MemberException;

import static roomescape.member.exception.MemberExceptionInformation.PASSWORD_NOT_MATCH;

class MemberTest {


    @Nested
    @DisplayName("register 메서드는")
    class RegisterTest {


        @Test
        @DisplayName("객체를 생성한")
        void 성공() {
            // given
            String name = "송송";
            String rawPassword = "1234";

            // when
            Member songsong = Member.register(name, rawPassword);

            // then
            Assertions.assertThat(songsong.getId()).isNull();
            Assertions.assertThat(songsong.getName()).isEqualTo(name);
            Assertions.assertThat(songsong.getRole()).isEqualTo(Role.MEMBER);
        }
    }

    @Nested
    @DisplayName("matchPassword 메서드는")
    class MatchPasswordTest {


        @Test
        @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다")
        void 실패() {
            // given
            Member songsong = Member.register("송송", "1234");
            String wrongPassword = "wrongPassword";

            // when & then
            Assertions.assertThatThrownBy(() -> songsong.matchPassword(wrongPassword))
                .isInstanceOf(MemberException.class)
                .hasMessage(PASSWORD_NOT_MATCH.getMessage());
        }
    }
}
