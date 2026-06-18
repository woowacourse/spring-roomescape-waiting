package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.PaymentAmountMismatchException;
import roomescape.common.vo.Name;
import roomescape.fixture.FakePaymentGateway;
import roomescape.member.Member;
import roomescape.member.MemberDao;
import roomescape.member.dao.MemberJdbcDao;
import roomescape.order.Order;
import roomescape.order.OrderDao;
import roomescape.order.OrderService;
import roomescape.order.OrderStatus;
import roomescape.order.dao.OrderJdbcDao;
import roomescape.payment.ConfirmOutcome;
import roomescape.payment.PaymentGatewayUnreachableException;
import roomescape.payment.PaymentHistoryService;
import roomescape.payment.PaymentService;
import roomescape.payment.web.MyOrderResponse;
import roomescape.payment.web.PaymentReadyResponse;
import roomescape.promotion.PromotionService;
import roomescape.promotion.dao.PromotionOutboxJdbcDao;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationDao;
import roomescape.reservation.ReservationStatus;
import roomescape.reservation.dao.ReservationJdbcDao;
import roomescape.reservation.service.ReservationCreator;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.web.ReservationRequestDto;
import roomescape.store.dao.StoreJdbcDao;
import roomescape.theme.Theme;
import roomescape.theme.ThemeDao;
import roomescape.theme.dao.ThemeJdbcDao;
import roomescape.time.Time;
import roomescape.time.TimeDao;
import roomescape.time.dao.TimeJdbcDao;
import roomescape.waiting.dao.WaitingJdbcDao;

/**
 * 다른 서비스 테스트와 동일하게 @JdbcTest 통합 스타일 — 외부 게이트웨이만 FakePaymentGateway로 대체하고
 * 주문·예약은 실제 DAO/서비스로 굴려 *실제 상태 전이*와 와이어링까지 검증한다.
 */
@JdbcTest
@Import({PaymentService.class, PaymentHistoryService.class, OrderService.class, ReservationService.class, ReservationCreator.class,
        ReservationAuthorizationService.class, PromotionService.class, FakePaymentGateway.class,
        ReservationJdbcDao.class, OrderJdbcDao.class, TimeJdbcDao.class, ThemeJdbcDao.class,
        MemberJdbcDao.class, StoreJdbcDao.class, WaitingJdbcDao.class, PromotionOutboxJdbcDao.class})
