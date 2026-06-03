package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static roomescape.domain.fixture.ReservationFixture.createEntry;
import static roomescape.domain.fixture.ReservationFixture.createSlotWithReservations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.domain.fixture.ReservationTimeFixture;
import roomescape.domain.fixture.ThemeFixture;
import roomescape.exception.DuplicateEntityException;
import roomescape.exception.EntityNotFoundException;
import roomescape.exception.RoomEscapeException;

class ReservationSlotTest {

    private ReservationTime reservationTime = ReservationTimeFixture.createDefault();
    private Theme theme = ThemeFixture.createDefaultTheme();

    @Test
    void 정상적인_예약_정보를_생성한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);

        // when
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, reservationTime);

        // then
        assertThat(slot)
                .extracting(ReservationSlot::getDate, ReservationSlot::getTime)
                .containsExactly(date, reservationTime);
    }

    @ParameterizedTest(name = "날짜 {0}, 테마 {1}, 시간 {2} 일 때, {3} 예외가 발생한다")
    @MethodSource("roomescape.domain.fixture.ReservationFixture#invalidReservationConstructor")
    void 예약_일시와_테마_검증_통합_테스트(LocalDate date, Theme theme, ReservationTime reservationTime, String expectedMessage) {
        // when & then
        assertThatThrownBy(() -> ReservationSlot.createSlot(date, theme, reservationTime))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void 예약_정보가_있을_때_예약을_할_수_있다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, reservationTime);

        // when
        slot.reserve("이프");

        // then
        assertThat(slot.getReservations())
                .singleElement()
                .extracting(Reservation::getName, Reservation::getStatus)
                .containsExactly("이프", ReservationStatus.RESERVED);
    }

    @Test
    void 이미_예약된_예약_정보에_예약을_하면_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, reservationTime);
        slot.reserve("이프");

        // when & then
        assertThatThrownBy(() -> slot.reserve("라텔"))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 된 날짜입니다.");
    }

    @Test
    void 동일_이름으로_예약을_중복_요청하면_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, reservationTime);
        slot.reserve("이프");

        // when & then
        assertThatThrownBy(() -> slot.reserve("이프"))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 대기가 존재합니다.");
    }

    @Test
    void 대기_신청_시_예약이_있으면_대기로_등록된다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, reservationTime);
        slot.reserve("이프");

        // when
        slot.joinWaitingList("라텔");

        // then
        assertThat(slot.getReservations())
                .extracting(Reservation::getName, Reservation::getStatus)
                .containsExactlyInAnyOrder(
                        tuple("이프", ReservationStatus.RESERVED),
                        tuple("라텔", ReservationStatus.WAITING)
                );
    }

    @Test
    void 대기_신청_시_예약이_없으면_예약으로_승격된다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, reservationTime);

        // when
        slot.joinWaitingList("라텔");

        // then
        assertThat(slot.getReservations())
                .singleElement()
                .extracting(Reservation::getName, Reservation::getStatus)
                .containsExactly("라텔", ReservationStatus.RESERVED);
    }

    @Test
    void 대기_신청_시_동일_이름이_이미_존재하면_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, reservationTime);
        slot.reserve("이프");

        // when & then
        assertThatThrownBy(() -> slot.joinWaitingList("이프"))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 대기가 존재합니다.");
    }

    @Test
    void 이미_지나버린_예약_정보에_예약을_하면_예외가_발생한다() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationSlot slot = new ReservationSlot(1L, pastDate, theme, reservationTime, List.of());

        // when & then
        assertThatThrownBy(() -> slot.reserve("이프"))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지난 예약입니다.");
    }

    @Test
    void 예약된_엔트리를_식별자로_조회한다() {
        // given
        ReservationSlot slot = createSlotWithReservations(List.of(
                createEntry(1L, "이프", ReservationStatus.RESERVED),
                createEntry(2L, "라텔", ReservationStatus.WAITING)
        ), theme, reservationTime);

        // when
        Reservation result = slot.findReservedReservation(1L);

        // then
        assertThat(result.getName()).isEqualTo("이프");
    }

    @Test
    void 예약_상태가_아닌_엔트리를_예약된_엔트리로_조회하면_예외가_발생한다() {
        // given
        ReservationSlot slot = createSlotWithReservations(List.of(
                createEntry(1L, "이프", ReservationStatus.WAITING)
        ), theme, reservationTime);

        // when & then
        assertThatThrownBy(() -> slot.findReservedReservation(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("예약 정보를 찾을 수 없습니다.");
    }

    @Test
    void 취소된_예약_엔트리를_예약된_엔트리로_조회하면_예외가_발생한다() {
        // given
        Reservation reservation = createEntry(1L, "이프", ReservationStatus.RESERVED);
        reservation.cancel();
        ReservationSlot slot = createSlotWithReservations(List.of(reservation), theme, reservationTime);

        // when & then
        assertThatThrownBy(() -> slot.findReservedReservation(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("예약 정보를 찾을 수 없습니다.");
    }

    @Test
    void 예약된_엔트리를_취소하면_가장_먼저_등록된_대기_엔트리가_예약된다() {
        // given
        Reservation reserved = createEntry(1L, "이프", ReservationStatus.RESERVED, LocalDateTime.now());
        Reservation firstWaiting = createEntry(2L, "라텔", ReservationStatus.WAITING, LocalDateTime.now().minusMinutes(2));
        Reservation secondWaiting = createEntry(3L, "도기", ReservationStatus.WAITING, LocalDateTime.now().minusMinutes(1));
        ReservationSlot slot = createSlotWithReservations(List.of(reserved, firstWaiting, secondWaiting), theme, reservationTime);

        // when
        slot.cancelReservation(1L);

        // then
        assertThat(slot.getReservations())
                .extracting(Reservation::getId, Reservation::getStatus, Reservation::getActiveStatus)
                .containsExactly(
                        tuple(1L, ReservationStatus.RESERVED, ReservationActiveStatus.CANCELED),
                        tuple(2L, ReservationStatus.RESERVED, ReservationActiveStatus.ACTIVE),
                        tuple(3L, ReservationStatus.WAITING, ReservationActiveStatus.ACTIVE)
                );
    }

    @Test
    void 취소된_예약_엔트리를_다시_취소해도_대기_엔트리를_승격하지_않는다() {
        // given
        Reservation canceled = createEntry(1L, "이프", ReservationStatus.RESERVED, LocalDateTime.now());
        canceled.cancel();
        Reservation waiting = createEntry(2L, "라텔", ReservationStatus.WAITING, LocalDateTime.now());
        ReservationSlot slot = createSlotWithReservations(List.of(canceled, waiting), theme, reservationTime);

        // when
        slot.cancelReservation(1L);

        // then
        assertThat(slot.getReservations())
                .extracting(Reservation::getId, Reservation::getStatus, Reservation::getActiveStatus)
                .containsExactly(
                        tuple(1L, ReservationStatus.RESERVED, ReservationActiveStatus.CANCELED),
                        tuple(2L, ReservationStatus.WAITING, ReservationActiveStatus.ACTIVE)
                );
    }

    @Test
    void 예약_하루_전에는_예약을_취소할_수_없다() {
        // given
        ReservationSlot slot = new ReservationSlot(
                1L,
                LocalDate.now().plusDays(1),
                theme,
                reservationTime,
                List.of(createEntry(1L, "이프", ReservationStatus.RESERVED))
        );

        // when & then
        assertThatThrownBy(() -> slot.cancelReservation(1L))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("예약 하루 전에는 취소할 수 없습니다.");
    }

    @Test
    void 예약_하루_전에도_대기를_취소할_수_있다() {
        // given
        ReservationSlot slot = new ReservationSlot(
                1L,
                LocalDate.now().plusDays(1),
                theme,
                reservationTime,
                List.of(createEntry(1L, "이프", ReservationStatus.WAITING))
        );

        // when
        slot.cancelReservation(1L);

        // then
        assertThat(slot.getReservations())
                .singleElement()
                .extracting(Reservation::getStatus, Reservation::getActiveStatus)
                .containsExactly(ReservationStatus.WAITING, ReservationActiveStatus.CANCELED);
    }

    @Test
    void 존재하지_않는_엔트리를_취소하면_아무_일도_일어나지_않는다() {
        // given
        ReservationSlot slot = createSlotWithReservations(List.of(
                createEntry(1L, "이프", ReservationStatus.RESERVED)
        ), theme, reservationTime);

        // when & then
        assertThatCode(() -> slot.cancelReservation(999L))
                .doesNotThrowAnyException();
        assertThat(slot.getReservations())
                .singleElement()
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void 같은_날짜와_시간이면_true를_반환한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, reservationTime);

        // when
        boolean result = slot.isSameSlot(date, reservationTime);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 날짜나_시간이_다르면_false를_반환한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, reservationTime);

        ReservationTime anotherTime = new ReservationTime(2L, LocalTime.of(15, 0), TimeStatus.ACTIVE);

        // when & then
        assertThat(slot.isSameSlot(date.plusDays(1), reservationTime)).isFalse();
        assertThat(slot.isSameSlot(date, anotherTime)).isFalse();
    }
}
