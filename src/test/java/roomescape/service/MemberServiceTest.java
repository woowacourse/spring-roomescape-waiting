package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.controller.dto.TokenResponse;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.member.dto.MemberLoginRequest;
import roomescape.controller.member.dto.SignupRequest;
import roomescape.domain.Member;
import roomescape.domain.exception.InvalidRequestException;
import roomescape.service.exception.DuplicateEmailException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
class MemberServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("멤버를 저장한다.")
    void save() {
        final SignupRequest request = new SignupRequest("aaa@mail.com", "qwer1234", "레디");
        final Member saved = memberService.save(request);

        assertAll(
                () -> assertThat(saved.getName()).isEqualTo("레디"),
                () -> assertThat(saved.getEmail()).isEqualTo("aaa@mail.com")
        );
    }

    @Test
    @DisplayName("멤버를 조회한다.")
    void findAll() {
        final List<Member> members = memberService.findAll();
        final List<Member> expected = List.of(
                new Member(1L, null, null, null, null),
                new Member(2L, null, null, null, null),
                new Member(3L, null, null, null, null)
        );

        assertThat(members).isEqualTo(expected);
    }

    @Test
    @DisplayName("이미 존재하는 email로 회원가입을 시도할 경우 예외가 발생")
    void duplicateEmail() {
        final SignupRequest request = new SignupRequest("gkatjraud1@redddybabo.com", "1234", "뉴멤버");

        assertThatThrownBy(() -> memberService.save(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인시 예외 발생")
    void invalidPassword() {
        final MemberLoginRequest request = new MemberLoginRequest("gkatjraud1@redddybabo.com", "invalid");

        assertThatThrownBy(() -> memberService.createToken(request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("토큰을 생성하고 토큰으로 회원정보를 파악한다.")
    void createToken() {
        //given
        final MemberLoginRequest request = new MemberLoginRequest("gkatjraud1@redddybabo.com", "1234");
        final TokenResponse token = memberService.createToken(request);

        //when
        final LoginMember loginMember = memberService.findMemberByToken(token.assessToken());

        //then
        assertThat(loginMember.name()).isEqualTo("재즈");
    }
}
