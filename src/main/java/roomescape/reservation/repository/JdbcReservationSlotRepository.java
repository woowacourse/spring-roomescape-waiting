package roomescape.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationSlot;

import java.sql.Date;

@Repository
@RequiredArgsConstructor
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public ReservationSlot upsert(ReservationSlot reservationSlot) {
        jdbcTemplate.update("""
                MERGE INTO reservation_slot (date, time_id, theme_id)
                KEY(date, time_id, theme_id)
                VALUES (:date, :timeId, :themeId)
                """, slotParameterSource(reservationSlot));

        Long id = jdbcTemplate.queryForObject("""
                SELECT id
                FROM reservation_slot
                WHERE date = :date
                  AND time_id = :timeId
                  AND theme_id = :themeId
                """, slotParameterSource(reservationSlot), Long.class);

        return ReservationSlot.of(id, reservationSlot.getDate(), reservationSlot.getTime(), reservationSlot.getTheme());
    }

    private MapSqlParameterSource slotParameterSource(ReservationSlot reservationSlot) {
        return new MapSqlParameterSource()
                .addValue("date", Date.valueOf(reservationSlot.getDate()))
                .addValue("timeId", reservationSlot.getTimeId())
                .addValue("themeId", reservationSlot.getThemeId());
    }

}
