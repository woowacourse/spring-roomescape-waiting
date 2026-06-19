package roomescape.slot.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.application.port.out.SlotRepository;
import roomescape.slot.domain.Slot;
import roomescape.theme.domain.Theme;

@Repository
@RequiredArgsConstructor
public class JdbcSlotRepository implements SlotRepository {
    private final NamedParameterJdbcTemplate template;
    private final RowMapper<Slot> slotRowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url"),
                resultSet.getInt("theme_price")
        );
        return Slot.of(
                resultSet.getLong("id"),
                resultSet.getDate("date").toLocalDate(),
                time,
                theme,
                resultSet.getInt("price")
        );
    };

    @Override
    public Slot save(Slot slot) {
        String sql = "INSERT INTO slot(date, time_id, theme_id, price) VALUES (:date, :timeId, :themeId, :price)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", slot.getDate())
                .addValue("timeId", slot.getTimeId())
                .addValue("themeId", slot.getThemeId())
                .addValue("price", slot.getPrice());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, params, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("slot 저장 후 생성된 ID를 반환받지 못했습니다.");
        }

        return Slot.of(
                id.longValue(),
                slot.getDate(),
                slot.getTime(),
                slot.getTheme(),
                slot.getPrice()
        );
    }

    @Override
    public Optional<Slot> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT
                    s.id,
                    s.date,
                    s.price,
                    rt.id AS time_id,
                    rt.start_at,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    t.price AS theme_price
                FROM slot s
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN theme t ON s.theme_id = t.id
                WHERE s.date = :date
                AND s.time_id = :timeId
                AND s.theme_id = :themeId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);

        return template.query(sql, params, slotRowMapper)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Slot> findById(long id) {
        String sql = """
                SELECT
                    s.id,
                    s.date,
                    s.price,
                    rt.id AS time_id,
                    rt.start_at,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    t.price AS theme_price
                FROM slot s
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN theme t ON s.theme_id = t.id
                WHERE s.id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return template.query(sql, params, slotRowMapper).stream().findFirst();
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        String sql = "SELECT COUNT(1) FROM slot WHERE time_id = :timeId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("timeId", timeId);

        Integer count = template.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        String sql = "SELECT COUNT(1) FROM slot WHERE theme_id = :themeId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", themeId);

        Integer count = template.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public List<Slot> findAll() {
        String sql = """
                SELECT
                    s.id,
                    s.date,
                    s.price,
                    rt.id AS time_id,
                    rt.start_at,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    t.price AS theme_price
                FROM slot s
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN theme t ON s.theme_id = t.id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        return template.query(sql, params, slotRowMapper);
    }

    @Override
    public void deleteById(long slotId) {
        String sql = "DELETE FROM slot WHERE id = :slotId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", slotId);

        try {
            template.update(sql, params);
        } catch (DataIntegrityViolationException e) {
            throw new EscapeRoomException(ErrorCode.SLOT_IN_USE, slotId);
        }
    }

    @Override
    public boolean existsByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM slot WHERE date = :date AND time_id = :timeId AND theme_id = :themeId)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);

        return Boolean.TRUE.equals(template.queryForObject(sql, params, Boolean.class));
    }
}
