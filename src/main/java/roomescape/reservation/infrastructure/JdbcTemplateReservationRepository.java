package roomescape.reservation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.infrastructure.mapper.BookedThemeRowMapper;
import roomescape.reservation.infrastructure.mapper.ReservationRowMapper;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.Entry;

@Repository
@RequiredArgsConstructor
public class JdbcTemplateReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public Map<Theme, Integer> findThemesToBookedCountByParamsOrderByBookedCount(final ReservationDate startDate,
                                                                                 final ReservationDate endDate,
                                                                                 final int count) {
        final String sql = """
                select
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail,
                    count(*) as booked_count
                from reservations r
                join themes t
                    on r.theme_id = t.id
                where
                    r.date between ? and ?
                group by 
                    t.id, t.name, t.description, t.thumbnail
                order by
                    booked_count desc  
                limit
                    ?
                """;

        return jdbcTemplate.query(
                        sql,
                        BookedThemeRowMapper.INSTANCE,
                        startDate.getValue(),
                        endDate.getValue(),
                        count)
                .stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    public List<Reservation> findAllByParams(final UserId userId,
                                             final ThemeId themeId,
                                             final ReservationDate dateFrom,
                                             final ReservationDate dateTo) {
        final StringBuilder sql = new StringBuilder("""
                select
                    r.id,
                    r.user_id,
                    r.date,
                    rt.id as time_id,
                    rt.start_at as start_at,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description as description,
                    t.thumbnail as thumbnail
                from reservations r
                join reservation_times rt
                    on r.time_id = rt.id
                join themes t
                    on r.theme_id = t.id
                where 1=1
                """);
        final List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND r.user_id = ?");
            params.add(userId.getValue());
        }
        if (themeId != null) {
            sql.append(" AND r.theme_id = ?");
            params.add(themeId.getValue());
        }
        if (dateFrom != null) {
            sql.append(" AND r.date >= ?");
            params.add(dateFrom.getValue());
        }
        if (dateTo != null) {
            sql.append(" AND r.date <= ?");
            params.add(dateTo.getValue());
        }

        return jdbcTemplate.query(sql.toString(), ReservationRowMapper.INSTANCE, params.toArray());
    }
}
