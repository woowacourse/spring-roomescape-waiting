package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.PaymentAmountMismatchException;
import roomescape.fixture.FakePaymentGateway;
import roomescape.member.Member;
import roomescape.member.MemberDao;
import roomescape.member.dao.MemberJdbcDao;
import roomescape.order.OrderDao;
import roomescape.order.OrderService;
import roomescape.order.OrderStatus;
import roomescape.order.dao.OrderJdbcDao;
import roomescape.payment.PaymentService;
import roomescape.promotion.PromotionService;
import roomescape.promotion.dao.PromotionOutboxJdbcDao;
import roomescape.reservation.ReservationCreator;
import roomescape.reservation.ReservationDao;
import roomescape.reservation.ReservationOrder;
import roomescape.reservation.ReservationService;
import roomescape.reservation.ReservationStatus;
import roomescape.reservation.dao.ReservationJdbcDao;
import roomescape.reservation.web.ReservationRequestDto;
import roomescape.store.dao.StoreJdbcDao;
import roomescape.theme.Theme;
import roomescape.theme.ThemeDao;
import roomescape.theme.dao.ThemeJdbcDao;
import roomescape.time.Time;
import roomescape.time.TimeDao;
import roomescape.time.dao.TimeJdbcDao;
import roomescape.common.vo.Name;
import roomescape.waiting.dao.WaitingJdbcDao;

/**
 * 다른 서비스 테스트와 동일하게 @JdbcTest 통합 스타일 — 외부 게이트웨이만 FakePaymentGateway로 대체하고
 * 주문·예약은 실제 DAO/서비스로 굴려 *실제 상태 전이*와 와이어링까지 검증한다.
 */
@JdbcTest
@Import({PaymentService.class, OrderService.class, ReservationService.class, ReservationCreator.class,
        ReservationAuthorizationService.class, PromotionService.class, FakePaymentGateway.class,
        ReservationJdbcDao.class, OrderJdbcDao.class, TimeJdbcDao.class, ThemeJdbcDao.class,
        MemberJdbcDao.class, StoreJdbcDao.class, WaitingJdbcDao.class, PromotionOutboxJdbcDao.class})
@ActiveProfiles("test")
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ReservationService reservationService;
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

    private ReservationOrder createReservationWithOrder() {
        return reservationService.create(member,
                new ReservationRequestDto(LocalDate.now().plusDays(1), time.getId(), theme.getId(), storeId));
    }

    @Test
    @DisplayName("금액이 일치하면 주문이 CONFIRMED, 예약이 BOOKED가 된다")
    void confirmSuccess() {
        ReservationOrder created = createReservationWithOrder();

        paymentService.confirm(member, "pk-1", created.order().getOrderId(), created.order().getAmount());

        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.BOOKED);
    }

    @Test
    @DisplayName("조작된 금액은 검증에서 막히고 주문·예약 상태가 그대로다")
    void confirmAmountMismatch() {
        ReservationOrder created = createReservationWithOrder();

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
        ReservationOrder created = createReservationWithOrder();

        paymentService.expire(created.order().getOrderId());

        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.FAILED);
        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("이미 확정된 주문은 만료 정리에서 건너뛴다")
    void expireSkipsConfirmed() {
        ReservationOrder created = createReservationWithOrder();
        paymentService.confirm(member, "pk-1", created.order().getOrderId(), created.order().getAmount());

        paymentService.expire(created.order().getOrderId());

        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.BOOKED);
    }
}
