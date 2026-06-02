package roomescape.waiting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;
import roomescape.reservation.ReservationRepository;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.dto.request.WaitingRequest;
import roomescape.waiting.dto.response.WaitingResponse;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"})
@Import(TestTimeConfig.class)
@ActiveProfiles("test")
class WaitingServiceIntegrationTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("대기 요청에 reservationId가 있으면 기존 예약을 취소하고 다른 슬롯에 대기를 신청한다.")
    void save_테스트_3() {
        long oldReservationId = 1L;
        long oldScheduleId = 1L;
        long targetScheduleId = 6L; // 2026-05-06, timeId=3, themeId=2
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 6), 3L, 2L, oldReservationId);

        WaitingResponse response = waitingService.save(request, 1L);

        assertThat(response.id()).isNotNull();
        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.scheduleId()).isEqualTo(targetScheduleId);
        assertThat(response.scheduleId()).isNotEqualTo(oldScheduleId);
        assertThat(response.waitingOrder()).isEqualTo(1L);
        assertThat(waitingRepository.findById(response.id())).isPresent();
        assertThat(reservationRepository.findById(oldReservationId)).isEmpty();
    }
}
