package roomescape;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 통합 테스트 공통 베이스. 매 테스트 실행 전에 모든 테이블을 비우고 AUTO_INCREMENT를 리셋한다. 컨텍스트는 모든 테스트가 공유하여 재기동 비용을 제거.
 * <p>
 * 이전엔 @DirtiesContext(BEFORE_EACH_TEST_METHOD)로 컨텍스트 자체를 매번 새로 띄웠지만, 진짜 필요한 건 "DB 격리 + ID 시퀀스 리셋"뿐이라 그 두 가지만 명시적으로 수행.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
    }

    @BeforeEach
    void cleanDatabase() {
        // FK 제약 때문에 자식 테이블부터 비움
        jdbcTemplate.execute("DELETE FROM reservation");
        jdbcTemplate.execute("DELETE FROM reservation_time");
        jdbcTemplate.execute("DELETE FROM theme");

        // AUTO_INCREMENT 리셋 (테스트가 ID 1부터 시작한다고 가정할 수 있도록)
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }
}
