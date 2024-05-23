package roomescape.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import roomescape.core.domain.Member;

@Component
@Profile("test")
public class AdminGenerator {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void generate() {
        final Member member = TestFixture.getAdmin();

        entityManager.persist(member);
        entityManager.flush();
        entityManager.clear();
    }
}
