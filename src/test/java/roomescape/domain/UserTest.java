package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserTest {

    @Nested
    @DisplayName("비밀번호가 동일한지 여부를 확인할 수 있다.")
    public class isEqualPassword {

        @DisplayName("비밀번호가 동일한 경우 true 반환")
        @Test
        void isEqualPassword() {
            // given
            User user = new User(1L, Role.ROLE_MEMBER, "회원", "test@test.com", "qwer1234!");

            // when
            boolean isEqual = user.isEqualPassword("qwer1234!");

            // then
            assertThat(isEqual).isTrue();
        }

        @DisplayName("비밀번호가 동일한 경우 false 반환")
        @Test
        void isNotEqualPassword() {
            // given
            User user = new User(1L, Role.ROLE_ADMIN, "회원", "test@test.com", "qwer1234!");

            // when
            boolean isNotEqual = user.isEqualPassword("asdf5678!");

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
            User user = new User(1L, Role.ROLE_MEMBER, "회원", "test@test.com", "qwer1234!");

            // when
            boolean isMember = user.isMember();

            // then
            assertThat(isMember).isTrue();
        }

        @DisplayName("어드민인 경우 true 반환")
        @Test
        void isAdmin() {
            // given
            User user = new User(1L, Role.ROLE_ADMIN, "회원", "test@test.com", "qwer1234!");

            // when
            boolean isMember = user.isMember();

            // then
            assertThat(isMember).isFalse();
        }
    }
}
