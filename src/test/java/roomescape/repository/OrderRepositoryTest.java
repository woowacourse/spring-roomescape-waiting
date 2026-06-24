package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Order;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 주문을_저장한다() {
        Reservation reservation = saveReservation("브라운");

        Order saved = orderRepository.save(Order.createWithoutId("order-1", 10000L, reservation));

        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getOrderId()).isEqualTo("order-1"),
                () -> assertThat(saved.getAmount()).isEqualTo(10000L),
                () -> assertThat(saved.getIdempotencyKey()).isNotBlank()
        );
    }

    @Test
    void orderId로_주문을_조회한다() {
        Reservation reservation = saveReservation("브라운");
        orderRepository.save(Order.createWithoutId("order-1", 10000L, reservation));

        Optional<Order> result = orderRepository.findByOrderId("order-1");

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo("order-1");
    }

    @Test
    void 존재하지_않는_orderId로_조회하면_빈_Optional을_반환한다() {
        Optional<Order> result = orderRepository.findByOrderId("not-exist");

        assertThat(result).isEmpty();
    }

    @Test
    void reservationId로_주문을_조회한다() {
        Reservation reservation = saveReservation("브라운");
        orderRepository.save(Order.createWithoutId("order-1", 10000L, reservation));

        Optional<Order> result = orderRepository.findByReservation_Id(reservation.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo("order-1");
    }

    @Test
    void 예약이_없는_reservationId로_조회하면_빈_Optional을_반환한다() {
        Optional<Order> result = orderRepository.findByReservation_Id(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void paymentKey를_업데이트할_수_있다() {
        Reservation reservation = saveReservation("브라운");
        Order order = orderRepository.save(Order.createWithoutId("order-1", 10000L, reservation));

        order.updatePaymentKey("pk_test_abc123");
        Order updated = orderRepository.save(order);

        assertThat(updated.getPaymentKey()).isEqualTo("pk_test_abc123");
    }

    @Test
    void 주문을_삭제하면_조회되지_않는다() {
        Reservation reservation = saveReservation("브라운");
        Order order = orderRepository.save(Order.createWithoutId("order-1", 10000L, reservation));

        orderRepository.deleteById(order.getId());

        assertThat(orderRepository.findByOrderId("order-1")).isEmpty();
    }

    private Reservation saveReservation(String memberName) {
        Member member = memberRepository.save(Member.createWithoutId(memberName));
        ReservationTime time = timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("방탈출1", "설명", "https://thumb.com"));
        return reservationRepository.save(
                Reservation.createWithoutId(member, new ReservationSlot(LocalDate.of(2026, 6, 1), time, theme))
        );
    }
}
