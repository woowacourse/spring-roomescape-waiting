package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.service.WaitingService;

@WebMvcTest(WaitingController.class)
class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingService waitingService;

    @Test
    void 대기_등록_시_이름이_공백이면_400_BadRequest를_반환한다() throws Exception {
        String request = """
                {
                    "name": "   ",
                    "date": "2030-01-01",
                    "timeId": 1,
                    "themeId": 1
                }
                """;

        mockMvc.perform(
                post("/api/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void 대기_등록_시_날짜_입력이_null이면_400_BadRequest를_반환한다() throws Exception {
        String request = """
                {
                    "name": "코코",
                    "date": null,
                    "timeId": 1,
                    "themeId": 1
                }
                """;

        mockMvc.perform(
                        post("/api/waitings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void 대기_등록_시_날짜_입력_형식이_틀리면_400_BadRequest를_반환한다() throws Exception {
        String request = """
                {
                    "name": "코코",
                    "date": "2030/01/01",
                    "timeId": 1,
                    "themeId": 1
                }
                """;

        mockMvc.perform(
                        post("/api/waitings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void 대기_등록_시_timeId가_null이면_400_BadRequest를_반환한다() throws Exception {
        String request = """
                {
                    "name": "코코",
                    "date": "2030-01-01",
                    "timeId": null,
                    "themeId": 1
                }
                """;

        mockMvc.perform(
                        post("/api/waitings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void 대기_등록_시_timeId가_양수가_아니면_400_BadRequest를_반환한다(Long timeId) throws Exception {
        String request = """
                {
                    "name": "코코",
                    "date": "2030-01-01",
                    "timeId": %d,
                    "themeId": 1
                }
                """.formatted(timeId);

        mockMvc.perform(
                        post("/api/waitings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void 대기_등록_시_themeId가_null이면_400_BadRequest를_반환한다() throws Exception {
        String request = """
                {
                    "name": "코코",
                    "date": "2030-01-01",
                    "timeId": 1,
                    "themeId": null
                }
                """;

        mockMvc.perform(
                        post("/api/waitings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void 대기_등록_시_themeId가_양수가_아니면_400_BadRequest를_반환한다(Long themeId) throws Exception {
        String request = """
                {
                    "name": "코코",
                    "date": "2030-01-01",
                    "timeId": 1,
                    "themeId": %d
                }
                """.formatted(themeId);

        mockMvc.perform(
                        post("/api/waitings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }
}
