package roomescape.reservationWaiting.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcReservationWaitingRepository implements ReservationWaitingRepository {

    private static final RowMapper<ReservationWaiting> RESERVATION_WAITING_ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("time_start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url")
        );

        return new ReservationWaiting(
                resultSet.getLong("reservation_waiting_id"),
                resultSet.getString("reservation_waiting_name"),
                resultSet.getDate("reservation_waiting_date").toLocalDate(),
                time,
                theme
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        String sql = """
                INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservationWaiting.getName());
            ps.setDate(2, Date.valueOf(reservationWaiting.getDate()));
            ps.setLong(3, reservationWaiting.getTime().getId());
            ps.setLong(4, reservationWaiting.getTheme().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return reservationWaiting.updateId(id);
    }

    @Override
    public Optional<ReservationWaiting> findById(Long id) {
        String sql = """
                SELECT r.id AS reservation_waiting_id,
                       r.name AS reservation_waiting_name,
                       r.reservation_date AS reservation_waiting_date,
                       r.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url
                FROM reservation_waiting r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_WAITING_ROW_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public Optional<ReservationWaiting> findByReservationDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT r.id AS reservation_waiting_id,
                       r.name AS reservation_waiting_name,
                       r.reservation_date AS reservation_waiting_date,
                       r.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url
                FROM reservation_waiting r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                WHERE r.reservation_date = ? AND time_id = ? AND theme_id = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_WAITING_ROW_MAPPER, date, timeId, themeId)
                .stream().findFirst();
    }

    @Override
    public List<ReservationWaiting> findAllByName(String name) {
        String sql = """
                SELECT r.id AS reservation_waiting_id,
                               r.name AS reservation_waiting_name,
                               r.reservation_date AS reservation_waiting_date,
                               r.time_id,
                               t.start_at AS time_start_at,
                               h.id AS theme_id,
                               h.name AS theme_name,
                               h.description AS theme_description,
                               h.thumbnail_url AS theme_thumbnail_url
                        FROM reservation_waiting r
                        INNER JOIN reservation_time t
                          ON r.time_id = t.id
                        INNER JOIN theme h
                          ON r.theme_id = h.id
                WHERE r.name = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_WAITING_ROW_MAPPER, name);
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeIdAndName(
            LocalDate date, Long timeId, Long themeId, String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation_waiting
                    WHERE reservation_date = ? AND time_id = ? AND theme_id = ? AND name = ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId, name);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation_waiting
                    WHERE reservation_date = ? AND time_id = ? AND theme_id = ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public int deleteById(Long id) {
        String sql = """
                DELETE FROM reservation_waiting
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, id);
    }

    @Override
    public long countByReservationDateAndTimeIdAndThemeIdAndIdLessThan(
            LocalDate date, Long timeId, Long themeId, Long id) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation_waiting 
                WHERE reservation_date = ? AND time_id = ? AND theme_id = ? AND id < ?;
                """;

        return jdbcTemplate.queryForObject(sql, Long.class, date, timeId, themeId, id);
    }
}
