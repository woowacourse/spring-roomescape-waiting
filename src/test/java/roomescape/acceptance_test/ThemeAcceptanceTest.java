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
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ThemeAcceptanceTest extends AcceptanceTestSupport {

    private static final LocalDate 인기_테마_조회_기준일 = LocalDate.of(2026, 5, 11);
    private static final LocalDate 기간_내_예약일 = 인기_테마_조회_기준일.minusDays(1);
    private static final LocalDate 기간_밖_예약일 = 인기_테마_조회_기준일.plusDays(1);

    @Test
    @DisplayName("테마 목록 조회")
    public void scenario1() throws JsonProcessingException {
        // given
        ThemeCreateRequest request = new ThemeCreateRequest("brown", "설명", "섬네일");
        Integer themeId = 테마_생성을_요청하고(request);

        // when
        ExtractableResponse<Response> response = 테마_목록_조회를_요청하면();

        // then
        생성한_테마가_포함된_테마_목록을_응답받는다(response, themeId, request);
    }

    @Test
    @DisplayName("테마 삭제")
    public void scenario2() throws JsonProcessingException {
        // given
        ThemeCreateRequest request = new ThemeCreateRequest("테마1", "설명", "섬네일");
        Integer themeId = 테마_생성을_요청하고(request);

        // when
        ExtractableResponse<Response> deleteResponse = 생성한_테마_삭제를_요청하면(themeId);

        // then
        테마_삭제가_성공한다(deleteResponse);
        ExtractableResponse<Response> findResponse = 테마_목록_조회를_요청하면();
        테마_목록_조회_시_삭제한_테마는_응답받지_않는다(findResponse, themeId);
    }

    @Test
    @DisplayName("인기 테마 목록 조회")
    public void scenario3() throws JsonProcessingException {
        // given
        mutableClock.setFixed(LocalDate.of(2026, 4, 1));

        PopularThemeIds themeIds = 여러_테마_생성을_요청하고();
        List<Integer> reservationTimeIds = 예약_시간_생성을_요청하고();
        기간_내_테마별_예약_수가_다르게_예약_생성을_요청하고(themeIds, reservationTimeIds);
        기간_밖_예약_생성을_요청하고(themeIds, reservationTimeIds);

        mutableClock.setFixed(인기_테마_조회_기준일);

        // when
        ExtractableResponse<Response> response = 인기_테마_목록_조회를_요청하면();

        // then
        기간_내_예약_수가_많은_순서대로_인기_테마_목록을_응답받는다(response, themeIds);
        기간_밖_예약만_있는_테마는_인기_테마_목록에_포함되지_않는다(response, themeIds);
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

    private ExtractableResponse<Response> 테마_목록_조회를_요청하면() {
        return given().log().all()
                .when()
                .get("/themes")
                .then().log().all()
                .extract();
    }

    private void 생성한_테마가_포함된_테마_목록을_응답받는다(
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

    private ExtractableResponse<Response> 생성한_테마_삭제를_요청하면(Integer themeId) {
        return given().log().all()
                .pathParam("id", themeId)
                .when()
                .delete("/admin/themes/{id}")
                .then().log().all()
                .extract();
    }

    private void 테마_삭제가_성공한다(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(204);
    }

    private void 테마_목록_조회_시_삭제한_테마는_응답받지_않는다(
            ExtractableResponse<Response> response,
            Integer themeId
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("themes.id", Integer.class)).doesNotContain(themeId);
    }

    private PopularThemeIds 여러_테마_생성을_요청하고() throws JsonProcessingException {
        return new PopularThemeIds(
                테마_생성을_요청하고(new ThemeCreateRequest("인기 테마1", "설명", "섬네일")),
                테마_생성을_요청하고(new ThemeCreateRequest("인기 테마2", "설명", "섬네일")),
                테마_생성을_요청하고(new ThemeCreateRequest("인기 테마3", "설명", "섬네일")),
                테마_생성을_요청하고(new ThemeCreateRequest("기간 밖 테마", "설명", "섬네일"))
        );
    }

    private List<Integer> 예약_시간_생성을_요청하고() throws JsonProcessingException {
        List<Integer> reservationTimeIds = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            reservationTimeIds.add(예약_시간_생성_요청(new ReservationTimeCreateRequest(LocalTime.of(i, 30))));
        }
        return reservationTimeIds;
    }

    private void 기간_내_테마별_예약_수가_다르게_예약_생성을_요청하고(
            PopularThemeIds themeIds,
            List<Integer> reservationTimeIds
    ) throws JsonProcessingException {
        예약_생성을_요청한다("brown", 기간_내_예약일, reservationTimeIds, themeIds.firstThemeId(), 13);
        예약_생성을_요청한다("pobi", 기간_내_예약일, reservationTimeIds, themeIds.secondThemeId(), 12);
        예약_생성을_요청한다("joy", 기간_내_예약일, reservationTimeIds, themeIds.thirdThemeId(), 11);
    }

    private void 기간_밖_예약_생성을_요청하고(
            PopularThemeIds themeIds,
            List<Integer> reservationTimeIds
    ) throws JsonProcessingException {
        예약_생성_요청("outOfRange", 기간_밖_예약일, reservationTimeIds.get(0), themeIds.outOfRangeThemeId());
    }

    private ExtractableResponse<Response> 인기_테마_목록_조회를_요청하면() {
        return given().log().all()
                .queryParam("days", 7)
                .queryParam("size", 3)
                .when()
                .get("/themes/popularity")
                .then().log().all()
                .extract();
    }

    private void 기간_내_예약_수가_많은_순서대로_인기_테마_목록을_응답받는다(
            ExtractableResponse<Response> response,
            PopularThemeIds themeIds
    ) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("themes.id", Integer.class))
                .containsExactly(themeIds.firstThemeId(), themeIds.secondThemeId(), themeIds.thirdThemeId());
    }

    private void 기간_밖_예약만_있는_테마는_인기_테마_목록에_포함되지_않는다(
            ExtractableResponse<Response> response,
            PopularThemeIds themeIds
    ) {
        assertThat(response.jsonPath().getList("themes.id", Integer.class))
                .doesNotContain(themeIds.outOfRangeThemeId());
    }

    private Integer 예약_시간_생성_요청(ReservationTimeCreateRequest request) throws JsonProcessingException {
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

    private void 예약_생성을_요청한다(
            String name,
            LocalDate date,
            List<Integer> reservationTimeIds,
            Integer themeId,
            int count
    ) throws JsonProcessingException {
        for (int i = 0; i < count; i++) {
            예약_생성_요청(name + i, date, reservationTimeIds.get(i), themeId);
        }
    }

    private Integer 예약_생성_요청(
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

    private record PopularThemeIds(
            Integer firstThemeId,
            Integer secondThemeId,
            Integer thirdThemeId,
            Integer outOfRangeThemeId
    ) {
    }
}
