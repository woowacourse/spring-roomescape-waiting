package roomescape.adapter.persistence;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationTimeRepository;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<ReservationTime> ROW_MAPPER = (rs, rowNum) -> ReservationTime.withId(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime()
    );


    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time",
                ROW_MAPPER
        );
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        List<ReservationTime> result = jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time WHERE id = ?",
                ROW_MAPPER,
                id
        );
        return result.stream().findFirst();
    }

    @Override
    public ReservationTime save(ReservationTime time) {
        String sql = "INSERT INTO reservation_time (start_at) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, Time.valueOf(time.getStartAt()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return ReservationTime.withId(id, time.getStartAt());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
    }

    @Override
    public List<ReservationTime> findAvailable(LocalDate date, Long themeId) {
        String sql = """
                SELECT id, start_at
                FROM reservation_time
                WHERE id NOT IN (
                    SELECT time_id FROM reservation
                    WHERE date = ? AND theme_id = ?
                )
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, Date.valueOf(date), themeId);
    }
}
