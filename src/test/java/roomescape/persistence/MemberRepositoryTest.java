package roomescape.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("이메일이 존재할 시 회원 정보를 반환한다.")
    @Test
    void findByEmail_returnsMember_whenEmailExists() {
        // given
        Member member = new Member(null, "Test", MemberRole.USER, "test@email.com", "Password1!");
        memberRepository.save(member);

        // when
        Optional<Member> found = memberRepository.findByEmail("test@email.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test");
    }

    @DisplayName("이메일이 존재하지 않을 시 빈 Optional을 반환한다.")
    @Test
    void findByEmail_returnsEmpty_whenEmailDoesNotExist() {
        // when
        Optional<Member> found = memberRepository.findByEmail("notfound@email.com");

        // then
        assertThat(found).isEmpty();
    }
}
