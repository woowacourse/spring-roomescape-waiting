package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReservationWaitingTest {

    private final ReservationSlot slot = new ReservationSlot(
            LocalDate.of(2026, 6, 1),
            new ReservationTime(1L, LocalTime.of(10, 0)),
            new Theme(1L, "방탈출1", "설명", "https://thumb.com")
    );
    private final LocalDateTime createdAt = LocalDateTime.of(2026, 5, 1, 12, 0);
    private final Member brown = new Member(1L, "브라운");
    private final Member roji = new Member(2L, "로지");

    @Test
    void member가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new ReservationWaiting(1L, null, createdAt, slot))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createdAt이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new ReservationWaiting(1L, brown, null, slot))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void slot이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new ReservationWaiting(1L, brown, createdAt, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void id가_모두_존재하면_id로_동등성을_비교한다() {
        ReservationWaiting waiting1 = new ReservationWaiting(1L, brown, createdAt, slot);
        ReservationWaiting waiting2 = new ReservationWaiting(1L, roji, createdAt, slot);

        assertThat(waiting1).isEqualTo(waiting2);
    }

    @Test
    void id가_다르면_동등하지_않다() {
        ReservationWaiting waiting1 = new ReservationWaiting(1L, brown, createdAt, slot);
        ReservationWaiting waiting2 = new ReservationWaiting(2L, brown, createdAt, slot);

        assertThat(waiting1).isNotEqualTo(waiting2);
    }

    @Test
    void id가_없으면_member와_createdAt과_slot으로_동등성을_비교한다() {
        ReservationWaiting waiting1 = new ReservationWaiting(null, brown, createdAt, slot);
        ReservationWaiting waiting2 = new ReservationWaiting(null, brown, createdAt, slot);

        assertThat(waiting1).isEqualTo(waiting2);
    }

    @Test
    void id가_없고_createdAt이_다르면_동등하지_않다() {
        ReservationWaiting waiting1 = new ReservationWaiting(null, brown, createdAt, slot);
        ReservationWaiting waiting2 = new ReservationWaiting(null, brown, createdAt.plusMinutes(1), slot);

        assertThat(waiting1).isNotEqualTo(waiting2);
    }
}
