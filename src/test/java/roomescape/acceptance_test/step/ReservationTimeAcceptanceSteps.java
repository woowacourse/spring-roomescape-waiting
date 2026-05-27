package roomescape.acceptance_test.step;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.acceptance_test.util.RequestUtil.get;
import static roomescape.acceptance_test.util.RequestUtil.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.Map;
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.theme.controller.dto.ThemeCreateRequest;

public final class ReservationTimeAcceptanceSteps {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ReservationTimeAcceptanceSteps() {
    }

    public static Integer 예약_시간_생성을_요청하고(
            ReservationTimeCreateRequest request
    ) {
        ExtractableResponse<Response> response = post(OBJECT_MAPPER, "/admin/times", request);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("startAt")).isEqualTo(request.startAt().toString());
        return response.path("id");
    }

    public static ExtractableResponse<Response> 예약_시간_목록_조회를_요청하면() {
        return get("/times");
    }

    public static void 생성한_예약_시간이_포함된_예약_시간_목록을_응답받는다(
            ExtractableResponse<Response> response,
            Integer reservationTimeId,
            ReservationTimeCreateRequest request
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("times.id", Integer.class)).contains(reservationTimeId);
        assertThat(response.jsonPath().getList("times.startAt", String.class)).contains(request.startAt().toString());
    }

    public static ExtractableResponse<Response> 같은_예약_시간_생성을_다시_요청하면(
            ReservationTimeCreateRequest request
    ) {
        return post(OBJECT_MAPPER, "/admin/times", request);
    }

    public static void 중복_예약_시간_생성은_실패한다(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(409);
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

    public static Integer 새로운_예약_시간_생성을_요청하고(
            ReservationTimeCreateRequest request
    ) {
        return 예약_시간_생성을_요청하고(request);
    }

    public static Integer 특정_날짜와_테마에_예약_생성을_요청하고(
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) {
        ReservationCreateRequest request = new ReservationCreateRequest(
                "brown",
                date,
                reservationTimeId.longValue(),
                themeId.longValue());

        ExtractableResponse<Response> response = post(OBJECT_MAPPER, "/reservations", request);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("guestName")).isEqualTo(request.guestName());
        assertThat(response.jsonPath().getString("date")).isEqualTo(request.date().toString());
        assertThat(response.jsonPath().getInt("time.id")).isEqualTo(reservationTimeId);
        assertThat(response.jsonPath().getInt("theme.id")).isEqualTo(themeId);
        return response.path("id");
    }

    public static ExtractableResponse<Response> 특정_날짜와_테마의_예약_가능_시간_조회를_요청하면(
            LocalDate date,
            Integer themeId
    ) {
        return get("/times/availability", Map.of("date", date.toString(), "themeId", themeId));
    }

    public static void 예약된_시간은_예약_불가로_응답받는다(
            ExtractableResponse<Response> response,
            Integer reservationTimeId
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath()
                .getBoolean("availableTimes.find { it.id == " + reservationTimeId + " }.isAvailable"))
                .isFalse();
    }

    public static void 예약되지_않은_시간은_예약_가능으로_응답받는다(
            ExtractableResponse<Response> response,
            Integer reservationTimeId
    ) {
        assertThat(response.jsonPath()
                .getBoolean("availableTimes.find { it.id == " + reservationTimeId + " }.isAvailable"))
                .isTrue();
    }
}
