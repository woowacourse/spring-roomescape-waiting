package roomescape.infrastructure.persistence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationQueryRepository;
import roomescape.domain.Theme;
import roomescape.domain.dto.AvailableTimeDto;
import roomescape.infrastructure.persistence.rowmapper.ReservationRowMapper;
import roomescape.infrastructure.persistence.rowmapper.ThemeRowMapper;

@Repository
public class JdbcReservationQueryRepository implements ReservationQueryRepository {
    private static final String SQL = """
            select r.id as reservation_id,
                   r.member_id as member_id,
                   m.name as member_name,
                   m.email as member_email,
                   m.password as member_password,
                   m.role as member_role,
                   r.date as reservation_date, 
                   r.time_id as reservation_time_id,
                   rt.start_at as reservation_time_start_at, 
                   r.theme_id as theme_id,
                   t.name as theme_name,
                   t.description as theme_description, 
                   t.thumbnail as theme_thumbnail 
            from reservation as r
            join reservation_time as rt
            on r.time_id = rt.id
            join theme as t
            on r.theme_id = t.id  
            join member as m
            on r.member_id = m.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Reservation> findById(long id) {
        String sql = SQL + "where r.id = ?";
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, ReservationRowMapper::joinedMapRow, id);
            return Optional.of(Objects.requireNonNull(reservation));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(SQL, ReservationRowMapper::joinedMapRow);
    }

    @Override
    public long findReservationCountByTimeId(long timeId) {
        String sql = "select count(*) from reservation where time_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, timeId);
    }

    @Override
    public boolean existBy(LocalDate date, long timeId, long themeId) {
        String sql = "select exists(select 1 from reservation where date = ? and time_id = ? and theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
    }

    @Override
    public List<AvailableTimeDto> findAvailableReservationTimes(LocalDate date, long themeId) {
        String sql = """
                select  id as reservation_time_id, 
                        start_at as reservation_time_start_at, 
                        start_at in (
                            select start_at
                            from reservation as r
                            join reservation_time as rt 
                            on r.time_id = rt.id
                            where date = ? and r.theme_id = ?
                        ) as is_booked
                from    reservation_time;
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AvailableTimeDto(
                rs.getLong("id"),
                rs.getTime("start_at").toLocalTime(),
                rs.getBoolean("is_booked")
        ), date, themeId);
    }

    @Override
    public List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
                select  t.id as theme_id, 
                        t.name as theme_name, 
                        t.description as theme_description, 
                        t.thumbnail as theme_thumbnail, 
                        count(r.id) as reservation_count
                from theme as t 
                join reservation as r 
                on t.id = r.theme_id
                where r.date between ? and ?
                group by t.id
                order by reservation_count desc
                limit ?
                """;

        return jdbcTemplate.query(sql, ThemeRowMapper::mapRow, startDate, endDate, limit);
    }

    @Override
    public List<Reservation> findByCriteria(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        String sql = SQL;

        addCondition(themeId, conditions, "r.theme_id = ?", params);
        addCondition(memberId, conditions, "r.member_id = ?", params);
        addCondition(dateFrom, conditions, "r.date >= ?", params);
        addCondition(dateTo, conditions, "r.date <= ?", params);

        if (!conditions.isEmpty()) {
            sql += " where " + String.join(" and ", conditions);
        }

        return jdbcTemplate.query(sql, ReservationRowMapper::joinedMapRow, params.toArray());
    }

    private void addCondition(Object param, List<String> conditions, String sql, List<Object> params) {
        if (param == null) {
            return;
        }
        conditions.add(sql);
        params.add(param);
    }
}
