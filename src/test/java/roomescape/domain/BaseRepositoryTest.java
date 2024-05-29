package roomescape.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.config.JpaAuditingConfig;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public abstract class BaseRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    protected <T> T save(T entity) {
        em.persist(entity);
        flushAndClear();
        return entity;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
