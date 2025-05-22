package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.SignUpRequest;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.member.MemberSignupResponse;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.MemberRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;
    private SignUpRequest signUpRequest;

    @BeforeEach
    void setUp() {
        testMember = new Member(1L, "Test User", "test@example.com", Role.USER, "password");
        signUpRequest = new SignUpRequest("Test User", "test@example.com", "password");
    }

    @Test
    @DisplayName("ID로 회원을 찾을 수 있다")
    void findMemberById_WithExistingId_ReturnsMember() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // when
        Member foundMember = memberService.findMemberById(1L);

        // then
        assertThat(foundMember).isEqualTo(testMember);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 회원을 찾으면 예외가 발생한다")
    void findMemberById_WithNonExistingId_ThrowsUnauthorizedException() {
        // given
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.findMemberById(999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("유저를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("모든 회원을 조회할 수 있다")
    void findAllMembers_ReturnsAllMembers() {
        // given
        Member member1 = new Member(1L, "User 1", "user1@example.com", Role.USER, "password1");
        Member member2 = new Member(2L, "User 2", "user2@example.com", Role.ADMIN, "password2");
        List<Member> members = Arrays.asList(member1, member2);

        when(memberRepository.findAll()).thenReturn(members);

        // when
        List<MemberResponse> responses = memberService.findAllMembers();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).name()).isEqualTo("User 1");
        assertThat(responses.get(0).email()).isEqualTo("user1@example.com");
        assertThat(responses.get(0).role()).isEqualTo(Role.USER);

        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(1).name()).isEqualTo("User 2");
        assertThat(responses.get(1).email()).isEqualTo("user2@example.com");
        assertThat(responses.get(1).role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("새로운 회원을 등록할 수 있다")
    void registerMember_WithValidRequest_ReturnsMemberSignupResponse() {
        // given
        when(memberRepository.existsByEmail(signUpRequest.email())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // when
        MemberSignupResponse response = memberService.registerMember(signUpRequest);

        // then
        assertThat(response.id()).isEqualTo(testMember.getId());
        assertThat(response.name()).isEqualTo(testMember.getName());
        assertThat(response.email()).isEqualTo(testMember.getEmail());
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원을 등록하면 예외가 발생한다")
    void registerMember_WithDuplicateEmail_ThrowsDuplicateContentException() {
        // given
        when(memberRepository.existsByEmail(signUpRequest.email())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.registerMember(signUpRequest))
                .isInstanceOf(DuplicateContentException.class)
                .hasMessageContaining("이메일은 중복될 수 없습니다");
    }
}
