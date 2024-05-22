package roomescape.service.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.reservation.dto.ReservationResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql("/truncate-with-time-and-theme.sql")
class WaitingServiceTest {
    @Autowired
    private WaitingService waitingService;

    @DisplayName("모든 예약 대기 내역을 조회한다.")
    @Test
    @Sql({"/truncate-with-time-and-theme.sql", "/insert-past-waiting.sql"})
    void findAllWaitings() {
        //when
        List<ReservationResponse> reservations = waitingService.findAll();

        //then
        assertThat(reservations).hasSize(1);
    }
}
