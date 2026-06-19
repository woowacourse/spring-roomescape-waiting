package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static roomescape.support.TestDateTimes.FIXED;

import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.EntityNotFoundException;

class ReservationEntriesTest {

    private ReservationEntry entry(Long id, String name, ReservationStatus status, LocalDateTime createdAt) {
        return ReservationEntry.restore(id, name, status, createdAt);
    }

    @Nested
    class 추가 {
        @Test
        void 예약_엔트리를_추가한다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of());

            // when
            entries.addReserved("이프", FIXED);

            // then
            assertThat(entries.getEntries())
                    .singleElement()
                    .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                    .containsExactly("이프", ReservationStatus.RESERVED);
        }

        @Test
        void 대기_엔트리를_추가한다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of());

            // when
            entries.addWaiting("이프", FIXED);

            // then
            assertThat(entries.getEntries())
                    .singleElement()
                    .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                    .containsExactly("이프", ReservationStatus.WAITING);
        }

        @Test
        void 결제_대기_엔트리를_추가한다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of());

            // when
            entries.addPending("이프", FIXED);

            // then
            assertThat(entries.getEntries())
                    .singleElement()
                    .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                    .containsExactly("이프", ReservationStatus.PENDING);
        }
    }

    @Nested
    class 조회 {
        @Test
        void 예약된_엔트리가_있으면_true를_반환한다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of(
                    entry(1L, "이프", ReservationStatus.WAITING, FIXED),
                    entry(2L, "라텔", ReservationStatus.RESERVED, FIXED)
            ));

            // when & then
            assertThat(entries.hasReservedEntry()).isTrue();
        }

        @Test
        void 예약된_엔트리가_없으면_false를_반환한다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of(
                    entry(1L, "이프", ReservationStatus.WAITING, FIXED),
                    entry(2L, "라텔", ReservationStatus.DELETED, FIXED)
            ));

            // when & then
            assertThat(entries.hasReservedEntry()).isFalse();
        }

        @Test
        void 결제_대기_엔트리가_있으면_hasReservedEntry가_true를_반환한다() {
            // given: PENDING 상태는 슬롯을 선점한다
            ReservationEntries entries = new ReservationEntries(List.of(
                    entry(1L, "이프", ReservationStatus.PENDING, FIXED)
            ));

            // when & then
            assertThat(entries.hasReservedEntry()).isTrue();
        }

        @Test
        void 식별자로_엔트리를_조회한다() {
            // given
            ReservationEntry expected = entry(2L, "라텔", ReservationStatus.WAITING, FIXED);
            ReservationEntries entries = new ReservationEntries(List.of(
                    entry(1L, "이프", ReservationStatus.RESERVED, FIXED),
                    expected
            ));

            // when & then
            assertThat(entries.findById(2L)).contains(expected);
        }

        @Test
        void 식별자가_없는_엔트리는_식별자로_조회되지_않는다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of(
                    ReservationEntry.reserve("이프", FIXED)
            ));

            // when & then
            assertThat(entries.findById(1L)).isEmpty();
        }

        @Test
        void 활성_상태의_이름이_존재하면_true를_반환한다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of(
                    entry(1L, "이프", ReservationStatus.RESERVED, FIXED),
                    entry(2L, "라텔", ReservationStatus.DELETED, FIXED)
            ));

            // when & then
            assertThat(entries.hasActiveEntryByName("이프")).isTrue();
            assertThat(entries.hasActiveEntryByName("라텔")).isFalse();
        }

        @Test
        void 대기_상태도_활성으로_간주한다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of(
                    entry(1L, "이프", ReservationStatus.WAITING, FIXED)
            ));

            // when & then
            assertThat(entries.hasActiveEntryByName("이프")).isTrue();
        }

        @Test
        void 이름과_상태로_엔트리를_조회한다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of(
                    ReservationEntry.reserve("라텔", FIXED)
            ));

            // when & then
            Assertions.assertThat(entries.findByNameAndStatus("라텔", ReservationStatus.RESERVED))
                    .isPresent();
        }

        @Test
        void 상태는_같지만_이름이_다르면_조회되지_않는다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of(
                    ReservationEntry.waiting("라텔", FIXED)
            ));

            // when & then
            Assertions.assertThat(entries.findByNameAndStatus("이프", ReservationStatus.WAITING))
                    .isNotPresent();
        }
    }

    @Nested
    class 대기_승격 {
        @Test
        void 가장_먼저_등록된_대기_엔트리를_예약으로_승격한다() {
            // given
            ReservationEntry reserved = entry(1L, "이프", ReservationStatus.RESERVED, FIXED);
            ReservationEntry firstWaiting = entry(2L, "라텔", ReservationStatus.WAITING, FIXED.minusMinutes(2));
            ReservationEntry secondWaiting = entry(3L, "이든", ReservationStatus.WAITING, FIXED.minusMinutes(1));
            ReservationEntries entries = new ReservationEntries(List.of(reserved, firstWaiting, secondWaiting));

            // when
            entries.promoteFirstWaiting();

            // then
            assertThat(entries.getEntries())
                    .extracting(ReservationEntry::getId, ReservationEntry::getStatus)
                    .containsExactly(
                            tuple(1L, ReservationStatus.RESERVED),
                            tuple(2L, ReservationStatus.RESERVED),
                            tuple(3L, ReservationStatus.WAITING)
                    );
        }

        @Test
        void 대기_엔트리가_없으면_승격하지_않는다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of(
                    entry(1L, "이프", ReservationStatus.RESERVED, FIXED),
                    entry(2L, "라텔", ReservationStatus.DELETED, FIXED)
            ));

            // when
            entries.promoteFirstWaiting();

            // then
            assertThat(entries.getEntries())
                    .extracting(ReservationEntry::getId, ReservationEntry::getStatus)
                    .containsExactly(
                            tuple(1L, ReservationStatus.RESERVED),
                            tuple(2L, ReservationStatus.DELETED)
                    );
        }
    }

    @Nested
    class 취소 {
        @Test
        void 엔트리를_취소하면_삭제_상태가_되고_나머지는_유지된다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of(
                    entry(1L, "이프", ReservationStatus.RESERVED, FIXED),
                    entry(2L, "라텔", ReservationStatus.WAITING, FIXED)
            ));

            // when
            entries.cancel(1L);

            // then
            assertThat(entries.getEntries())
                    .extracting(ReservationEntry::getId, ReservationEntry::getStatus)
                    .containsExactly(
                            tuple(1L, ReservationStatus.DELETED),
                            tuple(2L, ReservationStatus.WAITING)
                    );
        }

        @Test
        void 존재하지_않는_식별자로_취소하면_예외가_발생한다() {
            // given
            ReservationEntries entries = new ReservationEntries(List.of(
                    entry(1L, "이프", ReservationStatus.RESERVED, FIXED)
            ));

            // when & then
            assertThatThrownBy(() -> entries.cancel(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
