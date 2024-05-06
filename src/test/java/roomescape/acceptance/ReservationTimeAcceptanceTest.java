package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.application.member.dto.request.MemberRegisterRequest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.request.ReservationTimeRequest;
import roomescape.application.reservation.dto.request.ThemeRequest;
import roomescape.application.reservation.dto.response.AvailableTimeResponse;
import roomescape.application.reservation.dto.response.ReservationTimeResponse;

class ReservationTimeAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("관리자가 예약 시간을 생성한다.")
    void createReservationTimeTest() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));
        RestAssured.given().log().all()
                .cookie("token", fixture.getAdminToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("예약 시간을 모두 조회한다.")
    void findAllReservationTimesTest() {
        fixture.createReservationTime(10, 0);
        fixture.createReservationTime(11, 30);

        ReservationTimeResponse[] responses = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ReservationTimeResponse[].class);

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("관리자가 예약 시간을 삭제한다.")
    void deleteReservationTimeTest() {
        ReservationTimeResponse response = fixture.createReservationTime(10, 0);

        RestAssured.given().log().all()
                .cookie("token", fixture.getAdminToken())
                .when().delete("/times/{id}", response.id())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("예약 가능한 시간을 조회한다.")
    void findAvailableTimesTest() {
        fixture.registerMember(new MemberRegisterRequest("name", "email@mail.com", "password"));
        String token = fixture.loginAndGetToken("email@mail.com", "password");
        fixture.createReservationTime(10, 0);
        fixture.createReservationTime(11, 30);
        long timeId = fixture.createReservationTime(13, 0).id();
        long themeId = fixture.createTheme(new ThemeRequest("theme", "desc", "url")).id();
        ReservationRequest request = new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId);
        fixture.createReservation(token, request);

        AvailableTimeResponse[] responses = RestAssured.given().log().all()
                .queryParam("date", "2024-12-25")
                .queryParam("themeId", themeId)
                .when().get("/times/available")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(AvailableTimeResponse[].class);

        List<AvailableTimeResponse> actual = Arrays.stream(responses)
                .filter(AvailableTimeResponse::isBooked)
                .toList();
        assertThat(actual).hasSize(1);
    }
}
