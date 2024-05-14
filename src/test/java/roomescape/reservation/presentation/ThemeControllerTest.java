package roomescape.reservation.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import roomescape.auth.presentation.AdminAuthorizationInterceptor;
import roomescape.auth.presentation.LoginMemberArgumentResolver;
import roomescape.common.ControllerTest;
import roomescape.global.config.WebMvcConfiguration;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.dto.request.ThemeSaveRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixture.*;

@WebMvcTest(
        value = ThemeController.class,
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebMvcConfiguration.class, LoginMemberArgumentResolver.class, AdminAuthorizationInterceptor.class})
)
class ThemeControllerTest extends ControllerTest {
    @MockBean
    protected ThemeService themeService;

    @Test
    @DisplayName("테마 생성 POST 요청 시 상태코드 201을 반환한다.")
    void createTheme() throws Exception {
        // given
        ThemeSaveRequest request = new ThemeSaveRequest(WOOTECO_THEME_NAME, WOOTECO_THEME_DESCRIPTION, THEME_THUMBNAIL);

        BDDMockito.given(themeService.create(any()))
                .willReturn(WOOTECO_THEME(1L));

        // when & then
        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(WOOTECO_THEME_NAME))
                .andExpect(jsonPath("$.description").value(WOOTECO_THEME_DESCRIPTION))
                .andExpect(jsonPath("$.thumbnail").value(THEME_THUMBNAIL))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    @DisplayName("테마 생성 POST 요청 시 테마 이름이 비어 있다면 상태코드 400을 반환한다.")
    void createThemeWithInvalidName(String invalidName) throws Exception {
        // given
        ThemeSaveRequest request = new ThemeSaveRequest(invalidName, WOOTECO_THEME_DESCRIPTION, THEME_THUMBNAIL);

        // when & then
        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("테마 목록 GET 요청 시 상태코드 200을 반환한다.")
    void findAllThemes() throws Exception {
        // given
        BDDMockito.given(themeService.findAll())
                .willReturn(List.of(WOOTECO_THEME(1L)));

        // when & then
        mockMvc.perform(get("/themes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(WOOTECO_THEME_NAME))
                .andExpect(jsonPath("$[0].description").value(WOOTECO_THEME_DESCRIPTION))
                .andExpect(jsonPath("$[0].thumbnail").value(THEME_THUMBNAIL))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("테마 DELETE 요청 시 상태 코드 204를 반환한다.")
    void deleteTheme() throws Exception {
        // given
        BDDMockito.willDoNothing()
                .given(themeService)
                .deleteById(anyLong());

        // when & then
        mockMvc.perform(delete("/themes/{id}", anyLong()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("최근 일주일 인기 테마 목록 GET 요청 시 상태코드 200을 반환한다.")
    void findAllPopular() throws Exception {
        // given
        BDDMockito.given(themeService.findAllPopular())
                .willReturn(List.of(WOOTECO_THEME(1L), HORROR_THEME(2L)));

        // when & then
        mockMvc.perform(get("/themes/popular")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(WOOTECO_THEME_NAME))
                .andExpect(jsonPath("$[0].description").value(WOOTECO_THEME_DESCRIPTION))
                .andExpect(jsonPath("$[0].thumbnail").value(THEME_THUMBNAIL))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].name").value(HORROR_THEME_NAME))
                .andExpect(jsonPath("$[1].description").value(HORROR_THEME_DESCRIPTION))
                .andExpect(jsonPath("$[1].thumbnail").value(THEME_THUMBNAIL))
                .andExpect(jsonPath("$[1].id").value(2L));
    }
}
