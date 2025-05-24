package roomescape.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clear() {
        entityManager.createNativeQuery("""
                                SET REFERENTIAL_INTEGRITY FALSE;
                                TRUNCATE TABLE reservation;
                                ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;
                                TRUNCATE TABLE reservation_time;
                                ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;
                                TRUNCATE TABLE theme;
                                ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;
                                TRUNCATE TABLE member;
                                ALTER TABLE member ALTER COLUMN id RESTART WITH 1;
                                TRUNCATE TABLE reservation_waiting;
                                ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1;
                                SET REFERENTIAL_INTEGRITY TRUE;
                                """).executeUpdate();
    }
}
