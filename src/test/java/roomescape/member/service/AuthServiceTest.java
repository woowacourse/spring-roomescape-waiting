package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.controller.request.TokenLoginCreateRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.controller.response.TokenLoginResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.JwtTokenProvider;
import roomescape.member.repository.MemberRepository;
import roomescape.member.role.Role;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("토큰 활용하여 로그인한다.")
    void tokenLoginTest() {

        //given
        TokenLoginCreateRequest tokenLoginCreateRequest = new TokenLoginCreateRequest("matt.kakao", "1234");

        when(memberRepository.existsByEmailAndPassword(any(Email.class), any(Password.class))).thenReturn(true);
        when(jwtTokenProvider.createToken(tokenLoginCreateRequest.email())).thenReturn("token");

        //when
        TokenLoginResponse tokenLoginResponse = authService.tokenLogin(tokenLoginCreateRequest);

        //then
        assertThat(tokenLoginResponse.tokenResponse()).isNotBlank();
    }

    @Test
    @DisplayName("등록되지 않은 회원은 로그인할 수 없다.")
    void tokenLoginFailTest() {
        //when - then
        assertThatThrownBy(() ->
                authService.tokenLogin(new TokenLoginCreateRequest("matt.kakao", "123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 아이디 또는 비밀번호를 올바르게 입력해주세요.");

        assertThatThrownBy(() ->
                authService.tokenLogin(new TokenLoginCreateRequest("matt.kaka", "1234")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 아이디 또는 비밀번호를 올바르게 입력해주세요.");

        assertThatThrownBy(() ->
                authService.tokenLogin(new TokenLoginCreateRequest("matt.kaka", "123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 아이디 또는 비밀번호를 올바르게 입력해주세요.");
    }

    @Test
    @DisplayName("토큰을 활용하여 회원을 찾는다.")
    void findMemberByTokenTest() {
        //given
        Member matt = new Member(1L, new Name("매트"), new Email("matt.kakao"), new Password("1234"), Role.MEMBER);
        String token = "TOKEN";
        when(jwtTokenProvider.getPayload(token)).thenReturn("matt.kakao");
        when(memberRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(matt));

        //when
        MemberResponse userByToken = authService.findUserByToken(token);

        //then
        assertThat(userByToken.name()).isEqualTo("매트");
    }
}
