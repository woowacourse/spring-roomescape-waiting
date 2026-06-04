package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Store;
import roomescape.domain.Theme;

@Repository
public class SlotJdbcRepository implements SlotRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Slot> rowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_image_url")
        );
        Store store = new Store(
                resultSet.getLong("store_id"),
                resultSet.getString("store_name")
        );
        return new Slot(
                resultSet.getLong("id"),
                resultSet.getDate("date").toLocalDate(),
                theme,
                time,
                store
        );
    };

    private static final String SELECT_SLOT = """
            select rs.id, rs.date,
                   t.id as time_id, t.start_at,
                   th.id as theme_id, th.name as theme_name, th.description, th.thumbnail_image_url,
                   s.id as store_id, s.name as store_name
            from reservation_slot rs
            join reservation_time t on rs.time_id = t.id
            join theme th on rs.theme_id = th.id
            join store s on rs.store_id = s.id
            """;

    public SlotJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Slot> findById(Long id) {
        String sql = SELECT_SLOT + " where rs.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Slot> findByDateAndThemeAndTimeAndStore(LocalDate date, Long themeId, Long timeId, Long storeId) {
        String sql = SELECT_SLOT + " where rs.date = ? and rs.theme_id = ? and rs.time_id = ? and rs.store_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, date, themeId, timeId, storeId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Long save(Slot slot) {
        String sql = "insert into reservation_slot(date, theme_id, time_id, store_id) values(?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setDate(1, Date.valueOf(slot.getDate()));
            ps.setLong(2, slot.getTheme().getId());
            ps.setLong(3, slot.getTime().getId());
            ps.setLong(4, slot.getStore().getId());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}