package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;
import roomescape.reservation.Reservation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import(TestTimeConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class H2DatabaseTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String login() {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("name", "testAdmin");
        loginRequest.put("password", "test2");

        return RestAssured.given().log().all()
                .body(loginRequest)
                .contentType(ContentType.JSON)
                .post("/api/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("data.accessToken");
    }

    @Test
    void 데이터베이스_연동() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isNotBlank();
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void DB_조회_API_전환() {
        String accessToken = login();

        List<Reservation> reservations = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/manager/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList("data");

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }
}
