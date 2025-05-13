package roomescape.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.dto.request.ReservationCondition;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("start_at", LocalTime.class)
        );
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        );
        Member member = new Member(
                resultSet.getLong("member_id"),
                resultSet.getString("member_name"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                Role.of(resultSet.getString("role"))
        );
        return Reservation.of(
                resultSet.getLong("id"),
                member,
                resultSet.getObject("date", LocalDate.class),
                reservationTime,
                theme
        );
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                    r.id,
                    r.date,
                    rt.id as time_id,
                    rt.start_at,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email,
                    m.password as password,
                    m.role
                FROM reservation AS r
                JOIN reservation_time AS rt
                ON r.time_id = rt.id
                JOIN theme AS t
                ON r.theme_id = t.id
                JOIN member AS m
                ON r.member_id = m.id
                """;

        return this.jdbcTemplate.query(sql,
                RESERVATION_ROW_MAPPER);
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> parameter = Map.of(
                "member_id", reservation.getMember().getId(),
                "date", reservation.getDate(),
                "time_id", reservation.getReservationTime().getId(),
                "theme_id", reservation.getTheme().getId()
        );
        Long newId = simpleJdbcInsert.executeAndReturnKey(parameter).longValue();
        return reservation.withId(newId);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "delete from reservation where id = :id";
        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("id", id);
        jdbcTemplate.update(sql, parameter);
    }

    @Override
    public List<Reservation> findByTimeId(Long timeId) {
        String sql = """
                SELECT
                    r.id,
                    r.date,
                    rt.id as time_id,
                    rt.start_at,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email,
                    m.password,
                    m.role
                FROM reservation AS r
                JOIN reservation_time AS rt
                ON r.time_id = rt.id
                JOIN theme AS t
                ON r.theme_id = t.id
                JOIN member AS m
                ON r.member_id = m.id
                WHERE r.time_id = :timeId
                """;
        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("timeId", timeId);
        return jdbcTemplate.query(sql, parameter, RESERVATION_ROW_MAPPER);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                    r.id,
                    r.date,
                    rt.id as time_id,
                    rt.start_at,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email,
                    m.password,
                    m.role
                FROM reservation AS r
                JOIN reservation_time AS rt
                ON r.time_id = rt.id
                JOIN theme AS t
                ON r.theme_id = t.id
                JOIN member AS m
                ON r.member_id = m.id
                WHERE r.id = :id
                """;

        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("id", id);
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, parameter, RESERVATION_ROW_MAPPER);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Reservation> findByDateTimeAndTheme(LocalDate date, ReservationTime time, Theme theme) {
        String sql = """
                SELECT
                    r.id,
                    r.date,
                    rt.id as time_id,
                    rt.start_at,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email,
                    m.password,
                    m.role
                FROM reservation AS r
                JOIN reservation_time AS rt
                ON r.time_id = rt.id
                JOIN theme AS t
                ON r.theme_id = t.id
                JOIN member AS m
                ON r.member_id = m.id
                WHERE r.date = :date AND 
                      rt.id = :timeId AND
                      t.id = :themeId
                """;
        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", time.getId())
                .addValue("themeId", theme.getId());
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, parameter, RESERVATION_ROW_MAPPER);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findByCondition(ReservationCondition cond) {
        String sql = """
                SELECT
                    r.id,
                    r.date,
                    rt.id as time_id,
                    rt.start_at,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email,
                    m.password,
                    m.role
                FROM reservation AS r
                JOIN reservation_time AS rt
                ON r.time_id = rt.id
                JOIN theme AS t
                ON r.theme_id = t.id
                JOIN member AS m
                ON r.member_id = m.id
                WHERE 1 = 1
                """;
        StringBuilder sqlBuilder = new StringBuilder(sql);
        Map<String, Object> parameter = new HashMap<>();
        if (cond.memberId().isPresent()) {
            sqlBuilder.append("AND member_id = :memberId\n");
            parameter.put("memberId", cond.memberId().get());
        }
        if (cond.themeId().isPresent()) {
            sqlBuilder.append("AND theme_id = :themeId\n");
            parameter.put("themeId", cond.themeId().get());
        }
        if (cond.dateFrom().isPresent() && cond.dateTo().isPresent()) {
            sqlBuilder.append("AND date BETWEEN :dateFrom AND :dateTo\n");
            parameter.put("dateFrom", cond.dateFrom().get());
            parameter.put("dateTo", cond.dateTo().get());
        }
        return jdbcTemplate.query(sqlBuilder.toString(), parameter, RESERVATION_ROW_MAPPER);
    }

    @Override
    public List<Reservation> findByThemeId(Long themeId) {
        String sql = """
                SELECT
                    r.id,
                    r.date,
                    rt.id as time_id,
                    rt.start_at,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email,
                    m.password,
                    m.role
                FROM reservation AS r
                JOIN reservation_time AS rt
                ON r.time_id = rt.id
                JOIN theme AS t
                ON r.theme_id = t.id
                JOIN member AS m
                ON r.member_id = m.id
                WHERE r.theme_id = :themeId
                """;

        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("themeId", themeId);
        return jdbcTemplate.query(sql, parameter, RESERVATION_ROW_MAPPER);
    }
}
