package roomescape.controller.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.controller.BaseControllerUnitTest;
import roomescape.controller.client.dto.response.ThemeResponse;
import roomescape.controller.client.dto.response.ThemeTimesResponse;
import roomescape.service.ThemeService;
import roomescape.service.result.ThemeRegisterResult;
import roomescape.service.result.ThemeTimesResult;

@WebMvcTest(ThemeApiController.class)
class ThemeApiControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ThemeService themeService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @Test
    void 테마_시간대_조회_요청에_성공하면_정상_응답이_반환된다() {
        // given
        ThemeTimesResult timesResult = new ThemeTimesResult(1L, LocalTime.now(), true, "RESERVABLE");
        when(themeService.getThemeReservationStatus(anyLong(), any(LocalDate.class))).thenReturn(List.of(timesResult));

        // when & then
        List<ThemeTimesResponse> response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("date", "2026-05-06")
                .when().get("/api/themes/{id}/times", 1L)
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).containsOnly(ThemeTimesResponse.from(timesResult));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void 테마_조회_요청시_테마_식별자가_양수가_아니라면_400_BAD_REQUEST(long invalidThemeId) {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("date", "2026-05-06")
                .when().get("/api/themes/{id}/times", invalidThemeId)
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("테마 조회 식별자는 양수여야 합니다."));
    }

    @Test
    void 테마_목록_조회_요청에_성공하면_정상_응답이_반환된다() {
        // given
        ThemeRegisterResult themeResult = new ThemeRegisterResult(1L, "공포테마", "어마무시한 공포 테마입니다.",
                "https://image.com/image.png", 30000L, true);
        when(themeService.getAllActiveThemes()).thenReturn(List.of(themeResult));

        // when & then
        List<ThemeResponse> response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().get("/api/themes")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).containsOnly(ThemeResponse.from(themeResult));
    }

    @Test
    void 인기_테마_목록_조회_요청에_성공하면_정상_응답이_반환된다() {
        // given
        ThemeRegisterResult themeResult = new ThemeRegisterResult(1L, "공포테마", "어마무시한 공포 테마입니다.",
                "https://image.com/image.png", 30000L, true);
        when(themeService.getPopularThemes(any(LocalDate.class), any(LocalDate.class))).thenReturn(
                List.of(themeResult));

        // when & then
        List<ThemeResponse> response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("startDate", "2026-05-06")
                .queryParam("endDate", "2026-05-09")
                .when().get("/api/themes/popular")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).containsOnly(ThemeResponse.from(themeResult));
    }

    @Test
    void 인기_테마_목록_조회_요청_시_시작일이_없으면_400_BAD_REQUEST() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("endDate", "2026-05-06")
                .when().get("/api/themes/popular")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("startDate 파라미터가 누락 되었습니다."));
    }
}
