package roomescape.domain.waitingreservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.admin.AdminRequestValidator;

@WebMvcTest(WaitingReservationController.class)
class WaitingReservationControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingReservationService waitingReservationService;

    @MockitoBean
    private AdminRequestValidator adminRequestValidator;

    @Test
    void memberId가_없으면_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "dateId": 1,
                    "timeId": 1,
                    "themeId": 1
                }
                """;

        mockMvc.perform(post("/waiting-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dateId가_없으면_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "memberId": 1,
                    "timeId": 1,
                    "themeId": 1
                }
                """;

        mockMvc.perform(post("/waiting-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void timeId가_없으면_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "memberId": 1,
                    "dateId": 1,
                    "themeId": 1
                }
                """;

        mockMvc.perform(post("/waiting-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void themeId가_없으면_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "memberId": 1,
                    "dateId": 1,
                    "timeId": 1
                }
                """;

        mockMvc.perform(post("/waiting-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void memberId_파라미터가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/waiting-reservations"))
                .andExpect(status().isBadRequest());
    }
}
