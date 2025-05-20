package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.dto.request.SignupRequest;
import roomescape.member.dto.response.MemberResponse;
import roomescape.member.dto.response.SignupResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class MemberServiceMockTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("이미 가입된 이메일에 대한 예외 테스트")
    void createUser_exception() {
        // given
        String email = "member@naver.com";
        SignupRequest signupRequest = new SignupRequest(email, "멤버1", "1234");
        when(memberRepository.existsByEmail(email))
                .thenReturn(true);
        // when & then
        assertThatThrownBy(() -> memberService.createUser(signupRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("정상 가입 테스트")
    void createUser_test() {
        // given
        String email = "member@naver.com";
        SignupRequest signupRequest = new SignupRequest(email, "멤버1", "1234");
        SignupResponse expected = new SignupResponse(1L, "멤버1", email, "1234");
        when(memberRepository.existsByEmail(email))
                .thenReturn(false);
        when(memberRepository.save(any(Member.class)))
                .thenReturn(TestFixture.createMember("멤버1", email, "1234"));
        // when
        SignupResponse response = memberService.createUser(signupRequest);
        // then
        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("모든 회원 조회 테스트")
    void findAllMember() {
        // given
        Member member1 = TestFixture.createMemberWithoutId("멤버1", "member@naver.com", "1234");
        Member member2 = TestFixture.createMemberWithoutId("멤버2", "other@naver.com", "1234");
        when(memberRepository.findAll())
                .thenReturn(List.of(member1, member2));
        // when
        List<MemberResponse> allMember = memberService.findAllMember();
        // then
        assertThat(allMember).hasSize(2);
    }
}
