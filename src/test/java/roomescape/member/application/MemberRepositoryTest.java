package roomescape.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberFixtures;

@ActiveProfiles("test")
@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("이메일로 회원을 조회할 수 있다")
    void findByEmailSuccess() {
        // given
        String testEmail = "email1@email.com";

        MemberFixtures.persistUserMember(entityManager, testEmail);
        MemberFixtures.persistUserMember(entityManager, "email2@email.com");
        MemberFixtures.persistUserMember(entityManager, "email3@email.com");
        flushAndClear();

        // when
        Optional<Member> result = memberRepository.findByEmail(testEmail);

        // then
        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().getEmail()).isEqualTo(testEmail)
        );
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환한다")
    void findByNonExistentEmailReturnsEmpty() {
        // given
        MemberFixtures.persistUserMember(entityManager, "email1@email.com");
        flushAndClear();

        // when
        Optional<Member> result = memberRepository.findByEmail("nonexistent@email.com");

        // then
        assertThat(result).isEmpty();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
