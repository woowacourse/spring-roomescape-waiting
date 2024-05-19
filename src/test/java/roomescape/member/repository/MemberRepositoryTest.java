package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.test.RepositoryTest;

class MemberRepositoryTest extends RepositoryTest {
    private static final int COUNT_OF_MEMBER = 4;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("전체 멤버를 조회할 수 있다.")
    @Test
    void findAllTest() {
        List<Member> actual = memberRepository.findAll();

        assertThat(actual).hasSize(COUNT_OF_MEMBER);
    }

    @DisplayName("id로 멤버를 조회할 수 있다.")
    @Test
    void findByIdTest() {
        Optional<Member> actual = memberRepository.findById(1L);

        assertThat(actual.get().getId()).isEqualTo(1L);
    }

    @DisplayName("email과 password로 멤버를 조회할 수 있다.")
    @Test
    void findByEmailAndPasswordTest() {
        Email email = new Email("admin@abc.com");
        Password password = new Password("1234");
        Optional<Member> actual = memberRepository.findByEmailAndPassword(email, password);

        assertThat(actual.get().getId()).isEqualTo(1L);
    }

    @DisplayName("멤버를 삭제할 수 있다.")
    @Test
    void deleteTest() {
        memberRepository.deleteById(1L);

        Optional<Member> member = memberRepository.findById(1L);
        assertThat(member).isEmpty();
    }
}
