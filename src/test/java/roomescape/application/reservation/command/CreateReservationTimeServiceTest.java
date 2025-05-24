package roomescape.application.reservation.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.command.dto.CreateReservationTimeCommand;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.infrastructure.error.exception.ReservationTimeException;

class CreateReservationTimeServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private CreateReservationTimeService createReservationTimeService;

    @BeforeEach
    void setUp() {
        createReservationTimeService = new CreateReservationTimeService(reservationTimeRepository);
    }

    @Test
    void 운영시간_내의_예약시간을_생성할_수_있다() {
        // given
        CreateReservationTimeCommand createReservationTimeCommand = new CreateReservationTimeCommand(
                LocalTime.of(12, 0)
        );

        // when
        Long timeId = createReservationTimeService.register(createReservationTimeCommand);

        // then
        assertThat(reservationTimeRepository.findById(timeId))
                .isPresent()
                .hasValue(new ReservationTime(1L, LocalTime.of(12, 0)));
    }

    @Test
    void 운영시간_외의_예약시간을_생성할_수_없다() {
        // given
        CreateReservationTimeCommand createReservationTimeCommand = new CreateReservationTimeCommand(
                LocalTime.of(4, 0)
        );

        // when
        // then
        assertThatCode(() -> createReservationTimeService.register(createReservationTimeCommand))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage("해당 시간은 예약 가능 시간이 아닙니다.");
    }

    @Test
    void 이미_존재하는_예약시간을_추가하는_경우_예외가_발생한다() {
        // given
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        CreateReservationTimeCommand createReservationTimeCommand = new CreateReservationTimeCommand(
                LocalTime.of(12, 0)
        );

        // when
        // then
        assertThatCode(() -> createReservationTimeService.register(createReservationTimeCommand))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage("이미 존재하는 예약시간입니다.");
    }
}
