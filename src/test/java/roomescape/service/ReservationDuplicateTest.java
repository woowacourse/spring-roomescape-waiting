package roomescape.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.request.ReservationRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationDuplicateTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("이미 존재하는 예약 건과 중복으로 예약하면 예외가 발생한다")
    void throwsException_whenDuplicateReservationExists() {

        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 5, 1), 1L, 1L);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 지난 시간/날짜는 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("중복되지 않는 시간에 예약을 하면 통과한다.")
    void 중복이_없는_정상_예약_테스트() {

        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 5, 30), 1L, 1L);

        assertDoesNotThrow(() -> reservationService.save(request));
    }
}
