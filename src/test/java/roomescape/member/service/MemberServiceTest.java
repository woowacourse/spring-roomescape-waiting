package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.getMemberClover;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.controller.dto.SignUpRequest;
import roomescape.member.controller.dto.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberSignUp;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.dao.FakeMemberDao;

class MemberServiceTest {
    MemberRepository memberRepository;
    MemberService memberService;

    @BeforeEach
    void setUp() {
        memberRepository = new FakeMemberDao();
        memberService = new MemberService(memberRepository);
    }

    @DisplayName("사용자 생성에 성공한다.")
    @Test
    void create() {
        //given
        String password = "1234";
        Member memberClover = getMemberClover();
        SignUpRequest signUpRequest = new SignUpRequest(memberClover.getName(), memberClover.getEmail(), password);

        //when
        MemberResponse memberResponse = memberService.create(signUpRequest);

        //then
        assertThat(memberResponse.name()).isEqualTo(memberClover.getName());
    }

    @DisplayName("식별자로 사용자 조회에 성공한다.")
    @Test
    void findById() {
        //given
        String password = "1234";
        Member memberClover = getMemberClover();
        MemberSignUp memberSignUp = new MemberSignUp(
                memberClover.getName(), memberClover.getEmail(), password, Role.USER);
        Member member = memberRepository.save(memberSignUp);

        //when
        Member foundMember = memberService.findById(member.getId());

        //then
        assertThat(member).isEqualTo(foundMember);
    }
}
