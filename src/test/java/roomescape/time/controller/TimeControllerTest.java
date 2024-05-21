package roomescape.time.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.time.dto.AvailableTimeResponse;
import roomescape.time.dto.TimeCreateRequest;
import roomescape.time.dto.TimeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TimeControllerTest {
    private final int COUNT_OF_TIME = 3;

    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("시간 목록을 읽을 수 있다.")
    @Test
    void findTimes() {
        int size = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        assertThat(size).isEqualTo(COUNT_OF_TIME);
    }

    @DisplayName("예약 가능한 시간 목록을 읽을 수 있다.")
    @Test
    void findAvailableTimes() {
        List<AvailableTimeResponse> expected = List.of(
                new AvailableTimeResponse(new TimeResponse(1L, LocalTime.of(10, 0)), false),
                new AvailableTimeResponse(new TimeResponse(2L, LocalTime.of(19, 0)), true),
                new AvailableTimeResponse(new TimeResponse(3L, LocalTime.of(21, 0)), false));
        List<AvailableTimeResponse> response = RestAssured.given().log().all()
                .when().get("/times/available?date=2022-05-05&themeId=1")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", AvailableTimeResponse.class);

        assertThat(response).isEqualTo(expected);
    }

    @DisplayName("시간을 DB에 추가할 수 있다.")
    @Test
    void createTime() {
        TimeCreateRequest params = new TimeCreateRequest(LocalTime.of(8, 0));
        long expectedId = COUNT_OF_TIME + 1;

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/times/" + expectedId);
    }

    @DisplayName("삭제할 id를 받아서 DB에서 해당 시간을 삭제 할 수 있다.")
    @Test
    void deleteTime() {
        RestAssured.given().log().all()
                .when().delete("/times/3")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation_time", Integer.class);
        assertThat(countAfterDelete).isEqualTo(COUNT_OF_TIME - 1);
    }
}
