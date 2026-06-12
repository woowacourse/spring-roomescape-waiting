package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserReservationsTest {

    private static final Theme THEME = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com");

    @Test
    @DisplayName("예약과 대기를 날짜와 시간이 가까운 순서로 반환한다.")
    void 사용자_예약_대기_날짜_시간순_정렬() {
        Reservation third = createReservation(1L, "브라운", LocalDate.of(2026, 6, 5), 10);
        Reservation second = createReservation(2L, "브라운", LocalDate.of(2026, 6, 4), 11);
        WaitingWithNumber first = new WaitingWithNumber(
                createWaiting(3L, "브라운", LocalDate.of(2026, 6, 4), 10),
                0
        );

        UserReservations userReservations = new UserReservations(
                "브라운",
                List.of(third, second),
                List.of(first)
        );

        List<ReservationAndWaiting> result = userReservations.getReservationAndWaitings();

        assertThat(result)
                .extracting(ReservationAndWaiting::id)
                .containsExactly(first.waiting().getId(), second.getId(), third.getId());
    }

    @Test
    @DisplayName("다른 사용자의 예약이 포함되면 예외가 발생한다.")
    void 다른_사용자_예약_포함_예외_발생() {
        Reservation otherReservation = createReservation(1L, "브라운", LocalDate.of(2026, 6, 4), 10);

        assertThatThrownBy(() -> new UserReservations("네오", List.of(otherReservation), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("다른 사용자의 예약이나 대기가 포함될 수 없습니다.");
    }

    @Test
    @DisplayName("다른 사용자의 대기가 포함되면 예외가 발생한다.")
    void 다른_사용자_대기_포함_예외_발생() {
        WaitingWithNumber otherWaiting = new WaitingWithNumber(
                createWaiting(1L, "브라운", LocalDate.of(2026, 6, 4), 10),
                0
        );

        assertThatThrownBy(() -> new UserReservations("네오", List.of(), List.of(otherWaiting)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("다른 사용자의 예약이나 대기가 포함될 수 없습니다.");
    }

    @Test
    @DisplayName("사용자 이름이 null이거나 비어있으면 예외가 발생한다.")
    void 사용자_이름_공백_예외_발생() {
        assertThatThrownBy(() -> new UserReservations(" ", List.of(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자 이름은 필수입니다.");
    }

    private Reservation createReservation(Long id, String name, LocalDate date, int hour) {
        return new Reservation(id, name, createSlot(id, date, hour), LocalDateTime.of(2026, 6, 3, 10, 0),
                ReservationStatus.RESERVED);
    }

    private Reservation createWaiting(Long id, String name, LocalDate date, int hour) {
        return new Reservation(id, name, createSlot(id, date, hour), LocalDateTime.of(2026, 6, 3, 10, 0),
                ReservationStatus.WAITING);
    }

    private ReservationSlot createSlot(Long id, LocalDate date, int hour) {
        return new ReservationSlot(id, date, new TimeSlot(1L, LocalTime.of(hour, 0)), THEME);
    }
}
