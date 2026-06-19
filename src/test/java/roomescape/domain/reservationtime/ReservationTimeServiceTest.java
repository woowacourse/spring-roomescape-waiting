package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.dto.ReservationTimeAvailabilityResponse;
import roomescape.domain.reservationtime.dto.TimeCreationRequest;
import roomescape.domain.reservationtime.dto.TimeCreationResponse;
import roomescape.support.exception.RoomescapeException;

class ReservationTimeServiceTest {

    private ReservationTimeService reservationTimeService;
    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        reservationTimeRepository = mock(ReservationTimeRepository.class);
        reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository
        );
    }

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void createReservationTime() {
        TimeCreationRequest request = new TimeCreationRequest(LocalTime.of(10, 0));
        when(reservationTimeRepository.existsByStartAt(request.startAt())).thenReturn(false);
        when(reservationTimeRepository.save(any(ReservationTime.class)))
            .thenReturn(ReservationTime.of(1L, request.startAt()));

        TimeCreationResponse response = reservationTimeService.createReservationTime(request);

        assertThat(response.startAt()).isEqualTo(request.startAt());
        verify(reservationTimeRepository).save(any(ReservationTime.class));
    }

    @Test
    @DisplayName("중복된 시간 생성 시 예외가 발생한다.")
    void createDuplicateTime() {
        LocalTime startAt = LocalTime.of(10, 0);
        when(reservationTimeRepository.existsByStartAt(startAt)).thenReturn(true);

        assertThatThrownBy(() -> reservationTimeService.createReservationTime(new TimeCreationRequest(startAt)))
            .isInstanceOf(RoomescapeException.class);
        verify(reservationTimeRepository, never()).save(any(ReservationTime.class));
    }

    @Test
    @DisplayName("특정 테마와 날짜의 예약 가능 시간을 조회한다.")
    void getReservationTimeAvailability() {
        // given
        ReservationTime time1 = ReservationTime.of(1L, LocalTime.of(10, 0));
        ReservationTime time2 = ReservationTime.of(2L, LocalTime.of(11, 0));
        when(reservationTimeRepository.findAll()).thenReturn(List.of(time1, time2));
        when(reservationRepository.findReservedTimes(1L, 1L)).thenReturn(List.of(time1.getId()));

        // when
        List<ReservationTimeAvailabilityResponse> responses = reservationTimeService.getReservationTimeAvailability(1L,
            1L);

        // then
        assertThat(responses).hasSize(2);
        assertThat(
            responses.stream().filter(r -> r.timeId().equals(time1.getId())).findFirst().get().available()).isFalse();
        assertThat(
            responses.stream().filter(r -> r.timeId().equals(time2.getId())).findFirst().get().available()).isTrue();
    }

    @Test
    @DisplayName("사용 중인 시간을 삭제하려 하면 예외가 발생한다.")
    void deleteInUseTime() {
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        when(reservationTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(reservationRepository.countByTimeId(time.getId())).thenReturn(1);

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(time.getId()))
            .isInstanceOf(RoomescapeException.class);
        verify(reservationTimeRepository, never()).delete(time);
    }
}
