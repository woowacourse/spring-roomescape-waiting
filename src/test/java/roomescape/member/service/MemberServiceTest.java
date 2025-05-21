package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import roomescape.auth.application.TokenProvider;
import roomescape.auth.application.dto.LoginRequest;
import roomescape.common.exception.AuthenticationException;
import roomescape.common.exception.AuthorizationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberTokenResponse;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private MemberRepository memberRepository;
    private TokenProvider tokenProvider;
    private MemberService memberService;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        tokenProvider = mock(TokenProvider.class);
        passwordEncoder = new BCryptPasswordEncoder();
        memberService = new MemberService(tokenProvider, memberRepository);
    }

    @DisplayName("회원 목록을 조회하는 기능을 구현한다")
    @Test
    void findAll() {
        String hashedPassword = passwordEncoder.encode("password123");
        Member member = new Member(1L, "admin", "admin@email.com", hashedPassword, Role.ADMIN);
        when(memberRepository.findAll()).thenReturn(List.of(member));

        List<MemberResponse> members = memberService.findAll();

        assertThat(members).hasSize(1);
        verify(memberRepository, times(1)).findAll();
    }

    @DisplayName("회원을 이메일로 조회하는 기능을 구현한다")
    @Test
    void findByEmail() {
        String hashedPassword = passwordEncoder.encode("password123");
        Member member = new Member(1L, "admin", "admin@email.com", hashedPassword, Role.ADMIN);
        when(memberRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(member));

        MemberResponse foundMember = memberService.findByEmail("admin@email.com");

        assertThat(foundMember.email()).isEqualTo("admin@email.com");
        verify(memberRepository, times(1)).findByEmail("admin@email.com");
    }

    @DisplayName("회원을 토큰으로 조회하는 기능을 구현한다")
    @Test
    void findByToken() {
        String hashedPassword = passwordEncoder.encode("password123");
        Member member = new Member(1L, "admin", "admin@email.com", hashedPassword, Role.ADMIN);
        when(tokenProvider.getPayloadEmail("valid-token")).thenReturn("admin@email.com");
        when(memberRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(member));

        MemberResponse foundMember = memberService.findByToken("valid-token");

        assertThat(foundMember.id()).isEqualTo(1L);
        verify(tokenProvider, times(1)).getPayloadEmail("valid-token");
        verify(memberRepository, times(1)).findByEmail("admin@email.com");
    }

    @DisplayName("로그인 토큰을 생성하는 기능을 구현한다")
    @Test
    void createToken() {
        String hashedPassword = passwordEncoder.encode("password123");
        Member member = new Member(1L, "admin", "admin@email.com", hashedPassword, Role.ADMIN);
        when(memberRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(member));
        when(tokenProvider.createToken("admin@email.com", "admin")).thenReturn("access-token");

        LoginRequest loginRequest = new LoginRequest("admin@email.com", "password123");
        MemberTokenResponse tokenResponse = memberService.createToken(loginRequest);

        assertThat(tokenResponse.accessToken()).isEqualTo("access-token");
        verify(memberRepository, times(2)).findByEmail("admin@email.com");
        verify(tokenProvider, times(1)).createToken("admin@email.com", "admin");
    }

    @DisplayName("로그인 시 비밀번호가 일치하지 않는 경우 예외를 발생시킨다")
    @Test
    void exception_invalid_password() {
        String hashedPassword = passwordEncoder.encode("1234A");
        Member member = new Member(1L, "admin", "wooteco@gmail.com", hashedPassword, Role.ADMIN);
        when(memberRepository.findByEmail("wooteco@gmail.com")).thenReturn(Optional.of(member));

        LoginRequest loginRequest = new LoginRequest("wooteco@gmail.com", "1234");
        assertThatThrownBy(() -> memberService.createToken(loginRequest))
                .isInstanceOf(AuthenticationException.class);

        verify(memberRepository, times(1)).findByEmail("wooteco@gmail.com");
    }

    @DisplayName("회원가입 시 이미 존재하는 이메일인 경우 예외를 발생시킨다")
    @Test
    void exception_duplicate_email() {
        when(memberRepository.existsByEmail("admin@email.com")).thenReturn(true);

        MemberRequest signupRequest = new MemberRequest("admin@email.com", "password123", "admin");
        assertThatThrownBy(() -> memberService.add(signupRequest))
                .isInstanceOf(AuthorizationException.class);

        verify(memberRepository, times(1)).existsByEmail("admin@email.com");
    }
}
