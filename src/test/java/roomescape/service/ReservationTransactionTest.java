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
