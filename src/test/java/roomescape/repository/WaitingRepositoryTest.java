package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.WaitingWithRank;

@DataJpaTest
@Sql({"/schema.sql", "/test-data.sql"})
class WaitingRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("사용자의 대기 목록을 같은 테마 날짜 시간 안의 순번과 함께 조회한다")
    void findWithRankByMemberName() {
        LocalDate firstDate = LocalDate.now().plusDays(30);
        LocalDate secondDate = LocalDate.now().plusDays(31);
        insertConfirmedReservation(firstDate, 1L, 1L, "첫확정");
        insertWaiting("앞대기", firstDate, 1L, 1L);
        insertWaiting("브라운", firstDate, 1L, 1L);
        insertConfirmedReservation(secondDate, 2L, 2L, "둘확정");
        insertWaiting("브라운", secondDate, 2L, 2L);
        insertWaiting("뒤대기", secondDate, 2L, 2L);

        List<WaitingWithRank> waitings = waitingRepository.findWithRankByMemberName("브라운");

        assertThat(waitings)
                .extracting(WaitingWithRank::rank)
                .containsExactly(2L, 1L);
    }

    private void insertConfirmedReservation(LocalDate date, long themeId, long timeId, String name) {
        jdbcTemplate.update("""
                INSERT INTO theme_slot (theme_id, date, time_id, is_reserved)
                VALUES (?, ?, ?, true)
                """, themeId, date, timeId);
        Long themeSlotId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM theme_slot
                WHERE theme_id = ?
                AND date = ?
                AND time_id = ?
                """, Long.class, themeId, date, timeId);
        jdbcTemplate.update("""
                INSERT INTO reservation (name, status, theme_slot_id)
                VALUES (?, 'CONFIRMED', ?)
                """, name, themeSlotId);
    }

    private void insertWaiting(String memberName, LocalDate date, long themeId, long timeId) {
        jdbcTemplate.update("""
                INSERT INTO waiting (member_name, date, theme_id, time_id)
                VALUES (?, ?, ?, ?)
                """, memberName, date, themeId, timeId);
    }
}
