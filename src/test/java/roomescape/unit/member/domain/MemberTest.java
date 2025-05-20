package roomescape.unit.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.exception.LoginFailedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

class MemberTest {

    @Test
    void 비밀번호가_일치하지_않으면_예외가_발생한다() {
        // given
        Member member = new Member(null, "name1", "email@domain.com", "password1", Role.MEMBER);
        // when
        Assertions.assertThatThrownBy(() -> member.validatePassword("password2"))
                .isInstanceOf(LoginFailedException.class);
    }
}