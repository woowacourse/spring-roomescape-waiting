package roomescape.acceptance_test.step;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservation.controller.dto.ReservationEditRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.test_config.MutableClock;
import roomescape.theme.controller.dto.ThemeCreateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static roomescape.common.auth.UserArgumentResolver.GUEST_NAME_HEADER;

public final class ReservationAcceptanceSteps {

    private static final LocalDate 예약일 = LocalDate.of(2026, 10, 14);

    private ReservationAcceptanceSteps() {
    }

    public static Integer 테마_생성을_요청하고(
            ObjectMapper objectMapper,
            ThemeCreateRequest request
    ) throws JsonProcessingException {
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

    public static Integer 예약_시간_생성을_요청하고(
            ObjectMapper objectMapper,
            ReservationTimeCreateRequest request
    ) throws JsonProcessingException {
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

    public static Integer 변경할_예약_시간_생성을_요청하고(
            ObjectMapper objectMapper,
            ReservationTimeCreateRequest request
    ) throws JsonProcessingException {
        return 예약_시간_생성을_요청하고(objectMapper, request);
    }

    public static ReservationInfo 예약_생성을_요청하고(
            ObjectMapper objectMapper,
            String guestName,
            LocalDate date,
            LocalTime startAt
    ) throws JsonProcessingException {
        Integer reservationTimeId = 예약_시간_생성을_요청하고(objectMapper, new ReservationTimeCreateRequest(startAt));
        Integer themeId = 테마_생성을_요청하고(objectMapper, new ThemeCreateRequest("테마1", "설명", "섬네일"));
        return 예약_생성을_요청하고(objectMapper, guestName, date, reservationTimeId, themeId);
    }

    public static ReservationInfo 예약_생성을_요청하고(
            ObjectMapper objectMapper,
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(objectMapper, new ReservationCreateRequest(
                guestName,
                date,
                reservationTimeId.longValue(),
                themeId.longValue()
        ));
    }

    public static ReservationInfo 예약_생성을_요청하고(
            ObjectMapper objectMapper,
            ReservationCreateRequest request
    ) throws JsonProcessingException {
        Integer reservationId = given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("guestName", equalTo(request.guestName()))
                .body("date", equalTo(request.date().toString()))
                .body("time.id", equalTo(request.timeId().intValue()))
                .body("theme.id", equalTo(request.themeId().intValue()))
                .extract().path("id");

        return new ReservationInfo(reservationId, request);
    }

    public static ReservationInfo 특정_사용자_이름으로_예약_생성을_요청하고(
            ObjectMapper objectMapper,
            String guestName
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(objectMapper, guestName, 예약일, LocalTime.of(10, 30));
    }

    public static ReservationInfo 특정_사용자_이름으로_예약_생성을_요청하고(
            ObjectMapper objectMapper,
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(objectMapper, guestName, date, reservationTimeId, themeId);
    }

    public static ReservationInfo 같은_테마로_예약_생성을_요청하고(
            ObjectMapper objectMapper,
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(objectMapper, guestName, date, reservationTimeId, themeId);
    }

    public static ReservationInfo 같은_테마로_새로운_예약_생성을_요청하고(
            ObjectMapper objectMapper,
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(objectMapper, guestName, date, reservationTimeId, themeId);
    }

    public static ReservationInfo 다른_사용자_이름으로_새로운_예약_생성을_요청하고(
            ObjectMapper objectMapper,
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(objectMapper, guestName, date, reservationTimeId, themeId);
    }

    public static ExtractableResponse<Response> 관리자_예약_목록_조회를_요청하면() {
        return given().log().all()
                .when()
                .get("/admin/reservations")
                .then().log().all()
                .extract();
    }

    public static void 생성한_예약이_포함된_관리자_예약_목록을_응답받는다(
            ExtractableResponse<Response> response,
            ReservationInfo reservation
    ) {
        ReservationCreateRequest request = reservation.request();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("reservations.id", Integer.class)).contains(reservation.id());
        assertThat(response.jsonPath().getList("reservations.guestName", String.class)).contains(request.guestName());
        assertThat(response.jsonPath().getList("reservations.date", String.class)).contains(request.date().toString());
        assertThat(response.jsonPath().getList("reservations.time.id", Integer.class))
                .contains(request.timeId().intValue());
        assertThat(response.jsonPath().getList("reservations.theme.id", Integer.class))
                .contains(request.themeId().intValue());
    }

    public static ExtractableResponse<Response> 생성한_예약_삭제를_요청하면(Integer reservationId) {
        return given().log().all()
                .pathParam("id", reservationId)
                .when()
                .delete("/admin/reservations/{id}")
                .then().log().all()
                .extract();
    }

    public static void 생성한_예약_삭제가_성공한다(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(204);
    }

    public static void 관리자_예약_목록_조회_시_삭제한_예약은_응답받지_않는다(
            ExtractableResponse<Response> response,
            ReservationInfo reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("reservations.id", Integer.class)).doesNotContain(reservation.id());
    }

