package roomescape.theme.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeRepository.deleteAll();
    }

    @Test
    void 테마_생성_성공() throws Exception {
        // given
        ThemeRequest request = new ThemeRequest("테스트 테마", "테스트 설명", "테스트 썸네일");

        // when & then
        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"테스트 테마\", \"description\": \"테스트 설명\", \"thumbnail\": \"테스트 썸네일\"}")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value("테스트 테마"))
                .andExpect(jsonPath("description").value("테스트 설명"))
                .andExpect(jsonPath("thumbnail").value("테스트 썸네일"));
    }

    @Test
    void 모든_테마_조회_성공() throws Exception {
        // given
        themeService.saveTheme(new ThemeRequest("테마1", "설명1", "썸네일1"));
        themeService.saveTheme(new ThemeRequest("테마2", "설명2", "썸네일2"));

        // when & then
        mockMvc.perform(get("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("[0].name", is("테마1")))
                .andExpect(jsonPath("[1].name", is("테마2")));
    }

    @Test
    void 인기_테마_조회_성공() throws Exception {
        // given
        themeService.saveTheme(new ThemeRequest("테마1", "설명1", "썸네일1"));
        themeService.saveTheme(new ThemeRequest("테마2", "설명2", "썸네일2"));

        // when & then
        mockMvc.perform(get("/themes/ranking")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void 테마_삭제_성공() throws Exception {
        // given
        ThemeResponse response = themeService.saveTheme(new ThemeRequest("테스트 테마", "테스트 설명", "테스트 썸네일"));
        Long id = response.id();

        // when & then
        mockMvc.perform(delete("/themes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void 존재하지_않는_테마_삭제_실패() throws Exception {
        // given
        // when & then
        mockMvc.perform(delete("/themes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }
}
