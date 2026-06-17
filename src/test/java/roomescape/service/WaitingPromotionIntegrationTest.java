package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.User;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;
import roomescape.repository.ReservationRepository;

/**
 * 삭제와 대기 승격은 별도 트랜잭션(AFTER_COMMIT + REQUIRES_NEW)으로 분리되어 있다.
 * 트랜잭션 이벤트 리스너는 커밋 이후에만 동작하므로, 이 테스트는 @Transactional 롤백을 쓰지 않고
 * 실제 커밋을 발생시킨 뒤 승격 결과를 검증한다. (컨텍스트 재생성으로 테스트 간 DB 격리)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WaitingPromotionIntegrationTest {

    private static final long DEFAULT_STORE_ID = 1L;

    @Autowired
    private ReservationService service;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User manager;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO store(id, name) VALUES (?, ?)", DEFAULT_STORE_ID, "매장");
        long managerId = DbFixtures.insertManager(jdbcTemplate, "매니저");
        DbFixtures.assignManager(jdbcTemplate, DEFAULT_STORE_ID, managerId);
        manager = Fixtures.manager("매니저").withId(managerId);
    }

    @Test
    @DisplayName("확정 예약을 삭제하면 같은 슬롯의 가장 빠른 대기가 승격된다")
    void promotesEarliestWaitingWhenReservedIsDeleted() {
        LocalDate date = Fixtures.daysFromNow(1);
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");

        long reservedId = saveReserved("예약자", themeId, timeId, date);
        long firstWaitingId = saveWaiting("대기1", themeId, timeId, date);
        long secondWaitingId = saveWaiting("대기2", themeId, timeId, date);

        service.deleteReservation(reservedId, manager);

        assertThat(reservationRepository.findById(reservedId)).isEmpty();
        assertThat(statusOf(firstWaitingId)).isEqualTo("RESERVED");
        assertThat(statusOf(secondWaitingId)).isEqualTo("WAITING");
    }

    @Test
    @DisplayName("대기 예약을 삭제하면 확정 예약이 존재하므로 승격이 일어나지 않는다")
    void doesNotPromoteWhenWaitingIsDeleted() {
        LocalDate date = Fixtures.daysFromNow(1);
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");

        long reservedId = saveReserved("예약자", themeId, timeId, date);
        User waiter = member("대기1");
        long waitingId = DbFixtures.insertReservation(
                jdbcTemplate, waiter.getId(), themeId, date.toString(), timeId, "WAITING");

        service.deleteOwnReservation(Fixtures.deleteCommand(waitingId, waiter));

        assertThat(reservationRepository.findById(waitingId)).isEmpty();
        assertThat(statusOf(reservedId)).isEqualTo("RESERVED");
    }

    private User member(String name) {
        long id = DbFixtures.insertMember(jdbcTemplate, name);
        return Fixtures.member(name).withId(id);
    }

    private long saveReserved(String name, long themeId, long timeId, LocalDate date) {
        return DbFixtures.insertReservation(jdbcTemplate, member(name).getId(), themeId, date.toString(), timeId);
    }

    private long saveWaiting(String name, long themeId, long timeId, LocalDate date) {
        return DbFixtures.insertReservation(
                jdbcTemplate, member(name).getId(), themeId, date.toString(), timeId, "WAITING");
    }

    private String statusOf(long reservationId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM reservation WHERE id = ?", String.class, reservationId);
    }
}
