package roomescape.acceptance_test;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservation.controller.dto.ReservationEditRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.theme.controller.dto.ThemeCreateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static roomescape.common.auth.UserArgumentResolver.GUEST_NAME_HEADER;

public class ReservationAcceptanceTest extends AcceptanceTestSupport {

    private static final LocalDate 현재_날짜 = LocalDate.of(2026, 5, 12);
    private static final LocalDate 예약일 = LocalDate.of(2026, 10, 14);
    private static final LocalDate 변경_예약일 = LocalDate.of(2026, 10, 15);

    @Test
    @DisplayName("관리자 예약 목록 조회")
    public void scenario1() throws JsonProcessingException {
        mutableClock.setFixed(현재_날짜);

        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        ReservationFixture reservation = 예약_생성을_요청하고("brown", 예약일, reservationTimeId, themeId);

        // when
        ExtractableResponse<Response> response = 관리자_예약_목록_조회를_요청하면();

        // then
        생성한_예약이_포함된_관리자_예약_목록을_응답받는다(response, reservation);
    }

    @Test
    @DisplayName("관리자 예약 삭제")
    public void scenario2() throws JsonProcessingException {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationFixture reservation = 예약_생성을_요청하고("brown", 예약일, LocalTime.of(10, 30));

        // when
        ExtractableResponse<Response> deleteResponse = 생성한_예약_삭제를_요청하면(reservation.id());

        // then
        생성한_예약_삭제가_성공한다(deleteResponse);
        관리자_예약_목록_조회_시_삭제한_예약은_응답받지_않는다(관리자_예약_목록_조회를_요청하면(), reservation);
    }

    @Test
    @DisplayName("내 예약 목록 조회")
    public void scenario3() throws JsonProcessingException {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationFixture reservation = 특정_사용자_이름으로_예약_생성을_요청하고("brown");

        // when
        ExtractableResponse<Response> response = 내_예약_목록_조회를_요청하면(reservation.guestName());

        // then
        특정_사용자의_예약이_포함된_예약_목록을_응답받는다(response, reservation);
    }

    @Test
    @DisplayName("예약 수정")
    public void scenario4() throws JsonProcessingException {
        mutableClock.setFixed(현재_날짜);

        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer editedReservationTimeId = 변경할_예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        ReservationFixture reservation = 예약_생성을_요청하고("brown", 예약일, reservationTimeId, themeId);
        ReservationEditRequest editRequest = new ReservationEditRequest(변경_예약일, editedReservationTimeId.longValue());

        // when
        ExtractableResponse<Response> response = 예약_날짜와_시간_수정을_요청하면(reservation, editRequest);

        // then
        예약_수정이_성공한다(response, reservation);
        예약_날짜와_시간은_요청한_값으로_응답받는다(response, editRequest);
        예약_테마는_기존_테마로_응답받는다(response, reservation);
    }

    @Test
    @DisplayName("같은 테마의 중복 예약으로 예약 수정 실패")
    public void scenario5() throws JsonProcessingException {
        mutableClock.setFixed(현재_날짜);

        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer editedReservationTimeId = 변경할_예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        ReservationFixture reservation = 같은_테마로_예약_생성을_요청하고("brown", 예약일, reservationTimeId, themeId);
        ReservationFixture targetReservation = 같은_테마로_새로운_예약_생성을_요청하고(
                "pobi",
                변경_예약일,
                editedReservationTimeId,
                themeId
        );

        // when
        ExtractableResponse<Response> response = 새로운_예약을_기존_예약의_날짜와_시간으로_수정_요청하면(
                targetReservation,
                reservation
        );

        // then
        예약_수정_실패_응답을_받는다(response, 409);
    }

    @Test
    @DisplayName("이미 시작된 예약 수정 실패")
    public void scenario6() throws JsonProcessingException {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationFixture reservation = 예약_생성을_요청하고("brown", 예약일, LocalTime.of(10, 30));
        Integer editedReservationTimeId = 변경할_예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        현재_시간이_예약_시작_이후가_되고();
        ReservationEditRequest editRequest = new ReservationEditRequest(변경_예약일, editedReservationTimeId.longValue());

        // when
        ExtractableResponse<Response> response = 예약_날짜와_시간_수정을_요청하면(reservation, editRequest);

        // then
        예약_수정_실패_응답을_받는다(response, 422);
    }

