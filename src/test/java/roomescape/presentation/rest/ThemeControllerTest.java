package roomescape.presentation.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixtures.anyThemeWithNewId;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.application.ThemeService;
import roomescape.exception.InUseException;
import roomescape.exception.NotFoundException;
import roomescape.presentation.GlobalExceptionHandler;

class ThemeControllerTest {

    private final ThemeService themeService = Mockito.mock(ThemeService.class);
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new ThemeController(themeService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();

    @Test
    @DisplayName("방 테마 추가 요청시, id를 포함한 방 테마와 CREATED를 응답한다.")
    void register() throws Exception {
        Mockito.when(themeService.register(any(), any(), any()))
            .thenReturn(anyThemeWithNewId());

        mockMvc.perform(post("/themes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "공포 테마",
                        "description": "공포 테마입니다",
                        "thumbnail": "thumbnail.jpg"
                    }
                    """))
            .andExpect(jsonPath("$..['id','name','description','thumbnail']").exists())
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("방 테마 조회 요청시, 존재하는 모든 방 테마와 OK를 응답한다.")
    void getAllThemes() throws Exception {
        var expectedThemes = List.of(anyThemeWithNewId(), anyThemeWithNewId(), anyThemeWithNewId());
        Mockito.when(themeService.findAllThemes()).thenReturn(expectedThemes);

        mockMvc.perform(get("/themes"))
            .andExpect(jsonPath("$..['id','name','description','thumbnail']").exists())
            .andExpect(jsonPath("$", hasSize(expectedThemes.size())))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("방 테마 삭제 요청시, 주어진 아이디에 해당하는 방 테마가 있다면 삭제하고 NO CONTENT를 응답한다.")
    void deleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/themes/1"))
            .andExpect(status().isNoContent());

        Mockito.verify(themeService, times(1)).removeById(1L);
    }

    @Test
    @DisplayName("방 테마 삭제 요청시, 주어진 아이디에 해당하는 방 테마가 없다면 NOT FOUND를 응답한다.")
    void deleteWhenNotFound() throws Exception {
        Mockito.doThrow(new NotFoundException("theme not found"))
            .when(themeService).removeById(eq(999L));

        mockMvc.perform(delete("/themes/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("방 테마 삭제 요청시, 주어진 아이디에 해당하는 방테마가 사용중이라면 CONFLICT를 응답한다.")
    void deleteWhenConflict() throws Exception {
        Mockito.doThrow(new InUseException("some reservation is referencing this theme"))
            .when(themeService).removeById(eq(999L));

        mockMvc.perform(delete("/themes/999"))
            .andExpect(status().isConflict());
    }
}
