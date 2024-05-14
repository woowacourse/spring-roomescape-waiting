package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.config.TestConfig;
import roomescape.dto.request.LoginRequest;
import roomescape.security.JwtTokenProvider;
import roomescape.security.TokenProvider;

@SpringBootTest(
        classes = TestConfig.class,
        webEnvironment = WebEnvironment.RANDOM_PORT
)
public abstract class BaseControllerTest {

    protected static final Long ADMIN_ID = 1L;
    protected static final String ADMIN_EMAIL = "admin@gmail.com";
    protected static final String ADMIN_PASSWORD = "abc123";
    protected static final String USER_EMAIL = "user@gmail.com";
    protected static final String USER_PASSWORD = "abc123";

    @LocalServerPort
    private int port;

    @SpyBean
    protected TokenProvider tokenProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    protected String token;

    @BeforeEach
    void environmentSetUp() {
        RestAssured.port = port;
    }

    @AfterEach
    public void environmentAfterEach() {
        List<String> truncateQueries = getTruncateQueries(jdbcTemplate);
        truncateTables(jdbcTemplate, truncateQueries);
    }

    private List<String> getTruncateQueries(final JdbcTemplate jdbcTemplate) {
        // 모든 테이블을 삭제하는 쿼리를 생성
        String sql = """
                SELECT Concat('TRUNCATE TABLE ', TABLE_NAME, ' RESTART IDENTITY', ';') AS q
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'PUBLIC'
                """;

        return jdbcTemplate.queryForList(sql, String.class);
    }

    private void truncateTables(final JdbcTemplate jdbcTemplate, final List<String> truncateQueries) {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE"); // 외래 키 제약 조건 무시
        truncateQueries.forEach(jdbcTemplate::execute);
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE"); // 외래 키 제약 조건 활성화
    }

    protected void adminLogin() {
        token = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD))
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    protected void userLogin() {
        token = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(USER_EMAIL, USER_PASSWORD))
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }
}
