package roomescape.controller.admin;

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
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.controller.BaseControllerUnitTest;
import roomescape.controller.admin.dto.request.AdminThemeRequest;
import roomescape.controller.admin.dto.response.AdminThemeResponse;
import roomescape.service.ThemeService;
import roomescape.service.command.ThemeRegisterCommand;
import roomescape.service.result.ThemeRegisterResult;

@WebMvcTest(AdminThemeApiController.class)
class AdminThemeApiControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ThemeService themeService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @Test
    void 관리자가_테마_정보로_테마_추가_요청_시_성공한다() {
        // given
        AdminThemeRequest body = new AdminThemeRequest("공포", "공포 방탈출입니다.", "http://image.com/image.png", 30000L);

        ThemeRegisterResult result = new ThemeRegisterResult(1L, "공포", "공포 방탈출입니다.", "http://image.com/image.png",
                30000L, true);
        when(themeService.register(any(ThemeRegisterCommand.class))).thenReturn(result);

        AdminThemeResponse expected = AdminThemeResponse.from(result);

        // when : admin 헤더를 포함해서 요청한다.
        AdminThemeResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/admin/themes")
                .then().log().all()
                .status(HttpStatus.CREATED)
                .extract().as(new TypeRef<>() {
                });

        // then: 요청 성공 시 테마 등록 비즈니스 로직을 수행한다.
        assertThat(response).isEqualTo(expected);
    }

    @ParameterizedTest(name = "요청 정보가 {0} 일 때, 예외 메세지 \"{1}\"가 발생한다.")
    @MethodSource("roomescape.controller.admin.fixture.AdminThemeApiRequestFixture#themeFailRequestFixture")
    void 관리자가_테마_추가_요청_시_형식_검증에_실패하면_예외가_발생한다(AdminThemeRequest body, String exceptionMessage) {
        // given: 실패하는 request body가 주어짐

        // when & then: validation 위반으로 400 Bad Request가 발생한다.
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/admin/themes")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString(exceptionMessage));
    }

    @Test
    void 관리자가_특정_테마_비활성화_요청_시_204_NoContent를_응답한다() {
        // when & then: 관리자 헤더를 포함해서 요청했을 때, 삭제 로직을 수행한다.
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().patch("/api/admin/themes/{id}/deactivate", 1L)
                .then().log().all()
                .status(HttpStatus.NO_CONTENT);

        verify(themeService, times(1)).deactivate(anyLong());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void 관리자가_잘못된_테마_식별자로_비활성화_요청_시_예외가_발생한다(Long invalidId) {
        // when & then: 관리자 헤더를 포함해서 음수 식별자로 요청했을 때, 예외 발생
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().patch("/api/admin/themes/{id}/deactivate", invalidId)
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("테마 비활성화 식별자는 양수여야 합니다."));
    }

    @Test
    void 관리자가_특정_테마_활성화_요청_시_204_NoContent를_응답한다() {
        // when & then: 관리자 헤더를 포함해서 요청했을 때, 삭제 로직을 수행한다.
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().patch("/api/admin/themes/{id}/activate", 1L)
                .then().log().all()
                .status(HttpStatus.NO_CONTENT);

        verify(themeService, times(1)).activate(anyLong());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void 관리자가_잘못된_테마_식별자로_활성화_요청_시_예외가_발생한다(Long invalidId) {
        // when & then: 관리자 헤더를 포함해서 음수 식별자로 요청했을 때, 예외 발생
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().patch("/api/admin/themes/{id}/activate", invalidId)
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("테마 활성화 식별자는 양수여야 합니다."));
    }
}
