package roomescape.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Member;
import roomescape.domain.enums.Role;
import roomescape.dto.login.LoginRequest;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.signup.SignupRequest;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.repository.member.MemberRepository;
import roomescape.util.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("모든 회원을 조회할 수 있다")
    void findAllMembers() {
        // given
        List<Member> mockMembers = List.of(
                new Member(1L, "슬링키", "email1", "pw1", Role.USER),
                new Member(2L, "에드", "email2", "pw2", Role.ADMIN)
        );
        when(memberRepository.findAllMembers()).thenReturn(mockMembers);

        // when
        List<MemberResponse> responses = memberService.findAllMembers();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("슬링키");
    }

    @Test
    @DisplayName("ID로 회원을 조회할 수 있다")
    void findMemberById_success() {
        // given
        Member member = new Member(1L, "슬링키", "email", "pw", Role.USER);
        when(memberRepository.findMemberById(1L)).thenReturn(Optional.of(member));

        // when
        Member found = memberService.findMemberById(1L);

        // then
        assertThat(found.getName()).isEqualTo("슬링키");
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID를 조회하면 예외 발생")
    void findMemberById_fail() {
        // given
        when(memberRepository.findMemberById(99L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> memberService.findMemberById(99L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("회원가입 요청으로 새 회원을 추가할 수 있다")
    void addMember_success() {
        // given
        SignupRequest request = new SignupRequest("email", "pw","슬링키");

        when(memberRepository.addMember(any())).thenReturn(10L);

        // when
        MemberResponse response = memberService.addMember(request);

        // then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("슬링키");
    }

    @Test
    @DisplayName("로그인 요청으로 JWT 토큰을 생성할 수 있다")
    void createToken_success() {
        // given
        LoginRequest loginRequest = new LoginRequest("password", "email");
        Member member = new Member(1L, "슬링키", "email", "password", Role.ADMIN);

        when(memberRepository.findMemberByEmailAndPassword("email", "password")).thenReturn(Optional.of(member));

        String fakeToken = "jwt.token.here";
        when(jwtTokenProvider.createToken(member)).thenReturn(fakeToken);

        // when
        String token = memberService.createToken(loginRequest);

        // then
        assertThat(token).isEqualTo("jwt.token.here");
    }
}
