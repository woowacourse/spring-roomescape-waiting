package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;

class WaitlistOrderPolicyTest {

    private final WaitlistOrderPolicy waitlistOrderPolicy = new WaitlistOrderPolicy();

    @Test
    void 대기_목록을_생성시각과_id_순서로_순번을_계산한다() {
        LocalDateTime firstCreatedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime secondCreatedAt = firstCreatedAt.plusMinutes(1);

        Waitlist second = createWaitlist(2L, "브리", secondCreatedAt);
        Waitlist first = createWaitlist(1L, "네오", firstCreatedAt);
        Waitlist third = createWaitlist(3L, "포비", secondCreatedAt);

        int order = waitlistOrderPolicy.calculateOrder(third, List.of(second, third, first));

        assertThat(order).isEqualTo(3);
    }

    @Test
    void 대기_목록을_생성시각과_id_순서로_승격_대상을_선택한다() {
        LocalDateTime firstCreatedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime secondCreatedAt = firstCreatedAt.plusMinutes(1);

        Waitlist second = createWaitlist(2L, "브리", secondCreatedAt);
        Waitlist first = createWaitlist(1L, "네오", firstCreatedAt);
        Waitlist third = createWaitlist(3L, "포비", secondCreatedAt);

        assertThat(waitlistOrderPolicy.selectPromotionTarget(List.of(second, third, first)))
            .contains(first);
    }

    @Test
    void 대기_목록이_비어있으면_승격_대상을_선택하지_않는다() {
        assertThat(waitlistOrderPolicy.selectPromotionTarget(List.of()))
            .isEmpty();
    }

    private Waitlist createWaitlist(Long id, String name, LocalDateTime createdAt) {
        return new Waitlist(
            id,
            new Member(name),
            Slot.of(
                LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "방탈출 제목", "방탈출 설명", "thumbnail.png")
            ),
            createdAt
        );
    }
}