    @Test
    @DisplayName("지난 날짜와 시간으로 예약 수정 실패")
    public void scenario7() throws JsonProcessingException {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationFixture reservation = 예약_생성을_요청하고("brown", 예약일, LocalTime.of(10, 30));
        Integer editedReservationTimeId = 변경할_예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        현재_시간이_변경하려는_예약_날짜와_시간_이후가_되고();
        ReservationEditRequest editRequest = new ReservationEditRequest(
                LocalDate.of(2026, 10, 10),
                editedReservationTimeId.longValue()
        );

        // when
        ExtractableResponse<Response> response = 지난_날짜와_시간으로_예약_수정을_요청하면(reservation, editRequest);

        // then
        예약_수정_실패_응답을_받는다(response, 422);
    }

    @Test
    @DisplayName("다른 사용자의 예약 수정 실패")
    public void scenario8() throws JsonProcessingException {
        mutableClock.setFixed(현재_날짜);

        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer editedReservationTimeId = 변경할_예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        ReservationFixture otherReservation = 특정_사용자_이름으로_예약_생성을_요청하고(
                "brown",
                예약일,
                reservationTimeId,
                themeId
        );
        ReservationFixture myReservation = 다른_사용자_이름으로_새로운_예약_생성을_요청하고(
                "pobi",
                변경_예약일,
                editedReservationTimeId,
                themeId
        );

        // when
        ExtractableResponse<Response> response = 다른_사용자의_이름으로_예약_수정을_요청하면(
                myReservation,
                otherReservation
        );

        // then
        예약_수정_실패_응답을_받는다(response, 403);
    }

    @Test
    @DisplayName("내 예약 삭제")
    public void scenario9() throws JsonProcessingException {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationFixture reservation = 특정_사용자_이름으로_예약_생성을_요청하고("brown");

        // when
        ExtractableResponse<Response> deleteResponse = 내_예약_삭제를_요청하면(reservation);

        // then
        내_예약_삭제가_성공한다(deleteResponse);
        내_예약_목록_조회_시_삭제한_예약은_응답받지_않는다(내_예약_목록_조회를_요청하면(reservation.guestName()), reservation);
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

    private Integer 변경할_예약_시간_생성을_요청하고(
            ReservationTimeCreateRequest request
    ) throws JsonProcessingException {
        return 예약_시간_생성을_요청하고(request);
    }

    private ReservationFixture 예약_생성을_요청하고(
            String guestName,
            LocalDate date,
            LocalTime startAt
    ) throws JsonProcessingException {
        Integer reservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(startAt));
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        return 예약_생성을_요청하고(guestName, date, reservationTimeId, themeId);
    }

    private ReservationFixture 예약_생성을_요청하고(
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(new ReservationCreateRequest(
                guestName,
                date,
                reservationTimeId.longValue(),
                themeId.longValue()
        ));
    }

    private ReservationFixture 예약_생성을_요청하고(
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

        return new ReservationFixture(reservationId, request);
    }

    private ReservationFixture 특정_사용자_이름으로_예약_생성을_요청하고(
            String guestName
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(guestName, 예약일, LocalTime.of(10, 30));
    }

    private ReservationFixture 특정_사용자_이름으로_예약_생성을_요청하고(
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(guestName, date, reservationTimeId, themeId);
    }

    private ReservationFixture 같은_테마로_예약_생성을_요청하고(
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(guestName, date, reservationTimeId, themeId);
    }

    private ReservationFixture 같은_테마로_새로운_예약_생성을_요청하고(
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(guestName, date, reservationTimeId, themeId);
    }

    private ReservationFixture 다른_사용자_이름으로_새로운_예약_생성을_요청하고(
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return 예약_생성을_요청하고(guestName, date, reservationTimeId, themeId);
    }

    private ExtractableResponse<Response> 관리자_예약_목록_조회를_요청하면() {
        return given().log().all()
                .when()
                .get("/admin/reservations")
                .then().log().all()
                .extract();
    }

