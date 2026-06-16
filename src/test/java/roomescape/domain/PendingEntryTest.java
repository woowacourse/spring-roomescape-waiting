package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.support.TestDateTimes.FIXED;

import org.junit.jupiter.api.Test;

class PendingEntryTest {

    @Test
    void 결제_대기_상태를_반환한다() {
        ReservationEntry entry = PendingEntry.of("이프", FIXED);
        assertThat(entry.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void isPending은_true를_반환한다() {
        ReservationEntry entry = PendingEntry.of("이프", FIXED);
        assertThat(entry.isPending()).isTrue();
    }

    @Test
    void isReserved는_false를_반환한다() {
        ReservationEntry entry = PendingEntry.of("이프", FIXED);
        assertThat(entry.isReserved()).isFalse();
    }

    @Test
    void isWaiting은_false를_반환한다() {
        ReservationEntry entry = PendingEntry.of("이프", FIXED);
        assertThat(entry.isWaiting()).isFalse();
    }

    @Test
    void isActive는_true를_반환한다() {
        ReservationEntry entry = PendingEntry.of("이프", FIXED);
        assertThat(entry.isActive()).isTrue();
    }

    @Test
    void 취소하면_삭제_상태_엔트리가_반환된다() {
        ReservationEntry entry = PendingEntry.restore(1L, "이프", FIXED);
        ReservationEntry cancelled = entry.cancel();
        assertThat(cancelled.getStatus()).isEqualTo(ReservationStatus.DELETED);
    }

    @Test
    void 승격하면_예약_확정_상태_엔트리가_반환된다() {
        ReservationEntry entry = PendingEntry.restore(1L, "이프", FIXED);
        ReservationEntry promoted = entry.promote();
        assertThat(promoted.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(promoted.getId()).isEqualTo(1L);
        assertThat(promoted.getReserverName()).isEqualTo("이프");
    }
}
