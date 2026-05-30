package roomescape.reservation.repository;

import javax.sql.DataSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationSlot;

@Repository
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationSlotRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("reservation_slot")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public void saveIfAbsent(ReservationSlot reservationSlot) {
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("date_id", reservationSlot.getDateId())
            .addValue("time_id", reservationSlot.getTimeId())
            .addValue("theme_id", reservationSlot.getThemeId());
        try {
            simpleJdbcInsert.execute(params);
        } catch (DuplicateKeyException ignored) {

        }
    }

    @Override
    public void lockByDateTimeAndThemeId(Long dateId, Long timeId, Long themeId) {
        String sql = """
            SELECT id
            FROM reservation_slot
            WHERE date_id = :dateId
              AND time_id = :timeId
              AND theme_id = :themeId
            FOR UPDATE
            """;
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("dateId", dateId)
            .addValue("timeId", timeId)
            .addValue("themeId", themeId);

        try {
            jdbcTemplate.queryForObject(sql, params, Long.class);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalStateException("락을 획득할 슬롯이 존재하지 않습니다.", e);
        }
    }
}
