package roomescape.domain.reservationtime;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql("/truncate.sql")
class AdminReservationTimeControllerTest {

    private static final String ADMIN_HEADER = "X-ADMIN-TOKEN";
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @org.springframework.beans.factory.annotation.Value("${token:boyesumin2sanchaerin}")
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("관리자 권한으로 예약 시간을 생성한다.")
    void createReservationTime() {
        Map<String, Object> params = new HashMap<>();
        params.put("startAt", "23:00");

        RestAssured.given().log().all()
            .header(ADMIN_HEADER, adminToken)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(201)
            .body("startAt", is("23:00"));
    }

    @Test
    @DisplayName("관리자 권한으로 모든 예약 시간을 조회한다.")
    void getAllReservationTimes() {
        jdbcTemplate.update("insert into reservation_time(start_at) values (?)", "23:00");

        RestAssured.given().log().all()
            .header(ADMIN_HEADER, adminToken)
            .when().get("/admin/times")
            .then().log().all()
            .statusCode(200)
            .body("any { it.startAt == '23:00' }", is(true));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 시간을 삭제한다.")
    void deleteReservationTime() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into reservation_time(start_at) values (?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "23:30");
            return ps;
        }, keyHolder);
        Long timeId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        RestAssured.given().log().all()
            .header(ADMIN_HEADER, adminToken)
            .when().delete("/admin/times/" + timeId)
            .then().log().all()
            .statusCode(204);
    }
}
