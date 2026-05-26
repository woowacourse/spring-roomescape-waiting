package roomescape.controller.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ViewController.class)
class ViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 홈_페이지를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    void 예약_페이지를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/reservation"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation"));
    }

    @Test
    void 내_예약_조회_페이지를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/reservation/me"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-reservation"));
    }

    @Test
    void 관리자_홈_페이지를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-home"));
    }

    @Test
    void 관리자_예약_페이지를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/reservation"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-reservation"));
    }

    @Test
    void 관리자_시간_페이지를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/time"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-time"));
    }

    @Test
    void 관리자_테마_페이지를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/theme"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-theme"));
    }

    @Test
    void 존재하지_않는_URL_HTML_요청은_404_페이지를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/not-found")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void 존재하지_않는_URL_JSON_요청은_에러_응답을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/not-found")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("존재하지 않는 리소스입니다."));
    }
}
