package roomescape.helper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class DatabaseCleaner {
    @PersistenceContext
    private EntityManager entityManager;

    public DatabaseCleaner(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void execute() {
        clearMember();
        clearReservation();
        clearTime();
        clearTheme();
    }

    private void clearMember() {
        entityManager.createNativeQuery("DELETE FROM member").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART").executeUpdate();
    }

    private void clearReservation() {
        entityManager.createNativeQuery("DELETE FROM reservation").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE reservation ALTER COLUMN id RESTART").executeUpdate();
    }

    private void clearTime() {
        entityManager.createNativeQuery("DELETE FROM reservation_time").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE reservation_time ALTER COLUMN id RESTART").executeUpdate();
    }

    private void clearTheme() {
        entityManager.createNativeQuery("DELETE FROM theme").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE theme ALTER COLUMN id RESTART").executeUpdate();
    }
}
