package roomescape.domain.reservationdate;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.reservationdate.dto.ReservationDateResponse;

@WebMvcTest(ReservationDateController.class)
class ReservationDateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationDateService reservationDateService;

    @Test
    @DisplayName("예약 날짜 조회의 요청과 응답을 확인한다.")
    void getAllReservationDates() throws Exception {
        // given
        ReservationDateResponse response = new ReservationDateResponse(
            1L,
            LocalDate.of(2026, 5, 20)
        );
        given(reservationDateService.getAllReservationDate())
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/reservation-dates")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].reservationDate").value("2026-05-20"));

        verify(reservationDateService).getAllReservationDate();
    }
}