@ActiveProfiles("test")
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentHistoryService paymentHistoryService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private TimeDao timeDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private Long storeId;
    private Time time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        storeId = jdbcTemplate.queryForObject("SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
        jdbcTemplate.update("INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저", "user@test.com", "password", "USER");
        member = memberDao.findByEmail("user@test.com").orElseThrow();
        time = timeDao.insert(new Time(LocalTime.of(13, 0)));
        theme = themeDao.insert(new Theme(new Name("방탈출"), "http://url", "설명", 30000L));
    }

    private Created createReservationWithOrder() {
        Reservation reservation = reservationService.create(member,
                new ReservationRequestDto(LocalDate.now().plusDays(1), time.getId(), theme.getId(), storeId));
        Order order = orderService.create(reservation.getId(), theme.getPrice());
        return new Created(reservation, order);
    }

    private record Created(Reservation reservation, Order order) {
    }

    @Test
    @DisplayName("금액이 일치하면 주문이 CONFIRMED, 예약이 BOOKED가 된다")
    void confirmSuccess() {
        Created created = createReservationWithOrder();

        paymentService.confirm(member, "pk-1", created.order().getOrderId(), created.order().getAmount());

        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.BOOKED);
    }

    @Test
    @DisplayName("read timeout(결과 불명확)이면 주문은 NEEDS_CHECK, 예약은 PENDING 유지 — 실패로 단정하지 않는다")
    void confirmReadTimeoutMarksNeedsCheck() {
        Created created = createReservationWithOrder();

        ConfirmOutcome outcome = paymentService.confirm(member, FakePaymentGateway.READ_TIMEOUT_KEY,
                created.order().getOrderId(), created.order().getAmount());

        assertThat(outcome).isEqualTo(ConfirmOutcome.NEEDS_CHECK);
        // 마크가 살아남아야 한다(예외를 다시 안 던지므로 @Transactional이 커밋) — FAILED/CONFIRMED 아님.
        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.NEEDS_CHECK);
        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("connect 실패(확실히 안 됨)는 예외로 전파되고 주문·예약은 PENDING 그대로(롤백)")
    void confirmConnectFailurePropagates() {
        Created created = createReservationWithOrder();

        assertThatThrownBy(() -> paymentService.confirm(member, FakePaymentGateway.CONNECT_FAIL_KEY,
                created.order().getOrderId(), created.order().getAmount()))
                .isInstanceOf(PaymentGatewayUnreachableException.class);

        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PENDING);
        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("조작된 금액은 검증에서 막히고 주문·예약 상태가 그대로다")
    void confirmAmountMismatch() {
        Created created = createReservationWithOrder();

        assertThatThrownBy(() -> paymentService.confirm(member, "pk-1",
                created.order().getOrderId(), created.order().getAmount() + 1))
                .isInstanceOf(PaymentAmountMismatchException.class);

        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PENDING);
        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("만료 정리(expire)는 주문을 FAILED로, 예약을 CANCELED로 만든다")
    void expireAbandonedOrder() {
        Created created = createReservationWithOrder();

        paymentService.expire(created.order().getOrderId());

        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.FAILED);
        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("결제 준비를 다시 호출해도 같은 예약의 미결제 주문은 재사용된다(중복 생성 안 함)")
    void prepareReusesPendingOrder() {
        Reservation reservation = reservationService.create(member,
                new ReservationRequestDto(LocalDate.now().plusDays(1), time.getId(), theme.getId(), storeId));

        PaymentReadyResponse first = paymentService.prepare(member, reservation.getId());
        PaymentReadyResponse second = paymentService.prepare(member, reservation.getId());

        assertThat(second.orderId()).isEqualTo(first.orderId());
        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE reservation_id = ?", Integer.class, reservation.getId());
        assertThat(orderCount).isEqualTo(1);
    }

    @Test
    @DisplayName("한 예약에 주문을 2건 만들려 하면 UNIQUE(reservation_id) 제약으로 막힌다")
    void rejectsSecondOrderForSameReservation() {
        Reservation reservation = reservationService.create(member,
                new ReservationRequestDto(LocalDate.now().plusDays(1), time.getId(), theme.getId(), storeId));
        orderService.create(reservation.getId(), theme.getPrice());

        assertThatThrownBy(() -> orderService.create(reservation.getId(), theme.getPrice()))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("이미 확정된 주문은 만료 정리에서 건너뛴다")
    void expireSkipsConfirmed() {
        Created created = createReservationWithOrder();
        paymentService.confirm(member, "pk-1", created.order().getOrderId(), created.order().getAmount());

        paymentService.expire(created.order().getOrderId());

        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.BOOKED);
    }

    @Test
    @DisplayName("내 결제 내역은 내 예약에 묶인 주문을 상태와 함께 돌려준다")
    void findMyOrders() {
        Created created = createReservationWithOrder();

        List<MyOrderResponse> myOrders = paymentHistoryService.findMyOrders(member.getId());

        assertThat(myOrders).hasSize(1);
        MyOrderResponse response = myOrders.get(0);
        assertThat(response.reservationId()).isEqualTo(created.reservation().getId());
        assertThat(response.orderId()).isEqualTo(created.order().getOrderId());
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.amount()).isEqualTo(created.order().getAmount());
    }

    @Test
    @DisplayName("read timeout 후 내 결제 내역은 그 주문을 NEEDS_CHECK(확인 필요)로 보여준다")
    void findMyOrdersShowsNeedsCheck() {
        Created created = createReservationWithOrder();
        paymentService.confirm(member, FakePaymentGateway.READ_TIMEOUT_KEY,
                created.order().getOrderId(), created.order().getAmount());

        List<MyOrderResponse> myOrders = paymentHistoryService.findMyOrders(member.getId());

        assertThat(myOrders).hasSize(1);
        assertThat(myOrders.get(0).status()).isEqualTo("NEEDS_CHECK");
    }
}
