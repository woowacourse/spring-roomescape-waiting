package roomescape.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservationtime.dto.ReservationTimeRequest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ReservationTimeApiTest {

    private final JdbcTemplate jdbcTemplate;
    private final int port;

    public ReservationTimeApiTest(
            @LocalServerPort final int port,
            @Autowired final JdbcTemplate jdbcTemplate
    ) {
        this.port = port;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    @DisplayName("시간 생성")
    @ParameterizedTest
    @MethodSource
    void createTime(final Map<String, Object> body, final HttpStatus expectedStatus) {
        // given & when & then
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/times")
                .then().log().all()
                .statusCode(expectedStatus.value());
    }

    static Stream<Arguments> createTime() {
        return Stream.of(
                Arguments.of(Map.of("startAt", "10:40"), HttpStatus.CREATED),
                Arguments.of(Map.of("startAt", "25:40"), HttpStatus.BAD_REQUEST),
                Arguments.of(Map.of(), HttpStatus.BAD_REQUEST)
        );
    }

    @DisplayName("시간 모두 조회")
    @Test
    void findAllTime() {
        // given & when & then
        RestAssured.given().port(port).log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("시간 삭제")
    @Test
    void deleteTime() {
        // given
        final ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));
        givenCreateTime(request);

        // when & then
        RestAssured.given().port(port).log().all()
                .when().delete("times/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 아이디를 가진 시간을 삭제하려고하면 404를 반환한다.")
    @Test
    void deleteTime1() {
        // given & when & then
        RestAssured.given().port(port).log().all()
                .when().delete("times/100")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("startAt 관련 api 테스트")
    @Test
    void 칠단계() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        // when & then
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().port(port).log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().port(port).log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    private void givenCreateTime(final ReservationTimeRequest body) {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }


}
