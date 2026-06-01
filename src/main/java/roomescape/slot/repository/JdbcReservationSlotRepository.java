package roomescape.slot.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.slot.domain.ReservationSlot;

@Repository
public class JdbcReservationSlotRepository implements ReservationSlotRepository{

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

}
