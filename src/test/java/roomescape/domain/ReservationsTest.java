package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;

public class ReservationsTest {

    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final TimeSlot TIME_SLOT = new TimeSlot(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com", 50000L);

    @Test
    @DisplayName("예약된 목록에 예약을 추가하면 대기 상태가 된다.")
    void 예약된_목록에_추가하면_대기_상태() {
        Reservation reserved = createReserved(1L, "예약자", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservations reservations = new Reservations(List.of(reserved));

        Reservation waiting = reservations.add("대기자", createSlot(), LocalDateTime.of(2026, 6, 3, 10, 1));

        assertThat(waiting.isWaiting()).isTrue();
    }

    @Test
    @DisplayName("빈 목록에 예약을 추가하면 예약 상태가 된다.")
    void 빈_목록에_추가하면_예약_상태() {
        Reservations reservations = new Reservations(List.of());

        Reservation reservation = reservations.add("예약자", createSlot(), LocalDateTime.of(2026, 6, 3, 10, 0));

        assertThat(reservation.isReserved()).isTrue();
    }

    @Test
    @DisplayName("하나의 예약 목록에 확정 예약이 여러 개 있으면 예외가 발생한다.")
    void 확정_예약_여러개일_경우_예외() {
        Reservation first = createReserved(1L, "예약자1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation second = createReserved(2L, "예약자2", LocalDateTime.of(2026, 6, 3, 10, 1));

        assertThatThrownBy(() -> new Reservations(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("하나의 예약 슬롯에는 확정 예약이 하나만 존재할 수 있습니다.");
    }

    @Test
    @DisplayName("확정 예약 없이 대기만 있으면 예외가 발생한다.")
    void 확정_예약_없이_대기만_있을_경우_예외() {
        Reservation waiting = createWaiting(1L, "대기자", LocalDateTime.of(2026, 6, 3, 10, 0));

        assertThatThrownBy(() -> new Reservations(List.of(waiting)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확정 예약 없이 예약 대기만 존재할 수 없습니다.");
    }

    @Test
    @DisplayName("생성 시각이 빠른 예약 대기가 앞선 위치를 가진다.")
    void 생성_시각_기준_대기_위치_계산() {
        Reservation reserved = createReserved(3L, "예약자", LocalDateTime.of(2026, 6, 3, 9, 0));
        Reservation first = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation second = createWaiting(2L, "순번2", LocalDateTime.of(2026, 6, 3, 10, 1));
        Reservations reservations = new Reservations(List.of(reserved, second, first));

        assertThat(reservations.findWaitingIndex(first)).contains(0);
        assertThat(reservations.findWaitingIndex(second)).contains(1);
    }

    @Test
    @DisplayName("생성 시각이 같으면 ID가 작은 예약 대기가 앞선 위치를 가진다.")
    void ID_기준_대기_위치_계산() {
        LocalDateTime sameCreatedAt = LocalDateTime.of(2026, 6, 3, 10, 0);
        Reservation reserved = createReserved(3L, "예약자", LocalDateTime.of(2026, 6, 3, 9, 0));
        Reservation first = createWaiting(1L, "순번1", sameCreatedAt);
        Reservation second = createWaiting(2L, "순번2", sameCreatedAt);
        Reservations reservations = new Reservations(List.of(reserved, second, first));

        assertThat(reservations.findWaitingIndex(first)).contains(0);
        assertThat(reservations.findWaitingIndex(second)).contains(1);
    }

    @Test
    @DisplayName("대기 목록에 없는 예약의 위치를 조회하면 빈 값을 반환한다.")
    void 없는_예약_대기_위치_조회시_빈_값_반환() {
        Reservation reserved = createReserved(3L, "예약자", LocalDateTime.of(2026, 6, 3, 9, 0));
        Reservation waiting = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation otherWaiting = createWaiting(2L, "순번2", LocalDateTime.of(2026, 6, 3, 10, 1));
        Reservations reservations = new Reservations(List.of(reserved, waiting));

        assertThat(reservations.findWaitingIndex(otherWaiting)).isEmpty();
    }

    @Test
    @DisplayName("첫 번째 대기를 승급 대상으로 찾는다.")
    void 첫번째_대기_승급_대상_계산() {
        Reservation reserved = createReserved(1L, "예약자", LocalDateTime.of(2026, 6, 3, 10, 0));
        Reservation first = createWaiting(2L, "첫번째", LocalDateTime.of(2026, 6, 3, 10, 1));
        Reservation second = createWaiting(3L, "두번째", LocalDateTime.of(2026, 6, 3, 10, 2));
        Reservations reservations = new Reservations(List.of(reserved, second, first));

        Optional<Reservation> promoted = reservations.findPromotedFirstWaiting();

        assertThat(promoted).isPresent();
        assertThat(promoted.get().getId()).isEqualTo(first.getId());
        assertThat(promoted.get().isReserved()).isTrue();
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
