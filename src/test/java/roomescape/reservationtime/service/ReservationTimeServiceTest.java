package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.reservationtime.dto.TimeRequest;
import roomescape.reservationtime.dto.TimeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("시간 생성 성공")
    void 시간_생성_성공() {
        TimeResponse response = reservationTimeService.createTime(
                new TimeRequest(LocalTime.of(20, 0), LocalTime.of(21, 0)));
        assertThat(response.id()).isNotNull();
    }

    @Test
    @DisplayName("전체 시간 조회")
    void 전체_시간_조회() {
        List<TimeResponse> times = reservationTimeService.getAllTimes();
        assertThat(times).hasSize(3);
    }

    @Test
    @DisplayName("예약 가능 시간 조회")
    void 예약_가능_시간_조회() {
        List<TimeResponse> available = reservationTimeService.getAvailableTimes(LocalDate.of(2026, 5, 10), 1L);
        assertThat(available).hasSize(2);
    }

    @Test
    @DisplayName("시간 삭제 성공")
    void 시간_삭제_성공() {
        TimeResponse created = reservationTimeService.createTime(
                new TimeRequest(LocalTime.of(20, 0), LocalTime.of(21, 0)));
        reservationTimeService.deleteById(created.id());

        assertThat(reservationTimeService.getAllTimes()).hasSize(3);
    }

    @Test
    @DisplayName("id로 시간 조회 성공")
    void getById_성공() {
        assertThat(reservationTimeService.getById(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 id로 시간 조회 시 예외 발생")
    void getById_없으면_예외() {
        assertThatThrownBy(() -> reservationTimeService.getById(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.TIME_NOT_FOUND))
                .hasMessage(ErrorCode.TIME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약이 존재하는 시간은 삭제할 수 없다")
    void 예약_있는_시간_삭제_불가() {
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.TIME_HAS_RESERVATION))
                .hasMessage(ErrorCode.TIME_HAS_RESERVATION.getMessage());
    }
}