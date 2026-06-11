package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReservationWaitingQueueTest {
    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 6, 10);
    private static final ReservationTime TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "방탈출1", "설명", "https://thumb.com");

    @Test
    void 신청_순서대로_예약_대기_순번을_계산한다() {
        ReservationWaiting first = waiting(1L,"브라운",LocalDateTime.of(2026,6,1,10,0));
        ReservationWaiting second = waiting(2L,"로지",LocalDateTime.of(2026,6,1,11,0));
        ReservationWaiting third = waiting(3L,"맥스",LocalDateTime.of(2026,6,1,12,0));

        ReservationWaitingQueue queue = new ReservationWaitingQueue(List.of(first,second,third));
        int order = queue.orderOf(third);

        assertThat(order).isEqualTo(3);
    }

    @Test
    void 신청_시간이_같으면_id_순서로_예약_대기_순번을_계산한다() {
        ReservationWaiting first = waiting(1L,"브라운",LocalDateTime.of(2026,6,1,10,0));
        ReservationWaiting second = waiting(2L,"로지",LocalDateTime.of(2026,6,1,10,0));


        ReservationWaitingQueue queue = new ReservationWaitingQueue(List.of(first,second));

        int order = queue.orderOf(second);

        assertThat(order).isEqualTo(2);

    }

    @Test
    void 대기열에_없는_예약_대기의_순번은_계산할_수_없다() {
        ReservationWaiting contain = waiting(1L,"브라운",LocalDateTime.of(2026,6,1,10,0));
        ReservationWaiting isNotContain = waiting(2L,"로지",LocalDateTime.of(2026,6,1,10,0));


        ReservationWaitingQueue queue = new ReservationWaitingQueue(List.of(contain));
        assertThatThrownBy(()->queue.orderOf(isNotContain))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ReservationWaiting waiting(Long id, String name, LocalDateTime createdAt) {
        ReservationSlot slot = new ReservationSlot(RESERVATION_DATE, TIME, THEME);
        return new ReservationWaiting(id, name, createdAt, slot);
    }
}
