package roomescape.domain.reservationdate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.support.TestFixture;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationDateIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestFixture testFixture;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        testFixture.clear();
    }

    @Test
    @DisplayName("전체 예약 날짜 조회를 end-to-end로 확인한다.")
    void getAllReservationDates() {
        testFixture.saveDate("2026-06-01");

        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/reservation-dates")
            .then().log().all()
            .statusCode(200)
            .body("[0].reservationDate", is("2026-06-01"));
    }
}
