package roomescape.acceptance_test.step;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.acceptance_test.util.RequestUtil.delete;
import static roomescape.acceptance_test.util.RequestUtil.get;
import static roomescape.acceptance_test.util.RequestUtil.patch;
import static roomescape.acceptance_test.util.RequestUtil.post;
import static roomescape.common.auth.UserArgumentResolver.GUEST_NAME_HEADER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservation.controller.dto.ReservationEditRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.test_config.MutableClock;
import roomescape.theme.controller.dto.ThemeCreateRequest;

public final class ReservationAcceptanceSteps {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final LocalDate 예약일 = LocalDate.of(2026, 10, 14);

    private ReservationAcceptanceSteps() {
    }

    public static Integer 테마_생성을_요청하고(
            ThemeCreateRequest request
    ) {
        ExtractableResponse<Response> response = post(OBJECT_MAPPER, "/admin/themes", request);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("name")).isEqualTo(request.name());
        assertThat(response.jsonPath().getString("description")).isEqualTo(request.description());
        assertThat(response.jsonPath().getString("thumbnail")).isEqualTo(request.thumbnail());
        return response.path("id");
    }

    public static Integer 예약_시간_생성을_요청하고(
            ReservationTimeCreateRequest request
    ) {
        ExtractableResponse<Response> response = post(OBJECT_MAPPER, "/admin/times", request);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("startAt")).isEqualTo(request.startAt().toString());
        return response.path("id");
    }

    public static Integer 변경할_예약_시간_생성을_요청하고(
            ReservationTimeCreateRequest request
    ) {
        return 예약_시간_생성을_요청하고(request);
    }

    public static ReservationInfo 예약_생성을_요청하고(
            String guestName,
            LocalDate date,
            LocalTime startAt
    ) {
        Integer reservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(startAt));
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        return 예약_생성을_요청하고(guestName, date, reservationTimeId, themeId);
    }

    public static ReservationInfo 예약_생성을_요청하고(
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) {
        return 예약_생성을_요청하고(new ReservationCreateRequest(
                guestName,
                date,
                reservationTimeId.longValue(),
                themeId.longValue()
        ));
    }

    public static ReservationInfo 예약_생성을_요청하고(
            ReservationCreateRequest request
    ) {
        ExtractableResponse<Response> response = post(OBJECT_MAPPER, "/reservations", request);
        Integer reservationId = response.path("id");

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(reservationId).isNotNull();
        assertThat(response.jsonPath().getString("guestName")).isEqualTo(request.guestName());
        assertThat(response.jsonPath().getString("date")).isEqualTo(request.date().toString());
        assertThat(response.jsonPath().getInt("time.id")).isEqualTo(request.timeId().intValue());
        assertThat(response.jsonPath().getInt("theme.id")).isEqualTo(request.themeId().intValue());

        return new ReservationInfo(reservationId, request);
    }

    public static ReservationInfo 특정_사용자_이름으로_예약_생성을_요청하고(
            String guestName
    ) {
        return 예약_생성을_요청하고(guestName, 예약일, LocalTime.of(10, 30));
    }

    public static ReservationInfo 특정_사용자_이름으로_예약_생성을_요청하고(
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) {
        return 예약_생성을_요청하고(guestName, date, reservationTimeId, themeId);
    }

    public static ReservationInfo 다른_사용자_이름으로_새로운_예약_생성을_요청하고(
            String guestName,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) {
        return 예약_생성을_요청하고(guestName, date, reservationTimeId, themeId);
    }

    public static ExtractableResponse<Response> 관리자_예약_목록_조회를_요청하면() {
        return get("/admin/reservations");
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
        return delete("/admin/reservations/{id}", Map.of("id", reservationId));
    }

    public static void 생성한_예약_삭제가_성공한다(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(204);
    }

    public static ExtractableResponse<Response> 내_예약_목록_조회를_요청하면(String guestName) {
        return get("/reservations/me", Map.of(), Map.of(GUEST_NAME_HEADER, guestName));
    }

    public static void 특정_사용자의_예약이_포함된_예약_목록을_응답받는다(
            ExtractableResponse<Response> response,
            ReservationInfo reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("reservations.id", Integer.class)).contains(reservation.id());
        assertThat(response.jsonPath().getList("reservations.guestName", String.class)).contains(
                reservation.guestName());
    }

    public static ExtractableResponse<Response> 예약_날짜와_시간_수정을_요청하면(
            ReservationInfo reservation,
            ReservationEditRequest request
    ) {
        return patch(
                OBJECT_MAPPER,
                "/reservations/{id}",
                request,
                Map.of("id", reservation.id()),
                Map.of(GUEST_NAME_HEADER, reservation.guestName()));
    }

    public static void 예약_수정이_성공한다(
            ExtractableResponse<Response> response,
            ReservationInfo reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(204);
    }

    public static void 현재_시간이_예약_시작_이후가_되고(MutableClock mutableClock) {
        mutableClock.setFixed(LocalDateTime.of(2026, 10, 14, 10, 31));
    }

    public static void 현재_시간이_변경하려는_예약_날짜와_시간_이후가_되고(MutableClock mutableClock) {
        mutableClock.setFixed(LocalDateTime.of(2026, 10, 10, 12, 0));
    }

    public static ExtractableResponse<Response> 지난_날짜와_시간으로_예약_수정을_요청하면(
            ReservationInfo reservation,
            ReservationEditRequest request
    ) {
        return 예약_날짜와_시간_수정을_요청하면(reservation, request);
    }

    public static ExtractableResponse<Response> 다른_사용자의_이름으로_예약_수정을_요청하면(
            ReservationInfo myReservation,
            ReservationInfo otherReservation
    ) {
        ReservationEditRequest request = new ReservationEditRequest(
                otherReservation.date(),
                otherReservation.timeId()
        );

        return patch(
                OBJECT_MAPPER,
                "/reservations/{id}",
                request,
                Map.of("id", myReservation.id()),
                Map.of(GUEST_NAME_HEADER, otherReservation.guestName()));
    }

    public static void 예약_수정_실패_응답을_받는다(ExtractableResponse<Response> response, int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    public static ExtractableResponse<Response> 내_예약_삭제를_요청하면(ReservationInfo reservation) {
        return delete(
                "/reservations/{id}",
                Map.of("id", reservation.id()),
                Map.of(GUEST_NAME_HEADER, reservation.guestName()));
    }

    public static void 내_예약_삭제가_성공한다(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(204);
    }

    public static void 내_예약_목록_조회_시_삭제한_예약은_응답받지_않는다(
            ExtractableResponse<Response> response,
            ReservationInfo reservation
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("reservations.status", String.class)).contains("CANCELED");
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
