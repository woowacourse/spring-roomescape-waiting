package roomescape.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.response.ReservationTimeStatusResult;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Reservation save(final Reservation newReservation) {
        final String sql = """
                INSERT INTO reservation (name, date, time_id, theme_id)
                VALUES (:name, :date, :timeId, :themeId)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", newReservation.getName())
                .addValue("date", Date.valueOf(newReservation.getReservationDate().getDate()))
                .addValue("timeId", newReservation.getTime().getId())
                .addValue("themeId", newReservation.getTheme().getId());

        try {
            jdbcTemplate.update(sql, param, keyHolder);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_RESERVED);
        }

        final long newReservationId = keyHolder.getKey().longValue();
        return newReservation.withId(newReservationId);
    }

    public List<Reservation> findAll() {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.theme_id AS theme_id,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    t.end_at AS time_end_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme h ON r.theme_id = h.id
                ORDER BY r.id
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource(), reservationRowMapper())
                .stream()
                .toList();
    }

    public List<Reservation> findByName(final String name) {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.theme_id AS theme_id,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    t.end_at AS time_end_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme h ON r.theme_id = h.id
                WHERE r.name = :name
                ORDER BY r.id
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", name);

        return jdbcTemplate.query(sql, param, reservationRowMapper())
                .stream()
                .toList();
    }

    public Optional<Reservation> findById(final Long reservationId) {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.theme_id AS theme_id,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    t.end_at AS time_end_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme h ON r.theme_id = h.id
                WHERE r.id = :id
                """;

        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("id", reservationId);
            final Reservation reservation = jdbcTemplate.queryForObject(sql, param, reservationRowMapper());
            return Optional.of(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<ReservationTimeStatusResult> findReservationTimeStatusesByDateAndThemeId(final LocalDate date, final Long themeId) {
        final String sql = """
                SELECT
                    rt.id,
                    rt.start_at,
                    CASE
                        WHEN r.id IS NOT NULL THEN TRUE
                        ELSE FALSE
                    END AS reserved
                FROM reservation_time rt
                LEFT JOIN reservation r
                    ON r.time_id = rt.id
                   AND r.date = :date
                   AND r.theme_id = :themeId
                ORDER BY rt.start_at;
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId);

        return jdbcTemplate.query(sql, param, timesWithStatusRowMapper())
                .stream()
                .toList();
    }

    public boolean existsByNameAndDateAndTimeIdAndThemeId(final String name, final LocalDate date, final Long timeId, final Long themeId) {
        final String sql = """
                SELECT COUNT(id)
                FROM reservation
                WHERE name = :name AND date = :date AND time_id = :timeId AND theme_id = :themeId
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);

        final Integer count = jdbcTemplate.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        final String sql = """
                SELECT COUNT(id)
                FROM reservation
                WHERE date = :date AND time_id = :timeId AND theme_id = :themeId
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);

        final Integer count = jdbcTemplate.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    public boolean existsByTimeId(final Long timeId) {
        final String sql = """
                SELECT COUNT(id)
                FROM reservation
                WHERE time_id = :timeId
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("timeId", timeId);

        final Integer count = jdbcTemplate.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    public boolean existsByThemeId(final Long themeId) {
        final String sql = """
                SELECT COUNT(id)
                FROM reservation
                WHERE theme_id = :themeId
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("themeId", themeId);

        final Integer count = jdbcTemplate.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    public void update(final Reservation reservation) {
        final String sql = """
                UPDATE reservation
                SET date = :date, time_id = :timeId, theme_id = :themeId
                WHERE id = :id
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("date", reservation.getReservationDate().getDate())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId())
                .addValue("id", reservation.getId());

        jdbcTemplate.update(sql, param);
    }

    public void deleteById(final Long reservationId) {
        final String sql = """
                DELETE FROM reservation
                WHERE id = :id
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", reservationId);

        jdbcTemplate.update(sql, param);
    }

    private RowMapper<Reservation> reservationRowMapper() {
        return (rs, rowNum) -> {
            final ReservationTime reservationTime = ReservationTime.createWithId(
                    rs.getLong("time_id"),
                    rs.getTime("time_start_at").toLocalTime(),
                    rs.getTime("time_end_at").toLocalTime()
            );

            final Theme theme = Theme.createWithId(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail_url")
            );

            return Reservation.createWithId(
                    rs.getLong("reservation_id"),
                    rs.getString("reservation_name"),
                    rs.getDate("reservation_date").toLocalDate(),
                    reservationTime,
                    theme
            );
        };
    }

    private RowMapper<ReservationTimeStatusResult> timesWithStatusRowMapper() {
        return (rs, rowNum) -> new ReservationTimeStatusResult(
                rs.getLong("id"),
                rs.getTime("start_at").toLocalTime(),
                rs.getBoolean("reserved")
        );
    }
}
