package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;

@Repository
public class SlotRepository {
    private static final String SELECT_BASE = """
            SELECT s.id            AS slot_id,
                   s.date          AS slot_date,
                   rt.id           AS reservation_time_id,
                   rt.start_at     AS reservation_time_start_at,
                   t.id            AS theme_id,
                   t.name          AS theme_name,
                   t.description   AS theme_description,
                   t.thumbnail_url AS theme_thumbnail_url
            FROM slot s
            INNER JOIN reservation_time rt ON s.time_id  = rt.id
            INNER JOIN theme             t  ON s.theme_id = t.id
            """;

    public static final RowMapper<Slot> SLOT_ROW_MAPPER = (rs, rowNum) -> {
        ReservationTime reservationTime = ReservationTime.of(
                rs.getLong("reservation_time_id"),
                rs.getTime("reservation_time_start_at").toLocalTime());
        Theme theme = Theme.load(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail_url"));
        return Slot.load(
                rs.getLong("slot_id"),
                rs.getDate("slot_date").toLocalDate(),
                reservationTime,
                theme);
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public SlotRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("slot")
                .usingGeneratedKeyColumns("id")
                .usingColumns("date", "time_id", "theme_id");
    }

    public List<Slot> findAll() {
        return jdbcTemplate.query(SELECT_BASE, SLOT_ROW_MAPPER);
    }

    public List<Slot> findAllByName(String name) {
        return jdbcTemplate.query(
                SELECT_BASE + "INNER JOIN reservation r ON r.slot_id = s.id WHERE r.name = :name",
                new MapSqlParameterSource("name", name),
                SLOT_ROW_MAPPER);
    }

    public Optional<Slot> findById(long slotId) {
        List<Slot> result = jdbcTemplate.query(
                SELECT_BASE + "WHERE s.id = :slotId",
                new MapSqlParameterSource("slotId", slotId),
                SLOT_ROW_MAPPER);
        return result.stream().findFirst();
    }

    public Optional<Slot> findByDateAndTimeAndTheme(ReservationDate date, ReservationTime time, Theme theme) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date.getDate())
                .addValue("timeId", time.getId())
                .addValue("themeId", theme.getId());

        List<Slot> result = jdbcTemplate.query(
                SELECT_BASE + "WHERE s.date = :date AND s.time_id = :timeId AND s.theme_id = :themeId",
                params,
                SLOT_ROW_MAPPER);
        return result.stream().findFirst();
    }

    public Optional<Slot> findByDateAndTimeAndThemeForUpdate(ReservationDate date, ReservationTime time, Theme theme) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date.getDate())
                .addValue("timeId", time.getId())
                .addValue("themeId", theme.getId());

        List<Slot> result = jdbcTemplate.query(
                SELECT_BASE + "WHERE s.date = :date AND s.time_id = :timeId AND s.theme_id = :themeId FOR UPDATE",
                params,
                SLOT_ROW_MAPPER);
        return result.stream().findFirst();
    }

    public Optional<Slot> findByIdForUpdate(long slotId) {
        List<Slot> result = jdbcTemplate.query(
                SELECT_BASE + "WHERE s.id = :slotId FOR UPDATE",
                new MapSqlParameterSource("slotId", slotId),
                SLOT_ROW_MAPPER);
        return result.stream().findFirst();
    }

    public Slot save(Slot slot) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", slot.getDate().getDate())
                .addValue("time_id", slot.getTime().getId())
                .addValue("theme_id", slot.getTheme().getId());

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return slot.withId(generatedKey);
    }

    public Slot update(long id, Slot target) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", target.getDate().getDate())
                .addValue("time_id", target.getTime().getId())
                .addValue("theme_id", target.getTheme().getId())
                .addValue("id", id);

        jdbcTemplate.update("UPDATE slot SET date = :date, time_id = :time_id, theme_id = :theme_id WHERE id = :id", params);
        return findById(id).orElseThrow();
    }

    public void deleteById(long id) {
        jdbcTemplate.update("DELETE FROM slot WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    public boolean existsByTimeId(long timeId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM slot WHERE time_id = :timeId)",
                new MapSqlParameterSource("timeId", timeId),
                Boolean.class));
    }

    public boolean existsByThemeId(long themeId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM slot WHERE theme_id = :themeId)",
                new MapSqlParameterSource("themeId", themeId),
                Boolean.class));
    }
}
