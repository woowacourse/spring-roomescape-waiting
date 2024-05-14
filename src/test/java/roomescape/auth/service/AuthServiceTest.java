package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.getMemberChoco;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.auth.controller.dto.SignUpRequest;
import roomescape.auth.controller.dto.TokenResponse;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.service.jwt.FakeTokenProvider;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberSignUp;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.dao.FakeMemberDao;

@DisplayName("회원 로직 테스트")
class AuthServiceTest {

    MemberRepository memberRepository;
    TokenProvider tokenProvider;
    AuthService authService;

    @BeforeEach
    void setUp() {
        memberRepository = new FakeMemberDao();
        tokenProvider = new FakeTokenProvider();
        authService = new AuthService(memberRepository, tokenProvider);
    }

    @DisplayName("토큰 생성에 성공한다.")
    @Test
    void createToken() {
        //given
        String password = "1234";
        Member member = memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), password,
                        getMemberChoco().getRole()));
        LoginRequest loginRequest = new LoginRequest(member.getEmail(), password);

        //when
        TokenResponse token = authService.createToken(loginRequest);

        //then
        assertThat(tokenProvider.isToken(token.accessToken())).isTrue();
        assertThat(tokenProvider.getPayload(token.accessToken()).getValue()).isEqualTo(member.getEmail());
    }

    @DisplayName("토큰으로 사용자 정보 조회에 성공한다.")
    @Test
    void fetchByToken() {
        //given
        String password = "1234";
        Member member = memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), password,
                        getMemberChoco().getRole()));
        String accessToken = tokenProvider.createAccessToken(member.getEmail());

        //when
        AuthInfo authInfo = authService.fetchByToken(accessToken);

        //then
        assertThat(authInfo.getEmail()).isEqualTo(member.getEmail());
    }

    @DisplayName("유효하지 않는 토큰에 예외가 발생한다.")
    @Test
    void fetchByToken_InvalidToken() {
        //given
        String invalidToken = "12f3m$%g2gdgdgd";

        //when & then
        assertThatThrownBy(() -> authService.fetchByToken(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorType.SECURITY_EXCEPTION.getMessage());
    }

    @DisplayName("사용자 회원가입에 성공한다.")
    @Test
    void signUp() {
        //given
        String password = "1234";
        SignUpRequest signUpRequest =
                new SignUpRequest(getMemberChoco().getName(), getMemberChoco().getEmail(), password);

        //when
        authService.signUp(signUpRequest);

        //then
        Optional<Member> memberOptional = memberRepository.findBy(getMemberChoco().getEmail());

        assertAll(
                () -> assertThat(memberOptional).isNotNull(),
                () -> assertThat(memberOptional.get().getName()).isEqualTo(getMemberChoco().getName())
        );
    }

    @DisplayName("중복 이메일 사용자 생성 시, 예외가 발생한다.")
    @Test
    void createDuplicatedEmail() {
        //given
        String password = "1234";
        memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), password, Role.USER));
        SignUpRequest signUpRequest =
                new SignUpRequest(getMemberChoco().getName(), getMemberChoco().getEmail(), password);

        //when & then
        assertThatThrownBy(() -> authService.signUp(signUpRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorType.DUPLICATED_EMAIL_ERROR.getMessage());
    }

    @DisplayName("존재하지 않는 사용자 정보 조회에 실패한다.")
    @Test
    void unauthorizedMember() {
        //given
        String invalidEmail = "invalidEmail@invalid.com";
        String accessToken = tokenProvider.createAccessToken(invalidEmail);

        //when & then
        assertThatThrownBy(() -> authService.fetchByToken(accessToken))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorType.TOKEN_PAYLOAD_EXTRACTION_FAILURE.getMessage());
    }
}
