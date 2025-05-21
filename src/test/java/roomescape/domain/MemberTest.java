package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberTest {

    @Nested
    @DisplayName("회원을 생성할 때 검증을 수행한다.")
    public class validate {

        @DisplayName("비어있는 권한으로는 멤버를 생성할 수 없다")
        @Test
        void cannotCreateBecauseNullRole() {
            // when & then
            assertThatThrownBy(() -> new Member(1L, null, "이름", "test@test.com", "asdfe123!"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 권한으로 멤버를 생성할 수 없습니다.");
        }

        @DisplayName("비어있는 이름으로 멤버를 생성할 수 없다")
        @ParameterizedTest
        @NullAndEmptySource
        void cannotCreateBecauseNullName(String name) {
            // when & then
            assertThatThrownBy(() -> new Member(1L, Role.GENERAL, name, "test@test.com", "qwer1234!"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 이름로 멤버를 생성할 수 없습니다.");
        }

        @DisplayName("최대길이를 초과한 이름으로 멤버를 생성할 수 없다")
        @Test
        void cannotCreateBecauseTooLongName() {
            // given
            String tooLongName = "1".repeat(256);

            // when & then
            assertThatThrownBy(() -> new Member(1L, Role.GENERAL, tooLongName, "test@test.com", "qwer1234!"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("최대길이를 초과한 이름으로는 멤버를 생성할 수 없습니다.");
        }

        @DisplayName("비어있는 이메일로 멤버를 생성할 수 없다")
        @ParameterizedTest
        @NullAndEmptySource
        void cannotCreateBecauseNullEmail(String email) {
            // when & then
            assertThatThrownBy(() -> new Member(1L, Role.GENERAL, "이름", email, "qwer1234!"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 이메일로 멤버를 생성할 수 없습니다.");
        }

        @DisplayName("최대길이를 초과한 이메일로 멤버를 생성할 수 없다")
        @Test
        void cannotCreateBecauseTooLongEmail() {
            // given
            String tooLongEmail = "1".repeat(256);

            // when & then
            assertThatThrownBy(() -> new Member(1L, Role.GENERAL, "이름", tooLongEmail, "비밀번호"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("최대길이를 초과한 이메일로는 멤버를 생성할 수 없습니다.");
        }

        @DisplayName("올바르지 않은 형식의 이메일로 멤버를 생성할 수 없다.")
        @Test
        void cannotCreateBecauseInvalidEmail() {
            // given
            String invalidEmail = "invalidEmail";

            // when & then
            assertThatThrownBy(() -> new Member(1L, Role.GENERAL, "이름", invalidEmail, "비밀번호"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("올바르지 않은 형식의 이메일로는 멤버를 생성할 수 없습니다.");
        }

        @DisplayName("비어있는 비밀번호로 멤버를 생성할 수 없다")
        @ParameterizedTest
        @NullAndEmptySource
        void cannotCreateBecauseNullPassword(String password) {
            // when & then
            assertThatThrownBy(() -> new Member(1L, Role.GENERAL, "이름", "test@test.com", password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 비밀번호로 멤버를 생성할 수 없습니다.");
        }

        @DisplayName("최대길이를 초과한 비밀번호로는 회원을 생성할 수 없다.")
        @Test
        void cannotCreateBecauseTooLongPassword() {
            // given
            String tooLongPassword = "i".repeat(51);
            // when & then
            assertThatThrownBy(() -> new Member(1L, Role.GENERAL, "이름", "test@test.com", tooLongPassword))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("최대길이를 초과한 비밀번호로는 멤버를 생성할 수 없습니다.");
        }

        @DisplayName("유효하지 않은 형식의 비밀번호로는 회원을 생성할 수 없다")
        @ParameterizedTest
        @ValueSource(strings = {"asdfasdf1", "asdfasdf!", "12341234!", "a213!"})
        void cannotCreateBecauseInvalidPassword(String invalidPassword) {
            // when & then
            assertThatThrownBy(() -> new Member(1L, Role.GENERAL, "이름", "test@test.com", invalidPassword))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("올바르지 않은 형식의 비밀번호로는 멤버를 생성할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("비밀번호가 동일한지 여부를 확인할 수 있다.")
    public class isEqualPassword {

        @DisplayName("비밀번호가 동일한 경우 true 반환")
        @Test
        void isEqualPassword() {
            // given
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qwer1234!");

            // when
            boolean isEqual = member.isEqualPassword("qwer1234!");

            // then
            assertThat(isEqual).isTrue();
        }

        @DisplayName("비밀번호가 동일한 경우 false 반환")
        @Test
        void isNotEqualPassword() {
            // given
            Member member = new Member(1L, Role.ADMIN, "회원", "test@test.com", "qwer1234!");

            // when
            boolean isNotEqual = member.isEqualPassword("asdf5678!");

            // then
            assertThat(isNotEqual).isFalse();
        }
    }

    @Nested
    @DisplayName("일반 회원인지 여부를 확인할 수 있다.")
    public class isMember {

        @DisplayName("일반 회원인 경우 true 반환")
        @Test
        void isMember() {
            // given
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qwer1234!");

            // when
            boolean isMember = member.isMember();

            // then
            assertThat(isMember).isTrue();
        }

        @DisplayName("어드민인 경우 true 반환")
        @Test
        void isAdmin() {
            // given
            Member member = new Member(1L, Role.ADMIN, "회원", "test@test.com", "qwer1234!");

            // when
            boolean isMember = member.isMember();

            // then
            assertThat(isMember).isFalse();
        }
    }
}
