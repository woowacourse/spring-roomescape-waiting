package roomescape.theme.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.presentation.BaseControllerUnitTest;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.presentation.ThemeController;
import roomescape.theme.presentation.dto.ThemeResponse;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ThemeService themeService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @Test
    void 테마_목록_조회_요청에_성공하면_200_OK와_정상_응답이_반환된다() {
        // given
        List<ThemeInfo> expectedInfos = List.of(
                new ThemeInfo(1L, "공포테마", "https://image.com/image.png", "어마무시한 공포 테마입니다.", true),
                new ThemeInfo(2L, "놀이동산테마", "https://image.com/image2.png", "놀이동산 테마입니다.", true)
        );
        when(themeService.getThemes(0, 10)).thenReturn(expectedInfos);

        // when & then
        List<ThemeResponse> response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("page", "0")
                .queryParam("size", "10")
                .when().get("/themes")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).containsExactlyElementsOf(expectedInfos.stream().map(ThemeResponse::from).toList());
    }

    @Test
    void 테마_목록_조회_요청_시_페이지가_음수이면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("page", "-1")
                .queryParam("size", "10")
                .when().get("/themes")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("페이지 번호는 0 이상이어야 합니다."));
    }

    @Test
    void 테마_목록_조회_요청_시_조회_개수가_양수가_아니면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("page", "0")
                .queryParam("size", "0")
                .when().get("/themes")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("조회 개수는 양수여야 합니다."));
    }

    @Test
    void 인기_테마_목록_조회_요청에_성공하면_200_OK와_정상_응답이_반환된다() {
        // given
        List<ThemeInfo> expectedInfos = List.of(
                new ThemeInfo(1L, "공포테마", "https://image.com/image.png", "어마무시한 공포 테마입니다.", true)
        );
        when(themeService.getWeeksTopThemes(any(LocalDate.class), any(LocalDate.class), any(Integer.class)))
                .thenReturn(expectedInfos);

        // when & then
        List<ThemeResponse> response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("startDate", "2026-05-06")
                .queryParam("endDate", "2026-05-09")
                .queryParam("size", "10")
                .when().get("/themes/weeks/top")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).containsExactlyElementsOf(expectedInfos.stream().map(ThemeResponse::from).toList());
    }

    @Test
    void 인기_테마_목록_조회_요청_시_시작일이_없으면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("endDate", "2026-05-09")
                .queryParam("size", "10")
                .when().get("/themes/weeks/top")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("startDate 파라미터가 누락 되었습니다."));
    }

    @Test
    void 인기_테마_목록_조회_요청_시_종료일이_없으면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("startDate", "2026-05-06")
                .queryParam("size", "10")
                .when().get("/themes/weeks/top")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("endDate 파라미터가 누락 되었습니다."));
    }

    @Test
    void 인기_테마_목록_조회_요청_시_조회_개수가_양수가_아니면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("startDate", "2026-05-06")
                .queryParam("endDate", "2026-05-09")
                .queryParam("size", "0")
                .when().get("/themes/weeks/top")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("조회 개수는 양수여야 합니다."));
    }
}
