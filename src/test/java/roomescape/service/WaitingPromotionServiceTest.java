package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.User;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;

class WaitingPromotionServiceTest extends ServiceIntegrationTest {

    @Autowired
    private WaitingPromotionService waitingPromotionService;

    private long themeId;
    private long timeId;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        insertDefaultStore();
        themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        date = Fixtures.daysFromNow(1);
    }

    @Test
    @DisplayName("promoteFirstWaiting - 확정 예약이 없으면 가장 빠른 대기를 RESERVED로 승격한다")
    void promotesEarliestWaitingWhenNoReservedExists() {
        long firstWaitingId = saveWaiting("대기1");
        long secondWaitingId = saveWaiting("대기2");

        waitingPromotionService.promoteFirstWaiting(slotId());

        assertThat(statusOf(firstWaitingId)).isEqualTo("RESERVED");
        assertThat(statusOf(secondWaitingId)).isEqualTo("WAITING");
    }

    @Test
    @DisplayName("promoteFirstWaiting - 확정 예약이 이미 있으면 승격하지 않는다")
    void doesNotPromoteWhenReservedAlreadyExists() {
        long reservedId = saveReserved("예약자");
        long waitingId = saveWaiting("대기1");

        waitingPromotionService.promoteFirstWaiting(slotId());

        assertThat(statusOf(reservedId)).isEqualTo("RESERVED");
        assertThat(statusOf(waitingId)).isEqualTo("WAITING");
    }

    @Test
    @DisplayName("promoteFirstWaiting - 대기가 없으면 아무 변화가 없다")
    void doesNothingWhenNoWaitingExists() {
        long reservedId = saveReserved("예약자");

        waitingPromotionService.promoteFirstWaiting(slotId());

        assertThat(statusOf(reservedId)).isEqualTo("RESERVED");
    }

    private User member(String name) {
        long id = DbFixtures.insertMember(jdbcTemplate, name);
        return Fixtures.member(name).withId(id);
    }

    private long saveReserved(String name) {
        return DbFixtures.insertReservation(jdbcTemplate, member(name).getId(), themeId, date.toString(), timeId);
    }

    private long saveWaiting(String name) {
        return DbFixtures.insertReservation(
                jdbcTemplate, member(name).getId(), themeId, date.toString(), timeId, "WAITING");
    }

    private long slotId() {
        return DbFixtures.slotId(jdbcTemplate, themeId, date.toString(), timeId, DEFAULT_STORE_ID);
    }

    private String statusOf(long reservationId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM reservation WHERE id = ?", String.class, reservationId);
    }
}
