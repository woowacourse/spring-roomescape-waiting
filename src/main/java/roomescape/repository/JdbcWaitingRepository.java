package roomescape.repository;

import java.time.LocalDate;
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
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    private static RowMapper<Waiting> getWaitingRowMapper() {
        return (resultSet, rowNum) -> {
            ReservationTime time = ReservationTime.of(
                    resultSet.getLong("reservation_time_id"),
                    LocalTime.parse(resultSet.getString("time_value"))
            );

            Theme theme = Theme.of(resultSet.getLong("reservation_theme_id"),
                    resultSet.getString("reservation_theme_name"),
                    resultSet.getString("reservation_theme_description"),
                    resultSet.getString("reservation_theme_image_url")
            );

            ReservationSlot slot = ReservationSlot.of(
                    resultSet.getDate("date").toLocalDate(),
                    time,
                    theme
            );

            return Waiting.of(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    slot,
                    resultSet.getLong("waiting_number"));
        };
    }

    private static RowMapper<WaitingWithOrder> getWaitingWithOrderRowMapper() {
        return (resultSet, rowNum) -> {
            ReservationTime time = ReservationTime.of(
                    resultSet.getLong("reservation_time_id"),
                    LocalTime.parse(resultSet.getString("time_value"))
            );

            Theme theme = Theme.of(resultSet.getLong("reservation_theme_id"),
                    resultSet.getString("reservation_theme_name"),
                    resultSet.getString("reservation_theme_description"),
                    resultSet.getString("reservation_theme_image_url")
            );

            ReservationSlot slot = ReservationSlot.of(
                    resultSet.getDate("date").toLocalDate(),
                    time,
                    theme
            );

            Waiting waiting = Waiting.of(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    slot,
                    resultSet.getLong("waiting_number"));

            return WaitingWithOrder.of(
                    waiting,
                    resultSet.getLong("waiting_order")
            );
        };
    }

    @Override
    public Waiting save(Waiting waiting) {
        ReservationSlot slot = waiting.getReservationSlot();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", waiting.getName())
                .addValue("date", slot.getDate())
                .addValue("time_id", slot.getTime().getId())
                .addValue("theme_id", slot.getTheme().getId())
                .addValue("waiting_number", waiting.getWaitingNumber());

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(
                params).longValue();

        return Waiting.of(
                generatedKey,
                waiting.getName(),
                waiting.getReservationSlot(),
                waiting.getWaitingNumber());
    }

    @Override
    public void delete(Long id) {
        String sql = """
                    DELETE FROM waiting
                    WHERE id = :id
                """;
        Map<String, Object> params = Map.of("id", id);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        String sql = """
                    SELECT w.id AS id,
                           w.name,
                           w.date,
                           w.waiting_number,
                           t.id AS reservation_time_id,
                           t.start_at AS time_value,
                           th.id AS reservation_theme_id,
                           th.name AS reservation_theme_name,
                           th.description AS reservation_theme_description,
                           th.image_url AS reservation_theme_image_url
                    FROM waiting AS w
                    INNER JOIN reservation_time AS t
                      ON w.time_id = t.id
                    INNER JOIN theme AS th
                      ON w.theme_id = th.id
                    WHERE w.id = :id
                """;

        Map<String, Object> params = Map.of("id", id);

        List<Waiting> results = jdbcTemplate.query(sql, params, getWaitingRowMapper());
        return results.stream().findFirst();
    }

    @Override
    public List<WaitingWithOrder> findAll() {
        String sql = """
                    SELECT w1.id AS id,
                               w1.name,
                               w1.date,
                               w1.waiting_number,
                               t.id AS reservation_time_id,
                               t.start_at AS time_value,
                               th.id AS reservation_theme_id,
                               th.name AS reservation_theme_name,
                               th.description AS reservation_theme_description,
                               th.image_url AS reservation_theme_image_url,
                               (
                                    SELECT COUNT(*)
                                    FROM waiting AS w2
                                    WHERE w2.date = w1.date
                                          AND w2.time_id = w1.time_id
                                          AND w2.theme_id = w1.theme_id 
                                          AND w2.waiting_number <= w1.waiting_number
                                ) AS waiting_order
                        FROM waiting AS w1
                        INNER JOIN reservation_time AS t
                          ON w1.time_id = t.id
                        INNER JOIN theme AS th
                          ON w1.theme_id = th.id
                        ORDER BY
                            w1.date ASC,
                            w1.time_id ASC,
                            w1.theme_id ASC,
                            w1.waiting_number ASC;
                """;

        return jdbcTemplate.query(sql, getWaitingWithOrderRowMapper());

    }

    @Override
    public List<WaitingWithOrder> findByName(String name) {
        String sql = """
                    SELECT w1.id AS id,
                               w1.name,
                               w1.date,
                               w1.waiting_number,
                               t.id AS reservation_time_id,
                               t.start_at AS time_value,
                               th.id AS reservation_theme_id,
                               th.name AS reservation_theme_name,
                               th.description AS reservation_theme_description,
                               th.image_url AS reservation_theme_image_url,
                               (
                                    SELECT COUNT(*)
                                    FROM waiting AS w2
                                    WHERE w2.date = w1.date
                                          AND w2.time_id = w1.time_id
                                          AND w2.theme_id = w1.theme_id 
                                          AND w2.waiting_number <= w1.waiting_number
                                ) AS waiting_order
                        FROM waiting AS w1
                        INNER JOIN reservation_time AS t
                          ON w1.time_id = t.id
                        INNER JOIN theme AS th
                          ON w1.theme_id = th.id
                        WHERE w1.name = :name
                        ORDER BY
                            w1.date ASC,
                            w1.time_id ASC,
                            w1.theme_id ASC,
                            w1.waiting_number ASC;
                """;

        Map<String, Object> params = Map.of("name", name);

        return jdbcTemplate.query(sql, params, getWaitingWithOrderRowMapper());
    }

    @Override
    public Optional<Long> findMaxWaitingNumberBy(LocalDate date, ReservationTime reservationTime,
            Theme theme) {
        String sql = """
                    SELECT MAX(waiting_number)
                    FROM waiting
                    WHERE date = :date
                      AND time_id = :time_id
                      AND theme_id = :theme_id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("time_id", reservationTime.getId())
                .addValue("theme_id", theme.getId());

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, Long.class));
    }

    @Override
    public Optional<Waiting> findPromotableWaitingBySlot(ReservationSlot slot) {
        String sql = """
                SELECT w.id AS id,
                           w.name,
                           w.date,
                           w.waiting_number,
                           t.id AS reservation_time_id,
                           t.start_at AS time_value,
                           th.id AS reservation_theme_id,
                           th.name AS reservation_theme_name,
                           th.description AS reservation_theme_description,
                           th.image_url AS reservation_theme_image_url
                FROM waiting AS w
                INNER JOIN reservation_time AS t 
                ON w.time_id = t.id
                INNER JOIN theme AS th
                 ON w.theme_id = th.id
                WHERE w.date = :date
                    AND t.id = :time_id
                    AND th.id = :theme_id
                ORDER BY w.waiting_number ASC
                LIMIT 1;
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", slot.getDate())
                .addValue("time_id", slot.getTime().getId())
                .addValue("theme_id", slot.getTheme().getId());

        List<Waiting> result = jdbcTemplate.query(sql, params, getWaitingRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public boolean existsByNameAndSlot(String name, ReservationSlot slot) {
        String sql = """
                    SELECT EXISTS (
                      SELECT 1
                      FROM waiting
                      WHERE name = :name
                        AND date = :date
                        AND time_id = :time_id
                        AND theme_id = :theme_id
                    )
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("date", slot.getDate())
                .addValue("time_id", slot.getTime().getId())
                .addValue("theme_id", slot.getTheme().getId());

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, params, Boolean.class)
        );
    }
}
