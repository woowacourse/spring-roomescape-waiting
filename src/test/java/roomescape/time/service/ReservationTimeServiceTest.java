package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.time.fixture.DateTimeFixture.TIME_10_00;
import static roomescape.time.fixture.ReservationTimeFixture.RESERVATION_TIME_10_00_ID_1;
import static roomescape.time.fixture.ReservationTimeFixture.TIME_ADD_REQUEST_10_00;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.exception.DuplicateSaveException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.dto.ReservationTimeResponse;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("모든 예약 시간을 조회하고 응답 형태로 반환할 수 있다")
    @Test
    void should_return_all_reservation_times_as_responses() {
        when(reservationTimeRepository.findAll()).thenReturn(List.of(RESERVATION_TIME_10_00_ID_1));

        assertThat(reservationTimeService.findAllReservationTime()).hasSize(1);
    }

    @DisplayName("예약 시간을 추가하고 응답을 반환할 수 있다")
    @Test
    void should_save_reservation_time_when_requested() {
        when(reservationTimeRepository.save(any(ReservationTime.class))).thenReturn(RESERVATION_TIME_10_00_ID_1);
        when(reservationTimeRepository.existByStartAt(TIME_10_00)).thenReturn(false);

        ReservationTimeResponse saved = reservationTimeService.saveReservationTime(TIME_ADD_REQUEST_10_00);

        assertThat(saved).isEqualTo(new ReservationTimeResponse(RESERVATION_TIME_10_00_ID_1));
    }

    @DisplayName("이미 존재하는 예약 시간에 대한 추가 요청 시 예외가 발생한다")
    @Test
    void should_throw_exception_when_reservation_time_already_exists() {
        when(reservationTimeRepository.existByStartAt(TIME_ADD_REQUEST_10_00.startAt())).thenReturn(true);

        assertThatThrownBy(() -> reservationTimeService.saveReservationTime(TIME_ADD_REQUEST_10_00))
                .isInstanceOf(DuplicateSaveException.class);
    }
}
