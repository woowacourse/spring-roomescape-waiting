package roomescape.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (selectedReservation, rowNum) -> {
        ReservationTime time = mapReservationTime(selectedReservation);
        Theme theme = mapTheme(selectedReservation);
        Member member = mapMember(selectedReservation);

        return new Reservation(
                selectedReservation.getLong("id"),
                member,
                LocalDate.parse(selectedReservation.getString("date")),
                time,
                theme
        );
    };

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> mapTheme(rs);
    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (rs, rowNum) -> mapReservationTime(rs);

    private static ReservationTime mapReservationTime(ResultSet resultSet) throws SQLException {
        return new ReservationTime(
                resultSet.getLong("time_id"),
                LocalTime.parse(resultSet.getString("start_at"))
        );
    }

    private static Theme mapTheme(ResultSet resultSet) throws SQLException {
        return new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        );
    }

    private static Member mapMember(ResultSet resultSet) throws SQLException {
        return new Member(
                resultSet.getLong("member_id"),
                resultSet.getString("member_name"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                Role.valueOf(resultSet.getString("role"))
        );
    }

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert reservationInsert;

    public ReservationJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        BeanPropertySqlParameterSource reservationParameters = new BeanPropertySqlParameterSource(reservation);
        Long savedReservationId = reservationInsert.executeAndReturnKey(reservationParameters).longValue();
        return new Reservation(
                savedReservationId,
                reservation.getMember(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String selectQuery = """
            SELECT
                r.id as reservation_id,
                r.member_id,
                m.name as member_name,
                m.email,
                m.password,
                m.role,
                r.date,
                rt.id as time_id,
                rt.start_at,
                t.id as theme_id,
                t.name as theme_name,
                t.description,
                t.thumbnail
            FROM reservation as r
            LEFT JOIN reservation_time as rt
            ON r.time_id = rt.id
            LEFT JOIN theme as t
            ON r.theme_id = t.id
            LEFT JOIN member as m
            ON r.member_id = m.id
            WHERE r.id = ?
        """;

        try {
            Reservation reservation = jdbcTemplate.queryForObject(selectQuery, RESERVATION_ROW_MAPPER, id);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<ReservationTime> findTimeByDateAndThemeId(LocalDate date, Long themeId) {
        String selectQuery = """
            SELECT
                rt.id as time_id,
                rt.start_at
            FROM reservation_time as rt
            LEFT JOIN reservation as r
            ON r.time_id = rt.id
            LEFT JOIN theme as t
            ON r.theme_id = t.id
            WHERE r.date = ? AND r.theme_id = ?
        """;

        return jdbcTemplate.query(selectQuery, RESERVATION_TIME_ROW_MAPPER, date, themeId);
    }

    @Override
    public boolean existByTimeId(Long timeId) {
        String sql = """
                SELECT 
                CASE WHEN EXISTS (
                        SELECT 1
                        FROM reservation
                        WHERE time_id = ?
                    )
                    THEN TRUE
                    ELSE FALSE
                END""";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, timeId));
    }

    @Override
    public boolean existByThemeId(Long themeId) {
        String sql = """
                SELECT 
                CASE WHEN EXISTS (
                        SELECT 1
                        FROM reservation
                        WHERE theme_id = ?
                    )
                    THEN TRUE
                    ELSE FALSE
                END""";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeId));
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT 
                CASE WHEN EXISTS (
                        SELECT 1
                        FROM reservation
                        WHERE date = ? AND time_id = ? AND theme_id = ?
                    )
                    THEN TRUE
                    ELSE FALSE
                END""";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId));
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public List<Theme> findTopThemesDurationOrderByCount(LocalDate startDate, LocalDate endDate, Integer limit) {
        String sql = """
                SELECT 
                    t.id as theme_id, 
                    t.name as theme_name, 
                    t.description, 
                    t.thumbnail
                FROM theme AS t
                LEFT JOIN reservation AS r
                ON t.id = r.theme_id
                WHERE r.date BETWEEN ? AND ?
                GROUP BY t.id
                ORDER BY COUNT(r.id) DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER, startDate, endDate, limit);
    }

    @Override
    public List<Reservation> findByDurationAndThemeIdAndMemberId(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        String sql = """
            SELECT
                r.id as reservation_id,
                r.member_id,
                m.name as member_name,
                m.email,
                m.password,
                m.role,
                r.date,
                rt.id as time_id,
                rt.start_at,
                t.id as theme_id,
                t.name as theme_name,
                t.description,
                t.thumbnail
            FROM reservation as r
            LEFT JOIN reservation_time as rt
            ON r.time_id = rt.id
            LEFT JOIN theme as t
            ON r.theme_id = t.id
            LEFT JOIN member as m
            ON r.member_id = m.id
        """;

        QueryBuilder queryBuilder = QueryBuilder.build(sql)
                .addCondition(dateFrom, "r.date >= ?")
                .addCondition(dateTo, "r.date <= ?")
                .addCondition(themeId, "t.id = ?")
                .addCondition(memberId, "m.id = ?");

        return jdbcTemplate.query(queryBuilder.getQuery(), RESERVATION_ROW_MAPPER, queryBuilder.getParam());
    }
}
