package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.*;
import roomescape.infrastructure.MemberRepository;
import roomescape.service.request.MemberSignUpDto;
import roomescape.service.response.MemberDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.Fixture.*;

@ExtendWith(MockitoExtension.class)
class MemberAuthServiceTest {

    @InjectMocks
    private MemberAuthService memberAuthService;
    @Mock
    private MemberRepository memberRepository;

    @DisplayName("회원을 저장한다.")
    @Test
    void signUp() {
        MemberSignUpDto request = new MemberSignUpDto(VALID_USER_NAME.getName(),
                VALID_USER_EMAIL.getEmail(), VALID_USER_PASSWORD.getPassword());

        when(memberRepository.save(any(Member.class)))
                .thenReturn(
                        new Member(1L, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD, MemberRole.USER));

        MemberDto actual = memberAuthService.signUp(request);
        MemberDto expected = new MemberDto(1L, VALID_USER_NAME.getName(),
                MemberRole.USER.name());

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("같은 이메일로 중복 회원가입을 시도하면 예외가 발생한다.")
    @Test
    void signUp_duplicatedEmail() {
        MemberSignUpDto request = new MemberSignUpDto(VALID_USER_NAME.getName(),
                VALID_USER_EMAIL.getEmail(), VALID_USER_PASSWORD.getPassword());

        when(memberRepository.existsByEmail(VALID_USER_EMAIL))
                .thenReturn(true);

        assertThatThrownBy(() -> memberAuthService.signUp(request))
                .isInstanceOf(IllegalStateException.class);
    }

    @DisplayName("이메일을 통해 회원을 조회한다.")
    @Test
    void findMemberByEmail() {
        when(memberRepository.findByEmail(VALID_USER_EMAIL))
                .thenReturn(Optional.of(
                        new Member(1L, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD, MemberRole.USER)));

        MemberDto actual = memberAuthService.findMemberByEmail(VALID_USER_EMAIL.getEmail());
        MemberDto expected = new MemberDto(1L, VALID_USER_NAME.getName(),
                MemberRole.USER.name());

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("이메일의 회원이 없을 경우 예외가 발생한다.")
    @Test
    void findMemberByEmail_NoSuch() {
        when(memberRepository.findByEmail(VALID_USER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberAuthService.findMemberByEmail(VALID_USER_EMAIL.getEmail()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("전체 회원을 조회한다.")
    @Test
    void findAll() {
        Member member1 = new Member(1L, new MemberName("회원1"), new MemberEmail("email1@gmail.com"),
                new MemberPassword("123"), MemberRole.USER);
        Member member2 = new Member(2L, new MemberName("관리자"), new MemberEmail("email2@gmail.com"),
                new MemberPassword("123"), MemberRole.ADMIN);
        when(memberRepository.findAll())
                .thenReturn(List.of(member1, member2));

        List<MemberDto> actual = memberAuthService.findAll();
        List<MemberDto> expected = List.of(new MemberDto(1L, "회원1", "USER"),
                new MemberDto(2L, "관리자", "ADMIN"));

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
