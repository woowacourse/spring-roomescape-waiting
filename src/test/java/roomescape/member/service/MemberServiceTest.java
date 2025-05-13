package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Role;
import roomescape.member.dto.response.MemberResponse;
import roomescape.member.dto.request.SignupRequest;
import roomescape.member.dto.response.SignupResponse;

class MemberServiceTest {

    private MemberRepository memberRepository = new FakeMemberRepository(new ArrayList<>());
    private MemberService memberService = new MemberService(memberRepository);

    @Test
    @DisplayName("이미 가입된 이메일에 대한 예외 테스트")
    void createUser_exception() {
        // given
        Member member = Member.createWithoutId("a", "a@naver.com", "a", Role.USER);
        memberRepository.save(member);

        SignupRequest signupRequest = new SignupRequest("a@naver.com", "a", "a");
        // when & then
        assertThatThrownBy(() -> memberService.createUser(signupRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("정상 가입 테스트")
    void createUser_test() {
        // given
        SignupRequest signupRequest = new SignupRequest("a@naver.com", "a", "a");
        SignupResponse expected = new SignupResponse(1L, "a", "a@naver.com", "a");
        // when
        SignupResponse response = memberService.createUser(signupRequest);
        // then
        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("모든 회원 조회 테스트")
    void findAllMember() {
        // given
        Member member1 = Member.createWithoutId("a", "a@naver.com", "a", Role.USER);
        Member member2 = Member.createWithoutId("a", "b@naver.com", "a", Role.USER);
        memberRepository.save(member1);
        memberRepository.save(member2);
        // when
        List<MemberResponse> allMember = memberService.findAllMember();
        // then
        assertThat(allMember).hasSize(2);
    }
}
