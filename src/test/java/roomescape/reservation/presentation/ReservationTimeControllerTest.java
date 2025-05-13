package roomescape.reservation.presentation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.presentation.fixture.MemberFixture;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.fixture.ReservationFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationTimeControllerTest {
    private final ReservationFixture reservationFixture = new ReservationFixture();
    private final MemberFixture memberFixture = new MemberFixture();

    @Test
    @DisplayName("시간 추가 테스트")
    void createTimeTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        final ReservationTimeRequest reservationTime = reservationFixture.createReservationTimeRequest(
                LocalTime.of(10, 30));

        // when-then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("중복된 시간 추가는 불가능하다.")
    void createTimeDuplicateExceptionTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        final ReservationTimeRequest reservationTime = reservationFixture.createReservationTimeRequest(
                LocalTime.of(10, 30));

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        // then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("시간 조회 테스트")
    void getTimesTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);

        // when-then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 테스트")
    void getAvailableTimesTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);
        reservationFixture.createReservationTime(LocalTime.of(11, 30), cookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                cookies
        );

        reservationFixture.createReservation(LocalDate.of(2025, 8, 5), 1L, 2L, cookies);

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/times/available?date=2025-08-05&themeId=1")
                .then().log().all()
                .statusCode(200)
                .body("alreadyBooked", is(List.of(false, true)));
    }

    @Test
    @DisplayName("시간 삭제 테스트")
    void deleteTimeTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);

        // when-then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("예약이 이미 존재하는 시간은 삭제할 수 없다.")
    void deleteTimeExceptionTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                cookies
        );

        reservationFixture.createReservation(LocalDate.of(2025, 8, 5), 1L, 1L, cookies);

        // when-then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(400);
    }

}
