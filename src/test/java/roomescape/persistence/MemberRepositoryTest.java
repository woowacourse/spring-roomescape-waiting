package roomescape.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.fixture.Fixtures;

@DataJpaTest
class MemberRepositoryTest {
    private final MemberRepository memberRepository;
    private final EntityManager entityManager;

    @Autowired
    public MemberRepositoryTest(MemberRepository memberRepository, EntityManager entityManager) {
        this.memberRepository = memberRepository;
        this.entityManager = entityManager;
    }

    @Test
    void findByEmail() {
        // given
        Member member = Fixtures.member();
        entityManager.persist(member);

        // when
        Optional<Member> foundMemberOptional = memberRepository.findByEmail(member.getEmail());

        // then
        assertTrue(foundMemberOptional.isPresent(), "Member should be found by email");
    }

    @Test
    void existsByEmail() {
        // given
        Member member = Fixtures.member();
        entityManager.persist(member);

        // when
        boolean actual = memberRepository.existsByEmail(member.getEmail());

        // then
        assertTrue(actual, "Member should exist by email");
    }
}
