package roomescape;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.vo.Name;
import roomescape.member.Member;
import roomescape.member.MemberDao;
import roomescape.order.Order;
import roomescape.order.OrderDao;
import roomescape.order.OrderService;
import roomescape.order.OrderStatus;
import roomescape.promotion.OutboxStatus;
import roomescape.promotion.PromotionOutboxDao;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationDao;
import roomescape.reservation.ReservationStatus;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.web.dto.ReservationRequestDto;
import roomescape.store.Store;
import roomescape.theme.Theme;
import roomescape.theme.ThemeDao;
import roomescape.time.Time;
import roomescape.time.TimeDao;
import roomescape.worker.ExpiredOrderWorker;

@SpringBootTest(properties = "scheduling.enabled=false")
@ActiveProfiles("test")
class ExpiredOrderWorkerTest {

    @Autowired
    private ExpiredOrderWorker worker;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private PromotionOutboxDao promotionOutboxDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private TimeDao timeDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private Time time;
    private Theme theme;
    private Store store;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        Long storeId = jdbcTemplate.queryForObject("SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
        store = new Store(storeId, "강남점");
        jdbcTemplate.update("INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저", "user@test.com", "password", "USER");
        member = memberDao.findByEmail("user@test.com").orElseThrow();
        time = timeDao.insert(new Time(LocalTime.of(13, 0)));
        theme = themeDao.insert(new Theme(new Name("방탈출"), "http://url", "설명", 30000L));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM promotion_outbox");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM reservations");
        jdbcTemplate.update("DELETE FROM times");
        jdbcTemplate.update("DELETE FROM themes");
        jdbcTemplate.update("DELETE FROM members");
        jdbcTemplate.update("DELETE FROM stores");
    }

    private Pending createPending() {
        Reservation reservation = reservationService.create(member, new ReservationRequestDto(
                LocalDate.now().plusDays(1), time.getId(), theme.getId(), store.getId()));
        Order order = orderService.create(reservation.getId(), theme.getPrice());
        return new Pending(reservation, order);
    }

    private record Pending(Reservation reservation, Order order) {
    }

    @Test
    @DisplayName("TTL 지난 미결제 PENDING은 정리되어 예약 CANCELED, 주문 FAILED")
    void expiresAbandonedPending() {
        Pending created = createPending();
        // created_at을 TTL(기본 30분)보다 한참 전으로 조작 — 방치된 것처럼.
        jdbcTemplate.update("UPDATE orders SET created_at = ? WHERE order_id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusHours(1)), created.order().getOrderId());

        worker.expireAbandonedOrders();

        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.FAILED);
        // 슬롯이 비었으니 다음 대기자 승격을 위한 아웃박스 할 일이 기록된다 (abandon이 enqueue).
        assertThat(promotionOutboxDao.findByStatus(OutboxStatus.PENDING)).hasSize(1);
    }

    @Test
    @DisplayName("TTL 이내의 갓 만든 PENDING은 건드리지 않는다 (결제 진행 중 보호 — 4:59 걱정)")
    void keepsFreshPending() {
        Pending created = createPending(); // created_at = now (fresh)

        worker.expireAbandonedOrders();

        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.PENDING);
        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Order 없는 만료 PENDING(승격 대기 등)도 정리되어 CANCELED")
    void expiresOrphanPendingWithoutOrder() {
        Reservation orphan = reservationDao.insert(Reservation.createByUser(
                member, LocalDate.now().plusDays(1), time, theme, store, LocalDateTime.now()));
        // order 없이 PENDING만 존재. created_at을 TTL 이전으로 조작 — 방치된 승격 PENDING처럼.
        jdbcTemplate.update("UPDATE reservations SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusHours(1)), orphan.getId());

        worker.expireAbandonedOrders();

        assertThat(reservationDao.findById(orphan.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }
}
