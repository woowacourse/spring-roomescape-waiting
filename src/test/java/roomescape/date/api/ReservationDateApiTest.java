package roomescape.date.api;

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

class ReservationDateApiTest extends AcceptanceTest {


    @Test
    void 사용자는_본인의_예약을_조회할_수_있다() {
        RestAssured.given().log().all()
            .when().get("/member/dates")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(0));
    }


    @Test
    @Sql(
        scripts = "classpath:past-reservation-date.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void 사용자는_본인의_미래_예약만_조회할_수_있다() {
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
    @DisplayName("활성화된 예약만 조회한다")
    void 사용자는_활성화된_예약만_조회할_수_있다() {
        Integer activeDateId = createReservationDate(managerToken,
            LocalDate.now().plusDays(1).toString());
        updateDateStatus(managerToken, activeDateId, true);

        RestAssured.given().log().all()
            .when().get("/member/dates")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1));
    }
}
