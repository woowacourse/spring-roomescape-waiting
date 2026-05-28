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
        // FK 제약 때문에 자식 테이블(waiting, reservation)부터 비운다.
        jdbcTemplate.execute("DELETE FROM waiting");
        jdbcTemplate.execute("DELETE FROM reservation");
        jdbcTemplate.execute("DELETE FROM reservation_time");
        jdbcTemplate.execute("DELETE FROM theme");

        // 각 테스트가 id 1부터 시작한다고 가정할 수 있도록 시퀀스를 리셋한다.
        jdbcTemplate.execute("ALTER TABLE waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }
}
