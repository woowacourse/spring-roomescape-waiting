package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.ReservationErrorCode;
import roomescape.exception.RoomEscapeException;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    private static RowMapper<Reservation> getReservationRowMapper() {
        return (resultSet, rowNum) -> {
            ReservationTime time = ReservationTime.of(resultSet.getLong("reservation_time_id"),
                    LocalTime.parse(resultSet.getString("time_value")));

            Theme theme = Theme.of(resultSet.getLong("reservation_theme_id"),
                    resultSet.getString("reservation_theme_name"),
                    resultSet.getString("reservation_theme_description"),
                    resultSet.getString("reservation_theme_image_url"));

            ReservationSlot slot = ReservationSlot.of(resultSet.getDate("date").toLocalDate(), time,
                    theme);

            return Reservation.of(resultSet.getLong("reservation_id"),
                    resultSet.getString("name"),
                    slot);
        };
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationSlot slot = reservation.getReservationSlot();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", slot.getDate())
                .addValue("time_id", slot.getTime().getId())
                .addValue("theme_id", slot.getTheme().getId());

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(
                params).longValue();

        return Reservation.of(generatedKey, reservation.getName(),
                reservation.getReservationSlot());
    }

    @Override
    public Reservation update(Long id, ReservationSlot slot) {
        String sql = """
                    UPDATE reservation
                    SET date = :date,
                        time_id = :time_id
                    WHERE id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", slot.getDate())
                .addValue("time_id", slot.getTime().getId())
                .addValue("id", id);

        jdbcTemplate.update(sql, params);
        return findById(id).orElseThrow(
                () -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    @Override
    public void delete(Long id) {
        String sql = """
                    DELETE FROM reservation
                    WHERE id = :id
                """;
        Map<String, Object> params = Map.of("id", id);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                    SELECT r.id AS reservation_id,
                           r.name,
                           r.date,
                           t.id AS reservation_time_id,
                           t.start_at AS time_value,
                           th.id AS reservation_theme_id,
                           th.name AS reservation_theme_name,
                           th.description AS reservation_theme_description,
                           th.image_url AS reservation_theme_image_url
                    FROM reservation AS r
                    INNER JOIN reservation_time AS t
                      ON r.time_id = t.id
                    INNER JOIN theme AS th
                      ON r.theme_id = th.id
                    WHERE r.id = :id
                """;

        Map<String, Object> params = Map.of("id", id);

        List<Reservation> results = jdbcTemplate.query(sql, params, getReservationRowMapper());
        return results.stream().findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                    SELECT r.id AS reservation_id,
                           r.name,
                           r.date,
                           t.id AS reservation_time_id,
                           t.start_at AS time_value,
                           th.id AS reservation_theme_id,
                           th.name AS reservation_theme_name,
                           th.description AS reservation_theme_description,
                           th.image_url AS reservation_theme_image_url
                    FROM reservation AS r
                    INNER JOIN reservation_time AS t
                      ON r.time_id = t.id
                    INNER JOIN theme AS th
                      ON r.theme_id = th.id
                """;

        return jdbcTemplate.query(sql, getReservationRowMapper());
    }

    @Override
    public List<Reservation> findByName(String name) {
        String sql = """
                    SELECT r.id AS reservation_id,
                           r.name,
                           r.date,
                           t.id AS reservation_time_id,
                           t.start_at AS time_value,
                           th.id AS reservation_theme_id,
                           th.name AS reservation_theme_name,
                           th.description AS reservation_theme_description,
                           th.image_url AS reservation_theme_image_url
                    FROM reservation AS r
                    INNER JOIN reservation_time AS t
                      ON r.time_id = t.id
                    INNER JOIN theme AS th
                      ON r.theme_id = th.id
                    WHERE r.name = :name
                """;

        Map<String, Object> params = Map.of("name", name);

        return jdbcTemplate.query(sql, params, getReservationRowMapper());
    }

    @Override
    public Optional<Reservation> findBySlotWithLock(ReservationSlot slot) {
        String sql = """
                    SELECT r.id AS reservation_id,
                           r.name,
                           r.date,
                           t.id AS reservation_time_id,
                           t.start_at AS time_value,
                           th.id AS reservation_theme_id,
                           th.name AS reservation_theme_name,
                           th.description AS reservation_theme_description,
                           th.image_url AS reservation_theme_image_url
                    FROM reservation AS r
                    INNER JOIN reservation_time AS t
                      ON r.time_id = t.id
                    INNER JOIN theme AS th
                      ON r.theme_id = th.id
                    WHERE r.date = :date
                      AND t.id = :time_id
                      AND th.id = :theme_id
                    FOR UPDATE
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", slot.getDate())
                .addValue("time_id", slot.getTime().getId()).
                addValue("theme_id", slot.getTheme().getId());

        List<Reservation> results = jdbcTemplate.query(sql, params, getReservationRowMapper());
        return results.stream().findFirst();
    }

    @Override
    public boolean existByTimeId(Long timeId) {
        String sql = """
                    SELECT EXISTS (
                      SELECT 1
                      FROM reservation
                      WHERE time_id = :time_id
                    )
                """;
        Map<String, Object> params = Map.of("time_id", timeId);
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, params, Boolean.class)
        );
    }

    @Override
    public boolean existByThemeId(Long themeId) {
        String sql = """
                    SELECT EXISTS (
                      SELECT 1
                      FROM reservation
                      WHERE theme_id = :theme_id
                    )
                """;
        Map<String, Object> params = Map.of("theme_id", themeId);
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, params, Boolean.class)
        );
    }

    @Override
    public boolean existsBySlot(ReservationSlot slot) {
        String sql = """
                SELECT EXISTS (
                      SELECT 1
                      FROM reservation
                      WHERE date = :date
                        AND time_id = :time_id
                        AND theme_id = :theme_id
                    )
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", slot.getDate())
                .addValue("time_id", slot.getTime().getId())
                .addValue("theme_id", slot.getTheme().getId());

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, params, Boolean.class)
        );
    }
}
