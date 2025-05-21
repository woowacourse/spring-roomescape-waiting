package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import roomescape.member.fixture.MemberFixture;

class MemberTest {

    @Test
    void PK_와_함께_생성된다() {

        // given
        Long primaryKey = 1L;
        Member memberWithoutPrimaryKey = new Member(
            "name",
            "email@email.com",
            MemberRole.USER,
            "password"
        );

        // when
        Member member = Member.generateWithPrimaryKey(memberWithoutPrimaryKey, primaryKey);

        // then
        assertThat(memberWithoutPrimaryKey.getId()).isNull();
        assertThat(member.getId()).isEqualTo(primaryKey);
    }

    @Test
    void 같은_이메일을_가지는지_확인한다() {

        // given
        Member member = MemberFixture.createMember(MemberRole.USER);

        // when
        boolean result = member.hasSameEmail(member.getEmail());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 같은_비밀번호를_가지는지_확인한다() {

        // given
        Member member = MemberFixture.createMember(MemberRole.USER);

        // when
        boolean result = member.hasSamePassword(member.getPassword());

        // then
        assertThat(result).isTrue();
    }
}
