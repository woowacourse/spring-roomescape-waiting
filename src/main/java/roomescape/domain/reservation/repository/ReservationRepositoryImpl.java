package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.domain.Role;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private static final String RESERVATION_SQL = """
                SELECT *
                FROM reservation as r
                INNER JOIN reservation_time as t
                ON r.time_id = t.id
                INNER JOIN theme
                ON r.theme_id = theme.id
                INNER JOIN member as m 
                ON r.member_id = m.id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) ->
            new Reservation(
                    resultSet.getLong("id"),
                    resultSet.getDate("date").toLocalDate(),
                    new ReservationTime(
                            resultSet.getLong("reservation_time.id"),
                            resultSet.getTime("reservation_time.start_at").toLocalTime()
                    ),
                    new Theme(
                            resultSet.getLong("theme.id"),
                            resultSet.getString("theme.name"),
                            resultSet.getString("theme.description"),
                            resultSet.getString("theme.thumbnail")
                    ),
                    new Member(resultSet.getLong("member.id"),
                            resultSet.getString("member.name"),
                            resultSet.getString("member.email"),
                            resultSet.getString("member.password"),
                            Role.convertToRole(resultSet.getString("member.role"))
                    )
            );

    private RowMapper<ReservationTime> timeRowMapper = ((rs, rowNum) -> new ReservationTime(
            rs.getLong("reservation_time.id"),
            rs.getTime("reservation_time.start_at").toLocalTime()
    ));
    private RowMapper<Theme> themeRowMapper = ((rs, rowNum) -> new Theme(
            rs.getLong("theme.id"),
            rs.getString("theme.name"),
            rs.getString("theme.description"),
            rs.getString("theme.thumbnail")
    ));

    public ReservationRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("RESERVATION")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(RESERVATION_SQL, rowMapper);
    }

    @Override
    public List<Reservation> findAllBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        String condition = findWhereStatement(themeId, memberId, dateFrom, dateTo);
        String sql = RESERVATION_SQL + condition;
        return jdbcTemplate.query(sql, rowMapper);
    }

    private static String findWhereStatement(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        List<String> condition = new ArrayList<>();
        if (themeId != null) {
            condition.add("r.theme_id = " + themeId);
        }
        if (memberId != null) {
            condition.add("r.member_id = " + memberId);
        }
        if (dateFrom != null) {
            condition.add("r.date >= '" + dateFrom + "'");
        }
        if (dateTo != null) {
            condition.add("r.date <= '" + dateTo + "'");
        }
        if (condition.isEmpty()) {
            return "";
        }
        return "WHERE " + String.join(" AND ", condition);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = RESERVATION_SQL + " WHERE r.id = ? ";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public Reservation insert(Reservation reservation) {
        Map<String, Object> reservationRow = new HashMap<>();
        reservationRow.put("date", reservation.getDate());
        reservationRow.put("time_id", reservation.getTimeId());
        reservationRow.put("theme_id", reservation.getThemeId());
        reservationRow.put("member_id", reservation.getMemberId());

        Long id = simpleJdbcInsert.executeAndReturnKey(reservationRow).longValue();
        return new Reservation(id, reservation.getDate(), reservation.getTime(),
                reservation.getTheme(), reservation.getMember());
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                select exists (
                    select 1
                    from reservation
                    where date = ? and time_id = ? and theme_id = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
    }

    @Override
    public List<ReservationTime> findTimesByDateAndTheme(LocalDate date, Long themeId) {
        String sql = """
                select t.id, t.start_at
                from reservation as r
                inner join reservation_time as t
                on r.time_id = t.id
                where date = ? and theme_id = ?
                """;

        return jdbcTemplate.query(sql, timeRowMapper, date, themeId);
    }

    @Override
    public List<Theme> findThemeOrderByReservationCount() {
        String sql = """
                select
                    th.id as theme_id,
                    th.name as theme_name,
                    th.description as theme_description,
                    th.thumbnail as theme_thumbnail,
                    count(th.id) as reservation_count
                from reservation as r
                inner join theme as th
                on r.theme_id = th.id
                where r.date between dateadd('day', -7, current_date()) and dateadd('day', -1, current_date())
                group by th.id
                order by reservation_count desc
                limit 10
                """;

        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("delete from reservation where id = ?", id);
    }
}
