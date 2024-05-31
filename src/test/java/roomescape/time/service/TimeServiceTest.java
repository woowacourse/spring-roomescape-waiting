package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import roomescape.exception.ConflictException;
import roomescape.reservation.repository.ReservationDetailRepository;
import roomescape.time.domain.Time;
import roomescape.time.dto.TimeRequest;
import roomescape.time.dto.TimeResponse;
import roomescape.time.repository.TimeRepository;

@ExtendWith(MockitoExtension.class)
class TimeServiceTest {
    private final Time time = new Time(1L, LocalTime.of(17, 3));

    @InjectMocks
    private TimeService timeService;

    @Mock
    private ReservationDetailRepository detailRepository;
    @Mock
    private TimeRepository timeRepository;

    @Test
    @DisplayName("성공 : 시간을 추가한다.")
    void addReservationTime() {
        when(timeRepository.save(any(Time.class)))
                .thenReturn(time);

        TimeRequest timeRequest = new TimeRequest(time.getStartAt());
        TimeResponse timeResponse = timeService.addReservationTime(timeRequest);

        Assertions.assertThat(timeResponse.id())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("성공 : 시간을 찾는다.")
    void findReservationTimes() {
        when(timeRepository.findAllByOrderByStartAtAsc())
                .thenReturn(List.of(time));

        List<TimeResponse> timeResponses = timeService.findReservationTimes();

        Assertions.assertThat(timeResponses)
                .hasSize(1);
    }

    @Test
    @DisplayName("실패 : 중복된 예약 시간 생성 요청시 예외를 던진다.")
    void addReservationTime_Exception() {
        when(timeRepository.existsByStartAt(any(LocalTime.class)))
                .thenReturn(true);

        TimeRequest timeRequest = new TimeRequest(LocalTime.now());
        assertThatThrownBy(() -> timeService.addReservationTime(timeRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 존재하는 예약 시간입니다.");
    }

    @Test
    @DisplayName("성공 : 시간을 지운다.")
    void removeReservationTime() {
        Mockito.doNothing()
                .when(timeRepository)
                .deleteById(time.getId());

        assertThatCode(() -> timeService.removeReservationTime(time.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패 : 예약이 존재하는 예약 시간 삭제 요청시 예외를 던진다.")
    void removeReservationTime_Exception() {
        when(detailRepository.countReservationsByTime_Id(1L))
                .thenReturn(1);

        assertThatThrownBy(() -> timeService.removeReservationTime(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("삭제를 요청한 시간에 예약이 존재합니다.");
    }
}
