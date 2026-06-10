package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.InactiveException;
import roomescape.common.exception.ValidationException;
import roomescape.reservation.domain.fixture.ReservationFixture;
import roomescape.time.domain.fixture.ReservationTimeFixture;
import roomescape.theme.domain.fixture.ThemeFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationTest {

    private final ReservationTime reservationTime = ReservationTimeFixture.createDefaultReservationTime();
    private final Theme theme = ThemeFixture.createDefaultTheme();

    @Test
    void 정상적인_예약_정보를_생성한다() {
        // given
        LocalDate date = LocalDate.now(ReservationFixture.FIXED_CLOCK).plusDays(1);

        // when
        Reservation reservation = Reservation.create("바니", date, reservationTime, theme, Status.RESERVED,
                ReservationFixture.FIXED_CLOCK);

        // then
        assertThat(reservation).extracting(Reservation::getName, Reservation::getDate, Reservation::getTime,
                        Reservation::getTheme, Reservation::getStatus)
                .containsExactly("바니", date, reservationTime, theme, Status.RESERVED);
    }

    @ParameterizedTest(name = "이름 {0}, 날짜 {1}, 테마 {2}, 시간 {3} 일 때, {4} 예외가 발생한다")
    @MethodSource("roomescape.reservation.domain.fixture.ReservationFixture#invalidReservationConstructor")
    void 유효하지_않은_예약_정보로_예약을_생성하면_예외가_발생한다(String name, LocalDate date, Theme theme,
                                                    ReservationTime reservationTime, String expectedMessage) {
        // when & then
        assertThatThrownBy(() -> Reservation.create(name, date, reservationTime, theme, Status.RESERVED,
                ReservationFixture.FIXED_CLOCK))
                .isInstanceOf(ValidationException.class);
    }

    @ParameterizedTest(name = "날짜 {0}, 테마 {1}, 시간 {2} 일 때, 과거 예약 일시 예외가 발생한다")
    @MethodSource("roomescape.reservation.domain.fixture.ReservationFixture#pastReservationDateTimeConstructor")
    void 과거_시간으로_예약을_생성하면_예외가_발생한다(LocalDate date, Theme theme, ReservationTime reservationTime,
                                       String expectedMessage) {
        // when & then
        assertThatThrownBy(() -> Reservation.create("바니", date, reservationTime, theme, Status.RESERVED,
                ReservationFixture.FIXED_CLOCK))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void 예약을_취소하면_예약_상태는_취소_상태가_된다() {
        // given
        Reservation reservation = ReservationFixture.createDefaultReservation();

        // when
        Reservation canceledReservation = reservation.cancel();

        // then
        assertThat(canceledReservation.getStatus()).isEqualTo(Status.CANCELED);
    }

    @Test
    void 예약을_수정한다() {
        // given
        Reservation reservation = ReservationFixture.createDefaultReservation();
        LocalDate changedDate = LocalDate.now(ReservationFixture.FIXED_CLOCK).plusDays(2);
        ReservationTime changedTime = ReservationTime.create(LocalTime.of(11, 0));
        Theme changedTheme = Theme.create("추리테마", "https://image.com/other.png", "추리 테마입니다.");

        // when
        Reservation changedReservation = reservation.modify(changedDate, changedTime, changedTheme, Status.WAITING,
                ReservationFixture.FIXED_CLOCK);

        // then
        assertThat(changedReservation).extracting(Reservation::getName, Reservation::getDate, Reservation::getTime,
                        Reservation::getTheme, Reservation::getStatus, Reservation::getCreatedAt)
                .containsExactly(reservation.getName(), changedDate, changedTime, changedTheme, Status.WAITING,
                        reservation.getCreatedAt());
    }

    @Test
    void 취소된_예약은_수정할_수_없다() {
        // given
        Reservation canceledReservation = ReservationFixture.createDefaultReservation().cancel();

        // when & then
        assertThatThrownBy(() -> canceledReservation.modify(canceledReservation.getDate(), reservationTime, theme,
                Status.RESERVED, ReservationFixture.FIXED_CLOCK))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void 이미_지난_예약은_수정할_수_없다() {
        // given
        Reservation reservation = Reservation.restore(1L, "바니", LocalDate.now(ReservationFixture.FIXED_CLOCK),
                ReservationTime.create(LocalTime.MIN), theme, Status.RESERVED,
                ReservationFixture.FIXED_CLOCK.instant().atZone(ReservationFixture.FIXED_CLOCK.getZone())
                        .toLocalDateTime());

        // when & then
        assertThatThrownBy(() -> reservation.modify(LocalDate.now(ReservationFixture.FIXED_CLOCK).plusDays(1),
                reservationTime, theme, Status.RESERVED, ReservationFixture.FIXED_CLOCK))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void 비활성화된_테마로_예약을_생성하면_예외가_발생한다() {
        // given
        Theme inactiveTheme = theme.deactivate();
        LocalDate date = LocalDate.now(ReservationFixture.FIXED_CLOCK).plusDays(1);

        // when & then
        assertThatThrownBy(() -> Reservation.create("바니", date, reservationTime, inactiveTheme, Status.RESERVED,
                ReservationFixture.FIXED_CLOCK))
                .isInstanceOf(InactiveException.class);
    }

    @Test
    void 비활성화된_시간으로_예약을_생성하면_예외가_발생한다() {
        // given
        ReservationTime inactiveTime = reservationTime.deactivate();
        LocalDate date = LocalDate.now(ReservationFixture.FIXED_CLOCK).plusDays(1);

        // when & then
        assertThatThrownBy(() -> Reservation.create("바니", date, inactiveTime, theme, Status.RESERVED,
                ReservationFixture.FIXED_CLOCK))
                .isInstanceOf(InactiveException.class);
    }

    @Test
    void 예약에_ID를_부여한다() {
        // given
        Reservation reservation = ReservationFixture.createDefaultReservation();

        // when
        Reservation savedReservation = reservation.withId(1L);

        // then
        assertThat(savedReservation).extracting(Reservation::getId, Reservation::getName, Reservation::getDate,
                        Reservation::getTime, Reservation::getTheme, Reservation::getStatus)
                .containsExactly(1L, reservation.getName(), reservation.getDate(), reservation.getTime(),
                        reservation.getTheme(), reservation.getStatus());
    }

    @Test
    void 대기_예약을_승인_상태로_변경한다() {
        // given
        Reservation waitingReservation = ReservationFixture.createWaitingReservation("바니", theme, reservationTime);

        // when
        Reservation reservedReservation = waitingReservation.reserved();

        // then
        assertThat(reservedReservation.isReserved()).isTrue();
        assertThat(reservedReservation.isWaiting()).isFalse();
    }

    @Test
    void 예약자_이름으로_소유자를_확인한다() {
        // given
        Reservation reservation = ReservationFixture.createDefaultReservation("바니");

        // when & then
        assertThat(reservation.isOwner("바니")).isTrue();
        assertThat(reservation.isOwner("포비")).isFalse();
    }
}
