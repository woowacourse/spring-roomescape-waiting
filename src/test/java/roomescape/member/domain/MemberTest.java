package roomescape.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.auth.domain.AuthRole;

class MemberTest {

    @Test
    void 이름이_null이면_예외가_발생한다() {
        // given
        final String name = null;
        final String email = "abc@abc.com";
        final String password = "1234";
        final AuthRole role = AuthRole.MEMBER;

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(name, email, password, role))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "      "})
    void 이름이_blank면_예외가_발생한다(final String name) {
        // given
        final String email = "abc@abc.com";
        final String password = "1234";
        final AuthRole role = AuthRole.MEMBER;

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(name, email, password, role))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이메일이_null이면_예외가_발생한다() {
        // given
        final String name = "abc";
        final String email = null;
        final String password = "1234";
        final AuthRole role = AuthRole.MEMBER;

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(name, email, password, role))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "      "})
    void 이메일이_blank면_예외가_발생한다(final String email) {
        // given
        final String name = "abc";
        final String password = "1234";
        final AuthRole role = AuthRole.MEMBER;

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(name, email, password, role))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "a@a", "a@a.", "a@.a", "a@a.a."})
    void 이메일_형식이_올바르지_않으면_예외가_발생한다(final String email) {
        // given
        final String name = "abc";
        final String password = "1234";
        final AuthRole role = AuthRole.MEMBER;

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(name, email, password, role))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호가_null이면_예외가_발생한다() {
        // given
        final String name = "abc";
        final String email = "abc@abc.com";
        final String password = null;
        final AuthRole role = AuthRole.MEMBER;

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(name, email, password, role))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "      "})
    void 비밀번호가_blank면_예외가_발생한다(final String password) {
        // given
        final String name = "abc";
        final String email = "abc@abc.com";
        final AuthRole role = AuthRole.MEMBER;

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(name, email, password, role))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 권한이_null이면_예외가_발생한다() {
        // given
        final String name = "abc";
        final String email = "abc@abc.com";
        final String password = "1234";
        final AuthRole role = null;

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(name, email, password, role))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
