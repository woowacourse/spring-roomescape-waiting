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
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.reservation.error.type.ReservationErrorType;
import roomescape.domain.reservation.vo.ReservationSchedule;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;
import roomescape.global.error.exception.GeneralException;

public class JdbcReservationRepository {

    private static final String WAITING_NUMBER_EXPRESSION = """
        CASE
            WHEN r.status = 'WAITING' THEN
                SUM(CASE WHEN r.status = 'WAITING' THEN 1 ELSE 0 END) OVER (
                    PARTITION BY r.date, r.time_id, r.theme_id
                    ORDER BY r.id ASC
                    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                )
            ELSE NULL
        END AS waiting_number
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("reservation")
            .usingColumns("name", "date", "time_id", "theme_id", "status")
            .usingGeneratedKeyColumns("id");
    }


    public List<ReservationWithWaitingNumber> findReservationsByNotDeletedWithWaitingNumber() {
        String sql = """
            SELECT r.id, r.name, r.date, r.status, r.version,
                   rt.id AS time_id, rt.start_at, rt.deleted_at AS time_deleted_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   t.deleted_at AS theme_deleted_at,
                   %s
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            WHERE r.deleted_at IS NULL
            ORDER BY r.id ASC
            """.formatted(WAITING_NUMBER_EXPRESSION);

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapReservationWithWaitingNumber(rs));
    }

    private ReservationWithWaitingNumber mapReservationWithWaitingNumber(ResultSet rs) throws SQLException {
        return new ReservationWithWaitingNumber(mapReservation(rs), getNullableInteger(rs, "waiting_number"));
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
            ReservationStatus.valueOf(rs.getString("status")),
            rs.getLong("version")
        );
    }

    private Integer getNullableInteger(ResultSet rs, String columnLabel) throws SQLException {
        Number number = (Number) rs.getObject(columnLabel);
        if (number == null) {
            return null;
        }
        return number.intValue();
    }

    private LocalDateTime getNullableLocalDateTime(ResultSet rs, String columnLabel)
        throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(columnLabel);
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }


    public List<ReservationWithWaitingNumber> findReservationsByNameAndNotDeletedWithWaitingNumber(String name) {
        String sql = """
            SELECT ranked.id, ranked.name, ranked.date, ranked.status, ranked.version,
                   ranked.time_id, ranked.start_at, ranked.time_deleted_at,
                   ranked.theme_id, ranked.theme_name, ranked.description, ranked.image_url,
                   ranked.theme_deleted_at, ranked.waiting_number
            FROM (
                SELECT r.id, r.name, r.date, r.status, r.version,
                       rt.id AS time_id, rt.start_at, rt.deleted_at AS time_deleted_at,
                       t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                       t.deleted_at AS theme_deleted_at,
                       %s
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.deleted_at IS NULL
            ) ranked
            WHERE ranked.name = :name
            ORDER BY ranked.date ASC, ranked.start_at ASC, ranked.id ASC
            """.formatted(WAITING_NUMBER_EXPRESSION);
        SqlParameterSource parameters = new MapSqlParameterSource("name", name);

        return jdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapReservationWithWaitingNumber(rs));
    }


    public Optional<Reservation> findReservationByIdAndNotDeleted(Long id) {
        String sql = """
            SELECT r.id, r.name, r.date, r.status, r.version,
                   rt.id AS time_id, rt.start_at, rt.deleted_at AS time_deleted_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   t.deleted_at AS theme_deleted_at
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            WHERE r.id = :id
              AND r.deleted_at IS NULL
            """;
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        List<Reservation> reservations = jdbcTemplate.query(
            sql,
            parameters,
            (rs, rowNum) -> mapReservation(rs)
        );

        return reservations.stream().findFirst();
    }


    public Optional<Reservation> lockReservationByIdAndNotDeleted(Long id) {
        String sql = """
            SELECT r.id, r.name, r.date, r.status, r.version,
                   rt.id AS time_id, rt.start_at, rt.deleted_at AS time_deleted_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   t.deleted_at AS theme_deleted_at
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            WHERE r.id = :id
              AND r.deleted_at IS NULL
            FOR UPDATE
            """;
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        List<Reservation> reservations = jdbcTemplate.query(
            sql,
            parameters,
            (rs, rowNum) -> mapReservation(rs)
        );

        return reservations.stream().findFirst();
    }


    public List<Long> findTimeIdsByDateAndThemeIdAndNotDeleted(LocalDate date, Long themeId) {
        String sql = """
            SELECT r.time_id
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            WHERE r.date = :date
              AND r.theme_id = :themeId
              AND r.status = 'ACTIVE'
              AND r.deleted_at IS NULL
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


