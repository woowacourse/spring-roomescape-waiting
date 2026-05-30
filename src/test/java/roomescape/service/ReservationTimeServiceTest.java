package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.ConflictException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.service.dto.command.ReservationTimeCommand;
import roomescape.service.dto.result.ReservationTimeResult;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {
    private ReservationTimeService reservationTimeService;

    @Mock
    private ReservationTimeDao reservationTimeDao;

    @Mock
    private ReservationDao reservationDao;

    @BeforeEach
    void setUp() {
        reservationTimeService = new ReservationTimeService(reservationTimeDao, reservationDao);
    }

    @Test
    @DisplayName("예약 시간을 전체 조회한다.")
    void find_all_reservation_times() {
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalTime.parse("10:00")),
                new ReservationTime(2L, LocalTime.parse("11:00"))
        );
        given(reservationTimeDao.findAll()).willReturn(times);

        List<ReservationTimeResult> results = reservationTimeService.findReservationTimes();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).startAt()).isEqualTo("10:00");
        assertThat(results.get(1).startAt()).isEqualTo("11:00");
    }

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void create_reservation_time() {
        String time = "19:00";
        ReservationTimeCommand command = new ReservationTimeCommand(LocalTime.parse(time));
        ReservationTime savedTime = new ReservationTime(1L, LocalTime.parse(time));
        given(reservationTimeDao.save(any(ReservationTime.class))).willReturn(savedTime);

        ReservationTimeResult saved = reservationTimeService.registerReservationTime(command);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.startAt()).isEqualTo(time);
    }

    @Test
    @DisplayName("예약이 없는 예약 시간을 삭제한다.")
    void delete_reservation_time_when_no_reservation_exists() {
        Long timeId = 1L;
        given(reservationDao.existsByTimeId(timeId)).willReturn(false);

        assertDoesNotThrow(() -> reservationTimeService.deleteReservationTime(timeId));
    }

    @Test
    @DisplayName("예약이 존재하는 시간을 삭제하면 에러가 발생한다.")
    void delete_reservation_time_when_reservation_exists() {
        Long timeId = 1L;
        given(reservationDao.existsByTimeId(timeId)).willReturn(true);

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(timeId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("예약이 존재하는 시간은 삭제할 수 없습니다.");
    }
}
