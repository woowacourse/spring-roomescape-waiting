package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;

public class SlotTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "무서운 이야기", "공포", "example.com");
    private final static LocalDate tomorrow = LocalDate.now().plusDays(1);

    @Test
    void isExpired는_슬롯의_만료_여부를_반환한다() {
        Slot futureSlot = Slot.restore(1L, LocalDate.now().plusDays(1), reservationTime, theme);
        Slot pastSlot = Slot.restore(1L, LocalDate.now().minusDays(1), reservationTime, theme);

        assertThat(futureSlot.isExpired()).isFalse();
        assertThat(pastSlot.isExpired()).isTrue();
    }

    @Test
    void isEqualSlot은_날짜_시간_테마가_모두_같으면_true를_반환한다() {
        Slot slot = Slot.restore(1L, tomorrow, reservationTime, theme);

        assertThat(slot.isEqualSlot(tomorrow, 1L, 1L)).isTrue();
    }

    @Test
    void isEqualSlot은_날짜가_다르면_false를_반환한다() {
        Slot slot = Slot.restore(1L, tomorrow, reservationTime, theme);

        assertThat(slot.isEqualSlot(tomorrow.plusDays(1), 1L, 1L)).isFalse();
    }

    @Test
    void isEqualSlot은_시간이_다르면_false를_반환한다() {
        Slot slot = Slot.restore(1L, tomorrow, reservationTime, theme);

        assertThat(slot.isEqualSlot(tomorrow, 2L, 1L)).isFalse();
    }

    @Test
    void isEqualSlot은_테마가_다르면_false를_반환한다() {
        Slot slot = Slot.restore(1L, tomorrow, reservationTime, theme);

        assertThat(slot.isEqualSlot(tomorrow, 1L, 2L)).isFalse();
    }

    @Test
    void withId는_원본을_변경하지_않고_id가_부여된_새_슬롯을_반환한다() {
        Slot slot = Slot.create(tomorrow, reservationTime, theme);

        Slot withId = slot.withId(5L);

        assertThat(slot.getId()).isNull();
        assertThat(withId.getId()).isEqualTo(5L);
        assertThat(withId.getDate()).isEqualTo(slot.getDate());
        assertThat(withId.getTime()).isEqualTo(reservationTime);
        assertThat(withId.getTheme()).isEqualTo(theme);
    }
}
