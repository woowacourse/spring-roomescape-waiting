package roomescape.reservation;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.support.ApiTest;
import roomescape.support.TestDataHelper;

@ApiTest
class WaitingApiTest {

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("이미 예약된 날짜와 시간으로 대기 예약 생성을 테스트합니다.")
    @Test
    void save_waiting_if_reservation_exists() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservation(
                "비밥",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        Map<String, String> params = ReservationFixture.futureWaitingParams(themeId, timeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0))
                .body("name", equalTo("스타크"))
                .body("date", equalTo("2099-12-31"))
                .body("time.id", equalTo(timeId.intValue()))
                .body("time.startAt", equalTo("09:00"))
                .body("theme.id", equalTo(themeId.intValue()))
                .body("theme.name", equalTo("공포 테마"))
                .body("status", equalTo("WAITING"))
                .body("rank", equalTo(1))
                .body("totalRankCount", equalTo(1));
    }

    @DisplayName("대기 예약 순번 반환을 테스트합니다.")
    @Test
    void save_waiting_with_rank() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservation(
                "네오",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "피노",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        Map<String, String> params = ReservationFixture.futureWaitingParams(themeId, timeId);
        params.put("name", "카야");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("status", equalTo("WAITING"))
                .body("rank", equalTo(3))
                .body("totalRankCount", equalTo(3));
    }

    @DisplayName("예약이 존재하지 않는 날짜와 시간으로 대기 예약 생성 시 422 응답 반환을 테스트합니다.")
    @Test
    void save_waiting_without_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        Map<String, String> params = ReservationFixture.futureWaitingParams(themeId, timeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(422)
                .body("errorMessage", equalTo("예약이 존재하지 않는 경우, 대기를 신청할 수 없습니다."));
    }

    @DisplayName("대기 예약 생성 요청 값이 올바르지 않을 시 400 응답 반환을 테스트합니다.")
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
    void save_waiting_with_invalid_params(String fieldName, String invalidValue, String expectedMessage) {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        Map<String, String> params = ReservationFixture.futureWaitingParams(themeId, timeId);
        params.put(fieldName, invalidValue);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo(expectedMessage));
    }

    @DisplayName("같은 사람이 이미 대기한 날짜와 시간으로 대기 예약 생성 시 409 응답 반환을 테스트합니다.")
    @Test
    void save_duplicated_waiting() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservation(
                "비밥",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        Map<String, String> params = ReservationFixture.futureWaitingParams(themeId, timeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409)
                .body("errorMessage", equalTo("이미 해당 테마의 날짜와 시간에 대기를 신청했습니다."));
    }

    @DisplayName("동일한 사용자가 이미 예약한 날짜와 시간으로 대기 예약 생성 시 409 응답 반환을 테스트합니다.")
    @Test
    void save_waiting_with_confirmed_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        Map<String, String> params = ReservationFixture.futureWaitingParams(themeId, timeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409)
                .body("errorMessage", equalTo("이미 예약한 날짜와 시간에는 대기를 신청할 수 없습니다."));
    }

    @DisplayName("사용자 이름으로 대기 예약 목록 조회 API를 테스트합니다.")
    @Test
    void find_waitings_by_name() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertWaiting(
                "비밥",
                ReservationFixture.futureReservationDate(),
                themeId,
                nineTimeId
        );
        testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                nineTimeId
        );
        testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                tenTimeId
        );

        RestAssured.given()
                .param("username", "스타크")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", greaterThan(0))
                .body("[0].name", equalTo("스타크"))
                .body("[0].date", equalTo("2099-12-31"))
                .body("[0].time.id", equalTo(nineTimeId.intValue()))
                .body("[0].time.startAt", equalTo("09:00"))
                .body("[0].theme.id", equalTo(themeId.intValue()))
                .body("[0].theme.name", equalTo("공포 테마"))
                .body("[0].status", equalTo("WAITING"))
                .body("[0].rank", equalTo(2))
                .body("[0].totalRankCount", equalTo(2))

                .body("[1].id", greaterThan(0))
                .body("[1].name", equalTo("스타크"))
                .body("[1].date", equalTo("2099-12-31"))
                .body("[1].time.id", equalTo(tenTimeId.intValue()))
                .body("[1].time.startAt", equalTo("10:00"))
                .body("[1].theme.id", equalTo(themeId.intValue()))
                .body("[1].theme.name", equalTo("공포 테마"))
                .body("[1].status", equalTo("WAITING"))
                .body("[1].rank", equalTo(1))
                .body("[1].totalRankCount", equalTo(1));
    }

    @DisplayName("사용자 이름 없이 예약 대기 목록 조회 시 400 응답 반환을 테스트합니다.")
    @Test
    void find_waitings_without_username() {
        RestAssured.given()
                .when().get("/waitings")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("username은(는) 필수입니다."));
    }

    @DisplayName("빈 사용자 이름으로 예약 대기 목록 조회 시 400 응답 반환을 테스트합니다.")
    @Test
    void find_waitings_with_blank_username() {
        RestAssured.given()
                .param("username", " ")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("이름은 비어있을 수 없습니다."));
    }

    @DisplayName("방탈출 예약 대기 삭제 API를 테스트합니다.")
    @Test
    void delete_waiting_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        RestAssured.given()
                .when().delete("/waitings/{id}", waitingId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 대기 예약을 삭제 시 404 응답 반환을 테스트합니다.")
    @Test
    void delete_not_existing_waiting_reservation() {
        RestAssured.given()
                .when().delete("/waitings/{id}", 999L)
                .then().log().all()
                .statusCode(404)
                .body("errorMessage", equalTo("존재하지 않는 대기입니다."));
    }

    @DisplayName("이미 지나간 시간의 대기 예약을 삭제 시 422 응답 반환을 테스트합니다.")
    @Test
    void delete_past_waiting_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        RestAssured.given()
                .when().delete("/waitings/{id}", waitingId)
                .then().log().all()
                .statusCode(422)
                .body("errorMessage", equalTo("이미 지나간 예약은 삭제할 수 없습니다."));
    }

    @DisplayName("대기를 지정한 순번 만큼 미루는 API를 테스트합니다.")
    @Test
    void postpone_waiting_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "피케이",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "네오",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "이안",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        RestAssured.given()
                .queryParam("steps", 2)
                .when().post("/waitings/{id}/postpone", waitingId)
                .then().log().all()
                .statusCode(200)
                .body("id", equalTo(waitingId.intValue()))
                .body("rank", equalTo(3));

        ReservationSlot slot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(10, 0))
                .build();

        Integer starkRank = testHelper.findWaitingRank("스타크", slot);
        Integer pkRank = testHelper.findWaitingRank("피케이", slot);
        Integer neoRank = testHelper.findWaitingRank("네오", slot);
        Integer ianRank = testHelper.findWaitingRank("이안", slot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(starkRank).isEqualTo(3);
            softly.assertThat(pkRank).isEqualTo(1);
            softly.assertThat(neoRank).isEqualTo(2);
            softly.assertThat(ianRank).isEqualTo(4);
        });
    }

    @DisplayName("대기를 남은 순번보다 많이 미루면 마지막 순번으로 이동하는 것을 테스트합니다.")
    @Test
    void postpone_waiting_reservation_to_last_rank() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "피케이",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "네오",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        RestAssured.given()
                .queryParam("steps", 99)
                .when().post("/waitings/{id}/postpone", waitingId)
                .then().log().all()
                .statusCode(200)
                .body("id", equalTo(waitingId.intValue()))
                .body("rank", equalTo(3));

        ReservationSlot slot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(10, 0))
                .build();

        Integer starkRank = testHelper.findWaitingRank("스타크", slot);
        Integer pkRank = testHelper.findWaitingRank("피케이", slot);
        Integer neoRank = testHelper.findWaitingRank("네오", slot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(starkRank).isEqualTo(3);
            softly.assertThat(pkRank).isEqualTo(1);
            softly.assertThat(neoRank).isEqualTo(2);
        });
    }

    @DisplayName("대기를 양수가 아닌 순번 만큼 미룰 시 400 응답 반환을 테스트합니다.")
    @Test
    void postpone_waiting_reservation_with_invalid_steps() {
        RestAssured.given()
                .queryParam("steps", 0)
                .when().post("/waitings/{id}/postpone", 1L)
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("미룰 순번은 양수여야 합니다."));
    }

    @DisplayName("이미 지나간 시간의 대기 예약을 미룰 시 422 응답 반환을 테스트합니다.")
    @Test
    void postpone_past_waiting_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        RestAssured.given()
                .queryParam("steps", 1)
                .when().post("/waitings/{id}/postpone", waitingId)
                .then().log().all()
                .statusCode(422)
                .body("errorMessage", equalTo("이미 지나간 예약은 미룰 수 없습니다."));
    }
}
