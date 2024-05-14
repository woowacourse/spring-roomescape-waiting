package roomescape.repository;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.rowmapper.ReservationTimeRowMapper;

@Repository
public class JdbcTemplateReservationTimeRepository implements ReservationTimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ReservationTimeRowMapper reservationTimeRowMapper;

    public JdbcTemplateReservationTimeRepository(JdbcTemplate jdbcTemplate,
                                                 ReservationTimeRowMapper reservationTimeRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationTimeRowMapper = reservationTimeRowMapper;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        save(reservationTime, keyHolder);
        long id = keyHolder.getKey().longValue();
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private void save(ReservationTime reservationTime, KeyHolder keyHolder) {
        jdbcTemplate.update(con -> {
            String sql = "INSERT INTO reservation_time(start_at) VALUES ( ? )";
            PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"id"});
            pstmt.setTime(1, Time.valueOf(reservationTime.getStartAt()));
            return pstmt;
        }, keyHolder);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation_time WHERE start_at = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, startAt));
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
        return jdbcTemplate.query(sql, reservationTimeRowMapper, id)
                .stream()
                .findAny();
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(sql, reservationTimeRowMapper);
    }

    @Override
    public List<ReservationTime> findUsedTimeByDateAndTheme(LocalDate date, Theme theme) {
        String sql = """
                SELECT
                    rt.id, start_at
                FROM reservation_time rt
                JOIN reservation r
                    ON rt.id = r.time_id
                WHERE r.date = ?
                AND r.theme_id = ?
                """;
        return jdbcTemplate.query(sql, reservationTimeRowMapper, date, theme.getId());
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
