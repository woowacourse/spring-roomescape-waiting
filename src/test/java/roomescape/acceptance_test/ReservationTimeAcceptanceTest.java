package roomescape.acceptance_test;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.theme.controller.dto.ThemeCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ReservationTimeAcceptanceTest extends AcceptanceTestSupport {

    @Test
    @DisplayName("예약 시간 목록 조회")
    public void scenario1() throws JsonProcessingException {
        // given
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 30));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(request);

        // when
        ExtractableResponse<Response> response = 예약_시간_목록_조회를_요청하면();

        // then
        생성한_예약_시간이_포함된_예약_시간_목록을_응답받는다(response, reservationTimeId, request);
    }

    @Test
    @DisplayName("중복 예약 시간 생성")
    public void scenario2() throws JsonProcessingException {
        // given
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 30));
        예약_시간_생성을_요청하고(request);

        // when
        ExtractableResponse<Response> response = 같은_예약_시간_생성을_다시_요청하면(request);

        // then
        중복_예약_시간_생성은_실패한다(response);
    }

    @Test
    @DisplayName("예약 가능 시간 조회")
    public void scenario3() throws JsonProcessingException {
        // given
        LocalDate date = LocalDate.of(2026, 10, 14);
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservedTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(21, 30)));
        Integer availableTimeId = 새로운_예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(22, 30)));
        특정_날짜와_테마에_예약_생성을_요청하고(date, reservedTimeId, themeId);

        // when
        ExtractableResponse<Response> response = 특정_날짜와_테마의_예약_가능_시간_조회를_요청하면(date, themeId);

        // then
        예약된_시간은_예약_불가로_응답받는다(response, reservedTimeId);
        예약되지_않은_시간은_예약_가능으로_응답받는다(response, availableTimeId);
    }

    private Integer 예약_시간_생성을_요청하고(ReservationTimeCreateRequest request) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", equalTo(request.startAt().toString()))
                .extract().path("id");
    }

    private ExtractableResponse<Response> 예약_시간_목록_조회를_요청하면() {
        return given().log().all()
                .when()
                .get("/times")
                .then().log().all()
                .extract();
    }

    private void 생성한_예약_시간이_포함된_예약_시간_목록을_응답받는다(
            ExtractableResponse<Response> response,
            Integer reservationTimeId,
            ReservationTimeCreateRequest request
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("times.id", Integer.class)).contains(reservationTimeId);
        assertThat(response.jsonPath().getList("times.startAt", String.class)).contains(request.startAt().toString());
    }

    private ExtractableResponse<Response> 같은_예약_시간_생성을_다시_요청하면(
            ReservationTimeCreateRequest request
    ) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/admin/times")
                .then().log().all()
                .extract();
    }

    private void 중복_예약_시간_생성은_실패한다(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(409);
    }

    private Integer 테마_생성을_요청하고(ThemeCreateRequest request) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo(request.name()))
                .body("description", equalTo(request.description()))
                .body("thumbnail", equalTo(request.thumbnail()))
                .extract().path("id");
    }

    private Integer 새로운_예약_시간_생성을_요청하고(
            ReservationTimeCreateRequest request
    ) throws JsonProcessingException {
        return 예약_시간_생성을_요청하고(request);
    }

    private Integer 특정_날짜와_테마에_예약_생성을_요청하고(
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        ReservationCreateRequest request = new ReservationCreateRequest(
                "brown",
                date,
                reservationTimeId.longValue(),
                themeId.longValue());

        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("guestName", equalTo(request.guestName()))
                .body("date", equalTo(request.date().toString()))
                .body("time.id", equalTo(reservationTimeId))
                .body("theme.id", equalTo(themeId))
                .extract().path("id");
    }

    private ExtractableResponse<Response> 특정_날짜와_테마의_예약_가능_시간_조회를_요청하면(
            LocalDate date,
            Integer themeId
    ) {
        return given().log().all()
                .queryParam("date", date.toString())
                .queryParam("themeId", themeId)
                .when()
                .get("/times/availability")
                .then().log().all()
                .extract();
    }

    private void 예약된_시간은_예약_불가로_응답받는다(
            ExtractableResponse<Response> response,
            Integer reservationTimeId
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath()
                .getBoolean("availableTimes.find { it.id == " + reservationTimeId + " }.isAvailable"))
                .isFalse();
    }

    private void 예약되지_않은_시간은_예약_가능으로_응답받는다(
            ExtractableResponse<Response> response,
            Integer reservationTimeId
    ) {
        assertThat(response.jsonPath()
                .getBoolean("availableTimes.find { it.id == " + reservationTimeId + " }.isAvailable"))
                .isTrue();
    }
}
