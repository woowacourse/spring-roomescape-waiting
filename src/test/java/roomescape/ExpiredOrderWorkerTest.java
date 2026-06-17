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
import roomescape.dao.MemberDao;
import roomescape.dao.OrderDao;
import roomescape.reservation.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.domain.member.Member;
import roomescape.domain.payment.OrderStatus;
import roomescape.reservation.ReservationOrder;
import roomescape.reservation.ReservationService;
import roomescape.reservation.ReservationStatus;
import roomescape.domain.store.Store;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.Time;
import roomescape.common.vo.Name;
import roomescape.reservation.web.ReservationRequestDto;
import roomescape.worker.ExpiredOrderWorker;

@SpringBootTest(properties = "scheduling.enabled=false")
@ActiveProfiles("test")
class ExpiredOrderWorkerTest {

    @Autowired
    private ExpiredOrderWorker worker;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private OrderDao orderDao;
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
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM reservations");
        jdbcTemplate.update("DELETE FROM times");
        jdbcTemplate.update("DELETE FROM themes");
        jdbcTemplate.update("DELETE FROM members");
        jdbcTemplate.update("DELETE FROM stores");
    }

    private ReservationOrder createPending() {
        return reservationService.create(member, new ReservationRequestDto(
                LocalDate.now().plusDays(1), time.getId(), theme.getId(), store.getId()));
    }

    @Test
    @DisplayName("TTL 지난 미결제 PENDING은 정리되어 예약 CANCELED, 주문 FAILED")
    void expiresAbandonedPending() {
        ReservationOrder created = createPending();
        // created_at을 TTL(기본 30분)보다 한참 전으로 조작 — 방치된 것처럼.
        jdbcTemplate.update("UPDATE orders SET created_at = ? WHERE order_id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusHours(1)), created.order().getOrderId());

        worker.expireAbandonedOrders();

        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.FAILED);
    }

    @Test
    @DisplayName("TTL 이내의 갓 만든 PENDING은 건드리지 않는다 (결제 진행 중 보호 — 4:59 걱정)")
    void keepsFreshPending() {
        ReservationOrder created = createPending(); // created_at = now (fresh)

        worker.expireAbandonedOrders();

        assertThat(reservationDao.findById(created.reservation().getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.PENDING);
        assertThat(orderDao.findByOrderId(created.order().getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PENDING);
    }
}
