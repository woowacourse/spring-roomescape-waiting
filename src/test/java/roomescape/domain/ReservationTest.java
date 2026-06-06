package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidOwnershipException;

class ReservationTest {

    @Test
    @DisplayName("정상적인 값을 입력하면 예약 객체가 생성된다.")
    void createValidReservation() {
        Session session = createMockSlot();
        Reservation reservation = new Reservation(1L, "브라운", session);
        assertThat(reservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("예약자 이름이 null이거나 비어있으면 예외가 발생한다.")
    void createInvalidNameThrowsException() {
        Session session = createMockSlot();
        assertThatThrownBy(() -> new Reservation(1L, " ", session))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("슬롯 객체가 null이면 예외가 발생한다.")
    void createNullSlotThrowsException() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("transientOf를 통해 비영속 상태의 예약 객체를 생성할 수 있다.")
    void transientOfCreatesTransientReservation() {
        Session session = createMockSlot();
        Reservation reservation = Reservation.transientOf("브라운", session);
        assertThat(reservation.getId()).isNull();
    }

    @Test
    @DisplayName("예약 소유자가 일치하지 않으면 예외가 발생한다.")
    void validateModifiableThrowsException() {
        Reservation reservation = new Reservation(1L, "브라운", createMockSlot());
        assertThatThrownBy(() -> reservation.validateModifiable("포비", LocalDateTime.now()))
                .isInstanceOf(InvalidOwnershipException.class);
    }

    private Session createMockSlot() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "설명", "url");
        return new Session(1L, LocalDate.now().plusDays(1), timeSlot, theme);
    }
}
