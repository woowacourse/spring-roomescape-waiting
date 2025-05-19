package roomescape.member.application;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.fixture.config.TestConfig;
import roomescape.fixture.domain.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.member.ui.dto.SignUpRequest;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 회원을_추가한다() {
        // given
        final SignUpRequest request = new SignUpRequest(
                MemberFixture.NOT_SAVED_MEMBER_1().getEmail(),
                MemberFixture.NOT_SAVED_MEMBER_1().getPassword(),
                MemberFixture.NOT_SAVED_MEMBER_1().getName()
        );

        // when
        final MemberResponse.IdName savedMember = memberService.create(request);

        // then
        assertAll(
                () -> assertNotNull(savedMember),
                () -> assertNotNull(savedMember.id()),
                () -> assertEquals(request.name(), savedMember.name())
        );
    }

    @Test
    void 회원을_삭제한다() {
        // given
        final Member member = MemberFixture.NOT_SAVED_MEMBER_1();
        final Member savedMember = memberRepository.save(member);

        // when
        memberService.delete(savedMember.getId());

        // then
        final Member foundMember = memberRepository.findById(savedMember.getId()).orElse(null);
        Assertions.assertThat(foundMember).isNull();
    }
}
