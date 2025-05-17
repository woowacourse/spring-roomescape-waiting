package roomescape.domain;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MemberTest {

    @Test
    void 비밀번호가_일치하지_않으면_true를_반환한다() {
        Member member = Member.create("듀이", Role.USER, "test@test.com", "1234");
        String password = "pass";

        assertThat(member.isIncorrectPassword(password)).isTrue();
    }

    @Test
    void 비밀번호가_일치하면_false를_반환한다() {
        Member member = Member.create("듀이", Role.USER, "test@test.com", "1234");
        String password = "1234";

        assertThat(member.isIncorrectPassword(password)).isFalse();
    }
}
