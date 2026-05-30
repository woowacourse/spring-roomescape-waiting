package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReservationEntriesTest {

    @Test
    void 예약_엔트리를_추가한다() {
        // given
        ReservationEntries entries = new ReservationEntries(List.of());

        // when
        entries.addReserved("이프");

        // then
        assertThat(entries.getEntries())
                .singleElement()
                .extracting(ReservationEntry::getName, ReservationEntry::getStatus)
                .containsExactly("이프", ReservationStatus.RESERVED);
    }

    @Test
    void 대기_엔트리를_추가한다() {
        // given
        ReservationEntries entries = new ReservationEntries(List.of());

        // when
        entries.addWaiting("이프");

        // then
        assertThat(entries.getEntries())
                .singleElement()
                .extracting(ReservationEntry::getName, ReservationEntry::getStatus)
                .containsExactly("이프", ReservationStatus.WAITING);
    }

    @Test
    void 예약된_엔트리가_있으면_true를_반환한다() {
        // given
        ReservationEntries entries = new ReservationEntries(List.of(
                entry(1L, "이프", ReservationStatus.WAITING, LocalDateTime.now()),
                entry(2L, "라텔", ReservationStatus.RESERVED, LocalDateTime.now())
        ));

        // when & then
        assertThat(entries.hasReservedEntry()).isTrue();
    }

    @Test
    void 예약된_엔트리가_없으면_false를_반환한다() {
        // given
        ReservationEntries entries = new ReservationEntries(List.of(
                entry(1L, "이프", ReservationStatus.WAITING, LocalDateTime.now()),
                entry(2L, "라텔", ReservationStatus.DELETED, LocalDateTime.now())
        ));

        // when & then
        assertThat(entries.hasReservedEntry()).isFalse();
    }

    @Test
    void 식별자로_엔트리를_조회한다() {
        // given
        ReservationEntry expected = entry(2L, "라텔", ReservationStatus.WAITING, LocalDateTime.now());
        ReservationEntries entries = new ReservationEntries(List.of(
                entry(1L, "이프", ReservationStatus.RESERVED, LocalDateTime.now()),
                expected
        ));

        // when & then
        assertThat(entries.findById(2L)).contains(expected);
    }

    @Test
    void 식별자가_없는_엔트리는_식별자로_조회되지_않는다() {
        // given
        ReservationEntries entries = new ReservationEntries(List.of(
                entry(null, "이프", ReservationStatus.RESERVED, LocalDateTime.now())
        ));

        // when & then
        assertThat(entries.findById(1L)).isEmpty();
    }

    @Test
    void 활성_상태의_이름이_존재하면_true를_반환한다() {
        // given
        ReservationEntries entries = new ReservationEntries(List.of(
                entry(1L, "이프", ReservationStatus.RESERVED, LocalDateTime.now()),
                entry(2L, "라텔", ReservationStatus.DELETED, LocalDateTime.now())
        ));

        // when & then
        assertThat(entries.hasActiveEntryByName("이프")).isTrue();
        assertThat(entries.hasActiveEntryByName("라텔")).isFalse();
    }

    @Test
    void 대기_상태도_활성으로_간주한다() {
        // given
        ReservationEntries entries = new ReservationEntries(List.of(
                entry(1L, "이프", ReservationStatus.WAITING, LocalDateTime.now())
        ));

        // when & then
        assertThat(entries.hasActiveEntryByName("이프")).isTrue();
    }

    @Test
    void 가장_먼저_등록된_대기_엔트리를_예약으로_승격한다() {
        // given
        ReservationEntry reserved = entry(1L, "이프", ReservationStatus.RESERVED, LocalDateTime.now());
        ReservationEntry firstWaiting = entry(2L, "라텔", ReservationStatus.WAITING, LocalDateTime.now().minusMinutes(2));
        ReservationEntry secondWaiting = entry(3L, "이든", ReservationStatus.WAITING, LocalDateTime.now().minusMinutes(1));
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
                entry(1L, "이프", ReservationStatus.RESERVED, LocalDateTime.now()),
                entry(2L, "라텔", ReservationStatus.DELETED, LocalDateTime.now())
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

    private ReservationEntry entry(Long id, String name, ReservationStatus status, LocalDateTime createdAt) {
        return ReservationEntry.from(id, name, status, createdAt);
    }
}
