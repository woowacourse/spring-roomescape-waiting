package roomescape.reservation.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.error.exception.BadRequestException;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.ReservationTimeService;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    private static final LocalTime VALID_START_TIME = LocalTime.of(10, 0);
    private static final LocalTime INVALID_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime ANOTHER_VALID_TIME = LocalTime.of(12, 0);
    private static final Long TIME_ID = 1L;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("시간을 생성한다.")
    void createTime() {
        // given
        var request = new ReservationTimeCreateRequest(VALID_START_TIME);
        var savedTime = new ReservationTime(TIME_ID, request.startAt());
        given(reservationTimeRepository.save(any()))
                .willReturn(savedTime);

        // when
        var response = reservationTimeService.createTime(request);

        // then
        assertThat(response.startAt()).isEqualTo(VALID_START_TIME);
        then(reservationTimeRepository).should().save(any());
    }

    @Test
    @DisplayName("운영 시간 이외의 시간을 생성하면 예외가 발생한다.")
    void createTimeWithInvalidTime() {
        // given
        var request = new ReservationTimeCreateRequest(INVALID_START_TIME);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.createTime(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("운영 시간 이외의 날짜는 예약할 수 없습니다.");
        
        then(reservationTimeRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("모든 시간을 조회한다.")
    void getAllTimes() {
        // given
        var time = new ReservationTime(TIME_ID, VALID_START_TIME);
        given(reservationTimeRepository.findAll())
                .willReturn(List.of(time));

        // when
        var responses = reservationTimeService.getAllTimes();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses.getFirst().startAt()).isEqualTo(VALID_START_TIME)
        );
        then(reservationTimeRepository).should().findAll();
    }

    @Test
    @DisplayName("특정 날짜와 테마에 대한 가능한 시간을 조회한다.")
    void getAvailableTimes() {
        // given
        var time1 = new ReservationTime(1L, VALID_START_TIME);
        var time2 = new ReservationTime(2L, ANOTHER_VALID_TIME);
        given(reservationTimeRepository.findAll())
                .willReturn(List.of(time1, time2));
        given(reservationTimeRepository.findAllReservedTimeByDateAndThemeId(any(), any()))
                .willReturn(List.of(time2));

        // when
        var responses = reservationTimeService.getAvailableTimes(LocalDate.now(), 1L);

        // then
        var response = responses.getFirst();
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(response.startAt()).isEqualTo(VALID_START_TIME),
                () -> assertThat(response.alreadyBooked()).isFalse()
        );
        then(reservationTimeRepository).should().findAll();
        then(reservationTimeRepository).should().findAllReservedTimeByDateAndThemeId(any(), any());
    }

    @Test
    @DisplayName("시간을 삭제한다.")
    void deleteTime() {
        // given
        given(reservationRepository.existsByTimeId(anyLong()))
                .willReturn(false);

        // when
        reservationTimeService.deleteTime(TIME_ID);

        // then
        then(reservationTimeRepository).should().deleteById(TIME_ID);
    }

    @Test
    @DisplayName("예약이 있는 시간을 삭제하면 예외가 발생한다.")
    void deleteTimeWithReservation() {
        // given
        given(reservationRepository.existsByTimeId(anyLong()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(TIME_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 시간에 예약된 내역이 존재하므로 삭제할 수 없습니다.");
        
        then(reservationTimeRepository).should(never()).deleteById(any());
    }
}
