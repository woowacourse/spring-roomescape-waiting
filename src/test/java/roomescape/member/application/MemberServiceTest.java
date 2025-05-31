package roomescape.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_ADMIN_1;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_2;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_3;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.fixture.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.member.ui.dto.CreateMemberRequest;
import roomescape.member.ui.dto.MemberResponse.IdName;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 사용자를_생성한다() {
        // given
        final CreateMemberRequest request = new CreateMemberRequest("roo@kie.com", "비밀번호", "루키");

        // when
        memberService.create(request);

        // then
        assertThat(memberRepository.findByEmail("roo@kie.com")).isNotEmpty();
    }

    @Test
    void 사용자를_삭제한다() {
        // given
        final Member member = NOT_SAVED_MEMBER_1();
        memberRepository.save(member);

        // when
        memberService.delete(member.getId());

        // then
        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }

    @Test
    void 사용자들의_이름_목록을_조회한다() {
        // given
        final Member member1 = NOT_SAVED_MEMBER_1();
        final Member member2 = NOT_SAVED_MEMBER_2();
        final Member member3 = NOT_SAVED_MEMBER_3();
        final Member member4 = NOT_SAVED_ADMIN_1();
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        // when
        final List<IdName> allNames = memberService.findAllNames();

        // then
        assertAll(
                () -> assertThat(allNames.size()).isEqualTo(5),
                () -> assertThat(allNames).extracting(IdName::name)
                        .containsExactlyInAnyOrder("어드민이에용", member1.getName(), member2.getName(), member3.getName(),
                                member4.getName())
        );
    }
}
