package roomescape.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.service.ReservationService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "rate-limit.capacity=1",
        "rate-limit.refill-per-second=0.001"
})
class RateLimitInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private ReservationService reservationService;

    @Test
    void 한도_초과_요청은_429와_RetryAfter를_반환하고_컨트롤러를_호출하지_않는다() throws Exception {
        mockMvc.perform(get("/reservations").param("name", "브라운"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reservations").param("name", "브라운"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));

        verify(reservationService, times(1)).getMyReservations(anyString());
    }
}
