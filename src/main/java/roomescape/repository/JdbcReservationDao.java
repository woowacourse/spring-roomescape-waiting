package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationName;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.util.WhereClauseParamSet;
import roomescape.util.WhereClauseParamSetBuilder;

@Repository
public class JdbcReservationDao implements ReservationRepository {

    private static final RowMapper<Reservation> rowMapper = (rs, rowNum) -> {
        LocalDate date = rs.getDate("date").toLocalDate();
        Long memberId = rs.getLong("member_id");
        Long timeId = rs.getLong("reservation_time_id");
        LocalTime timeValue = rs.getTime("start_at").toLocalTime();
        ReservationName reservationName = new ReservationName(memberId, rs.getString("member_name"));
        ReservationTime reservationTime = new ReservationTime(timeId, timeValue);
        Theme theme = new Theme(
                rs.getLong("reservation_theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("thumbnail")
        );

        return new Reservation(
                rs.getLong("id"),
                reservationName,
                date,
                reservationTime,
                theme
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationDao(final JdbcTemplate jdbcTemplate, DataSource source) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(source)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public long save(final Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("member_id", reservation.getName().getId())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId());

        return jdbcInsert.executeAndReturnKey(params).longValue();
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                r.id,
                r.date,
                m.name as member_name,
                r.member_id,
                r.time_id as reservation_time_id,
                r.theme_id as reservation_theme_id,
                t.start_at,
                th.name as theme_name,
                th.description,
                th.thumbnail
                FROM reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join member as m on r.member_id = m.id
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        String sql = """
                SELECT
                r.id,
                r.date,
                m.name as member_name,
                r.member_id,
                r.time_id as reservation_time_id,
                r.theme_id as reservation_theme_id,
                t.start_at,
                th.name as theme_name,
                th.description,
                th.thumbnail
                FROM reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join member as m on r.member_id = m.id
                where r.id = ?
                """;
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findByDateTimeTheme(LocalDate date, LocalTime time, long themeId) {
        String sql = """
                SELECT
                r.id,
                r.date,
                m.name as member_name,
                r.member_id,
                r.time_id as reservation_time_id,
                r.theme_id as reservation_theme_id,
                t.start_at,
                th.name as theme_name,
                th.description,
                th.thumbnail
                FROM reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join member as m on r.member_id = m.id
                where r.date = ? and t.start_at = ? and r.theme_id = ?
                """;
        try {
            return jdbcTemplate.query(sql, rowMapper, date, time, themeId);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    @Override
    public List<Reservation> findByDateAndTheme(LocalDate date, long themeId) {
        String sql = """
                SELECT
                r.id,
                r.date,
                m.name as member_name,
                r.member_id,
                r.time_id as reservation_time_id,
                r.theme_id as reservation_theme_id,
                t.start_at,
                th.name as theme_name,
                th.description,
                th.thumbnail
                FROM reservation as r
                INNER JOIN reservation_time as t on r.time_id = t.id
                INNER JOIN theme as th on r.theme_id = th.id
                INNER JOIN member as m on r.member_id = m.id
                WHERE r.date = ? AND r.theme_id = ?
                """;
        try {
            return jdbcTemplate.query(sql, rowMapper, date, themeId);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    @Override
    public List<Reservation> findByThemeMemberDateRange(Long themeId, Long memberId, LocalDate from, LocalDate to) {
        Map<String, Object> params = new HashMap<>();
        params.put("r.theme_id = ?", themeId);
        params.put("r.member_id = ?", memberId);
        params.put("r.date >= ?", from);
        params.put("r.date <= ?", to);

        WhereClauseParamSet paramSet = WhereClauseParamSetBuilder.makeFrom(params);

        if (paramSet.isEmpty()) return findAll();

        String sql = """
                SELECT
                r.id,
                r.date,
                m.name as member_name,
                r.member_id,
                r.time_id as reservation_time_id,
                r.theme_id as reservation_theme_id,
                t.start_at,
                th.name as theme_name,
                th.description,
                th.thumbnail
                FROM reservation as r
                INNER JOIN reservation_time as t on r.time_id = t.id
                INNER JOIN theme as th on r.theme_id = th.id
                INNER JOIN member as m on r.member_id = m.id
                """ + "\n" + paramSet.getWhereClause();
        try {
            return jdbcTemplate.query(sql, rowMapper, paramSet.params().toArray());
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    @Override
    public int deleteById(final long id) {
        String sql = "delete from reservation where id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
