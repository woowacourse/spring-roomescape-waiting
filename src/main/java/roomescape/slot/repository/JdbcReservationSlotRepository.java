package roomescape.slot.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.date.domain.ReservationDate;
import roomescape.slot.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationSlotRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("reservation_slot")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationSlot save(ReservationSlot slot) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date_id", slot.getDateId())
                .addValue("time_id", slot.getTimeId())
                .addValue("theme_id", slot.getThemeId());

        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return ReservationSlot.load(id, slot.getDate(), slot.getTime(), slot.getTheme());
    }

    @Override
    public List<ReservationSlot> findAll() {
        String sql = """
                SELECT
                    rs.id           AS slot_id,
                    rd.id           AS date_id,
                    rd.date         AS date,
                    rd.is_active    AS date_is_active,
                    rt.id           AS time_id,
                    rt.start_at     AS start_at,
                    rt.is_active    AS time_is_active,
                    t.id            AS theme_id,
                    t.name          AS theme_name,
                    t.description   AS description,
                    t.thumbnail_url AS thumbnail_url,
                    t.is_active     AS theme_is_active,
                    t.amount        AS amount
                FROM reservation_slot rs
                JOIN reservation_date rd ON rs.date_id  = rd.id
                JOIN reservation_time rt ON rs.time_id  = rt.id
                JOIN theme             t  ON rs.theme_id = t.id
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<ReservationSlot> findById(Long slotId) {
        String sql = """
                SELECT
                    rs.id           AS slot_id,
                    rd.id           AS date_id,
                    rd.date         AS date,
                    rd.is_active    AS date_is_active,
                    rt.id           AS time_id,
                    rt.start_at     AS start_at,
                    rt.is_active    AS time_is_active,
                    t.id            AS theme_id,
                    t.name          AS theme_name,
                    t.description   AS description,
                    t.thumbnail_url AS thumbnail_url,
                    t.is_active     AS theme_is_active,
                    t.amount        AS amount
                FROM reservation_slot rs
                JOIN reservation_date rd ON rs.date_id  = rd.id
                JOIN reservation_time rt ON rs.time_id  = rt.id
                JOIN theme             t  ON rs.theme_id = t.id
                WHERE rs.id = :slotId
                  AND rd.is_active  = true
                  AND rt.is_active  = true
                  AND t.is_active   = true
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", slotId);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ReservationSlot> findByIdWithLock(Long slotId) {
        String sql = """
                SELECT
                    rs.id           AS slot_id,
                    rd.id           AS date_id,
                    rd.date         AS date,
                    rd.is_active    AS date_is_active,
                    rt.id           AS time_id,
                    rt.start_at     AS start_at,
                    rt.is_active    AS time_is_active,
                    t.id            AS theme_id,
                    t.name          AS theme_name,
                    t.description   AS description,
                    t.thumbnail_url AS thumbnail_url,
                    t.is_active     AS theme_is_active,
                    t.amount        AS amount
                FROM reservation_slot rs
                JOIN reservation_date rd ON rs.date_id  = rd.id
                JOIN reservation_time rt ON rs.time_id  = rt.id
                JOIN theme             t  ON rs.theme_id = t.id
                WHERE rs.id = :slotId
                  AND rd.is_active  = true
                  AND rt.is_active  = true
                  AND t.is_active   = true
                FOR UPDATE OF rs
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", slotId);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ReservationSlot> findAvailableByDateIdTimeIdThemeId(Long dateId, Long timeId, Long themeId) {
        String sql = """
                SELECT
                    rs.id           AS slot_id,
                    rd.id           AS date_id,
                    rd.date         AS date,
                    rd.is_active    AS date_is_active,
                    rt.id           AS time_id,
                    rt.start_at     AS start_at,
                    rt.is_active    AS time_is_active,
                    t.id            AS theme_id,
                    t.name          AS theme_name,
                    t.description   AS description,
                    t.thumbnail_url AS thumbnail_url,
                    t.is_active     AS theme_is_active,
                    t.amount        AS amount
                FROM reservation_slot rs
                JOIN reservation_date rd ON rs.date_id  = rd.id
                JOIN reservation_time rt ON rs.time_id  = rt.id
                JOIN theme             t  ON rs.theme_id = t.id
                WHERE rs.date_id = :dateId
                  AND rs.time_id = :timeId
                  AND rs.theme_id = :themeId
                  AND rd.is_active  = true
                  AND rt.is_active  = true
                  AND t.is_active   = true
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("dateId", dateId)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private final RowMapper<ReservationSlot> rowMapper = (rs, rowNum) -> {
        ReservationDate date = ReservationDate.load(
                rs.getLong("date_id"),
                rs.getObject("date", LocalDate.class),
                rs.getBoolean("date_is_active")
        );

        ReservationTime time = ReservationTime.load(
                rs.getLong("time_id"),
                rs.getObject("start_at", LocalTime.class),
                rs.getBoolean("time_is_active")
        );

        Theme theme = Theme.load(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("thumbnail_url"),
                rs.getBoolean("theme_is_active"),
                rs.getLong("amount")
        );

        return ReservationSlot.load(
                rs.getLong("slot_id"),
                date,
                time,
                theme
        );
    };

}
