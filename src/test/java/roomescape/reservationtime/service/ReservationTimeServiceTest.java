package roomescape.reservationtime.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.NotFoundException;
import roomescape.reservationtime.domain.exception.ReservationTimeInUseException;
import roomescape.reservationtime.service.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.service.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.service.support.FakeReservationTimeRepository;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTimeServiceTest {

    private FakeReservationTimeRepository reservationTimeRepository;
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationTimeService = new ReservationTimeService(reservationTimeRepository);
    }

    @Test
    @DisplayName("예약 시간을 생성한다")
    void createReservationTime() {
        // when
        ReservationTimeResponse response = reservationTimeService.create(
                new ReservationTimeCreateRequest(
                        LocalTime.of(10, 0)
                )
        );

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reservationTimeRepository.savedTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하면 예외가 발생한다")
    void throwExceptionWhenDeletingNonExistingReservationTime() {
        // given
        reservationTimeRepository.failToDelete();

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("해당 시간에 예약이 있으면 예약 시간 삭제시 예외가 발생한다")
    void throwExceptionWhenDeletingReservationTimeInUse() {
        // given
        reservationTimeRepository.failToDeleteByInUse();

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(ReservationTimeInUseException.class);
    }

}
