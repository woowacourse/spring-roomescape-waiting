package roomescape.acceptance_test.step;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.theme.controller.dto.ThemeCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static roomescape.acceptance_test.util.RequestUtil.delete;
import static roomescape.acceptance_test.util.RequestUtil.get;
import static roomescape.acceptance_test.util.RequestUtil.post;
import static org.assertj.core.api.Assertions.assertThat;

public final class ThemeAcceptanceSteps {

    private ThemeAcceptanceSteps() {
    }

    public static Integer 테마_생성을_요청하고(
            ObjectMapper objectMapper,
            ThemeCreateRequest request
    ) throws JsonProcessingException {
        ExtractableResponse<Response> response = post(objectMapper, "/admin/themes", request);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("name")).isEqualTo(request.name());
        assertThat(response.jsonPath().getString("description")).isEqualTo(request.description());
        assertThat(response.jsonPath().getString("thumbnail")).isEqualTo(request.thumbnail());
        return response.path("id");
    }

    public static ExtractableResponse<Response> 테마_목록_조회를_요청하면() {
        return get("/themes");
    }

    public static void 생성한_테마가_포함된_테마_목록을_응답받는다(
            ExtractableResponse<Response> response,
            Integer themeId,
            ThemeCreateRequest request
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("themes.id", Integer.class)).contains(themeId);
        assertThat(response.jsonPath().getList("themes.name", String.class)).contains(request.name());
        assertThat(response.jsonPath().getList("themes.description", String.class)).contains(request.description());
        assertThat(response.jsonPath().getList("themes.thumbnail", String.class)).contains(request.thumbnail());
    }

    public static ExtractableResponse<Response> 생성한_테마_삭제를_요청하면(Integer themeId) {
        return delete("/admin/themes/{id}", Map.of("id", themeId));
    }

    public static void 테마_삭제가_성공한다(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(204);
    }

    public static void 테마_목록_조회_시_삭제한_테마는_응답받지_않는다(
            ExtractableResponse<Response> response,
            Integer themeId
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("themes.id", Integer.class)).doesNotContain(themeId);
    }

    public static PopularThemeIds 여러_테마_생성을_요청하고(
            ObjectMapper objectMapper
    ) throws JsonProcessingException {
        return new PopularThemeIds(
                테마_생성을_요청하고(objectMapper, new ThemeCreateRequest("인기 테마1", "설명", "섬네일")),
                테마_생성을_요청하고(objectMapper, new ThemeCreateRequest("인기 테마2", "설명", "섬네일")),
                테마_생성을_요청하고(objectMapper, new ThemeCreateRequest("인기 테마3", "설명", "섬네일")),
                테마_생성을_요청하고(objectMapper, new ThemeCreateRequest("기간 밖 테마", "설명", "섬네일"))
        );
    }

    public static List<Integer> 예약_시간_생성을_요청하고(
            ObjectMapper objectMapper
    ) throws JsonProcessingException {
        List<Integer> reservationTimeIds = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            reservationTimeIds.add(예약_시간_생성_요청(
                    objectMapper,
                    new ReservationTimeCreateRequest(LocalTime.of(i, 30))
            ));
        }
        return reservationTimeIds;
    }

    public static void 기간_내_테마별_예약_수가_다르게_예약_생성을_요청하고(
            ObjectMapper objectMapper,
            PopularThemeIds themeIds,
            List<Integer> reservationTimeIds,
            LocalDate date
    ) throws JsonProcessingException {
        예약_생성을_요청한다(objectMapper, "brown", date, reservationTimeIds, themeIds.firstThemeId(), 13);
        예약_생성을_요청한다(objectMapper, "pobi", date, reservationTimeIds, themeIds.secondThemeId(), 12);
        예약_생성을_요청한다(objectMapper, "joy", date, reservationTimeIds, themeIds.thirdThemeId(), 11);
    }

    public static void 기간_밖_예약_생성을_요청하고(
            ObjectMapper objectMapper,
            PopularThemeIds themeIds,
            List<Integer> reservationTimeIds,
            LocalDate outOfRangeDate
    ) throws JsonProcessingException {
        예약_생성_요청(
                objectMapper,
                "outOfRange",
                outOfRangeDate,
                reservationTimeIds.get(0),
                themeIds.outOfRangeThemeId()
        );
    }

    public static ExtractableResponse<Response> 인기_테마_목록_조회를_요청하면() {
        return get("/themes/popularity", Map.of("days", 7, "size", 3));
    }

    public static void 기간_내_예약_수가_많은_순서대로_인기_테마_목록을_응답받는다(
            ExtractableResponse<Response> response,
            PopularThemeIds themeIds
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("themes.id", Integer.class))
                .containsExactly(themeIds.firstThemeId(), themeIds.secondThemeId(), themeIds.thirdThemeId());
    }

    public static void 기간_밖_예약만_있는_테마는_인기_테마_목록에_포함되지_않는다(
            ExtractableResponse<Response> response,
            PopularThemeIds themeIds
    ) {
        assertThat(response.jsonPath().getList("themes.id", Integer.class))
                .doesNotContain(themeIds.outOfRangeThemeId());
    }

    private static Integer 예약_시간_생성_요청(
            ObjectMapper objectMapper,
            ReservationTimeCreateRequest request
    ) throws JsonProcessingException {
        ExtractableResponse<Response> response = post(objectMapper, "/admin/times", request);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("startAt")).isEqualTo(request.startAt().toString());
        return response.path("id");
    }

    private static void 예약_생성을_요청한다(
            ObjectMapper objectMapper,
            String name,
            LocalDate date,
            List<Integer> reservationTimeIds,
            Integer themeId,
            int count
    ) throws JsonProcessingException {
        for (int i = 0; i < count; i++) {
            예약_생성_요청(objectMapper, name + i, date, reservationTimeIds.get(i), themeId);
        }
    }

    private static Integer 예약_생성_요청(
            ObjectMapper objectMapper,
            String name,
            LocalDate date,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        ReservationCreateRequest request = new ReservationCreateRequest(
                name,
                date,
                reservationTimeId.longValue(),
                themeId.longValue());

        ExtractableResponse<Response> response = post(objectMapper, "/reservations", request);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("guestName")).isEqualTo(request.guestName());
        assertThat(response.jsonPath().getString("date")).isEqualTo(request.date().toString());
        assertThat(response.jsonPath().getInt("time.id")).isEqualTo(reservationTimeId);
        assertThat(response.jsonPath().getInt("theme.id")).isEqualTo(themeId);
        return response.path("id");
    }

    public record PopularThemeIds(
            Integer firstThemeId,
            Integer secondThemeId,
            Integer thirdThemeId,
            Integer outOfRangeThemeId
    ) {
    }
}
