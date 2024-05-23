package roomescape;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestDataInitializer {
    private List<String> tableNames = new ArrayList<>();

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void resetDatabase() {
        entityManager.flush();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        truncateData();
        addTestData();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    @PostConstruct
    private void findDatabaseTableNames() throws Exception {
        List<Object[]> tableInfos = entityManager.createNativeQuery("SHOW TABLES").getResultList();
        for (Object[] tableInfo : tableInfos) {
            String tableName = tableInfo[0].toString();
            tableNames.add(tableName);
        }
    }

    private void truncateData() {
        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN id RESTART")
                    .executeUpdate();
        }
    }

    private void addTestData() {
        entityManager.createNativeQuery("""
                INSERT INTO reservation_time(start_at) 
                VALUES ('10:00'), ('11:00'), ('12:00'), ('13:00')
                """).executeUpdate();

        entityManager.createNativeQuery("""
                INSERT INTO theme(name, description, thumbnail)
                VALUES ('레벨1 탈출', '우테코 레벨1를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
                       ('레벨2 탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
                       ('레벨3 탈출', '우테코 레벨3를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
                       ('레벨4 탈출', '우테코 레벨4를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg') 
                """).executeUpdate();

        entityManager.createNativeQuery("""
                INSERT INTO member(email, password, salt, name, role)
                VALUES ('user@test.com', 'hashedpassword', 'salt', 'poke', 'USER'),
                       ('admin@test.com', 'yAD5RFQKSwFkOImAX+hDr1RSlsR7MBBSFr/xni5sGOE=', 'SZj5iqlnmc4cLvpOAS0a1g==', 'wedge', 'ADMIN'),
                       ('user2@email.com', 'IebTDq8qNH5Z43RTP4ZLgXw8ZmdkdrASk6IsYWJ4Wp4=', 'XvAa9NlQEVXpoBT6OpnaRw==', '사용자2', 'USER')
                       """).executeUpdate();

        entityManager.createNativeQuery("""
                INSERT INTO reservation(date, time_id, theme_id, member_id, status)
                VALUES ('2024-05-01', 3, 2, 1, 'RESERVED'),
                       ('2024-05-01', 2, 2, 1, 'RESERVED'),
                       ('2024-04-30', 2, 2, 1, 'RESERVED'),
                       ('2024-04-30', 1, 1, 1, 'RESERVED'),
                       ('2024-04-02', 3, 3, 1, 'RESERVED'),
                       ('2024-03-02', 3, 3, 1, 'RESERVED'),
                       ('2099-04-30', 1, 1, 1, 'RESERVED'),
                       ('2999-04-30', 1, 1, 2, 'RESERVED'),
                       (DATEADD(DAY, 1, current_date), 1, 1, 1, 'RESERVED'),
                       (DATEADD(DAY, 1, current_date), 2, 1, 1, 'RESERVED')
                       """).executeUpdate();

        entityManager.createNativeQuery("""
                INSERT INTO waiting(date, time_id, theme_id, member_id, status)
                VALUES ('2024-04-30', 1, 1, 2, 'WAITING'),
                       (DATEADD(DAY, 1, current_date), 1, 1, 2, 'WAITING'),
                       (DATEADD(DAY, 1, current_date), 1, 1, 3, 'WAITING')
                       """).executeUpdate();
    }
}
