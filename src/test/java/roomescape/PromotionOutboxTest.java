package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.dao.MemberDao;
import roomescape.dao.PromotionOutboxDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.domain.member.Member;
import roomescape.domain.promotion.OutboxStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.store.Store;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.Time;
import roomescape.domain.vo.Name;
import roomescape.dto.request.ReservationRequestDto;
import roomescape.dto.request.WaitingRequestDto;
import roomescape.domain.reservation.ReservationService;
import roomescape.domain.waiting.WaitingService;
import roomescape.worker.PromotionOutboxWorker;

@SpringBootTest(properties = "scheduling.enabled=false")
@ActiveProfiles("test")
class PromotionOutboxTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private WaitingService waitingService;
    @Autowired
    private PromotionOutboxWorker worker;
    @Autowired
    private ReservationDao reservationDao;
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

    private Member owner;
    private Member waiter;
    private Time time;
    private Theme theme;
    private Store store;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        Long storeId = jdbcTemplate.queryForObject("SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
        store = new Store(storeId, "강남점");

        jdbcTemplate.update("INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "예약자", "owner@test.com", "password", "USER");
        owner = memberDao.findByEmail("owner@test.com").orElseThrow();
        jdbcTemplate.update("INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "대기자", "waiter@test.com", "password", "USER");
        waiter = memberDao.findByEmail("waiter@test.com").orElseThrow();

        time = timeDao.insert(new Time(LocalTime.of(13, 0)));
        theme = themeDao.insert(new Theme(new Name("방탈출"), "http://url", "설명"));
        reservation = reservationDao.insert(
                Reservation.createByAdmin(owner, LocalDate.now().plusDays(1), time, theme, store));
        waitingService.create(
                new WaitingRequestDto(reservation.getDate(), time.getId(), theme.getId(), store.getId()),
                waiter);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM promotion_outbox");
        jdbcTemplate.update("DELETE FROM waitings");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM reservations");
        jdbcTemplate.update("DELETE FROM times");
        jdbcTemplate.update("DELETE FROM themes");
        jdbcTemplate.update("DELETE FROM members");
        jdbcTemplate.update("DELETE FROM stores");
    }

    @Test
    @DisplayName("예약을 취소하면 즉시 승격되지 않고, 아웃박스에 PENDING 할 일만 기록된다")
    void cancelEnqueuesPromotionInsteadOfPromotingImmediately() {
        reservationService.cancel(reservation.getId(), owner);

        // 대기자는 아직 예약을 받지 못했다 (즉시 승격 X).
        assertThat(reservationDao.findAllByMemberId(waiter.getId())).isEmpty();
        // 대신 아웃박스에 PENDING 할 일이 한 줄 생겼다.
        assertThat(promotionOutboxDao.findByStatus(OutboxStatus.PENDING)).hasSize(1);
    }

    @Test
    @DisplayName("워커가 PENDING 할 일을 처리하면 대기자가 예약으로 승격되고 할 일은 DONE이 된다")
    void workerPromotesPendingWaiter() {
        reservationService.cancel(reservation.getId(), owner);

        worker.processPendingTasks();

        assertThat(reservationDao.findAllByMemberId(waiter.getId())).hasSize(1);
        assertThat(waitingService.findAllByMemberId(waiter.getId())).isEmpty();
        assertThat(promotionOutboxDao.findByStatus(OutboxStatus.PENDING)).isEmpty();
        assertThat(promotionOutboxDao.findByStatus(OutboxStatus.DONE)).hasSize(1);
    }

    @Test
    @DisplayName("승격 대기 중인 빈 슬롯(대기자 존재)은 다른 사용자가 직접 예약할 수 없다")
    void cannotDirectlyBookSlotWithWaitingQueue() {
        reservationService.cancel(reservation.getId(), owner);
        // 워커가 아직 승격하지 않아 슬롯은 비어 있지만, 대기자가 남아 있다.
        jdbcTemplate.update("INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "새치기", "jumper@test.com", "password", "USER");
        Member jumper = memberDao.findByEmail("jumper@test.com").orElseThrow();

        assertThatThrownBy(() -> reservationService.create(
                jumper,
                new ReservationRequestDto(reservation.getDate(), time.getId(), theme.getId(), store.getId())))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("워커가 같은 할 일을 두 번 처리해도 멱등하게 한 명만 승격된다")
    void workerIsIdempotent() {
        reservationService.cancel(reservation.getId(), owner);

        worker.processPendingTasks();
        worker.processPendingTasks();

        // 두 번 돌아도 대기자의 예약은 하나뿐 (이중 승격 없음).
        assertThat(reservationDao.findAllByMemberId(waiter.getId())).hasSize(1);
    }
}
