package roomescape.reservation.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.DuplicateException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> {
        Theme theme = Theme.restore(
                resultSet.getLong("t_id"),
                resultSet.getString("t_name"),
                resultSet.getString("t_thumbnail_image_url"),
                resultSet.getString("t_description"),
                resultSet.getBoolean("t_is_active")
        );

        ReservationTime time = ReservationTime.restore(
                resultSet.getLong("rt_id"),
                resultSet.getTime("rt_start_at").toLocalTime(),
                resultSet.getBoolean("rt_is_active")
        );

        return Reservation.restore(
                resultSet.getLong("r_id"),
                resultSet.getString("r_name"),
                resultSet.getDate("r_date").toLocalDate(),
                time,
                theme,
                Status.valueOf(resultSet.getString("r_status")),
                resultSet.getTimestamp("r_created_at").toLocalDateTime()
        );
    };

    private static final String BASE_SELECT = """
            SELECT
                r.id AS r_id,
                r.name AS r_name,
                r.date AS r_date,
                r.status AS r_status,
                r.created_at AS r_created_at,
                t.id AS t_id,
                t.name AS t_name,
                t.thumbnail_image_url AS t_thumbnail_image_url,
                t.description AS t_description,
                t.is_active AS t_is_active,
                rt.id AS rt_id,
                rt.start_at AS rt_start_at,
                rt.is_active AS rt_is_active
            FROM reservation r
            INNER JOIN theme t ON r.theme_id = t.id
            INNER JOIN reservation_time rt ON r.time_id = rt.id
            """;

    @Override
    public Reservation save(Reservation reservation) {
        try {
            String sql = """
                    INSERT INTO reservation(name, date, time_id, theme_id, status, created_at)
                    VALUES(:name, :date, :timeId, :themeId, :status, :createdAt)
                    """;

            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("name", reservation.getName())
                    .addValue("date", reservation.getDate())
                    .addValue("timeId", reservation.getTime().getId())
                    .addValue("themeId", reservation.getTheme().getId())
                    .addValue("status", reservation.getStatus().name())
                    .addValue("createdAt", reservation.getCreatedAt());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
            long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
            return reservation.withId(generatedId);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e.getMessage());
        }
    }

    @Override
    public void update(Reservation reservation) {
        try {
            String sql = """
                    UPDATE reservation
                    SET date = :date,
                        time_id = :timeId,
                        theme_id = :themeId,
                        status = :status
                    WHERE id = :id
                        AND status IN ('RESERVED', 'WAITING')
                    """;

            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("date", reservation.getDate())
                    .addValue("timeId", reservation.getTime().getId())
                    .addValue("themeId", reservation.getTheme().getId())
                    .addValue("status", reservation.getStatus().name())
                    .addValue("id", reservation.getId());

            jdbcTemplate.update(sql, params);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e.getMessage());
        }
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = BASE_SELECT + """
                WHERE r.id = :id
                    AND r.status IN ('RESERVED', 'WAITING')
                """;
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper).stream().findFirst();
    }

    @Override
    public List<Reservation> findAll(int page, int size) {
        String sql = BASE_SELECT + """
                WHERE r.status = 'RESERVED'
                ORDER BY r.date ASC, rt.start_at ASC
                LIMIT :size OFFSET :offset
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("size", size)
                .addValue("offset", page * size);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<Reservation> findByThemeAndDate(Long themeId, LocalDate date) {
        String sql = BASE_SELECT + """
                WHERE r.theme_id = :themeId
                    AND r.date = :date
                    AND r.status = 'RESERVED'
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("date", date);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<Reservation> findAllByName(String username) {
        String sql = BASE_SELECT + """
                WHERE r.name = :username
                    AND r.status IN ('RESERVED', 'WAITING')
                ORDER BY r.created_at DESC
                """;

        return jdbcTemplate.query(sql, Map.of("username", username), rowMapper);
    }

    @Override
    public Long countWaitingBefore(Reservation reservation) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE status = 'WAITING'
                    AND date = :date
                    AND time_id = :timeId
                    AND theme_id = :themeId
                    AND (
                        created_at < :createdAt
                        OR (created_at = :createdAt AND id < :id)
                    )
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId())
                .addValue("createdAt", reservation.getCreatedAt())
                .addValue("id", reservation.getId());

        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public boolean existsByReservationTime(Long timeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE time_id = :timeId
                        AND status = 'RESERVED'
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("timeId", timeId), Boolean.class));
    }

    @Override
    public boolean existsActiveReservationByDateTimeAndTheme(Long timeId, Long themeId, LocalDate date) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE time_id = :timeId
                        AND theme_id = :themeId
                        AND date = :date
                        AND status = 'RESERVED'
                )
                """;
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Map.of("timeId", timeId, "themeId", themeId, "date", date),
                        Boolean.class));
    }

    @Override
    public boolean existsByUsernameAndDateTimeAndTheme(Long timeId, Long themeId, LocalDate date, String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE theme_id = :themeId
                        AND time_id = :timeId
                        AND date = :date
                        AND name = :name
                        AND status IN ('WAITING', 'RESERVED')
                )
                """;
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql,
                        Map.of("themeId", themeId, "timeId", timeId, "date", date, "name", name),
                        Boolean.class));
    }

    @Override
    public boolean existsByTheme(Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE theme_id = :themeId
                        AND status IN ('RESERVED', 'WAITING')
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("themeId", themeId), Boolean.class));
    }

    @Override
    public Optional<Reservation> findNextWaitingReservation(LocalDate date, Long timeId, Long themeId) {
        String sql = BASE_SELECT + """
                WHERE r.date = :date
                    AND r.time_id = :timeId
                    AND r.theme_id = :themeId
                    AND r.status = 'WAITING'
                ORDER BY r.created_at ASC
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);

        return jdbcTemplate.query(sql, params, rowMapper).stream().findFirst();
    }
}
