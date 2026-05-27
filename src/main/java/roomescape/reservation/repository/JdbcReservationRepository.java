package roomescape.reservation.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert reservationInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    private static final String BASE_SELECT = """
            SELECT r.id,
                   r.name,
                   r.time_id,
                   r.theme_id,
                   r.status,
                   r.created_at,
                   rt.start_time,
                   rt.end_time,
                   t.name AS theme_name,
                   t.description AS theme_description,
                   t.image_url AS theme_image_url
            FROM reservation r
            LEFT JOIN reservation_time rt ON r.time_id = rt.id
            LEFT JOIN theme t ON r.theme_id = t.id
            """;

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(BASE_SELECT, new ReservationRowMapper());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        List<Reservation> results = jdbcTemplate.query(
                BASE_SELECT + "WHERE r.id = ?",
                new ReservationRowMapper(),
                id
        );
        return results.stream().findFirst();
    }

    @Override
    public List<Reservation> findByName(String name) {
        return jdbcTemplate.query(
                BASE_SELECT + "WHERE r.name = ?",
                new ReservationRowMapper(),
                name
        );
    }

    @Override
    public boolean update(Long id, Long timeId) {
        int affected = jdbcTemplate.update(
                "UPDATE reservation SET time_id = ? WHERE id = ?",
                timeId, id
        );
        return affected > 0;
    }

    @Override
    public Reservation save(Reservation reservation) {
        Number id = reservationInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name())
                .addValue("created_at", reservation.getCreatedAt()));
        return reservation.withId(id.longValue());
    }

    @Override
    public boolean isDuplicated(Long themeId, ReservationTime time) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE theme_id = ? AND time_id = ?)",
                Integer.class,
                themeId,
                time.getId()
        );
        return exists != null && exists == 1;
    }

    @Override
    public List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date) {
        return jdbcTemplate.query(
                """
                SELECT r.time_id FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                WHERE r.theme_id = ? AND rt.start_time >= ? AND rt.start_time < ?
                """,
                (rs, rowNum) -> rs.getLong("time_id"),
                themeId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );
    }

    @Override
    public boolean deleteById(Long id) {
        int affectedRows = jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
        return affectedRows > 0;
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE time_id = ?)",
                Integer.class,
                timeId
        );
        return exists != null && exists == 1;
    }

    @Override
    public Optional<Long> findEarliestWaiting(Long timeId, Long themeId) {
        Long waitingId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation "
                        + "WHERE time_id = ? "
                        + "AND theme_id = ? "
                        + "AND status = WAITING "
                        + "ORDER BY created_at ASC "
                        + "LIMIT 1",
                Long.class,
                timeId, themeId);
        return Optional.ofNullable(waitingId);
    }

    @Override
    public boolean updateStatus(Long waitingId) {
        int affected = jdbcTemplate.update(
                "UPDATE reservation SET status = RESERVED where id = ?",
                waitingId);
        return affected > 0;
    }

    private static class ReservationRowMapper implements RowMapper<Reservation> {
        @Override
        public Reservation mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReservationTime time = null;
            Long timeId = rs.getLong("time_id");
            if (!rs.wasNull()) {
                time = new ReservationTime(
                        timeId,
                        rs.getObject("start_time", LocalDateTime.class),
                        rs.getObject("end_time", LocalDateTime.class)
                );
            }

            Theme theme = null;
            String themeName = rs.getString("theme_name");
            if (themeName != null) {
                theme = new Theme(
                        themeName,
                        rs.getString("theme_description"),
                        rs.getString("theme_image_url")
                ).withId(rs.getLong("theme_id"));
            }

            Status status = Status.valueOf(rs.getString("status"));
            LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);

            return new Reservation(
                    rs.getString("name"),
                    time,
                    theme,
                    status,
                    createdAt
            ).withId(rs.getLong("id"));
        }
    }


}
