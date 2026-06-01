package roomescape.theme.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.presentation.BaseControllerUnitTest;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.presentation.AdminThemeController;
import roomescape.theme.presentation.dto.ThemeRequest;
import roomescape.theme.presentation.dto.ThemeResponse;

@WebMvcTest(AdminThemeController.class)
class AdminThemeControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ThemeService themeService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @Test
    void 테마_추가_요청에_성공하면_201_CREATED와_정상_응답을_반환한다() {
        // given
        ThemeRequest request = new ThemeRequest("공포", "http://image.com/image.png", "공포 방탈출입니다.");
        ThemeInfo expectedInfo = new ThemeInfo(1L, "공포", "http://image.com/image.png", "공포 방탈출입니다.", true);
        when(themeService.create(any(ThemeCommand.class))).thenReturn(expectedInfo);

        // when
        ThemeResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(request)
                .when().post("/admin/themes")
                .then().log().all()
                .status(HttpStatus.CREATED)
                .extract().as(new TypeRef<>() {
                });

        // then
        assertThat(response).isEqualTo(ThemeResponse.from(expectedInfo));
    }

    @ParameterizedTest(name = "요청 정보가 {0} 일 때, 예외 메세지 \"{1}\"가 발생한다.")
    @MethodSource("roomescape.presentation.fixture.AdminThemeRequestFixture#themeFailRequestFixture")
    void 테마_추가_요청_시_형식_검증에_실패하면_예외가_발생한다(ThemeRequest body, String exceptionMessage) {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/admin/themes")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString(exceptionMessage));
    }

    @Test
    void 특정_테마_비활성화_요청_시_204_NO_CONTENT를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().delete("/admin/themes/1")
                .then().log().all()
                .status(HttpStatus.NO_CONTENT);

        verify(themeService, times(1)).deactivate(anyLong());
    }
}
