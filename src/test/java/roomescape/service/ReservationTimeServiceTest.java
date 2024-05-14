package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static roomescape.TestFixture.*;
import static roomescape.TestFixture.RESERVATION_TIME_SIX;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.ReservationTime;
import roomescape.dto.reservation.AvailableReservationTimeResponse;
import roomescape.dto.reservation.AvailableReservationTimeSearch;
import roomescape.dto.reservation.ReservationTimeResponse;
import roomescape.dao.ReservationTimeDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {
    @Mock
    private ReservationTimeDao reservationTimeDao;

    @Mock
    ReservationDao reservationDao;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void create() {
        // given
        final ReservationTime reservationTime = RESERVATION_TIME_SIX(1L);
        given(reservationTimeDao.save(any())).willReturn(reservationTime);

        // when
        final ReservationTimeResponse response = reservationTimeService.create(reservationTime);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(reservationTime.getId()),
                () -> assertThat(response.startAt()).isEqualTo(reservationTime.getStartAt().toString())
        );
    }

    @Test
    @DisplayName("예약 시간 목록을 조회한다.")
    void getAll() {
        // given
        final ReservationTime reservationTime = RESERVATION_TIME_SIX();
        given(reservationTimeDao.findAll()).willReturn(List.of(reservationTime));

        // when
        final List<ReservationTimeResponse> responses = reservationTimeService.findAll();

        // then
        assertThat(responses).hasSize(1)
                .extracting(ReservationTimeResponse::startAt)
                .contains(START_AT_SIX);
    }

    @Test
    @DisplayName("예약 시간을 삭제한다.")
    void delete() {
        // given
        final ReservationTime reservationTime = RESERVATION_TIME_SIX(1L);

        given(reservationTimeDao.findById(anyLong())).willReturn(Optional.of(reservationTime));

        // when & then
        assertThatCode(() -> reservationTimeService.delete(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("삭제하려는 예약 시간에 이미 예약이 존재할 경우 예외가 발생한다.")
    void throwExceptionWhenHasReservation() {
        // given
        final ReservationTime reservationTime = RESERVATION_TIME_SIX(1L);

        given(reservationTimeDao.findById(anyLong())).willReturn(Optional.of(reservationTime));
        given(reservationDao.countByTimeId(anyLong())).willReturn(1);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("선택한 날짜와 테마로 예약 가능한 시간 목록을 조회한다.")
    void findAvailableReservationTimes() {
        // given
        final ReservationTime reservedTime = RESERVATION_TIME_SIX(1L);
        final AvailableReservationTimeSearch availableReservationTimeSearch
                = new AvailableReservationTimeSearch(LocalDate.parse(DATE_MAY_EIGHTH), 1L);
        given(reservationDao.findTimeIds(availableReservationTimeSearch)).willReturn(List.of(1L));
        given(reservationTimeDao.findAll())
                .willReturn(List.of(reservedTime, RESERVATION_TIME_SEVEN(2L)));

        // when
        final List<AvailableReservationTimeResponse> availableReservationTimes
                = reservationTimeService.findAvailableReservationTimes(availableReservationTimeSearch);

        // then
        assertAll(() -> {
            assertThat(isReserved(availableReservationTimes, START_AT_SIX)).isTrue();
            assertThat(isReserved(availableReservationTimes, START_AT_SEVEN)).isFalse();
        });
    }

    private boolean isReserved(final List<AvailableReservationTimeResponse> availableReservationTimes, final String time) {
        return availableReservationTimes.stream()
                .filter(response -> response.startAt().equals(time))
                .findFirst()
                .get()
                .isReserved();
    }
}