    public boolean existsActiveReservationBySchedule(ReservationSchedule schedule) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.date = :date
                  AND r.theme_id = :themeId
                  AND r.time_id = :timeId
                  AND r.status = 'ACTIVE'
                  AND r.deleted_at IS NULL
                  AND rt.deleted_at IS NULL
                  AND t.deleted_at IS NULL
            )
            """;
        SqlParameterSource parameters = toScheduleParameterSource(schedule);

        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }


    public Optional<Long> lockActiveReservationBySchedule(ReservationSchedule schedule) {
        String sql = """
            SELECT r.id
            FROM reservation r
            WHERE r.date = :date
              AND r.theme_id = :themeId
              AND r.time_id = :timeId
              AND r.status = 'ACTIVE'
              AND r.deleted_at IS NULL
              AND EXISTS (
                  SELECT 1
                  FROM reservation_time rt
                  WHERE rt.id = r.time_id
                    AND rt.deleted_at IS NULL
              )
              AND EXISTS (
                  SELECT 1
                  FROM theme t
                  WHERE t.id = r.theme_id
                    AND t.deleted_at IS NULL
              )
            FOR UPDATE
            """;
        SqlParameterSource parameters = toScheduleParameterSource(schedule);

        List<Long> reservationIds = jdbcTemplate.query(
            sql,
            parameters,
            (rs, rowNum) -> rs.getLong("id")
        );

        return reservationIds.stream().findFirst();
    }


    public Optional<Reservation> lockFirstWaitingReservationBySchedule(ReservationSchedule schedule) {
        String sql = """
            SELECT r.id, r.name, r.date, r.status, r.version,
                   rt.id AS time_id, rt.start_at, rt.deleted_at AS time_deleted_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   t.deleted_at AS theme_deleted_at
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            WHERE r.date = :date
              AND r.theme_id = :themeId
              AND r.time_id = :timeId
              AND r.status = 'WAITING'
              AND r.deleted_at IS NULL
              AND rt.deleted_at IS NULL
              AND t.deleted_at IS NULL
            ORDER BY r.id ASC
            LIMIT 1
            FOR UPDATE
            """;
        SqlParameterSource parameters = toScheduleParameterSource(schedule);

        List<Reservation> reservations = jdbcTemplate.query(
            sql,
            parameters,
            (rs, rowNum) -> mapReservation(rs)
        );

        return reservations.stream().findFirst();
    }

    private SqlParameterSource toScheduleParameterSource(ReservationSchedule schedule) {
        return new MapSqlParameterSource(Map.of(
            "date", schedule.date(),
            "themeId", schedule.themeId(),
            "timeId", schedule.timeId()
        ));
    }


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


    public Reservation update(Reservation reservation) {
        String sql = """
            UPDATE reservation
            SET name = :name,
                date = :date,
                time_id = :timeId,
                theme_id = :themeId,
                status = :status,
                version = version + 1
            WHERE id = :id
              AND deleted_at IS NULL
              AND status <> 'CANCELED'
              AND version = :version
            """;
        SqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("id", reservation.getId())
            .addValue("name", reservation.getName())
            .addValue("date", reservation.getDate())
            .addValue("timeId", reservation.getTime().getId())
            .addValue("themeId", reservation.getTheme().getId())
            .addValue("status", reservation.getStatus().name())
            .addValue("version", reservation.getVersion());

        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            if (!existsReservationByIdAndNotDeleted(reservation.getId())) {
                throw new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND);
            }
            if (isCanceledReservationById(reservation.getId())) {
                throw new GeneralException(ReservationErrorType.ALREADY_CANCELED);
            }
            throw new GeneralException(ReservationErrorType.RESERVATION_ALREADY_UPDATED);
        }

        return Reservation.reconstruct(reservation.getId(), reservation.getName(), reservation.getDate(),
            reservation.getTime(), reservation.getTheme(), reservation.getStatus(), reservation.getVersion() + 1);
    }

    private boolean isCanceledReservationById(Long id) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE id = :id
                  AND status = 'CANCELED'
                  AND deleted_at IS NULL
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }


    public void deleteReservationById(Long id) {
        String sql = "UPDATE reservation SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id AND deleted_at IS NULL";
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            throw new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND);
        }
    }


    public boolean existsReservationByIdAndNotDeleted(Long id) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE id = :id
                  AND deleted_at IS NULL
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }


    public boolean existsReservationAndStatus(Reservation reservation, ReservationStatus status) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE date = :date
                  AND name = :name
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND status = :status
                  AND deleted_at IS NULL
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "date", reservation.getDate(),
            "name", reservation.getName(),
            "timeId", reservation.getTime().getId(),
            "themeId", reservation.getTheme().getId(),
            "status", status.name()
        ));

        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }
}
