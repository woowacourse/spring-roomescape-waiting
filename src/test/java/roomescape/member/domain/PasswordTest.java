package roomescape.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.member.exception.MemberException;

import static roomescape.member.exception.MemberExceptionInformation.PASSWORD_NOT_MATCH;

class PasswordTest {


    @Nested
    @DisplayName("from 메서드는")
    class FromTest {


        @Test
        @DisplayName("객체를 생성한")
        void 성공1() {
            // given
            String rawPassword = "1234";

            // when
            Password password = Password.from(rawPassword);
            String encryptedPassword = password.getValue();

            // then
            Assertions.assertThat(rawPassword)
                .isNotEqualTo(encryptedPassword);
        }


        @Test
        @DisplayName("같은 암호로 만들어진 객체는 서로 동등하다")
        void 성공2() {
            // given
            String rawPassword = "1234";
            Password password = Password.from(rawPassword);
            Password samePassword = Password.from(rawPassword);

            // when & then
            Assertions.assertThat(password.getValue())
                .isEqualTo(samePassword.getValue());
        }
    }

    @Nested
    @DisplayName("validateMatches 메서드는")
    class ValidateMatchesTest {


        @Test
        @DisplayName("비밀번호가 다르면 예외가 발생한다")
        void 실패() {
            // given
            String rawPassword = "1234";
            String wrongPassword = "abcd";
            Password password = Password.from(rawPassword);

            // when & then
            Assertions.assertThatThrownBy(() -> password.validateMatches(wrongPassword))
                .isInstanceOf(MemberException.class)
                .hasMessage(PASSWORD_NOT_MATCH.getMessage());
        }
    }
}
