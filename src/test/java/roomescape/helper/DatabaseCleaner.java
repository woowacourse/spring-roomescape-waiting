package roomescape.helper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DatabaseCleaner {
    @PersistenceContext
    private EntityManager entityManager;

    public void execute() { // TODO: 테이블 이름 가져와서 일괄로 처리하는 로직 짜보기
        clearReservation();
        clearMember();
        clearTime();
        clearTheme();
    }

    private void clearMember() {
        entityManager.createNativeQuery("DELETE FROM member").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART").executeUpdate();
    }

    private void clearTime() {
        entityManager.createNativeQuery("DELETE FROM reservation_time").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE reservation_time ALTER COLUMN id RESTART").executeUpdate();
    }

    private void clearTheme() {
        entityManager.createNativeQuery("DELETE FROM theme").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE theme ALTER COLUMN id RESTART").executeUpdate();
    }

    private void clearReservation() {
        entityManager.createNativeQuery("DELETE FROM reservation").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE reservation ALTER COLUMN id RESTART").executeUpdate();
    }
}
