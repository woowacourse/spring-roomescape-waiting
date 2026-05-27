package roomescape.acceptance_test;

import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.기간_내_예약_수가_많은_순서대로_인기_테마_목록을_응답받는다;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.기간_내_테마별_예약_수가_다르게_예약_생성을_요청하고;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.기간_밖_예약_생성을_요청하고;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.기간_밖_예약만_있는_테마는_인기_테마_목록에_포함되지_않는다;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.생성한_테마_삭제를_요청하면;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.생성한_테마가_포함된_테마_목록을_응답받는다;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.여러_테마_생성을_요청하고;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.예약_시간_생성을_요청하고;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.인기_테마_목록_조회를_요청하면;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.테마_목록_조회_시_삭제한_테마는_응답받지_않는다;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.테마_목록_조회를_요청하면;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.테마_삭제가_성공한다;
import static roomescape.acceptance_test.step.ThemeAcceptanceSteps.테마_생성을_요청하고;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.acceptance_test.step.ThemeAcceptanceSteps.PopularThemeIds;
import roomescape.theme.controller.dto.ThemeCreateRequest;

public class ThemeAcceptanceTest extends AcceptanceTestSupport {

    private static final LocalDate 인기_테마_조회_기준일 = LocalDate.of(2026, 5, 11);
    private static final LocalDate 기간_내_예약일 = 인기_테마_조회_기준일.minusDays(1);
    private static final LocalDate 기간_밖_예약일 = 인기_테마_조회_기준일.plusDays(1);

    @Test
    @DisplayName("테마 목록 조회")
    public void scenario1() {
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
    public void scenario2() {
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
    public void scenario3() {
        // given
        mutableClock.setFixed(LocalDate.of(2026, 4, 1));

        PopularThemeIds themeIds = 여러_테마_생성을_요청하고();
        List<Integer> reservationTimeIds = 예약_시간_생성을_요청하고();
        기간_내_테마별_예약_수가_다르게_예약_생성을_요청하고(themeIds, reservationTimeIds, 기간_내_예약일);
        기간_밖_예약_생성을_요청하고(themeIds, reservationTimeIds, 기간_밖_예약일);

        mutableClock.setFixed(인기_테마_조회_기준일);

        // when
        ExtractableResponse<Response> response = 인기_테마_목록_조회를_요청하면();

        // then
        기간_내_예약_수가_많은_순서대로_인기_테마_목록을_응답받는다(response, themeIds);
        기간_밖_예약만_있는_테마는_인기_테마_목록에_포함되지_않는다(response, themeIds);
    }
}
