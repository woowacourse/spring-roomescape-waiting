package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.UnableCreateMemberException;

class MemberTest {

    @Nested
    class FailureTest {

        @ParameterizedTest
        @ValueSource(strings = {"A", "ThisNameIsTooLong"})
        @DisplayName("이름이 유효하지 않으면 예외가 발생한다")
        void invalid_name_throws_exception(String name) {
            assertThatThrownBy(() -> new Member(1L, name, MemberRole.USER, "test@example.com", "Password1!"))
                    .isInstanceOf(UnableCreateMemberException.class)
                    .hasMessageContaining("회원 이름은 2글자에서 10글자까지만 가능합니다.");
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "test@.com", "test@domain"})
        @DisplayName("이메일이 유효하지 않으면 예외가 발생한다")
        void invalid_email_throws_exception(String email) {
            assertThatThrownBy(() -> new Member(1L, "ValidName", MemberRole.USER, email, "Password1!"))
                    .isInstanceOf(UnableCreateMemberException.class)
                    .hasMessageContaining("유효한 이메일 주소를 입력해주세요.");
        }

        @ParameterizedTest
        @ValueSource(strings = {"short", "nouppercase1", "NOLOWERCASE1", "NoNumber@", "NoSpecialChar1"})
        @DisplayName("비밀번호가 유효하지 않으면 예외가 발생한다")
        void invalid_password_throws_exception(String password) {
            assertThatThrownBy(() -> new Member(1L, "ValidName", MemberRole.USER, "test@example.com", password))
                    .isInstanceOf(UnableCreateMemberException.class)
                    .hasMessageContaining("비밀번호는 최소 8글자 이상, 하나 이상의 대문자와 숫자, 특수문자를 포함해야 합니다.");
        }
    }

    @Nested
    class SuccessTest {

        @Test
        @DisplayName("동일한 id를 가진 회원은 동등하다")
        void members_with_same_id_are_equal() {
            Member member1 = new Member(1L, "Alice", MemberRole.USER, "alice@example.com", "Password1!");
            Member member2 = new Member(1L, "Bob", MemberRole.ADMIN, "bob@example.com", "Password2!");

            assertThat(member1).isEqualTo(member2);
        }

        @Test
        @DisplayName("다른 id를 가진 회원은 동등하지 않다")
        void members_with_different_id_are_not_equal() {
            Member member1 = new Member(1L, "Alice", MemberRole.USER, "alice@example.com", "Password1!");
            Member member2 = new Member(2L, "Alice", MemberRole.USER, "alice@example.com", "Password1!");

            assertThat(member1).isNotEqualTo(member2);
        }

        @Test
        @DisplayName("id가 null인 회원은 동등하지 않다")
        void members_with_null_id_are_not_equal() {
            Member member1 = new Member(null, "Alice", MemberRole.USER, "alice@example.com", "Password1!");
            Member member2 = new Member(null, "Bob", MemberRole.ADMIN, "bob@example.com", "Password2!");

            assertThat(member1).isNotEqualTo(member2);
        }

        @Test
        @DisplayName("유효한 이름은 예외를 발생시키지 않는다")
        void valid_name_does_not_throw_exception() {
            assertThatCode(() -> new Member(1L, "ValidName", MemberRole.USER, "test@example.com", "Password1!"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("유효한 이메일은 예외를 발생시키지 않는다")
        void valid_email_does_not_throw_exception() {
            assertThatCode(() -> new Member(1L, "ValidName", MemberRole.USER, "test@example.com", "Password1!"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("유효한 비밀번호는 예외를 발생시키지 않는다")
        void valid_password_does_not_throw_exception() {
            assertThatCode(() -> new Member(1L, "ValidName", MemberRole.USER, "test@example.com", "Password1!"))
                    .doesNotThrowAnyException();
        }
    }
}
