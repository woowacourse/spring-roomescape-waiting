package roomescape.member.domain;

import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    void 회원이_정상적으로_생성된다() {
        // Given
        final String name = "강산";
        final String email = "123@gmail.com";
        final Role role = Role.MEMBER;

        // When & Then
        assertThatNoException().isThrownBy(() -> Member.withoutId(
                MemberName.from(name),
                MemberEmail.from(email),
                role
        ));
    }

    @Test
    void 역할이_null이라면_예외가_발생한다() {
        // Given
        final String name = "강산";
        final String email = "123@gmail.com";

        // When & Then
        assertThatThrownBy(() -> Member.withoutId(
                MemberName.from(name),
                MemberEmail.from(email),
                null
        )).isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 관리자인_경우_true를_반환한다() {
        // Given
        final String name = "강산";
        final String email = "123@gmail.com";
        final Role role = Role.ADMIN;
        final Member member = Member.withoutId(
                MemberName.from(name),
                MemberEmail.from(email),
                role
        );

        // When & Then
        assertThat(member.isAdmin()).isTrue();
    }

    @Test
    void 관리자가_아닌_일반_회원인_경우_false를_반환한다() {
        // Given
        final String name = "강산";
        final String email = "123@gmail.com";
        final Role role = Role.MEMBER;
        final Member member = Member.withoutId(
                MemberName.from(name),
                MemberEmail.from(email),
                role
        );

        // When & Then
        assertThat(member.isAdmin()).isFalse();
    }
}
