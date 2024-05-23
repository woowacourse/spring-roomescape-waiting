package roomescape.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public abstract class BaseRepositoryTest {
    @PersistenceContext
    protected EntityManager em;

    protected <T> T save(T entity) {
        em.persist(entity);
        synchronize();
        return entity;
    }

    private void synchronize() {
        em.flush();
        em.clear();
    }
}
