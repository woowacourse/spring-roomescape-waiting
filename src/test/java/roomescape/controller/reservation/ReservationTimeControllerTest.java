package roomescape.controller.reservation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import roomescape.service.reservation.dto.ReservationTimeCreateRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = {"classpath:truncate-with-guests.sql"})
class ReservationTimeControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void initPort() {
        RestAssured.port = port;
    }

    @DisplayName("시간 정보를 추가한다.")
    @Test
    void createReservationTime() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationTimeCreateRequest("10:00"))
                .when().post("/times")
                .then().log().all().statusCode(201).body("id", is(greaterThan(0)));
    }

    @DisplayName("시간 추가 실패 테스트 - 중복 시간 오류")
    @Test
    void createDuplicateTime() {
        //given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationTimeCreateRequest("10:00"))
                .when().post("/times");

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationTimeCreateRequest("10:00"))
                .when().post("/times")
                .then().log().all().statusCode(400).body("message", is("이미 같은 시간이 존재합니다."));
    }

    @DisplayName("시간 추가 실패 테스트 - 시간 오류")
    @Test
    void createInvalidReservationTime() {
        //given
        String invalidTime = "25:00";

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationTimeCreateRequest(invalidTime))
                .when().post("/times")
                .then().log().all()
                .assertThat().statusCode(400).body("message", is("올바르지 않은 시간입니다."));
    }

    @DisplayName("등록된 시간 내역을 조회한다.")
    @Test
    void findAllReservationTime() {
        //given
        RestAssured.given().contentType(ContentType.JSON).body(new ReservationTimeCreateRequest("10:00"))
                .when().post("/times");

        //when&then
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all().statusCode(200).body("size()", is(1));
    }

    @DisplayName("시간 정보를 id로 삭제한다.")
    @Test
    void deleteReservationTimeById() {
        //given
        var id = RestAssured.given().contentType(ContentType.JSON).body(new ReservationTimeCreateRequest("10:00"))
                .when().post("/times")
                .then().log().all().extract().response().jsonPath().get("id");

        //when&then
        RestAssured.given().log().all()
                .when().delete("/times/" + id)
                .then().log().all()
                .assertThat().statusCode(204);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .assertThat().body("size()", is(0));
    }

    @DisplayName("시간 삭제 실패 테스트 - 이미 예약이 존재하는 시간(timeId = 1) 삭제 시도 오류")
    @Test
    @Sql(scripts = {"classpath:insert-time-with-reservation.sql"})
    void cannotDeleteReservationTime() {
        //given
        int timeId = 1;

        //when&then
        RestAssured.given().log().all()
                .when().delete("/times/" + timeId)
                .then().log().all()
                .assertThat().statusCode(400).body("message", is("해당 시간에 예약이 존재해서 삭제할 수 없습니다."));
    }

    @DisplayName("예약 가능한 시간 조회 테스트 - 10:00: 예약 존재, (11:00,12:00): 예약 미존재.")
    @Test
    @Sql(scripts = {"classpath:insert-time-with-reservation.sql"})
    void findAvailableTime() {
        //given
        long themeId = 1;
        String date = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);

        //when&then
        RestAssured.given().log().all()
                .when().get("/times/available?date=" + date + "&themeId=" + themeId)
                .then().log().all().statusCode(200).body("size()", is(3));
    }
}
