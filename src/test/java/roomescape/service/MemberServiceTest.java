package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.exception.NotFoundMemberException;
import roomescape.exception.UnauthorizedException;
import roomescape.persistence.MemberRepository;
import roomescape.service.param.LoginMemberParam;
import roomescape.service.param.RegisterMemberParam;
import roomescape.service.result.MemberResult;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;
    @InjectMocks
    MemberService memberService;

    @Test
    void 로그인_시_존재하지_않는_email로_로그인_시_예외() {
        // given
        LoginMemberParam loginMemberParam = new LoginMemberParam("email2@email.com", "Password1!");
        when(memberRepository.findByEmail("email2@email.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.login(loginMemberParam))
                .isInstanceOf(NotFoundMemberException.class)
                .hasMessageContaining(loginMemberParam.email() + "에 해당하는 유저가 없습니다.");
    }

    @Test
    void 로그인_시_email에_대해_password가_일치하지_않으면_예외() {
        // given
        Member member = new Member(1L, "name1", MemberRole.USER, "email1@email.com", "Password1!");
        when(memberRepository.findByEmail("email1@email.com")).thenReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> memberService.login(new LoginMemberParam("email1@email.com", "Password2!")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("비밀 번호가 일치하지 않습니다.");
    }

    @Test
    void email과_password로_로그인_가능() {
        // given
        Member member = new Member(1L, "name1", MemberRole.USER, "email1@email.com", "Password1!");
        when(memberRepository.findByEmail("email1@email.com")).thenReturn(Optional.of(member));
        LoginMemberParam loginMemberParam = new LoginMemberParam("email1@email.com", "Password1!");

        // when & then
        assertThat(memberService.login(loginMemberParam)).isEqualTo(
                new MemberResult(1L, "name1", MemberRole.USER, "email1@email.com"));
    }

    @Test
    void 멤버를_생성할_수_있다() {
        // given
        RegisterMemberParam param = new RegisterMemberParam("name1", "email1@email.com", "Password1!");
        Member saved = new Member(1L, "name1", MemberRole.USER, "email1@email.com", "Password1!");
        when(memberRepository.save(any(Member.class))).thenReturn(saved);

        // when & then
        assertThat(memberService.create(param))
                .isEqualTo(new MemberResult(1L, "name1", MemberRole.USER, "email1@email.com"));
    }

    @Test
    void id를_통해_멤버를_찾을_수_있다() {
        // given
        Member member = new Member(1L, "name1", MemberRole.USER, "email1@email.com", "Password1!");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when & then
        assertThat(memberService.findById(1L)).isEqualTo(
                new MemberResult(1L, "name1", MemberRole.USER, "email1@email.com"));
    }

    @Test
    void id를_통해_멤버를_찾으려_할_때_해당하는_id의_멤버가_존재하지_않으면_예외() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.findById(1L))
                .isInstanceOf(NotFoundMemberException.class)
                .hasMessageContaining(1L + "에 해당하는 유저가 없습니다.");
    }
}
