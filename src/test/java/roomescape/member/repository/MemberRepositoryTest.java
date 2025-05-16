package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
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
    void 이메일과_비밀번호로_회원을_찾을_수_있다() {
        // given
        String email = "test@example.com";
        String rawPassword = "password123";
        Password password = Password.encrypt(rawPassword, passwordEncryptor);
        Member member = Member.signUpUser("테스트", email, password);
        memberRepository.save(member);

        // when
        Optional<Member> foundMember = memberRepository.findByEmailAndPassword(email, rawPassword);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(foundMember).isPresent();
        softly.assertThat(foundMember.get().getEmail()).isEqualTo(email);
        softly.assertAll();
    }

    @Test
    void 존재하지_않는_이메일과_비밀번호로_회원을_찾을_수_없다() {
        // given
        String email = "nonexistent@example.com";
        String password = "password123";

        // when
        Optional<Member> foundMember = memberRepository.findByEmailAndPassword(email, password);

        // then
        assertThat(foundMember).isEmpty();
    }

    @Test
    void 이메일_존재_여부를_확인할_수_있다() {
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

    @Test
    void 유저_저장_테스트(){
        Password password = new Password("1234");

        Member member = Member.signUpUser("꾹", "admin@naver.com", password);
        assertThatCode(() -> memberRepository.save(member))
                .doesNotThrowAnyException();
    }
}
