package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.fake.FakeReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.dto.WaitingCreateCommand;

class WaitingAdapterTest {

    private ReservationRepository reservationRepository;
    private WaitingAdapter waitingAdapter;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        waitingAdapter = new WaitingAdapter(reservationRepository);
    }

    @Test
    @DisplayName("대기하려는 슬롯에 예약이 있으면 예외가 발생하지 않는다")
    void 대기하려는_슬롯에_예약이_있으면_예외가_발생하지_않는다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        reservationRepository.save(Reservation.create("리오", date, time, theme));
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                date,
                time.getId(),
                theme.getId()
        );

        // when & then
        assertThatCode(() -> waitingAdapter.validateExistReservation(command))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대기하려는 슬롯에 예약이 없으면 예외가 발생한다")
    void 대기하려는_슬롯에_예약이_없으면_예외가_발생한다() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                LocalDate.now().plusDays(1),
                time.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingAdapter.validateExistReservation(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("예약이 존재하지 않으면, 대기요청을 할 수 없습니다.");
    }

    @Test
    @DisplayName("본인이 예약한 슬롯에 대기를 신청하면 예외가 발생한다")
    void 본인이_예약한_슬롯에_대기를_신청하면_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        reservationRepository.save(Reservation.create("브라운", date, time, theme));
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                date,
                time.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingAdapter.validateExistReservation(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
    }
}
