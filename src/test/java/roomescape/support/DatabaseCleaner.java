package roomescape.support;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 테스트 간 데이터 격리를 담당하는 컴포넌트.
 *
 * <p>격리 전략으로 @Transactional 롤백이 아니라 명시적 DELETE + AUTO_INCREMENT 리셋을 택했다.
 * 이유는 두 가지다.
 * <ul>
 *   <li>이 미션의 핵심 검증 대상 중 하나가 "예약 취소 → 첫 대기 승격"의 트랜잭션 경계인데,
 *       테스트에 @Transactional을 걸면 그 경계가 테스트 트랜잭션에 흡수되어 검증이 무의미해진다.
 *       (사이클 2에서 @Transactional이 코드에 들어오면 이 차이가 결정적이 된다.)</li>
 *   <li>실제 커밋된 상태를 검증할 수 있다. 롤백은 커밋 시점 제약·동시성을 가린다.</li>
 * </ul>
 * <p>
 * * <p>삭제를 TRUNCATE가 아니라 DELETE로 하는 이유: reservation·waiting이 reservation_time·theme을
 *  * FK로 참조하는데, H2는 다른 테이블이 FK로 참조하는 테이블을 TRUNCATE하면 자식을 먼저 비웠더라도
 *  * 거부한다(에러 90106). TRUNCATE로 가려면 SET REFERENTIAL_INTEGRITY FALSE나 제약 drop/재생성으로
 *  * 제약을 우회해야 하는데, 이는 cleaner에 숨은 전역 상태를 더한다. 자식(waiting, reservation) →
 *  * 부모(reservation_time, theme) 순서의 DELETE는 FK를 그대로 존중하면서 동작이 코드에 드러난다.
 *
 *
 * <p>AUTO_INCREMENT를 리셋하는 이유: 일부 테스트가 "생성된 첫 예약의 id" 같은 값에 의존할 때,
 * 리셋하지 않으면 두 번째 실행부터 id가 이어지며 깨진다. (격리의 흔한 함정)
 */
@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleaner(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void clean() {
        // FK 제약 때문에 자식 테이블(payment, waiting, reservation)부터 비운다.
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM waiting");
        jdbcTemplate.execute("DELETE FROM reservation");
        jdbcTemplate.execute("DELETE FROM reservation_time");
        jdbcTemplate.execute("DELETE FROM theme");

        // 각 테스트가 id 1부터 시작한다고 가정할 수 있도록 시퀀스를 리셋한다.
        jdbcTemplate.execute("ALTER TABLE payment ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }
}
