package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.Fixture.VALID_USER_EMAIL;
import static roomescape.Fixture.VALID_USER_NAME;
import static roomescape.Fixture.VALID_USER_PASSWORD;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Member;
import roomescape.domain.MemberEmail;
import roomescape.domain.MemberName;
import roomescape.domain.MemberPassword;
import roomescape.domain.MemberRepository;
import roomescape.domain.MemberRole;
import roomescape.service.request.MemberSignUpAppRequest;
import roomescape.service.response.MemberAppResponse;

@ExtendWith(MockitoExtension.class)
class MemberAuthServiceTest {

    @InjectMocks
    private MemberAuthService memberAuthService;
    @Mock
    private MemberRepository memberRepository;

    @DisplayName("회원을 저장한다.")
    @Test
    void signUp() {
        MemberSignUpAppRequest request = new MemberSignUpAppRequest(VALID_USER_NAME.getName(),
            VALID_USER_EMAIL.getEmail(), VALID_USER_PASSWORD.getPassword());

        when(memberRepository.save(any(Member.class)))
            .thenReturn(
                new Member(1L, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD, MemberRole.USER));

        MemberAppResponse actual = memberAuthService.signUp(request);
        MemberAppResponse expected = new MemberAppResponse(1L, VALID_USER_NAME.getName(),
            MemberRole.USER.name());

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("같은 이메일로 중복 회원가입을 시도하면 예외가 발생한다.")
    @Test
    void signUp_duplicatedEmail() {
        MemberSignUpAppRequest request = new MemberSignUpAppRequest(VALID_USER_NAME.getName(),
            VALID_USER_EMAIL.getEmail(), VALID_USER_PASSWORD.getPassword());

        when(memberRepository.isExistsByEmail(VALID_USER_EMAIL.getEmail()))
            .thenReturn(true);

        assertThatThrownBy(() -> memberAuthService.signUp(request))
            .isInstanceOf(IllegalStateException.class);
    }

    @DisplayName("이메일을 통해 회원을 조회한다.")
    @Test
    void findMemberByEmail() {
        when(memberRepository.findByEmail(VALID_USER_EMAIL.getEmail()))
            .thenReturn(Optional.of(
                new Member(1L, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD, MemberRole.USER)));

        MemberAppResponse actual = memberAuthService.findMemberByEmail(VALID_USER_EMAIL.getEmail());
        MemberAppResponse expected = new MemberAppResponse(1L, VALID_USER_NAME.getName(),
            MemberRole.USER.name());

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("이메일의 회원이 없을 경우 예외가 발생한다.")
    @Test
    void findMemberByEmail_NoSuch() {
        when(memberRepository.findByEmail(VALID_USER_EMAIL.getEmail()))
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

        List<MemberAppResponse> actual = memberAuthService.findAll();
        List<MemberAppResponse> expected = List.of(new MemberAppResponse(1L, "회원1", "USER"),
            new MemberAppResponse(2L, "관리자", "ADMIN"));

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}