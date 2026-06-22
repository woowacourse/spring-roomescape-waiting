package roomescape.payment.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.RoomescapeApplication;
import roomescape.controller.FixedClockConfig;
import roomescape.repository.ReservationDao;
import roomescape.service.PendingReservation;
import roomescape.service.ReservationCommandService;

@SpringBootTest(classes = RoomescapeApplication.class)
@AutoConfigureMockMvc
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ReservationCommandService reservationCommandService;
    @Autowired
    private ReservationDao reservationDao;

    @Test
    @DisplayName("결제 취소 요청은 결제대기 예약을 삭제한다.")
    void cancelPendingPayment() throws Exception {
        PendingReservation pending = reservationCommandService.createPendingPaymentReservation(
                "new-user", LocalDate.of(2026, 6, 5), 1L, 2L);

        mockMvc.perform(post("/payments/cancel")
                        .param("orderId", pending.orderId())
                        .param("code", "PAY_PROCESS_CANCELED")
                        .param("message", "결제창에서 돌아왔습니다."))
                .andExpect(status().isNoContent());

        assertThat(reservationDao.findById(pending.reservation().id())).isEmpty();
    }
}
