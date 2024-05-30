package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.reservation.repository.fixture.MemberFixture;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("모든 멤버를 조회한다.")
    @Test
    void findAll() {
        final var result = memberRepository.findAll();

        assertThat(result).hasSize(MemberFixture.count());
    }

    @DisplayName("id로 멤버를 조회한다.")
    @Test
    void findById() {
        final var result = memberRepository.findById(1L);

        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @DisplayName("멤버를 생성한다.")
    @Test
    void save() {
        final var member = Member.of("안나", "anna@gmail.com", "password", "ADMIN");

        memberRepository.save(member);

        assertThat(memberRepository.findAll()).hasSize(MemberFixture.count() + 1);
    }

    @DisplayName("이메일로 멤버를 조회한다.")
    @Test
    void findByEmail() {
        final var result = memberRepository.findByEmail(new Email("jerry@gmail.com"));

        assertThat(result.get().getId()).isEqualTo(1L);
    }
}