    private void 생성한_예약이_포함된_관리자_예약_목록을_응답받는다(
            ExtractableResponse<Response> response,
            ReservationFixture reservation
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

    private ExtractableResponse<Response> 생성한_예약_삭제를_요청하면(Integer reservationId) {
        return given().log().all()
                .pathParam("id", reservationId)
                .when()
                .delete("/admin/reservations/{id}")
                .then().log().all()
                .extract();
    }

    private void 생성한_예약_삭제가_성공한다(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(204);
    }

    private void 관리자_예약_목록_조회_시_삭제한_예약은_응답받지_않는다(
            ExtractableResponse<Response> response,
            ReservationFixture reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("reservations.id", Integer.class)).doesNotContain(reservation.id());
    }

    private ExtractableResponse<Response> 내_예약_목록_조회를_요청하면(String guestName) {
        return given().log().all()
                .header(GUEST_NAME_HEADER, guestName)
                .when()
                .get("/reservations/me")
                .then().log().all()
                .extract();
    }

    private void 특정_사용자의_예약이_포함된_예약_목록을_응답받는다(
            ExtractableResponse<Response> response,
            ReservationFixture reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("reservations.id", Integer.class)).contains(reservation.id());
        assertThat(response.jsonPath().getList("reservations.guestName", String.class)).contains(reservation.guestName());
    }

    private ExtractableResponse<Response> 예약_날짜와_시간_수정을_요청하면(
            ReservationFixture reservation,
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

    private void 예약_수정이_성공한다(
            ExtractableResponse<Response> response,
            ReservationFixture reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("id")).isEqualTo(reservation.id());
        assertThat(response.jsonPath().getString("guestName")).isEqualTo(reservation.guestName());
    }

    private void 예약_날짜와_시간은_요청한_값으로_응답받는다(
            ExtractableResponse<Response> response,
            ReservationEditRequest request
    ) {
        assertThat(response.jsonPath().getString("date")).isEqualTo(request.date().toString());
        assertThat(response.jsonPath().getInt("time.id")).isEqualTo(request.timeId().intValue());
    }

    private void 예약_테마는_기존_테마로_응답받는다(
            ExtractableResponse<Response> response,
            ReservationFixture reservation
    ) {
        assertThat(response.jsonPath().getInt("theme.id")).isEqualTo(reservation.themeId().intValue());
    }

    private ExtractableResponse<Response> 새로운_예약을_기존_예약의_날짜와_시간으로_수정_요청하면(
            ReservationFixture targetReservation,
            ReservationFixture reservation
    ) throws JsonProcessingException {
        ReservationEditRequest request = new ReservationEditRequest(
                reservation.date(),
                reservation.timeId()
        );
        return 예약_날짜와_시간_수정을_요청하면(targetReservation, request);
    }

    private void 현재_시간이_예약_시작_이후가_되고() {
        mutableClock.setFixed(LocalDateTime.of(2026, 10, 14, 10, 31));
    }

    private void 현재_시간이_변경하려는_예약_날짜와_시간_이후가_되고() {
        mutableClock.setFixed(LocalDateTime.of(2026, 10, 10, 12, 0));
    }

    private ExtractableResponse<Response> 지난_날짜와_시간으로_예약_수정을_요청하면(
            ReservationFixture reservation,
            ReservationEditRequest request
    ) throws JsonProcessingException {
        return 예약_날짜와_시간_수정을_요청하면(reservation, request);
    }

    private ExtractableResponse<Response> 다른_사용자의_이름으로_예약_수정을_요청하면(
            ReservationFixture myReservation,
            ReservationFixture otherReservation
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

    private void 예약_수정_실패_응답을_받는다(ExtractableResponse<Response> response, int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    private ExtractableResponse<Response> 내_예약_삭제를_요청하면(ReservationFixture reservation) {
        return given().log().all()
                .pathParam("id", reservation.id())
                .header(GUEST_NAME_HEADER, reservation.guestName())
                .when()
                .delete("/reservations/{id}")
                .then().log().all()
                .extract();
    }

    private void 내_예약_삭제가_성공한다(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(204);
    }

    private void 내_예약_목록_조회_시_삭제한_예약은_응답받지_않는다(
            ExtractableResponse<Response> response,
            ReservationFixture reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("reservations.id", Integer.class)).doesNotContain(reservation.id());
    }

    private record ReservationFixture(
            Integer id,
            ReservationCreateRequest request
    ) {

        private String guestName() {
            return request.guestName();
        }

        private LocalDate date() {
            return request.date();
        }

        private Long timeId() {
            return request.timeId();
        }

        private Long themeId() {
            return request.themeId();
        }
    }
}
