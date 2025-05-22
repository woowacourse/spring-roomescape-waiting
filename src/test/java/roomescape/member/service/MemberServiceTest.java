package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.UnauthorizedException;
import roomescape.fixture.FakeMemberRepositoryFixture;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.repository.FakeTokenProvider;

@DisplayName("사용자 조회")
class MemberServiceTest {

    private final MemberRepository memberRepository = FakeMemberRepositoryFixture.create();
    private final MemberService memberService = new MemberService(memberRepository, new FakeTokenProvider());

    @DisplayName("모든 사용자 정보를 추출할 수 있다")
    @Test
    void findAllTest() {
        // when
        List<Member> members = memberService.findAllMembers();

        // then
        assertAll(
                () -> assertThat(members.size()).isEqualTo(2),
                () -> assertThat(members.getFirst().getName()).isEqualTo("어드민"),
                () -> assertThat(members.get(1).getName()).isEqualTo("회원")
        );
    }
}
