package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.fake.FakeReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationValidatorTest {

    private ReservationRepository reservationRepository;
    private ReservationValidator reservationValidator;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationValidator = new ReservationValidator(reservationRepository);
    }

    @Test
    @DisplayName("중복 예약이 없으면 예외가 발생하지 않는다")
    void validateAlreadyReservation_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        ReservationCreateCommand command = new ReservationCreateCommand(
                "인직",
                LocalDate.now().plusDays(1),
                time.getId(),
                theme.getId()
        );

        // when & then
        assertThatCode(() -> reservationValidator.validateAlreadyReservation(command))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("중복 예약이 있으면 예외가 발생한다")
    void validateAlreadyReservation_fail_with_duplicate_reservation() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now().plusDays(1);
        reservationRepository.save(Reservation.create("브라운", date, time, theme));
        ReservationCreateCommand command = new ReservationCreateCommand(
                "인직",
                date,
                time.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationValidator.validateAlreadyReservation(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
    }

    @Test
    @DisplayName("자기 자신의 예약 시간으로 수정하면 예외가 발생하지 않는다")
    void validateAlreadyReservationExcludingSelf_success_when_self_reservation() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationRepository.save(Reservation.create("인직", date, time, theme));
        ReservationUpdateCommand command = new ReservationUpdateCommand(
                savedReservation.getId(),
                date,
                time.getId(),
                savedReservation.getName()
        );

        // when & then
        assertThatCode(() -> reservationValidator.validateAlreadyReservationExcludingSelf(
                command,
                savedReservation
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다른 예약과 시간이 중복되면 수정 시 예외가 발생한다")
    void validateAlreadyReservationExcludingSelf_fail_with_duplicate_reservation() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        ReservationTime otherTime = ReservationTime.createRow(2L, LocalTime.of(11, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now().plusDays(1);
        reservationRepository.save(Reservation.create("브라운", date, time, theme));
        Reservation targetReservation = reservationRepository.save(
                Reservation.create("인직", date, otherTime, theme)
        );
        ReservationUpdateCommand command = new ReservationUpdateCommand(
                targetReservation.getId(),
                date,
                time.getId(),
                targetReservation.getName()
        );

        // when & then
        assertThatThrownBy(() -> reservationValidator.validateAlreadyReservationExcludingSelf(
                command,
                targetReservation
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
    }

}
