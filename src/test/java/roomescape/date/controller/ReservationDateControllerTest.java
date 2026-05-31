package roomescape.date.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.date.fixture.ReservationDateApiFixture.updateDateStatus;

import io.restassured.RestAssured;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.AcceptanceTest;

class ReservationDateControllerTest extends AcceptanceTest {

    @Test
    @DisplayName("사용자는 예약 가능한 날짜 목록을 조회한다.")
    void getReservationDates() {
        RestAssured.given().log().all()
                .when().get("/member/dates")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("사용자는 오늘 이후의 예약 날짜 목록을 조회한다.")
    void getReservationDatesAfterToday() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        Integer id = createReservationDate(managerToken, tomorrow);
        updateDateStatus(managerToken, id, true);

        RestAssured.given().log().all()
                .when().get("/member/dates")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("사용자는 오늘 이전의 예약 날짜를 조회할 수 없다.")
    @Sql(
            scripts = "classpath:past-reservation-date.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void getReservationDatesExcludePastDates() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        Integer futureDateId = createReservationDate(managerToken, tomorrow);
        updateDateStatus(managerToken, futureDateId, true);

        RestAssured.given().log().all()
                .when().get("/member/dates")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("date", contains(tomorrow));
    }

    @Test
    @DisplayName("사용자는 비활성화된 날짜를 조회할 수 없다.")
    void getReservationDatesExcludeInactiveDates() {
        Integer activeDateId = createReservationDate(managerToken, LocalDate.now().plusDays(1).toString());
        Integer inactiveDateId = createReservationDate(managerToken, LocalDate.now().plusDays(2).toString());
        updateDateStatus(managerToken, activeDateId, true);

        RestAssured.given().log().all()
                .when().get("/member/dates")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

}
