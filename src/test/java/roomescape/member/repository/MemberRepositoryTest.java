package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.domain.PasswordEncryptor;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private PasswordEncryptor passwordEncryptor;

    @BeforeEach
    void setUp() {
        passwordEncryptor = Mockito.mock(PasswordEncryptor.class);
        when(passwordEncryptor.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("이메일과 비밀번호로 회원을 찾을 수 있다")
    void findByEmailAndPassword() {
        // given
        String email = "test@example.com";
        String rawPassword = "password123";
        Password password = Password.encrypt(rawPassword, passwordEncryptor);
        Member member = Member.signUpUser("테스트", email, password);
        memberRepository.save(member);

        // when
        Optional<Member> foundMember = memberRepository.findByEmailAndPassword_Password(email, rawPassword);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(foundMember).isPresent();
        softly.assertThat(foundMember.get().getEmail()).isEqualTo(email);
        softly.assertAll();
    }

    @Test
    @DisplayName("존재하지 않는 이메일과 비밀번호로 회원을 찾을 수 없다")
    void findByEmailAndPasswordWhenNotExists() {
        // given
        String email = "nonexistent@example.com";
        String password = "password123";

        // when
        Optional<Member> foundMember = memberRepository.findByEmailAndPassword_Password(email, password);

        // then
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인할 수 있다")
    void existsByEmail() {
        // given
        String email = "test@example.com";
        Password password = Password.encrypt("password123", passwordEncryptor);
        Member member = Member.signUpUser("테스트", email, password);
        memberRepository.save(member);

        // when
        boolean exists = memberRepository.existsByEmail(email);
        boolean notExists = memberRepository.existsByEmail("nonexistent@example.com");

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(exists).isTrue();
        softly.assertThat(notExists).isFalse();
        softly.assertAll();
    }
}
