package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.theme.domain.Theme;

class ReservationTest {

    private static final ReservationTime DEFAULT_TIME = ReservationTime.of(1L, LocalTime.of(10, 0));
    private static final LocalDate DEFAULT_DATE = LocalDate.of(2025, 1, 1);
    public static final Theme DEFAULT_THEME = Theme.of(1L, "name", "description", "url");

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void 이름이_공백이면_예외를_던진다(String blankName) {
        // when // then
        assertThatThrownBy(() -> Reservation.of(1L, blankName, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void 상태_전이_테스트_예약에서_취소() {
        // given
        Reservation reservation = Reservation.of(1L, "누누", DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED);

        // when
        reservation.changeStatus(ReservationStatus.CANCELED);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    void 상태_전이_불가_테스트_예약에서_대기() {
        // given
        Reservation reservation = Reservation.of(1L, "누누", DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED);

        // when // then
        assertThatThrownBy(() -> reservation.changeStatus(ReservationStatus.WAITING))
            .isInstanceOf(IllegalStateException.class);
    }
}
