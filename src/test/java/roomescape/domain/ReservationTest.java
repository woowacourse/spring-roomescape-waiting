package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static roomescape.support.TestDateTimes.FIXED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.domain.fixture.ReservationTimeFixture;
import roomescape.domain.fixture.ThemeFixture;
import roomescape.exception.DuplicateEntityException;
import roomescape.exception.EntityNotFoundException;
import roomescape.exception.RoomEscapeException;
import roomescape.support.TestDateTimes;

class ReservationTest {

    private final ReservationTime reservationTime = ReservationTimeFixture.createDefault();
    private final Theme theme = ThemeFixture.createDefaultTheme();

    @Test
    void 정상적인_예약_정보를_생성한다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();

        // when
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);

        // then
        assertThat(reservation)
                .extracting(Reservation::getDate, Reservation::getTime)
                .containsExactly(date, reservationTime);
    }

    @ParameterizedTest(name = "날짜 {0}, 테마 {1}, 시간 {2} 일 때, {3} 예외가 발생한다")
    @MethodSource("roomescape.domain.fixture.ReservationFixture#invalidReservationConstructor")
    void 슬롯_생성_시_날짜_테마_시간_누락_검증_통합_테스트(LocalDate date, Theme theme, ReservationTime reservationTime,
                                       String expectedMessage) {
        // when & then
        assertThatThrownBy(() -> Reservation.createSlot(date, theme, reservationTime))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void 예약_정보가_있을_때_예약을_할_수_있다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);

        // when
        reservation.reserve("이프", FIXED);

        // then
        assertThat(reservation.getEntries())
                .singleElement()
                .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                .containsExactly("이프", ReservationStatus.RESERVED);
    }

    @Test
    void 이미_예약된_예약_정보에_예약을_하면_예외가_발생한다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);
        reservation.reserve("이프", FIXED);

        // when & then
        assertThatThrownBy(() -> reservation.reserve("라텔", FIXED))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 된 날짜입니다.");
    }

    @Test
    void 동일_이름으로_예약을_중복_요청하면_예외가_발생한다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);
        reservation.reserve("이프", FIXED);

        // when & then
        assertThatThrownBy(() -> reservation.reserve("이프", FIXED))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 대기가 존재합니다.");
    }

    @Test
    void 대기_신청_시_예약이_있으면_대기로_등록된다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);
        reservation.reserve("이프", FIXED);

        // when
        reservation.reserveOrWait("라텔", FIXED);

        // then
        assertThat(reservation.getEntries())
                .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                .containsExactlyInAnyOrder(
                        tuple("이프", ReservationStatus.RESERVED),
                        tuple("라텔", ReservationStatus.WAITING)
                );
    }

    @Test
    void 대기_신청_시_예약이_없으면_예약으로_승격된다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);

        // when
        reservation.reserveOrWait("라텔", FIXED);

        // then
        assertThat(reservation.getEntries())
                .singleElement()
                .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                .containsExactly("라텔", ReservationStatus.RESERVED);
    }

    @Test
    void 대기_신청_시_동일_이름이_이미_존재하면_예외가_발생한다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);
        reservation.reserve("이프", FIXED);

        // when & then
        assertThatThrownBy(() -> reservation.reserveOrWait("이프", FIXED))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 대기가 존재합니다.");
    }

    @Test
    void 이미_지나버린_예약_정보에_예약을_하면_예외가_발생한다() {
        // given
        LocalDate pastDate = FIXED.minusDays(1).toLocalDate();
        Reservation reservation = Reservation.restore(1L, pastDate, theme, reservationTime, List.of());

        // when & then
        assertThatThrownBy(() -> reservation.reserve("이프", FIXED))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지난 예약입니다.");
    }

    @Test
    void 결제_준비_시_PENDING_엔트리가_추가된다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);

        // when
        reservation.addPendingEntry("이프", 30000L, FIXED);

        // then
        assertThat(reservation.getEntries())
                .singleElement()
                .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                .containsExactly("이프", ReservationStatus.PENDING);
    }

    @Test
    void 이미_예약된_슬롯에_결제_준비하면_예외가_발생한다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);
        reservation.reserve("기존예약자", FIXED);

        // when & then
        assertThatThrownBy(() -> reservation.addPendingEntry("이프", 30000L, FIXED))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 결제 중인 날짜입니다.");
    }

    @Test
    void 이미_결제_중인_슬롯에_결제_준비하면_예외가_발생한다() {
        // given: 다른 사용자가 이미 PENDING으로 선점
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);
        reservation.addPendingEntry("기존예약자", 30000L, FIXED);

        // when & then
        assertThatThrownBy(() -> reservation.addPendingEntry("이프", 30000L, FIXED))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 결제 중인 날짜입니다.");
    }

    @Test
    void 결제_준비_시_동일_이름이면_예외가_발생한다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);
        reservation.addPendingEntry("이프", 30000L, FIXED);

        // when & then
        assertThatThrownBy(() -> reservation.addPendingEntry("이프", 30000L, FIXED))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 대기가 존재합니다.");
    }

    @Test
    void 결제_준비_시_금액이_맞지_않으면_예외가_발생한다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);

        // when & then
        assertThatThrownBy(() -> reservation.addPendingEntry("이프", 9999L, FIXED))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("결제 금액이 테마 금액과 일치하지 않습니다.");
    }

    @Test
    void 예약된_엔트리를_식별자로_조회한다() {
        // given
        Reservation reservation = createReservationWithEntries(List.of(
                entry(1L, "이프", ReservationStatus.RESERVED),
                entry(2L, "라텔", ReservationStatus.WAITING)
        ));

        // when
        ReservationEntry result = reservation.findActiveEntry(1L);

        // then
        assertThat(result.getReserverName()).isEqualTo("이프");
    }

    @Test
    void 활성화_상태가_아닌_엔트리를_조회하면_예외가_발생한다() {
        // given
        Reservation reservation = createReservationWithEntries(List.of(
                entry(1L, "이프", ReservationStatus.DELETED)
        ));

        // when & then
        assertThatThrownBy(() -> reservation.findActiveEntry(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("예약 정보를 찾을 수 없습니다.");
    }

    @Test
    void 예약된_엔트리를_취소하면_가장_먼저_등록된_대기_엔트리가_예약된다() {
        // given
        ReservationEntry reserved = entry(1L, "이프", ReservationStatus.RESERVED, FIXED);
        ReservationEntry firstWaiting = entry(2L, "라텔", ReservationStatus.WAITING, FIXED.minusMinutes(2));
        ReservationEntry secondWaiting = entry(3L, "도기", ReservationStatus.WAITING, FIXED.minusMinutes(1));
        Reservation reservation = createReservationWithEntries(List.of(reserved, firstWaiting, secondWaiting));

        // when
        reservation.cancelEntry(1L);

        // then
        assertThat(reservation.getEntries())
                .extracting(ReservationEntry::getId, ReservationEntry::getStatus)
                .containsExactly(
                        tuple(1L, ReservationStatus.DELETED),
                        tuple(2L, ReservationStatus.RESERVED),
                        tuple(3L, ReservationStatus.WAITING)
                );
    }

    @Test
    void 존재하지_않는_엔트리를_취소하면_예외가_발생한다() {
        // given
        Reservation reservation = createReservationWithEntries(List.of(
                entry(1L, "이프", ReservationStatus.RESERVED)
        ));

        // when & then
        assertThatThrownBy(() -> reservation.cancelEntry(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("예약 정보를 찾을 수 없습니다.");
    }

    @Test
    void 같은_날짜와_시간이면_true를_반환한다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);

        // when
        boolean result = reservation.isSameSchedule(date, reservationTime);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 날짜나_시간이_다르면_false를_반환한다() {
        // given
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        Reservation reservation = Reservation.createSlot(date, theme, reservationTime);

        ReservationTime anotherTime = ReservationTime.restore(2L, TestDateTimes.hour(15), TimeStatus.ACTIVE);

        // when & then
        assertThat(reservation.isSameSchedule(date.plusDays(1), reservationTime)).isFalse();
        assertThat(reservation.isSameSchedule(date, anotherTime)).isFalse();
    }

    private Reservation createReservationWithEntries(List<ReservationEntry> entries) {
        return Reservation.restore(1L, FIXED.plusDays(1).toLocalDate(), theme, reservationTime, entries);
    }

    private ReservationEntry entry(long id, String name, ReservationStatus status) {
        return entry(id, name, status, LocalDateTime.now());
    }

    private ReservationEntry entry(long id, String name, ReservationStatus status, LocalDateTime createdAt) {
        return ReservationEntry.restore(id, name, status, createdAt);
    }
}
