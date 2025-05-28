package roomescape.business.model.entity;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.business.model.vo.UserRole;
import roomescape.exception.business.InvalidCreateArgumentException;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private static final String VALID_NAME = "dompoo";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "password123!";
    private static final String VALID_ID = "1";

    @Nested
    class 생성_테스트 {

        @Test
        void 유효한_정보로_사용자를_생성할_수_있다() {
            // when
            User user = User.member(VALID_NAME, VALID_EMAIL, VALID_PASSWORD);

            // then
            assertThat(user).isNotNull();
            assertThat(user.getName().value()).isEqualTo(VALID_NAME);
            assertThat(user.getEmail().value()).isEqualTo(VALID_EMAIL);
            assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
        }

        @Test
        void 이름이_10자_초과이면_예외가_발생한다() {
            // when, then
            final String tooLongName = "이름이너무길어서예외가발생합니다";

            assertThatThrownBy(() -> User.member(tooLongName, VALID_EMAIL, VALID_PASSWORD))
                    .isInstanceOf(InvalidCreateArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"홍길동1", "user123", "tester99"})
        void 이름에_숫자가_포함되면_예외가_발생한다(String nameWithNumber) {
            // when, then
            assertThatThrownBy(() -> User.member(nameWithNumber, VALID_EMAIL, VALID_PASSWORD))
                    .isInstanceOf(InvalidCreateArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"dompoo", "dompoo@", "dompoo@gmail", "dompoo.com"})
        void 이메일_형식이_아니면_예외가_발생한다(String invalidEmail) {
            assertThatThrownBy(() -> User.member(VALID_NAME, invalidEmail, VALID_PASSWORD))
                    .isInstanceOf(InvalidCreateArgumentException.class);
        }

        @Test
        void 저장_후_사용자_객체를_생성할_수_있다() {
            // when
            User user = User.member(VALID_NAME, VALID_EMAIL, VALID_PASSWORD);

            // then
            assertThat(user).isNotNull();
            assertThat(user.getName().value()).isEqualTo(VALID_NAME);
            assertThat(user.getEmail().value()).isEqualTo(VALID_EMAIL);
            assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
        }
    }

    @Nested
    class 비밀번호_검증_테스트 {

        @Test
        void 올바른_비밀번호로_검증에_성공한다() {
            // given
            User user = User.member(VALID_NAME, VALID_EMAIL, VALID_PASSWORD);

            // when
            boolean result = user.isPasswordCorrect(VALID_PASSWORD);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 잘못된_비밀번호로_검증에_실패한다() {
            // given
            User user = User.member(VALID_NAME, VALID_EMAIL, VALID_PASSWORD);

            // when
            boolean result = user.isPasswordCorrect("wrong_password");

            // then
            assertThat(result).isFalse();
        }
    }
}
