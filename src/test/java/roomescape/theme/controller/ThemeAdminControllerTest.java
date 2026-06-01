package roomescape.theme.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeResult;
import roomescape.theme.exception.ThemeErrorCode;

@WebMvcTest(ThemeAdminController.class)
@Import(WebMvcConfig.class)
class ThemeAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("테마를 성공적으로 생성한다.")
    void create_Success() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "name", "테마",
                "description", "설명",
                "thumbnailUrl", "url"
        );
        Theme theme = new Theme(1L, "테마", "설명", "url");

        given(themeService.save(any())).willReturn(ThemeResult.from(theme));

        // when & then
        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/themes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("테마"));
    }

    @Test
    @DisplayName("테마 생성 시 필수 필드가 누락되면 400 에러를 반환한다.")
    void create_MissingFields_BadRequest() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "name", "",
                "description", "설명",
                "thumbnailUrl", "url"
        );

        // when & then
        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("테마 입력 정보가 형식에 맞지 않습니다. 글자 수 제한 및 필수 입력 항목을 확인해 주세요."));
    }

    @Test
    @DisplayName("테마 생성 시 중복된 테마명이 존재하면 409 에러를 반환한다.")
    void create_DuplicateName_Conflict() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "name", "테마",
                "description", "설명",
                "thumbnailUrl", "url"
        );
        given(themeService.save(any()))
                .willThrow(new ConflictException(ThemeErrorCode.DUPLICATE_THEME.getMessage()));

        // when & then
        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ThemeErrorCode.DUPLICATE_THEME.getMessage()));
    }

    @Test
    @DisplayName("테마를 성공적으로 삭제한다.")
    void delete_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/themes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 테마 삭제 시 404 에러를 반환한다.")
    void delete_NotFound_NotFound() throws Exception {
        // given
        willThrow(new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage()))
                .given(themeService).delete(1L);

        // when & then
        mockMvc.perform(delete("/admin/themes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ThemeErrorCode.THEME_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("사용 중인 테마 삭제 시 400 에러를 반환한다.")
    void delete_InUse_BadRequest() throws Exception {
        // given
        willThrow(new BadRequestException(ThemeErrorCode.THEME_IN_USE.getMessage()))
                .given(themeService).delete(1L);

        // when & then
        mockMvc.perform(delete("/admin/themes/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ThemeErrorCode.THEME_IN_USE.getMessage()));
    }
}
