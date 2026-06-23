package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationLine;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.DuplicateException;
import roomescape.exception.PastTimeException;

public class ReservationLineTest {

    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final TimeSlot TIME_SLOT = new TimeSlot(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com");

    @Test
    @DisplayName("빈 대기 목록으로 대기 줄을 생성할 수 있다.")
    void 빈_대기_목록_생성() {
        assertThatCode(() -> new ReservationLine(createSlot(), List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("null 대기 목록으로 대기 줄을 생성하면 예외가 발생한다.")
    void null_대기_목록_예외_발생() {
        assertThatThrownBy(() -> new ReservationLine(createSlot(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 목록은 필수입니다.");
    }

    @Test
    @DisplayName("null 예약 슬롯으로 대기 줄을 생성하면 예외가 발생한다.")
    void null_예약_슬롯_예외_발생() {
        assertThatThrownBy(() -> new ReservationLine(null, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 슬롯은 필수입니다.");
    }

    @Test
    @DisplayName("같은 슬롯에 이미 예약 또는 대기 중이면 추가할 수 없다.")
    void 같은_슬롯_중복_추가_예외() {
        Reservation reserved = createReserved(1L, "브라운", LocalDateTime.of(2026, 6, 3, 10, 0));
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(reserved));

        assertThatThrownBy(() -> reservationLine.add("브라운", LocalDateTime.of(2026, 6, 3, 10, 1)))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("이미 예약 또는 대기 중인 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
    }

    @Test
    @DisplayName("다른 예약 슬롯의 대기가 섞이면 예외가 발생한다.")
    void 다른_예약_슬롯_대기_예외_발생() {
        Reservation waiting = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation otherSlot = new Reservation(
                2L,
                "순번2",
                new ReservationSlot(2L, DATE.plusDays(1), TIME_SLOT, THEME),
                LocalDateTime.of(2026, 6, 3, 10, 1),
                ReservationStatus.WAITING
        );

        assertThatThrownBy(() -> new ReservationLine(createSlot(), List.of(waiting, otherSlot)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 위치는 같은 예약 슬롯에 대해서만 계산 가능합니다.");
    }

    @Test
    @DisplayName("null 예약 대기의 위치를 조회하면 예외가 발생한다.")
    void null_예약_대기_위치_조회_예외_발생() {
        Reservation reserved = createReserved(2L, "예약자", LocalDateTime.of(2026, 6, 3, 9, 0));
        Reservation waiting = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(reserved, waiting));

        assertThatThrownBy(() -> reservationLine.findWaitingIndex(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약은 필수입니다.");
    }

    @Test
    @DisplayName("다른 예약 슬롯의 대기 위치를 조회하면 예외가 발생한다.")
    void 다른_예약_슬롯_대기_위치_조회_예외_발생() {
        Reservation reserved = createReserved(3L, "예약자", LocalDateTime.of(2026, 6, 3, 9, 0));
        Reservation waiting = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation otherSlot = new Reservation(
                2L,
                "순번2",
                new ReservationSlot(2L, DATE.plusDays(1), TIME_SLOT, THEME),
                LocalDateTime.of(2026, 6, 3, 10, 1),
                ReservationStatus.WAITING
        );
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(reserved, waiting));

        assertThatThrownBy(() -> reservationLine.findWaitingIndex(otherSlot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("같은 예약 슬롯의 예약만 처리할 수 있습니다.");
    }

    @Test
    @DisplayName("확정 예약 취소 시 첫 번째 대기가 승급 대상이 된다.")
    void 확정_예약_취소시_승급_대상_계산() {
        Reservation reserved = createReserved(1L, "예약자", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation waiting = createWaiting(2L, "대기자", LocalDateTime.of(2026, 6, 3, 10, 1));
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(reserved, waiting));

        Optional<Reservation> promoted = reservationLine.findPromotedReservationAfterCancel(
                reserved,
                DATE.minusDays(1).atTime(9, 0)
        );

        assertThat(promoted).isPresent();
        assertThat(promoted.get().getId()).isEqualTo(waiting.getId());
        assertThat(promoted.get().isReserved()).isTrue();
    }

    @Test
    @DisplayName("대기 예약 취소 시 승급 대상이 없다.")
    void 대기_예약_취소시_승급_대상_계산() {
        Reservation reserved = createReserved(1L, "예약자", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation waiting = createWaiting(2L, "대기자", LocalDateTime.of(2026, 6, 3, 10, 1));
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(reserved, waiting));

        Optional<Reservation> promoted = reservationLine.findPromotedReservationAfterCancel(
                waiting,
                DATE.minusDays(1).atTime(9, 0)
        );

        assertThat(promoted).isEmpty();
    }

    @Test
    @DisplayName("예약 시작 24시간 전부터는 예약을 취소할 수 없다.")
    void 예약_시작_24시간_전_취소_예외_발생() {
        ReservationSlot slot = new ReservationSlot(1L, LocalDate.of(2026, 6, 10), TIME_SLOT, THEME);
        Reservation reserved = new Reservation(
                1L,
                "예약자",
                slot,
                LocalDateTime.of(2026, 6, 3, 10, 0),
                ReservationStatus.RESERVED
        );
        ReservationLine reservationLine = new ReservationLine(slot, List.of(reserved));

        assertThatThrownBy(() -> reservationLine.findPromotedReservationAfterCancel(
                reserved,
                LocalDateTime.of(2026, 6, 9, 10, 0)
        ))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("예약 시작 24시간 전까지만 예약을 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("확정 예약이 라인을 떠나면 첫 번째 대기가 승급 대상이 된다.")
    void 확정_예약이_라인을_떠나면_첫번째_대기_승급() {
        Reservation reserved = createReserved(1L, "예약자", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation waiting = createWaiting(2L, "대기자", LocalDateTime.of(2026, 6, 3, 10, 1));
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(reserved, waiting));

        Optional<Reservation> promoted = reservationLine.findNextToPromote(reserved);

        assertThat(promoted).isPresent();
        assertThat(promoted.get().getId()).isEqualTo(waiting.getId());
        assertThat(promoted.get().isReserved()).isTrue();
    }

    @Test
    @DisplayName("대기 예약이 라인을 떠나면 승급 대상이 없다.")
    void 대기_예약이_라인을_떠나면_승급_대상_없음() {
        Reservation reserved = createReserved(1L, "예약자", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation waiting = createWaiting(2L, "대기자", LocalDateTime.of(2026, 6, 3, 10, 1));
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(reserved, waiting));

        Optional<Reservation> promoted = reservationLine.findNextToPromote(waiting);

        assertThat(promoted).isEmpty();
    }

    private Reservation createWaiting(Long id, String name, LocalDateTime createdAt) {
        return new Reservation(id, name, createSlot(), createdAt,
                ReservationStatus.WAITING);
    }

    private Reservation createReserved(Long id, String name, LocalDateTime createdAt) {
        return new Reservation(id, name, createSlot(), createdAt,
                ReservationStatus.RESERVED);
    }

    private ReservationSlot createSlot() {
        return new ReservationSlot(1L, DATE, TIME_SLOT, THEME);
    }
}
