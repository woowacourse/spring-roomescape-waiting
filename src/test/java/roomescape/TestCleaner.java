package roomescape;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestCleaner {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void cleanAll() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE reservation RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE reservation_time RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE theme RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE member RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
}
