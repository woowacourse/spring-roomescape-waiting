package roomescape.reservation.application;

import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.fixture.config.TestConfig;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeCommandRepository;
import roomescape.reservation.ui.dto.request.CreateReservationTimeRequest;

@JdbcTest
@Import(TestConfig.class)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeCommandRepository reservationTimeCommandRepository;

    @ParameterizedTest
    @CsvSource(value = {
            "10:00", "22:00"
    })
    void 예약_시간을_추가한다(final LocalTime startAt) {
        // given
        final CreateReservationTimeRequest request = new CreateReservationTimeRequest(startAt);

        // when & then
        Assertions.assertThatCode(() -> reservationTimeService.create(request))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약_시간을_삭제한다() {
        // given
        final LocalTime startAt = LocalTime.of(20, 28);
        final Long id = reservationTimeCommandRepository.save(new ReservationTime(startAt));

        // when & then
        Assertions.assertThatCode(() -> reservationTimeService.deleteById(id))
                .doesNotThrowAnyException();
    }

    @Test
    void 이미_존재하는_예약_시간을_추가하면_예외가_발생한다() {
        // given
        final LocalTime startAt = LocalTime.of(19, 55);
        reservationTimeCommandRepository.save(new ReservationTime(startAt));

        final CreateReservationTimeRequest request = new CreateReservationTimeRequest(startAt);

        // when & then
        Assertions.assertThatThrownBy(() -> reservationTimeService.create(request))
                .isInstanceOf(AlreadyExistException.class);
    }

    // TODO: 테스트 추가: 해당_예약_시간으로_등록된_예약이_있으면_삭제할_수_없다

    @Test
    void 삭제할_예약_시간이_없으면_예외가_발생한다() {
        // given
        final Long id = Long.MAX_VALUE;

        // when & then
        Assertions.assertThatThrownBy(() -> reservationTimeService.deleteById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
