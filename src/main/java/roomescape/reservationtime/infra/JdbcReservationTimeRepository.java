package roomescape.reservationtime.infra;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.NotFoundException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.AvailableReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.reservationtime.exception.ReservationTimeErrorMessage;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time WHERE id = ?",
                (rs, rowNum) -> ReservationTime.builder()
                        .id(rs.getLong("id"))
                        .startAt(rs.getTime("start_at").toLocalTime())
                        .build(),
                id).stream().findFirst();
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time ORDER BY start_at ASC",
                (rs, rowNum) -> ReservationTime.builder()
                        .id(rs.getLong("id"))
                        .startAt(rs.getTime("start_at").toLocalTime())
                        .build()
        );
    }

    @Override
    public List<AvailableReservationTime> findAvailableByThemeAndDate(Long themeId, LocalDate date) {
        String sql = """
                SELECT rt.id, rt.start_at,
                    NOT EXISTS (
                        SELECT 1 FROM reservation r
                        WHERE r.time_id = rt.id AND r.theme_id = ? AND r.date = ?
                    ) AS available
                FROM reservation_time rt
                ORDER BY rt.start_at ASC
                """;
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new AvailableReservationTime(
                        rs.getLong("id"),
                        rs.getTime("start_at").toLocalTime(),
                        rs.getBoolean("available")
                ),
                themeId, date);
    }

    @Override
    public ReservationTime save(ReservationTime time) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", time.getStartAt());
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return time.withId(id);
    }

    @Override
    public void delete(Long id) {
        int count = jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
        if (count == 0) {
            throw new NotFoundException(ReservationTimeErrorMessage.TIME_NOT_FOUND, id);
        }
    }

    @Override
    public Boolean existsByStartAt(LocalTime startAt) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation_time WHERE start_at = ?)",
                Boolean.class,
                startAt);
    }
}
