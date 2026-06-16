package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import roomescape.controller.dto.UserReservationRequest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Role;
import roomescape.repository.MemberDao;
import roomescape.repository.ReservationDao;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.datasource.url=jdbc:h2:mem:transaction_test"
)
class ReservationTransactionTest {

    private static final long TIME_ID = 1L;
    private static final long THEME_ID = 1L;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ReservationDao reservationDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM schedule");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("DELETE FROM reservation_time");

        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)", TIME_ID, "10:00");
        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail_url, price) VALUES (?, ?, ?, ?, ?)",
                THEME_ID,
                "테스트 테마",
                "테스트 설명",
                "https://example.com/theme.png",
                20000
        );
    }

    @DisplayName("예약 저장이 실패하면 같은 트랜잭션에서 생성한 스케줄도 롤백된다.")
    @Test
    void rollbackCreatedScheduleWhenReservationSaveFails() {
        LocalDate date = LocalDate.now().plusDays(40);
        Member member = saveMember("member-a", "러로");
        UserReservationRequest request = new UserReservationRequest(date, TIME_ID, THEME_ID);
        doThrow(new RuntimeException("reservation save failed"))
                .when(reservationDao)
                .save(any(Reservation.class));

        assertThatThrownBy(() -> reservationService.saveReservationByMember(request, member))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("reservation save failed");

        assertThat(countSchedules(date)).isZero();
        assertThat(countReservations()).isZero();
    }

    @DisplayName("예약 취소 후 대기 승격이 실패하면 취소 상태 변경도 롤백된다.")
    @Test
    void rollbackCanceledStatusWhenPromotionFails() {
        LocalDate date = LocalDate.now().plusDays(41);
        Member reserver = saveMember("member-a", "러로");
        Member waiting = saveMember("member-b", "현미밥");
        UserReservationRequest request = new UserReservationRequest(date, TIME_ID, THEME_ID);
        Long reservationId = reservationService.saveReservationByMember(request, reserver);
        reservationService.saveReservationByMember(request, waiting);
        long scheduleId = findScheduleId(date);
        doThrow(new RuntimeException("promotion failed"))
                .when(reservationDao)
                .promoteToReserved(anyLong());

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, reserver))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("promotion failed");

        assertThat(countByScheduleAndStatus(scheduleId)).containsExactlyInAnyOrderEntriesOf(Map.of(
                ReservationStatus.RESERVED.name(), 1,
                ReservationStatus.WAITING.name(), 1
        ));
    }

    private Member saveMember(String loginId, String name) {
        Long id = memberDao.save(new Member(null, loginId, name, "password", Role.USER));
        return memberDao.findById(id).orElseThrow();
    }

    private long findScheduleId(LocalDate date) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM schedule WHERE date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                date,
                TIME_ID,
                THEME_ID
        );
    }

    private int countSchedules(LocalDate date) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM schedule WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class,
                date,
                TIME_ID,
                THEME_ID
        );
    }

    private int countReservations() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
    }

    private Map<String, Integer> countByScheduleAndStatus(long scheduleId) {
        return jdbcTemplate.query(
                """
                        SELECT status, COUNT(*) AS count
                        FROM reservation
                        WHERE schedule_id = ?
                        GROUP BY status
                        """,
                rs -> {
                    Map<String, Integer> counts = new HashMap<>();
                    while (rs.next()) {
                        counts.put(rs.getString("status"), rs.getInt("count"));
                    }
                    return counts;
                },
                scheduleId
        );
    }
}
