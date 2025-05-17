package roomescape.member.application;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.fixture.config.TestConfig;
import roomescape.fixture.domain.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberCommandRepository;
import roomescape.member.domain.MemberQueryRepository;
import roomescape.member.ui.dto.CreateMemberRequest;
import roomescape.member.ui.dto.MemberResponse;

@DataJpaTest
@Import(TestConfig.class)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private MemberQueryRepository memberQueryRepository;

    @Test
    void 회원을_추가한다() {
        // given
        final CreateMemberRequest request = new CreateMemberRequest(
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
        final Member savedMember = memberCommandRepository.save(member);

        // when
        memberService.delete(savedMember.getId());

        // then
        final Member foundMember = memberQueryRepository.findById(savedMember.getId()).orElse(null);
        Assertions.assertThat(foundMember).isNull();
    }
}
