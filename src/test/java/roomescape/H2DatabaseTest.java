package roomescape;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;
import roomescape.reservation.domain.Reservation;

@ActiveProfiles("test")
@Import(TestTimeConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class H2DatabaseTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = 8080;
    }

    @Test
    @DisplayName("데이터베이스 연동에 성공한다.")
    void connects_to_database_successfully() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isNotBlank();
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("DB 조회 API 전환에 성공한다.")
    void returns_reservations_from_database_backed_api() {
        String accessToken = login();

        List<Reservation> reservations = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList("data");

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from reservation where member_id = 1",
                Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    private String login() {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("name", "a");
        loginRequest.put("password", "test1");

        return RestAssured.given().log().all()
                .body(loginRequest)
                .contentType(ContentType.JSON)
                .post("/api/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("data.accessToken");
    }

}
