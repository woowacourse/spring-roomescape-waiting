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
    private final Member brown = new Member(1L, "브라운");
    private final Member roji = new Member(2L, "로지");

    @Test
    void member가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(1L, null, slot))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void slot이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(1L, brown, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void id가_모두_존재하면_id로_동등성을_비교한다() {
        Reservation reservation1 = new Reservation(1L, brown, slot);
        Reservation reservation2 = new Reservation(1L, roji, slot);

        assertThat(reservation1).isEqualTo(reservation2);
    }

    @Test
    void id가_다르면_동등하지_않다() {
        Reservation reservation1 = new Reservation(1L, brown, slot);
        Reservation reservation2 = new Reservation(2L, brown, slot);

        assertThat(reservation1).isNotEqualTo(reservation2);
    }

    @Test
    void id가_없으면_member와_slot으로_동등성을_비교한다() {
        Reservation reservation1 = new Reservation(null, brown, slot);
        Reservation reservation2 = new Reservation(null, brown, slot);

        assertThat(reservation1).isEqualTo(reservation2);
    }

    @Test
    void id가_없고_member가_다르면_동등하지_않다() {
        Reservation reservation1 = new Reservation(null, brown, slot);
        Reservation reservation2 = new Reservation(null, roji, slot);

        assertThat(reservation1).isNotEqualTo(reservation2);
    }
}