    public static ExtractableResponse<Response> 내_예약_목록_조회를_요청하면(String guestName) {
        return given().log().all()
                .header(GUEST_NAME_HEADER, guestName)
                .when()
                .get("/reservations/me")
                .then().log().all()
                .extract();
    }

    public static void 특정_사용자의_예약이_포함된_예약_목록을_응답받는다(
            ExtractableResponse<Response> response,
            ReservationInfo reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("reservations.id", Integer.class)).contains(reservation.id());
        assertThat(response.jsonPath().getList("reservations.guestName", String.class)).contains(reservation.guestName());
    }

    public static ExtractableResponse<Response> 예약_날짜와_시간_수정을_요청하면(
            ObjectMapper objectMapper,
            ReservationInfo reservation,
            ReservationEditRequest request
    ) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .pathParam("id", reservation.id())
                .header(GUEST_NAME_HEADER, reservation.guestName())
                .when()
                .patch("/reservations/{id}")
                .then().log().all()
                .extract();
    }

    public static void 예약_수정이_성공한다(
            ExtractableResponse<Response> response,
            ReservationInfo reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("id")).isEqualTo(reservation.id());
        assertThat(response.jsonPath().getString("guestName")).isEqualTo(reservation.guestName());
    }

    public static void 예약_날짜와_시간은_요청한_값으로_응답받는다(
            ExtractableResponse<Response> response,
            ReservationEditRequest request
    ) {
        assertThat(response.jsonPath().getString("date")).isEqualTo(request.date().toString());
        assertThat(response.jsonPath().getInt("time.id")).isEqualTo(request.timeId().intValue());
    }

    public static void 예약_테마는_기존_테마로_응답받는다(
            ExtractableResponse<Response> response,
            ReservationInfo reservation
    ) {
        assertThat(response.jsonPath().getInt("theme.id")).isEqualTo(reservation.themeId().intValue());
    }

    public static ExtractableResponse<Response> 새로운_예약을_기존_예약의_날짜와_시간으로_수정_요청하면(
            ObjectMapper objectMapper,
            ReservationInfo targetReservation,
            ReservationInfo reservation
    ) throws JsonProcessingException {
        ReservationEditRequest request = new ReservationEditRequest(
                reservation.date(),
                reservation.timeId()
        );
        return 예약_날짜와_시간_수정을_요청하면(objectMapper, targetReservation, request);
    }

    public static void 현재_시간이_예약_시작_이후가_되고(MutableClock mutableClock) {
        mutableClock.setFixed(LocalDateTime.of(2026, 10, 14, 10, 31));
    }

    public static void 현재_시간이_변경하려는_예약_날짜와_시간_이후가_되고(MutableClock mutableClock) {
        mutableClock.setFixed(LocalDateTime.of(2026, 10, 10, 12, 0));
    }

    public static ExtractableResponse<Response> 지난_날짜와_시간으로_예약_수정을_요청하면(
            ObjectMapper objectMapper,
            ReservationInfo reservation,
            ReservationEditRequest request
    ) throws JsonProcessingException {
        return 예약_날짜와_시간_수정을_요청하면(objectMapper, reservation, request);
    }

    public static ExtractableResponse<Response> 다른_사용자의_이름으로_예약_수정을_요청하면(
            ObjectMapper objectMapper,
            ReservationInfo myReservation,
            ReservationInfo otherReservation
    ) throws JsonProcessingException {
        ReservationEditRequest request = new ReservationEditRequest(
                otherReservation.date(),
                otherReservation.timeId()
        );

        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .pathParam("id", myReservation.id())
                .header(GUEST_NAME_HEADER, otherReservation.guestName())
                .when()
                .patch("/reservations/{id}")
                .then().log().all()
                .extract();
    }

    public static void 예약_수정_실패_응답을_받는다(ExtractableResponse<Response> response, int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    public static ExtractableResponse<Response> 내_예약_삭제를_요청하면(ReservationInfo reservation) {
        return given().log().all()
                .pathParam("id", reservation.id())
                .header(GUEST_NAME_HEADER, reservation.guestName())
                .when()
                .delete("/reservations/{id}")
                .then().log().all()
                .extract();
    }

    public static void 내_예약_삭제가_성공한다(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(204);
    }

    public static void 내_예약_목록_조회_시_삭제한_예약은_응답받지_않는다(
            ExtractableResponse<Response> response,
            ReservationInfo reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("reservations.id", Integer.class)).doesNotContain(reservation.id());
    }

    public record ReservationInfo(
            Integer id,
            ReservationCreateRequest request
    ) {

        public String guestName() {
            return request.guestName();
        }

        public LocalDate date() {
            return request.date();
        }

        public Long timeId() {
            return request.timeId();
        }

        public Long themeId() {
            return request.themeId();
        }
    }
}
