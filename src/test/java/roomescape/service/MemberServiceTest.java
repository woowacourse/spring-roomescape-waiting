package roomescape.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.exception.member.DuplicatedEmailException;
import roomescape.service.dto.request.member.SignupRequest;
import roomescape.service.dto.response.member.MemberResponse;

@Transactional
@SpringBootTest
class MemberServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("모든 사용자들을 반환한다")
    void findAllMember_ShouldReturnAllMembers() {
        // given
        Member member1 = new Member("name", "email", "password");
        Member member2 = new Member("name", "email", "password");
        Member member3 = new Member("name", "email", "password");

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        // when
        List<MemberResponse> responses = memberService.findAllMember();

        // then
        Assertions.assertThat(responses).hasSize(3);
    }

    @Test
    @DisplayName("회원가입을 요청을 할 수 있다")
    void signup_ShouldRegistrationNewMember() {
        // given
        SignupRequest request = new SignupRequest("name", "email@email.com", "password");

        // when
        memberService.signup(request);

        // then
        Assertions.assertThat(memberService.findAllMember())
                .hasSize(1);
    }

    @Test
    @DisplayName("중복된 이메일은 회원가입에 실패한다")
    void signup_ShouldThrowException_WhenDuplicatedEmail() {
        // given
        memberRepository.save(new Member("name", "email@email.com", "password"));
        SignupRequest request = new SignupRequest("name2", "email@email.com", "password");

        // when & then
        Assertions.assertThatThrownBy(() -> memberService.signup(request))
                .isInstanceOf(DuplicatedEmailException.class);
    }

    @Test
    @DisplayName("회원정보를 삭제할 수 있다")
    void deleteMember_ShouldRemovePersistence() {
        // given
        Member member = new Member("name", "email", "password");
        Member savedMember = memberRepository.save(member);

        // when
        memberService.deleteMember(savedMember.getId());

        // then
        Assertions.assertThat(memberRepository.findAll()).isEmpty();
    }
}
