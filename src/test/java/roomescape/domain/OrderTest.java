package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    private static final User USER = new User("u@test.com", Password.ofEncrypted("pw"), "브라운", Role.MEMBER)
            .withId(1L);
    private static final Theme THEME = new Theme(1L, "테마", "설명", "https://thumbnail.url");
    private static final ReservationTime TIME = new ReservationTime(1L, LocalTime.of(12, 0));
    private static final Store STORE = new Store(1L, "매장");
    private static final LocalDate DATE = LocalDate.of(2026, 5, 8);

    private static Reservation reservationWithId(Long id) {
        return new Reservation(id, USER, THEME, DATE, TIME, STORE, ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("예약으로 주문 생성 시 주문번호가 생성된다")
    void generatesOrderIdFromReservation() {
        Order order = new Order(reservationWithId(10L));

        assertThat(order.getOrderId()).isNotNull();
        assertThat(order.getOrderId().getValue()).isNotBlank();
    }

    @Test
    @DisplayName("예약으로 주문 생성 시 생성된 주문번호는 제약(6~64자, 영숫자/-/_)을 만족한다")
    void generatedOrderIdSatisfiesConstraint() {
        Order order = new Order(reservationWithId(10L));

        String orderId = order.getOrderId().getValue();
        assertThat(orderId.length()).isBetween(6, 64);
        assertThat(orderId).matches("^[A-Za-z0-9\\-_]+$");
    }

    @Test
    @DisplayName("매 생성마다 서로 다른 주문번호가 발급된다")
    void generatesUniqueOrderIdPerCreation() {
        Order first = new Order(reservationWithId(10L));
        Order second = new Order(reservationWithId(10L));

        assertThat(first.getOrderId()).isNotEqualTo(second.getOrderId());
    }
}
