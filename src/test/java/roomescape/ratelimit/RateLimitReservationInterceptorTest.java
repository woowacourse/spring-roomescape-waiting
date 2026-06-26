package roomescape.ratelimit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.ReservationController;
import roomescape.exception.GlobalExceptionHandler;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
@Import({GlobalExceptionHandler.class, RateLimitConfig.class})
@TestPropertySource(properties = {
        "rate-limit.capacity=1",
        "rate-limit.refill-per-second=0.1"
})
class RateLimitReservationInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("예약 엔드포인트가 한도를 초과하면 429와 Retry-After 헤더를 반환하고 컨트롤러를 호출하지 않는다.")
    void rateLimitReservations() throws Exception {
        given(reservationService.findAllReservations()).willReturn(List.of());

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "10"));

        verify(reservationService, times(1)).findAllReservations();
    }
}
