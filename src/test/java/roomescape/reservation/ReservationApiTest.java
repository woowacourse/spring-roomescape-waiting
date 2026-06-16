package roomescape.reservation;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.support.ApiTest;
import roomescape.support.TestDataHelper;

@ApiTest
class ReservationApiTest {

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("방탈출 예약 추가 API를 테스트합니다.")
    @Test
    void save_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        Map<String, String> params = ReservationFixture.futureReservationParams(themeId, timeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0))
                .body("name", equalTo("스타크"))
                .body("date", equalTo("2099-12-31"))
                .body("time.id", equalTo(timeId.intValue()))
                .body("time.startAt", equalTo("09:00"))
                .body("theme.id", equalTo(themeId.intValue()))
                .body("theme.name", equalTo("공포 테마"))
                .body("status", equalTo("PAYMENT_PENDING"));
    }

    @DisplayName("방탈출 예약 삭제 API를 테스트합니다.")
    @Test
    void delete_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        RestAssured.given()
                .when().delete("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("과거 날짜로 예약 생성 요청 시 422 응답 반환을 테스트합니다.")
    @Test
    void save_reservation_with_past_date() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        Map<String, String> params = ReservationFixture.pastReservationParams(themeId, timeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422)
                .body("errorMessage", equalTo("현재 시간보다 이전 시간으로 예약을 할 수 없습니다."));
    }

    @DisplayName("예약 생성 요청 값이 올바르지 않을 시 400 응답 반환을 테스트합니다.")
    @ParameterizedTest
    @CsvSource({
            "name, '', 이름은 비어있을 수 없습니다.",
            "date, 2028/05/06, 날짜 형식은 yyyy-MM-dd 이어야 합니다.",
            "date, , 날짜는 비어있을 수 없습니다.",
            "themeId, , 테마는 비어있을 수 없습니다.",
            "themeId, -1, 테마ID는 양수여야 합니다.",
            "timeId, , 시간은 비어있을 수 없습니다.",
            "timeId, -1, 시간ID는 양수여야 합니다."
    })
    void save_reservation_with_invalid_params(String fieldName, String invalidValue, String expectedMessage) {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        Map<String, String> params = ReservationFixture.futureReservationParams(themeId, timeId);
        params.put(fieldName, invalidValue);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo(expectedMessage));
    }

    @DisplayName("이미 예약된 날짜와 시간으로 예약 시 409 응답 반환을 테스트합니다.")
    @Test
    void save_duplicated_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        Map<String, String> params = ReservationFixture.futureReservationParams(themeId, timeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("errorMessage", equalTo("이미 해당 날짜와 시간에 예약이 존재합니다."));
    }

    @DisplayName("방탈출 예약 날짜와 시간 변경 API를 테스트합니다.")
    @Test
    void update_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long updateTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        Map<String, String> params = ReservationFixture.futureReservationUpdateParams(updateTimeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(200)
                .body("id", equalTo(reservationId.intValue()))
                .body("name", equalTo("스타크"))
                .body("date", equalTo("2100-01-01"))
                .body("time.id", equalTo(updateTimeId.intValue()))
                .body("time.startAt", equalTo("10:00"))
                .body("theme.id", equalTo(themeId.intValue()))
                .body("theme.name", equalTo("공포 테마"))
                .body("status", equalTo("PAYMENT_PENDING"));
    }

    @DisplayName("존재하지 않는 예약을 변경 시 404 응답 반환을 테스트합니다.")
    @Test
    void update_not_existing_reservation() {
        Long updateTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        Map<String, String> params = ReservationFixture.futureReservationUpdateParams(updateTimeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", 999L)
                .then().log().all()
                .statusCode(404)
                .body("errorMessage", equalTo("존재하지 않는 예약입니다."));
    }

    @DisplayName("변경하려는 날짜와 시간에 이미 예약이 존재할 시 409 응답 반환을 테스트합니다.")
    @Test
    void update_duplicated_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long updateTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertReservation(
                "비밥",
                ReservationFixture.futureReservationUpdateDate(),
                themeId,
                updateTimeId
        );

        Map<String, String> params = ReservationFixture.futureReservationUpdateParams(updateTimeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(409)
                .body("errorMessage", equalTo("변경하려는 날짜와 시간에 이미 예약이 존재합니다."));
    }

    @DisplayName("과거 날짜로 예약 변경 요청 시 422 응답 반환을 테스트합니다.")
    @Test
    void update_reservation_with_past_date() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        Map<String, String> params = ReservationFixture.pastReservationUpdateParams(timeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(422)
                .body("errorMessage", equalTo("현재 시간보다 이전 시간으로 예약을 할 수 없습니다."));
    }

    @DisplayName("이미 지나간 예약 변경 요청 시 422 응답 반환을 테스트합니다.")
    @Test
    void update_past_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long updateTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        Map<String, String> params = ReservationFixture.futureReservationUpdateParams(updateTimeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(422)
                .body("errorMessage", equalTo("이미 지나간 예약은 변경할 수 없습니다."));
    }

    @DisplayName("존재하지 않는 예약을 삭제 시 404 응답 반환을 테스트합니다.")
    @Test
    void delete_not_existing_reservation() {
        RestAssured.given()
                .when().delete("/reservations/{id}", 999L)
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("관리자 예약 전체 조회 API를 테스트합니다.")
    @Test
    void find_all_reservations() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        LocalDate earlierDate = LocalDate.of(2026, 5, 6);
        LocalDate laterDate = LocalDate.of(2026, 5, 7);

        testHelper.insertReservation(
                "스타크",
                earlierDate,
                themeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "비밥",
                laterDate,
                themeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "스타크",
                laterDate,
                themeId,
                tenTimeId
        );

        RestAssured.given()
                .queryParam("page", 0)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("content", hasSize(3))
                .body("page", equalTo(0))
                .body("size", equalTo(20))
                .body("totalElements", equalTo(3))
                .body("totalPages", equalTo(1))
                .body("content[0].id", greaterThan(0))
                .body("content[0].name", equalTo("스타크"))
                .body("content[0].date", equalTo("2026-05-06"))
                .body("content[0].time.id", equalTo(nineTimeId.intValue()))
                .body("content[0].time.startAt", equalTo("09:00"))
                .body("content[0].theme.id", equalTo(themeId.intValue()))
                .body("content[0].theme.name", equalTo("공포 테마"))
                .body("content[0].status", equalTo("PAYMENT_PENDING"))

                .body("content[1].id", greaterThan(0))
                .body("content[1].name", equalTo("비밥"))
                .body("content[1].date", equalTo("2026-05-07"))
                .body("content[1].time.id", equalTo(nineTimeId.intValue()))
                .body("content[1].time.startAt", equalTo("09:00"))
                .body("content[1].theme.id", equalTo(themeId.intValue()))
                .body("content[1].theme.name", equalTo("공포 테마"))
                .body("content[1].status", equalTo("PAYMENT_PENDING"))

                .body("content[2].id", greaterThan(0))
                .body("content[2].name", equalTo("스타크"))
                .body("content[2].date", equalTo("2026-05-07"))
                .body("content[2].time.id", equalTo(tenTimeId.intValue()))
                .body("content[2].time.startAt", equalTo("10:00"))
                .body("content[2].theme.id", equalTo(themeId.intValue()))
                .body("content[2].theme.name", equalTo("공포 테마"))
                .body("content[2].status", equalTo("PAYMENT_PENDING"));
    }

    @DisplayName("관리자 예약 목록 조회 시 페이지가 0보다 작으면 400 응답 반환을 테스트합니다.")
    @Test
    void find_all_reservations_with_invalid_page_param() {
        RestAssured.given()
                .queryParam("page", -1)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("페이지 값은 0 이상이어야 합니다."));
    }

    @DisplayName("사용자 이름 없이 예약 목록 조회 시 400 응답 반환을 테스트합니다.")
    @Test
    void find_reservations_without_username() {
        RestAssured.given()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("username은(는) 필수입니다."));
    }

    @DisplayName("빈 사용자 이름으로 예약 목록 조회 시 400 응답 반환을 테스트합니다.")
    @Test
    void find_reservations_with_blank_username() {
        RestAssured.given()
                .param("username", " ")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("이름은 비어있을 수 없습니다."));
    }

    @DisplayName("사용자 이름으로 예약 목록 조회 API를 테스트합니다.")
    @Test
    void find_reservations_by_name() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        LocalDate earlierDate = LocalDate.of(2026, 5, 6);
        LocalDate laterDate = LocalDate.of(2026, 5, 7);

        testHelper.insertReservation(
                "스타크",
                earlierDate,
                themeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "비밥",
                laterDate,
                themeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "스타크",
                laterDate,
                themeId,
                tenTimeId
        );

        RestAssured.given()
                .param("username", "스타크")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", greaterThan(0))
                .body("[0].name", equalTo("스타크"))
                .body("[0].date", equalTo("2026-05-06"))
                .body("[0].time.id", equalTo(nineTimeId.intValue()))
                .body("[0].time.startAt", equalTo("09:00"))
                .body("[0].theme.id", equalTo(themeId.intValue()))
                .body("[0].theme.name", equalTo("공포 테마"))
                .body("[0].status", equalTo("PAYMENT_PENDING"))

                .body("[1].id", greaterThan(0))
                .body("[1].name", equalTo("스타크"))
                .body("[1].date", equalTo("2026-05-07"))
                .body("[1].time.id", equalTo(tenTimeId.intValue()))
                .body("[1].time.startAt", equalTo("10:00"))
                .body("[1].theme.id", equalTo(themeId.intValue()))
                .body("[1].theme.name", equalTo("공포 테마"))
                .body("[1].status", equalTo("PAYMENT_PENDING"));
    }

    @DisplayName("이미 지나간 시간의 예약을 삭제 시 422 응답 반환을 테스트합니다.")
    @Test
    void delete_past_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        RestAssured.given()
                .when().delete("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(422)
                .body("errorMessage", equalTo("이미 지나간 예약은 삭제할 수 없습니다."));
    }
}
