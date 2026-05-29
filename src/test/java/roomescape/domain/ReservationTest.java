package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.ForbiddenException;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    @Test
    @DisplayName("새로운 예약을 성공적으로 생성한다.")
    void createReservationTest() {
        // given
        String username = "파도";
        LocalDateTime now = LocalDateTime.of(2026, 5, 29, 10, 0);
        Slot slot = createValidSlot(now.plusDays(1));

        // when
        Reservation reservation = Reservation.create(username, slot, now);

        // then
        assertThat(reservation.getId()).isNull();
        assertThat(reservation.getUsername()).isEqualTo("파도");
        assertThat(reservation.getReservationDate()).isEqualTo(now.plusDays(1).toLocalDate());
    }

    @Test
    @DisplayName("예약의 소유자가 일치하는지 확인한다.")
    void isOwnedByTest() {
        // given
        Reservation reservation = Reservation.from(1L, "파도", createAnySlot());

        // when & then
        assertThat(reservation.isOwnedBy("파도")).isTrue();
        assertThat(reservation.isOwnedBy("다른사람")).isFalse();
    }

    @Test
    @DisplayName("타인의 예약에 접근할 경우 예외를 발생시킨다.")
    void validateOwnedByTest() {
        // given
        Reservation reservation = Reservation.from(1L, "파도", createAnySlot());

        // when & then
        assertThatThrownBy(() -> reservation.validateOwnedBy("다른사람"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("타인의 예약에 접근할 수 없습니다.");
    }

    @Test
    @DisplayName("예약의 슬롯을 새로운 슬롯으로 성공적으로 변경한다.")
    void withSlotTest() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 5, 29, 10, 0);
        Reservation reservation = Reservation.from(1L, "파도", createValidSlot(now.plusDays(1)));
        Slot newSlot = createValidSlot(now.plusDays(2));

        // when
        Reservation updatedReservation = reservation.withSlot(newSlot, now);

        // then
        assertThat(updatedReservation.getId()).isEqualTo(1L);
        assertThat(updatedReservation.getReservationDate()).isEqualTo(now.plusDays(2).toLocalDate());
    }

    @Test
    @DisplayName("동일한 ID를 가진 예약은 같은 객체로 판단한다.")
    void equalsAndHashCodeTest() {
        // given
        Slot slot = createAnySlot();
        Reservation reservation1 = Reservation.from(1L, "파도", slot);
        Reservation reservation2 = Reservation.from(1L, "다른이름", slot);

        // when & then
        assertThat(reservation1).isEqualTo(reservation2);
        assertThat(reservation1.hashCode()).isEqualTo(reservation2.hashCode());
    }

    private Slot createValidSlot(LocalDateTime dateTime) {
        ReservationTime time = ReservationTime.from(1L, LocalTime.from(dateTime));
        Theme theme = Theme.from(1L, "테마", "https://picsum.photos/seed/empty/400/300", "설명");
        return Slot.from(
                Schedule.from(
                        dateTime.toLocalDate(),
                        time),
                theme
        );
    }

    private Slot createAnySlot() {
        return createValidSlot(LocalDateTime.of(2026, 6, 5, 10, 0));
    }
}
