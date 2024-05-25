package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import roomescape.exception.AuthorizationLoginFailException;
import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.MemberCreateRequest;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    private static final String USERNAME = "username";
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_PASSWORD = "password";
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("실행 성공 : 이메일로 회원 정보를 얻을 수 있다.")
    void findAllMembers() {
        // Given
        Member member = new Member(USERNAME, USER_EMAIL, USER_PASSWORD);
        MemberProfileInfo expected = MemberProfileInfo.from(member);
        when(memberRepository.findAllByRole(any(MemberRole.class)))
                .thenReturn(List.of(member));

        // When
        List<MemberProfileInfo> actual = memberService.findAllMembers();

        // Then
        assertThat(actual).containsExactly(expected);
    }

    @Test
    @DisplayName("실행 성공 : 이메일로 회원 정보를 얻을 수 있다.")
    void findMemberByEmail() {
        // Given
        MemberLoginRequest request = new MemberLoginRequest(USER_EMAIL, USER_PASSWORD);
        Member expected = new Member(USERNAME, USER_EMAIL, USER_PASSWORD);
        when(memberRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(expected));

        // When
        Member actual = memberService.findMemberByEmail(request);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("실행 실패 : 등록되지 않은 이메일로 조회 시 예외가 발생한다.")
    void findMemberByEmail_Exception() {
        // Given
        String email = "nonexistent@example.com";
        MemberLoginRequest request = new MemberLoginRequest(email, "1111");

        when(memberRepository.findByEmail(any(String.class)))
                .thenReturn(Optional.empty());

        // When && Than
        assertThatThrownBy(() -> memberService.findMemberByEmail(request))
                .isInstanceOf(AuthorizationLoginFailException.class);
    }

    @Test
    @DisplayName("실행 성공 : id로 회원 정보를 얻을 수 있다.")
    void findMemberById() {
        // Given
        Member expected = new Member(1L, USERNAME, USER_EMAIL, USER_PASSWORD);
        when(memberRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(expected));

        // When
        Member actual = memberService.findMemberById(1L);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("실행 실패 : 등록되지 않은 이메일로 조회 시 예외가 발생한다.")
    void findMemberById_Exception() {
        // Given
        when(memberRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());

        // When && Than
        assertThatThrownBy(() -> memberService.findMemberById(1L))
                .isInstanceOf(AuthorizationLoginFailException.class);
    }

    @Test
    @DisplayName("실행 성공 : 회원 정보를 생성할 수 있다.")
    void createMember() {
        // Given
        MemberCreateRequest request = new MemberCreateRequest(USERNAME, USER_EMAIL, USER_PASSWORD);
        Member member = new Member(1L, USERNAME, USER_EMAIL, USER_PASSWORD);
        MemberProfileInfo expected = MemberProfileInfo.from(member);

        when(memberRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class)))
                .thenReturn(member);

        // When
        MemberProfileInfo actual = memberService.createMember(request);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("실행 실패 : 존재하는 이메일로 생성하려고 하면 예외가 발생한다.")
    void createMember_Exception() {
        // Given
        MemberCreateRequest request = new MemberCreateRequest(USERNAME, USER_EMAIL, USER_PASSWORD);
        Member member = new Member(1L, USERNAME, USER_EMAIL, USER_PASSWORD);
        when(memberRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(member));

        // When && Than
        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(ConflictException.class);
    }
}
