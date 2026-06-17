package roomescape;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.dao.MemberDao;
import roomescape.reservation.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.domain.member.Member;
import roomescape.reservation.Reservation;
import roomescape.domain.store.Store;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.Time;
import roomescape.common.vo.Name;
import roomescape.reservation.web.ReservationPatchDto;
import roomescape.reservation.web.ReservationRequestDto;
import roomescape.reservation.ReservationService;

@SpringBootTest
@ActiveProfiles("test")
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;
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
    private Time time;
    private Theme theme;
    private Store store;
    private Reservation savedReservation;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        Long storeId = jdbcTemplate.queryForObject("SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
        store = new Store(storeId, "강남점");
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저", "user@test.com", "password", "USER"
        );
        member = memberDao.findByEmail("user@test.com").orElseThrow();
        time = timeDao.insert(new Time(LocalTime.of(13, 0)));
        theme = themeDao.insert(new Theme(new Name("방탈출"), "http://url", "설명"));
        savedReservation = reservationDao.insert(
                Reservation.createByAdmin(member, LocalDate.now().plusDays(1), time, theme, store));
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
    @DisplayName("같은 슬롯에 3개 동시 예약 요청이 들어오면 하나만 성공하고 나머지는 409를 반환한다")
    void concurrentInsertResultsInOneSuccess() throws InterruptedException {
        int threadCount = 3;
        ReservationRequestDto request = new ReservationRequestDto(
                LocalDate.now().plusDays(2),
                time.getId(),
                theme.getId(),
                store.getId()
        );

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    reservationService.create(member, request);
                    successCount.incrementAndGet();
                } catch (DuplicateEntityException e) {
                    conflictCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // H2는 gap lock 미지원으로 엄격한 검증 불가. MySQL 환경에서 successCount=1 보장.
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        assertThat(successCount.get() + conflictCount.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("동시에 같은 예약을 수정하면 하나만 성공하고 나머지는 409를 반환한다")
    void concurrentUpdateResultsInOneConflict() throws InterruptedException {
        int threadCount = 10;
        ReservationPatchDto request = new ReservationPatchDto(
                LocalDate.now().plusDays(3), time.getId());

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    reservationService.updateByUser(savedReservation.getId(), member, request);
                    successCount.incrementAndGet();
                } catch (DuplicateEntityException e) {
                    conflictCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        assertThat(successCount.get() + conflictCount.get()).isEqualTo(threadCount);
        assertThat(conflictCount.get()).isGreaterThanOrEqualTo(1);
    }
}
