package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingsTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 5);

    private Slot slot(long timeId, long themeId) {
        ReservationTime time = ReservationTime.create(timeId, LocalTime.of(10, 0));
        Theme theme = Theme.create(themeId, "테마", "url", "설명");
        return new Slot(DATE, time, theme);
    }

    private Waiting waiting(long id, String name, Slot slot, LocalDateTime createdAt) {
        return Waiting.create(id, new Member(name), slot, createdAt);
    }

    @Test
    @DisplayName("같은 슬롯의 대기는 생성 시각 순으로 1, 2, 3 순위를 가진다.")
    void rankBySameSlot() {
        Slot slot = slot(1, 1);
        Waiting first = waiting(1, "a", slot, LocalDateTime.of(2026, 6, 1, 10, 0));
        Waiting second = waiting(2, "b", slot, LocalDateTime.of(2026, 6, 1, 11, 0));
        Waiting third = waiting(3, "c", slot, LocalDateTime.of(2026, 6, 1, 12, 0));

        Waitings waitings = new Waitings(List.of(third, first, second));

        assertThat(waitings.rankOf(first)).isEqualTo(1);
        assertThat(waitings.rankOf(second)).isEqualTo(2);
        assertThat(waitings.rankOf(third)).isEqualTo(3);
    }

    @Test
    @DisplayName("생성 시각이 같으면 id가 작은 쪽이 앞 순위가 된다.")
    void rankTieBreakById() {
        Slot slot = slot(1, 1);
        LocalDateTime sameTime = LocalDateTime.of(2026, 6, 1, 10, 0);
        Waiting smallerId = waiting(10, "a", slot, sameTime);
        Waiting biggerId = waiting(20, "b", slot, sameTime);

        Waitings waitings = new Waitings(List.of(biggerId, smallerId));

        assertThat(waitings.rankOf(smallerId)).isEqualTo(1);
        assertThat(waitings.rankOf(biggerId)).isEqualTo(2);
    }

    @Test
    @DisplayName("슬롯이 다르면 순위는 서로 독립적으로 매겨진다.")
    void rankIndependentPerSlot() {
        LocalDateTime time = LocalDateTime.of(2026, 6, 1, 10, 0);
        Waiting inSlotA = waiting(1, "a", slot(1, 1), time);
        Waiting inSlotB = waiting(2, "b", slot(2, 1), time);

        Waitings waitings = new Waitings(List.of(inSlotA, inSlotB));

        assertThat(waitings.rankOf(inSlotA)).isEqualTo(1);
        assertThat(waitings.rankOf(inSlotB)).isEqualTo(1);
    }

    @Test
    @DisplayName("이름으로 조회하면 해당 사용자의 대기만 각자의 슬롯 순위와 함께 반환한다.")
    void rankedByName() {
        Slot slotA = slot(1, 1);
        Waiting other = waiting(1, "other", slotA, LocalDateTime.of(2026, 6, 1, 10, 0));
        Waiting mineInA = waiting(2, "me", slotA, LocalDateTime.of(2026, 6, 1, 11, 0));
        Waiting mineInB = waiting(3, "me", slot(2, 1), LocalDateTime.of(2026, 6, 1, 9, 0));

        Waitings waitings = new Waitings(List.of(other, mineInA, mineInB));

        List<WaitingWithRank> result = waitings.rankedBy(new Member("me"));

        assertThat(result)
                .extracting(WaitingWithRank::id, WaitingWithRank::rank)
                .containsExactlyInAnyOrder(
                        tuple(2L, 2),
                        tuple(3L, 1)
                );
    }
}
