package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReservationTest {

    private final ReservationSlot slot = new ReservationSlot(
            LocalDate.of(2026, 6, 1),
            new ReservationTime(1L, LocalTime.of(10, 0)),
            new Theme(1L, "방탈출1", "설명", "https://thumb.com")
    );

    @Test
    void name이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(1L, null, slot))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void slot이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void id가_모두_존재하면_id로_동등성을_비교한다() {
        Reservation reservation1 = new Reservation(1L, "브라운", slot);
        Reservation reservation2 = new Reservation(1L, "로지", slot);

        assertThat(reservation1).isEqualTo(reservation2);
    }

    @Test
    void id가_다르면_동등하지_않다() {
        Reservation reservation1 = new Reservation(1L, "브라운", slot);
        Reservation reservation2 = new Reservation(2L, "브라운", slot);

        assertThat(reservation1).isNotEqualTo(reservation2);
    }

    @Test
    void id가_없으면_name과_slot으로_동등성을_비교한다() {
        Reservation reservation1 = new Reservation(null, "브라운", slot);
        Reservation reservation2 = new Reservation(null, "브라운", slot);

        assertThat(reservation1).isEqualTo(reservation2);
    }

    @Test
    void id가_없고_name이_다르면_동등하지_않다() {
        Reservation reservation1 = new Reservation(null, "브라운", slot);
        Reservation reservation2 = new Reservation(null, "로지", slot);

        assertThat(reservation1).isNotEqualTo(reservation2);
    }
}
