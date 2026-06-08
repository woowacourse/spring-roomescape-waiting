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
import roomescape.exception.DuplicateException;

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
    @DisplayName("생성 시각이 빠른 예약 대기가 우선 순번이다.")
    void 생성_시각_기준_대기_순번_계산() {
        Reservation first = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation second = createWaiting(2L, "순번2", LocalDateTime.of(2026, 6, 3, 10, 1));
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(second, first));

        assertThat(reservationLine.findWaitingNumber(first)).isEqualTo(1);
        assertThat(reservationLine.findWaitingNumber(second)).isEqualTo(2);
    }

    @Test
    @DisplayName("예약된 슬롯에 예약을 추가하면 대기 상태가 된다.")
    void 예약된_슬롯에_추가하면_대기_상태() {
        Reservation reserved = createReserved(1L, "예약자", LocalDateTime.of(2026, 6, 3, 10, 0));
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(reserved));

        Reservation waiting = reservationLine.add("대기자", LocalDateTime.of(2026, 6, 3, 10, 1));

        assertThat(waiting.isWaiting()).isTrue();
    }

    @Test
    @DisplayName("예약이 없는 슬롯에 예약을 추가하면 예약 상태가 된다.")
    void 빈_슬롯에_추가하면_예약_상태() {
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of());

        Reservation reservation = reservationLine.add("예약자", LocalDateTime.of(2026, 6, 3, 10, 0));

        assertThat(reservation.isReserved()).isTrue();
    }

    @Test
    @DisplayName("하나의 슬롯에 확정 예약이 여러 개 있으면 예외가 발생한다.")
    void 확정_예약_여러개일_경우_예외() {
        Reservation first = createReserved(1L, "예약자1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation second = createReserved(2L, "예약자2", LocalDateTime.of(2026, 6, 3, 10, 1));

        assertThatThrownBy(() -> new ReservationLine(createSlot(), List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("하나의 예약 슬롯에는 확정 예약이 하나만 존재할 수 있습니다.");
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
    @DisplayName("생성 시각이 같으면 ID가 작은 예약 대기가 우선 순번이다.")
    void ID_기준_대기_순번_계산() {
        LocalDateTime sameCreatedAt = LocalDateTime.of(2026, 6, 3, 10, 0);
        Reservation first = createWaiting(1L, "순번1", sameCreatedAt);
        Reservation second = createWaiting(2L, "순번2", sameCreatedAt);
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(second, first));

        assertThat(reservationLine.findWaitingNumber(first)).isEqualTo(1);
        assertThat(reservationLine.findWaitingNumber(second)).isEqualTo(2);
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
                .hasMessage("대기 순번은 같은 예약 슬롯에 대해서만 계산 가능합니다.");
    }

    @Test
    @DisplayName("null 예약 대기의 순번을 조회하면 예외가 발생한다.")
    void null_예약_대기_순번_조회_예외_발생() {
        Reservation waiting = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(waiting));

        assertThatThrownBy(() -> reservationLine.findWaitingNumber(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 순번 계산을 위해 예약 대기는 필수입니다.");
    }

    @Test
    @DisplayName("다른 예약 슬롯의 대기 순번을 조회하면 예외가 발생한다.")
    void 다른_예약_슬롯_대기_순번_조회_예외_발생() {
        Reservation waiting = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation otherSlot = new Reservation(
                2L,
                "순번2",
                new ReservationSlot(2L, DATE.plusDays(1), TIME_SLOT, THEME),
                LocalDateTime.of(2026, 6, 3, 10, 1),
                ReservationStatus.WAITING
        );
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(waiting));

        assertThatThrownBy(() -> reservationLine.findWaitingNumber(otherSlot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 순번은 같은 예약 슬롯에 대해서만 계산 가능합니다.");
    }

    @Test
    @DisplayName("대기자가 있으면 첫 대기자를 승격한다.")
    void 첫_대기자_승격() {
        Reservation waiting = createWaiting(1L, "대기자", LocalDateTime.of(2026, 6, 3, 10, 0));
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of(waiting));

        Optional<Reservation> promoted = reservationLine.promoteFirstWaiting();

        assertThat(promoted).isPresent();
        assertThat(promoted.get().isReserved()).isTrue();
    }

    @Test
    @DisplayName("대기자가 없으면 승격 대상이 없다.")
    void 빈_대기열_승격_대상_없음() {
        ReservationLine reservationLine = new ReservationLine(createSlot(), List.of());

        Optional<Reservation> promoted = reservationLine.promoteFirstWaiting();

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
