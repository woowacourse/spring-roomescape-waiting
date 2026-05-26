package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.fixture.ReservationTimeFixture;
import roomescape.domain.fixture.ThemeFixture;

class ReservationTest {

    private ReservationTime reservationTime = ReservationTimeFixture.createDefault();
    private Theme theme = ThemeFixture.createDefaultTheme();

    @Test
    void 정상적인_예약_정보를_생성한다() {
        // given
        String name = "이프";
        LocalDate date = LocalDate.now().plusDays(1);

        // when
        Reservation reservation = Reservation.createNew(name, date, theme, reservationTime);

        // then
        assertThat(reservation)
                .extracting(Reservation::getName, Reservation::getDate, Reservation::getTime)
                .containsExactly(name,  date, reservationTime);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void 예약자_이름이_비어있을_경우_예외가_발생한다(String invalidName) {
        // given
        LocalDate date = LocalDate.now().plusDays(1);

        // when & then
        assertThatThrownBy(() -> Reservation.createNew(invalidName, date, theme, reservationTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자 정보는 비어있을 수 없습니다.");
    }

    @ParameterizedTest(name = "날짜 {0}, 테마 {1}, 시간 {2} 일 때, {3} 예외가 발생한다")
    @MethodSource("roomescape.domain.fixture.ReservationFixture#invalidReservationConstructor")
    void 예약_일시와_테마_검증_통합_테스트(LocalDate date, Theme theme, ReservationTime reservationTime, String expectedMessage) {
        // when & then
        assertThatThrownBy(() -> Reservation.createNew("이프", date, theme, reservationTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void 예약의_날짜와_시간을_정상적으로_변경한다() {
        // given
        Reservation reservation = Reservation.createNew("이프", LocalDate.now().plusDays(1), theme, reservationTime);

        LocalDate newDate = LocalDate.now().plusDays(2);
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(15, 0), TimeStatus.ACTIVE);

        // when
        reservation.update(newDate, newTime);

        // then
        assertThat(reservation)
                .extracting(Reservation::getDate, Reservation::getTime)
                .containsExactly(newDate, newTime);
    }

    @Test
    void 이미_지나버린_예약을_변경하려고_하면_예외가_발생한다() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation pastReservation = new Reservation(1L, "이프", pastDate, theme, reservationTime);

        // when & then
        assertThatThrownBy(pastReservation::validateNotPast)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 지난 예약입니다.");
    }

    @Test
    void 이미_지나간_과거_일시로_예약을_변경하려고_하면_예외가_발생한다() {
        // given
        Reservation reservation = Reservation.createNew("이프", LocalDate.now().plusDays(1), theme, reservationTime);
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // when & then
        assertThatThrownBy(() -> reservation.update(pastDate, reservationTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이전 날짜로 예약 할 수 없습니다.");
    }

    @Test
    void 같은_날짜와_시간이면_true를_반환한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = Reservation.createNew("이프", date, theme, reservationTime);

        // when
        boolean result = reservation.isSameTime(date, reservationTime);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 날짜나_시간이_다르면_false를_반환한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = Reservation.createNew("이프", date, theme, reservationTime);

        ReservationTime anotherTime = new ReservationTime(2L, LocalTime.of(15, 0), TimeStatus.ACTIVE);

        // when & then
        assertThat(reservation.isSameTime(date.plusDays(1), reservationTime)).isFalse();
        assertThat(reservation.isSameTime(date, anotherTime)).isFalse();
    }
}
