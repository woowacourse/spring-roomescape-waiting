package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.repository.dto.ReservationDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.presentation.controller.ReservationController;
import roomescape.reservation.presentation.dto.ReservationResponse;

@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void save_reservation() throws Exception {
        given(reservationService.save(any(), any()))
                .willReturn(ReservationResponse.from(new ReservationDetail(
                        1L, "카야", LocalDate.of(2028, 5, 6),
                        1L, "theme", "desc", "img",
                        1L, LocalTime.of(9, 0)
                )));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "카야",
                                  "date": "2028-05-06",
                                  "themeId": 1,
                                  "timeId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("카야"))
                .andExpect(jsonPath("$.date").value("2028-05-06"));
    }
}
