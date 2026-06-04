package roomescape.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
class ReservationConcurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void 동시_충돌_발생_시_409를_반환한다() throws Exception {
        doThrow(DataIntegrityViolationException.class)
                .when(reservationService).delete(anyLong());

        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isConflict());
    }
}
