package roomescape.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.common.exception.MemberException;

public class MemberTest {

    @Test
    void 이름이_null이면_예외가_발생한다() {
        // given
        final String name = null;

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Member(1L, name, "wooga@email.com", "1234", Role.USER);
        }).isInstanceOf(MemberException.class);
    }

    @Test
    void 이름이_빈_문자열이면_예외가_발생한다() {
        // given
        final String name = "";

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Member(1L, name, "wooga@email.com", "1234", Role.USER);
        }).isInstanceOf(MemberException.class);
    }

    @Test
    void 이메일이_null이면_예외가_발생한다() {
        // given
        final String email = null;

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Member(1L, "wooga", email, "1234", Role.USER);
        }).isInstanceOf(MemberException.class);
    }

    @Test
    void 이메일이_빈_문자열이면_예외가_발생한다() {
        // given
        final String email = "";

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Member(1L, "wooga", email, "1234", Role.USER);
        }).isInstanceOf(MemberException.class);
    }

    @Test
    void 비밀번호가_null이면_예외가_발생한다() {
        // given
        final String password = null;

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Member(1L, "wooga", "wooga@email.com", password, Role.USER);
        }).isInstanceOf(MemberException.class);
    }

    @Test
    void 비밀번호가_빈_문자열이면_예외가_발생한다() {
        // given
        final String password = "";

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Member(1L, "wooga", "wooga@email.com", password, Role.USER);
        }).isInstanceOf(MemberException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "woogaemail.com",
            "wooga+@.com",
            "@wooga.com",
            "wooga@wooga",
            "wooga@wooga.",
            "wooga@wooga.c"
    }, delimiter = ',')
    void 이메일_형식이_올바르지_않으면_예외가_발생한다(final String email) {
        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Member(1L, "wooga", email, "1234", Role.USER);
        }).isInstanceOf(MemberException.class);
    }
}
