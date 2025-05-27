package roomescape.reservation.service;

import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.common.exception.DataExistException;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.time.ReservationTimeRepositoryInterface;
import roomescape.reservation.service.time.ReservationTimeService;

class ReservationTimeServiceTest {

    private final ReservationTimeRepositoryInterface reservationTimeRepository = new FakeReservationTimeRepository();
    private final ReservationTimeService reservationTime = new ReservationTimeService(reservationTimeRepository);

    @ParameterizedTest
    @CsvSource(value = {
            "20:00", "22:00"
    })
    void 예약시간을_추가한다(final LocalTime startAt) {
        // when & then
        Assertions.assertThatCode(() -> reservationTime.save(startAt))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약시간을_삭제한다() {
        // given
        final LocalTime startAt = LocalTime.of(20, 28);
        final ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(startAt));

        // when & then
        Assertions.assertThatCode(() -> reservationTime.deleteById(savedReservationTime.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void 이미_존재하는_예약시간을_추가하면_예외가_발생한다() {
        // given
        final LocalTime startAt = LocalTime.of(19, 55);
        reservationTimeRepository.save(new ReservationTime(startAt));

        // when & then
        Assertions.assertThatThrownBy(() -> reservationTime.save(startAt))
                .isInstanceOf(DataExistException.class);
    }
}
