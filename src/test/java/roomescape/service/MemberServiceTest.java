package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.repository.MemberRepository;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnAuthorizedException;
import roomescape.service.param.LoginMemberParam;
import roomescape.service.param.RegisterMemberParam;
import roomescape.service.result.MemberResult;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 로그인_시_존재하지_않는_email로_로그인_시_예외() {
        //given
        LoginMemberParam loginMemberParam = new LoginMemberParam("email2", "password2");

        //when & then
        assertThatThrownBy(() -> memberService.login(loginMemberParam))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessage("이메일이나 비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 로그인_시_email에_대해_password가_일치하지_않으면_예외() {
        //given
        Member member = memberRepository.save(TestFixture.createDefaultMember());

        //when & then
        assertThatThrownBy(() -> memberService.login(new LoginMemberParam(member.getEmail(), "password2")))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessage("이메일이나 비밀번호가 일치하지 않습니다.");
    }

    @Test
    void email과_password로_로그인_가능() {
        //given
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        LoginMemberParam loginMemberParam = new LoginMemberParam(member.getEmail(), member.getPassword());

        //when & then
        assertThat(memberService.login(loginMemberParam)).isEqualTo(new MemberResult(member.getId(), member.getName(), MemberRole.USER, member.getEmail()));
    }

    @Test
    void 멤버를_생성할_수_있다() {
        MemberResult memberResult = memberService.create(new RegisterMemberParam("email1", "password1", "name1"));
        assertThat(memberResult)
                .isEqualTo(new MemberResult(memberResult.id(), "name1", MemberRole.USER, "email1"));
    }

    @Test
    void id를_통해_멤버를_찾을_수_있다() {
        //given
        Member member = memberRepository.save(TestFixture.createDefaultMember());

        //when & then
        assertThat(memberService.getById(member.getId())).isEqualTo(new MemberResult(1L, member.getName(), MemberRole.USER, member.getEmail()));
    }

    @Test
    void id를_통해_멤버를_찾으려_할_때_해당하는_id의_멤버가_존재하지_않으면_예외() {
        assertThatThrownBy(() -> memberService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }
}
