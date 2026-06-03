package roomescape.reservationtime.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.NotFoundException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.exception.ReservationTimeInUseException;
import roomescape.reservationtime.domain.exception.ReservationTimeNotFoundException;
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

    @Nested
    @DisplayName("시간 아이디로 예약 시간을 조회한다")
    class GetById {

        @Test
        void 저장된_예약_시간을_아이디로_조회한다() {
            // given
            final ReservationTime saved = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(12, 0)));

            // when
            final ReservationTime reservationTime = reservationTimeService.getById(saved.getId());

            // then
            assertThat(reservationTime.getId()).isEqualTo(saved.getId());
            assertThat(reservationTime.getStartAt()).isEqualTo(saved.getStartAt());
        }

        @Test
        void 존재하지_않는_예약_시간의_아이디로_조회하면_예외가_발생한다() {
            // given
            final long unsavedId = 999L;

            // when & then
            assertThatThrownBy(() -> reservationTimeService.getById(unsavedId))
                .isInstanceOf(ReservationTimeNotFoundException.class);
        }
    }

    @Test
    void 예약_시간을_생성한다() {
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
    void 존재하지_않는_예약_시간을_삭제하면_예외가_발생한다() {
        // given
        reservationTimeRepository.failToDelete();

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 해당_시간에_예약이_있으면_예약_시간_삭제시_예외가_발생한다() {
        // given
        reservationTimeRepository.failToDeleteByInUse();

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(ReservationTimeInUseException.class);
    }

}
