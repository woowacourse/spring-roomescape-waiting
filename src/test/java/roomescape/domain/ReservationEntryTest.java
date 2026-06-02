package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.domain.fixture.ReservationFixture.FIXED;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ReservationEntryTest {

    @Test
    void 예약_엔트리를_생성한다() {
        // when
        ReservationEntry entry = ReservationEntry.reserve("이프", FIXED);

        // then
        assertThat(entry)
                .extracting(
                        ReservationEntry::getId,
                        ReservationEntry::getName,
                        ReservationEntry::getStatus
                )
                .containsExactly(null, "이프", ReservationStatus.RESERVED);
        assertThat(entry.getCreatedAt()).isNotNull();
    }

    @Test
    void 대기_엔트리를_생성한다() {
        // when
        ReservationEntry entry = ReservationEntry.waiting("이프", FIXED);

        // then
        assertThat(entry)
                .extracting(
                        ReservationEntry::getId,
                        ReservationEntry::getName,
                        ReservationEntry::getStatus
                )
                .containsExactly(null, "이프", ReservationStatus.WAITING);
        assertThat(entry.getCreatedAt()).isNotNull();
    }

    @Test
    void 예약_상태이면_true를_반환한다() {
        // given
        ReservationEntry entry = entry(1L, ReservationStatus.RESERVED);

        // when & then
        assertThat(entry.isReserved()).isTrue();
    }

    @Test
    void 대기_상태이면_true를_반환한다() {
        // given
        ReservationEntry entry = entry(1L, ReservationStatus.WAITING);

        // when & then
        assertThat(entry.isWaiting()).isTrue();
    }

    @Test
    void 같은_식별자이면_true를_반환한다() {
        // given
        ReservationEntry entry = entry(1L, ReservationStatus.RESERVED);

        // when & then
        assertThat(entry.isSameId(1L)).isTrue();
    }

    @Test
    void 식별자가_없으면_false를_반환한다() {
        // given
        ReservationEntry entry = entry(null, ReservationStatus.RESERVED);

        // when & then
        assertThat(entry.isSameId(1L)).isFalse();
    }

    @Test
    void 엔트리를_취소한다() {
        // given
        ReservationEntry entry = entry(1L, ReservationStatus.RESERVED);

        // when
        ReservationEntry cancelled = entry.cancel();

        // then
        assertThat(cancelled.getStatus()).isEqualTo(ReservationStatus.DELETED);
    }

    @Test
    void 엔트리를_예약으로_승격한다() {
        // given
        ReservationEntry entry = entry(1L, ReservationStatus.WAITING);

        // when
        ReservationEntry promoted = entry.promote();

        // then
        assertThat(promoted.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @ParameterizedTest
    @EnumSource(ReservationStatus.class)
    void status에_맞는_엔트리로_변환한다(ReservationStatus status) {
        // when
        ReservationEntry entry = ReservationEntry.from(1L, "이프", status, FIXED);

        // then
        assertThat(entry.getStatus()).isEqualTo(status);
    }

    @Test
    void 삭제_상태이면_비활성이다() {
        // given
        ReservationEntry entry = entry(1L, ReservationStatus.DELETED);

        // when & then
        assertThat(entry.isActive()).isFalse();
    }

    private ReservationEntry entry(Long id, ReservationStatus status) {
        return ReservationEntry.from(id, "이프", status, LocalDateTime.now());
    }
}
