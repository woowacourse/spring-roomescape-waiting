package roomescape.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.service.ReservationService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("예약자 이름이 빈 문자열이면 400을 반환한다.")
    void 빈_이름으로_예약_등록시_400() throws Exception {
        String body = """
                {"name": "", "themeSlotId": 1}
                """;

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("테마 슬롯 ID 없이 예약 요청하면 400을 반환한다.")
    void 테마슬롯ID_없이_예약_등록시_400() throws Exception {
        String body = """
                {"name": "브라운"}
                """;

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 변경 시 테마 슬롯 ID가 없으면 400을 반환한다.")
    void 테마슬롯ID_없이_예약_변경시_400() throws Exception {
        mockMvc.perform(patch("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 예약 조회 시 이름이 빈 문자열이면 400을 반환한다.")
    void 빈_이름으로_내_예약_조회시_400() throws Exception {
        mockMvc.perform(get("/reservations")
                        .param("name", ""))
                .andExpect(status().isBadRequest());
    }
}
