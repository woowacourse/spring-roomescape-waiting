package roomescape.service;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.exception.InvalidAuthorizationException;
import roomescape.fixture.FakeMemberRepositoryFixture;
import roomescape.member.service.MemberService;
import roomescape.repository.FakeTokenProvider;
import roomescape.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("사용자 조회")
class MemberServiceTest {

    private final MemberRepository memberRepository = FakeMemberRepositoryFixture.create();
    private final MemberService memberService = new MemberService(memberRepository, new FakeTokenProvider());

    @DisplayName("토큰 정보로 사용자를 추출할 수 있다")
    @Test
    void findMemberByTokenTest() {
        // given
        String token = "admin@gmail.com";

        // when
        Member member = memberService.findMemberByToken(token);

        // then
        assertAll(
                () -> assertThat(member.getId()).isEqualTo(1L),
                () -> assertThat(member.getName()).isEqualTo("어드민"),
                () -> assertThat(member.getEmail()).isEqualTo("admin@gmail.com")
        );
    }

    @DisplayName("사용자 추출 시 토큰 정보가 잘못되면 예외가 발생한다")
    @Test
    void findMemberExceptionTest() {
        // given
        String token = "invalid";

        // when & then
        assertThatThrownBy(() -> memberService.findMemberByToken(token)).isInstanceOf(InvalidAuthorizationException.class);
    }

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
