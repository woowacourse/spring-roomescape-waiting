package roomescape.domain.reservation.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.reservation.error.type.ReservationErrorType;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.type.ErrorType;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("reservation")
            .usingColumns("name", "date", "time_id", "theme_id", "status")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findReservationsByDeletedAtIsNull() {
        return jdbcTemplate.query(
            """
                SELECT r.id, r.name, r.date, r.status,
                       rt.id AS time_id, rt.start_at, rt.deleted_at AS time_deleted_at,
                       t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                       t.deleted_at AS theme_deleted_at
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.status <> 'DELETED'
                ORDER BY r.id ASC
                """,
            (rs, rowNum) -> mapReservation(rs)
        );
    }

    @Override
    public List<Reservation> findReservationsByNameAndDeletedAtIsNull(String name) {
        String sql = """
            SELECT r.id, r.name, r.date, r.status,
                   rt.id AS time_id, rt.start_at, rt.deleted_at AS time_deleted_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   t.deleted_at AS theme_deleted_at
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            WHERE r.name = :name
              AND r.status <> 'DELETED'
            ORDER BY r.date ASC, rt.start_at ASC
            """;
        SqlParameterSource parameters = new MapSqlParameterSource("name", name);

        return jdbcTemplate.query(
            sql,
            parameters,
            (rs, rowNum) -> mapReservation(rs)
        );
    }

    @Override
    public Optional<Reservation> findReservationByIdAndDeletedAtIsNull(Long id) {
        String sql = """
            SELECT r.id, r.name, r.date, r.status,
                   rt.id AS time_id, rt.start_at, rt.deleted_at AS time_deleted_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   t.deleted_at AS theme_deleted_at
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            WHERE r.id = :id
              AND r.status <> 'DELETED'
            """;
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        List<Reservation> reservations = jdbcTemplate.query(
            sql,
            parameters,
            (rs, rowNum) -> mapReservation(rs)
        );

        return reservations.stream().findFirst();
    }

    @Override
    public List<Long> findTimeIdsByDateAndThemeIdAndDeletedAtIsNull(LocalDate date, Long themeId) {
        String sql = """
            SELECT r.time_id
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            WHERE r.date = :date
              AND r.theme_id = :themeId
              AND r.status = 'ACTIVE'
              AND rt.deleted_at IS NULL
              AND t.deleted_at IS NULL
            """;
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "date", date,
            "themeId", themeId
        ));

        return jdbcTemplate.query(
            sql,
            parameters,
            (resultSet, rowNum) -> resultSet.getLong("time_id"));
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> args = Map.of(
            "name", reservation.getName(),
            "date", reservation.getDate(),
            "time_id", reservation.getTime().getId(),
            "theme_id", reservation.getTheme().getId(),
            "status", reservation.getStatus().name()
        );
        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(args).longValue();

        return Reservation.reconstruct(generatedKey, reservation.getName(), reservation.getDate(),
            reservation.getTime(), reservation.getTheme(), reservation.getStatus());
    }

    @Override
    public Reservation update(Reservation reservation) {
        String sql = """
            UPDATE reservation
            SET name = :name,
                date = :date,
                time_id = :timeId,
                theme_id = :themeId,
                status = :status
            WHERE id = :id
            """;
        SqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("id", reservation.getId())
            .addValue("name", reservation.getName())
            .addValue("date", reservation.getDate())
            .addValue("timeId", reservation.getTime().getId())
            .addValue("themeId", reservation.getTheme().getId())
            .addValue("status", reservation.getStatus().name());

        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            throw new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND);
        }

        return Reservation.reconstruct(reservation.getId(), reservation.getName(), reservation.getDate(),
            reservation.getTime(), reservation.getTheme(), reservation.getStatus());
    }

    @Override
    public int countByIdLessThanEqualAndDateAndTimeAndTheme(Long reservationId, LocalDate date, Time time, Theme theme) {
        String countSql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE id <= :id
                  AND date = :date
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND status = 'WAITING'
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", reservationId)
                .addValue("date", date)
                .addValue("timeId", time.getId())
                .addValue("themeId", theme.getId());

        return jdbcTemplate.queryForObject(countSql, parameters, Integer.class);
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        return Reservation.reconstruct(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getDate("date").toLocalDate(),
            Time.reconstruct(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime(),
                getNullableLocalDateTime(rs, "time_deleted_at")
            ),
            Theme.reconstruct(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("image_url"),
                getNullableLocalDateTime(rs, "theme_deleted_at")
            ),
            ReservationStatus.valueOf(rs.getString("status"))
        );
    }

    private LocalDateTime getNullableLocalDateTime(ResultSet rs, String columnLabel)
        throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(columnLabel);
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    @Override
    public void deleteReservationById(Long id) {
        String sql = "UPDATE reservation SET status = 'DELETED' WHERE id = :id AND status <> 'DELETED'";
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            throw new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND);
        }
    }

    @Override
    public boolean existsReservationByIdAndDeletedAtIsNull(Long id) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE id = :id
                  AND status <> 'DELETED'
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsReservationByDateAndTimeAndThemeAndDeletedAtIsNull(LocalDate date, Time time, Theme theme) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE date = :date
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND status = 'ACTIVE'
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "date", date,
            "timeId", time.getId(),
            "themeId", theme.getId()
        ));

        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsReservation(Reservation reservation) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE date = :date
                  AND name = :name
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND status = :status
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "date", reservation.getDate(),
            "name", reservation.getName(),
            "timeId", reservation.getTime().getId(),
            "themeId", reservation.getTheme().getId(),
            "status", reservation.getStatus().name()
        ));

        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsReservationByDateAndTimeAndThemeAndDeletedAtIsNullAndIdNot(LocalDate date, Time time,
        Theme theme, Long id) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE date = :date
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND id != :id
                  AND status = 'ACTIVE'
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "date", date,
            "timeId", time.getId(),
            "themeId", theme.getId(),
            "id", id
        ));

        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }
}
