package roomescape.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

class MemberThemeControllerTest extends ExcludeInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("테마를 추가한다")
    @Test
    void when_postTheme_then_created() throws Exception {
        // given
        String requestTheme = "{\"name\": \"테마\", \"description\": \"설명서\", \"thumbnail\": \"https://i.postimg" +
                              ".cc/cLqW2JLB/theme-SOS-SOS.jpg\"}";

        // when, then
        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestTheme))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @DisplayName("모든 테마를 조회한다")
    @Sql("/test-data/themes.sql")
    @Test
    void when_getThemes_then_returnThemes() throws Exception {
        // when, then
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)));
    }

    @DisplayName("테마를 삭제한다")
    @Sql("/test-data/themes.sql")
    @Test
    void when_deleteTheme_then_noContent() throws Exception {
        // when, then
        mockMvc.perform(delete("/themes/1"))
                .andExpect(status().isNoContent());
    }

    @Disabled
    @DisplayName("상위 10개 테마를 조회한다")
    @Test
    void when_getTop10Themes_then_returnThemes() throws Exception {
        // when, then
        mockMvc.perform(get("/themes/tops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themes", hasSize(10)));
    }
}
