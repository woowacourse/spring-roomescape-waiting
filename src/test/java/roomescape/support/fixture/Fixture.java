package roomescape.support.fixture;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class Fixture {

    @PersistenceContext
    protected EntityManager em;

    protected void synchronize() {
        em.flush();
        em.clear();
    }
}
