package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.support.TestDateTimes.FIXED;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ReservationEntryTest {

    private ReservationEntry entry(Long id, ReservationStatus status) {
        return ReservationEntry.restore(id, "이프", status, FIXED);
    }

    private ReservationEntry entry(ReservationStatus status) {
        return entry(1L, status);
    }

    @Nested
    class 생성 {
        @Test
        void 예약_엔트리를_생성한다() {
            // when
            ReservationEntry entry = ReservationEntry.reserve("이프", FIXED);

            // then
            assertThat(entry)
                    .extracting(
                            ReservationEntry::getReserverName,
                            ReservationEntry::getStatus
                    )
                    .containsExactly("이프", ReservationStatus.RESERVED);
            assertThat(entry.getCreatedAt()).isNotNull();
        }

        @Test
        void 대기_엔트리를_생성한다() {
            // when
            ReservationEntry entry = ReservationEntry.waiting("이프", FIXED);

            // then
            assertThat(entry)
                    .extracting(
                            ReservationEntry::getReserverName,
                            ReservationEntry::getStatus
                    )
                    .containsExactly("이프", ReservationStatus.WAITING);
            assertThat(entry.getCreatedAt()).isNotNull();
        }

        @ParameterizedTest
        @EnumSource(ReservationStatus.class)
        void status에_맞는_엔트리로_변환한다(ReservationStatus status) {
            // when
            ReservationEntry entry = ReservationEntry.restore(1L, "이프", status, FIXED);

            // then
            assertThat(entry.getStatus()).isEqualTo(status);
        }
    }

    @Nested
    class 상태_판별 {
        @Test
        void 예약_상태이면_true를_반환한다() {
            // given
            ReservationEntry entry = entry(ReservationStatus.RESERVED);

            // when & then
            assertThat(entry.isReserved()).isTrue();
        }

        @Test
        void 대기_상태이면_true를_반환한다() {
            // given
            ReservationEntry entry = entry(ReservationStatus.WAITING);

            // when & then
            assertThat(entry.isWaiting()).isTrue();
        }

        @Test
        void 예약_상태이면_활성화된_예약으로_판단한다() {
            // given
            ReservationEntry reservedEntry = entry(ReservationStatus.RESERVED);

            // when & then
            assertThat(reservedEntry.isActive()).isTrue();
        }

        @Test
        void 대기_상태이면_활성화된_예약으로_판단한다() {
            // given
            ReservationEntry reservedEntry = entry(ReservationStatus.WAITING);

            // when & then
            assertThat(reservedEntry.isActive()).isTrue();
        }

        @Test
        void 삭제_상태이면_비활성이다() {
            // given
            ReservationEntry entry = entry(ReservationStatus.DELETED);

            // when & then
            assertThat(entry.isActive()).isFalse();
        }
    }

    @Nested
    class 식별자_비교 {
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
            ReservationEntry entry = ReservationEntry.reserve("이프", FIXED);

            // when & then
            assertThat(entry.isSameId(1L)).isFalse();
        }
    }

    @Nested
    class 상태_변경 {
        @Test
        void 엔트리를_취소한다() {
            // given
            ReservationEntry entry = entry(ReservationStatus.RESERVED);

            // when
            ReservationEntry cancelled = entry.cancel();

            // then
            assertThat(cancelled.isActive()).isFalse();
        }

        @Test
        void 엔트리를_예약으로_승격한다() {
            // given
            ReservationEntry entry = entry(ReservationStatus.WAITING);

            // when
            ReservationEntry promoted = entry.promote();

            // then
            assertThat(promoted.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        }

        @Test
        void 취소_상태를_예약으로_승격할_수_없다() {
            // given
            ReservationEntry canceledEntry = entry(ReservationStatus.DELETED);

            // when & then
            assertThatThrownBy(canceledEntry::promote).isInstanceOf(IllegalStateException.class);
        }
    }
}
