package roomescape.reservation.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSearch;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class ReservationDao {
    private final String FIND_SQL = """
            SELECT reservation.id, reservation.member_id, reservation.date, reservation.time_id, reservation.theme_id,
                    member.name, member.email, member.role,
                    reservation_time.start_at,
                    theme.name AS theme_name, theme.description, theme.thumbnail
            FROM reservation
            JOIN member ON reservation.member_id = member.id
            JOIN reservation_time ON reservation.time_id = reservation_time.id
            JOIN theme ON reservation.theme_id = theme.id 
            """;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Reservation> rowMapper;

    public ReservationDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.rowMapper = (resultSet, rowNum) -> new Reservation(
                resultSet.getLong("id"),
                new Member(
                        resultSet.getLong("member_id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("role")),
                resultSet.getObject("date", LocalDate.class),
                new ReservationTime(
                        resultSet.getLong("time_id"),
                        resultSet.getObject("start_at", LocalTime.class)),
                new Theme(
                        resultSet.getLong("theme_id"),
                        resultSet.getString("theme_name"),
                        resultSet.getString("description"),
                        resultSet.getString("thumbnail"))
        );
    }

    public List<Reservation> findReservations() {
        return jdbcTemplate.query(FIND_SQL, rowMapper);
    }

    public List<Reservation> findReservations(ReservationSearch condition) {
        String filterSql = makeFilterSql(condition);
        Map<String, Object> parameterMap = makeParameterMap(condition);
        return namedParameterJdbcTemplate.query(FIND_SQL + filterSql, parameterMap, rowMapper);
    }

    private String makeFilterSql(ReservationSearch searchCondition) {
        StringJoiner joiner = new StringJoiner(" AND ", " WHERE ", ";");
        if (searchCondition.getThemeId() != null) {
            joiner.add("reservation.theme_id = :theme_id");
        }
        if (searchCondition.getMemberId() != null) {
            joiner.add("reservation.member_id = :member_id");
        }
        if (searchCondition.getStartDate() != null) {
            joiner.add("reservation.date >= :start_date");
        }
        if (searchCondition.getEndDate() != null) {
            joiner.add("reservation.date <= :end_date");
        }
        return joiner.toString();
    }

    private Map<String, Object> makeParameterMap(ReservationSearch searchCondition) {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("theme_id", searchCondition.getThemeId());
        parameterMap.put("member_id", searchCondition.getMemberId());
        parameterMap.put("start_date", searchCondition.getStartDate());
        parameterMap.put("end_date", searchCondition.getEndDate());
        return parameterMap;
    }

    private Optional<Reservation> findReservationById(Long id) {
        String filterSql = " WHERE reservation.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_SQL + filterSql, rowMapper, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Reservation createReservation(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> createPreparedStatementForUpdate(connection, reservation), keyHolder);
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("해당 날짜, 시간, 테마에 이미 예약이 존재합니다.");
        }

        Long id = keyHolder.getKey().longValue();
        return findReservationById(id).orElseThrow();
    }

    private PreparedStatement createPreparedStatementForUpdate(Connection connection, Reservation reservation)
            throws SQLException {
        String sql = "INSERT INTO reservation (member_id, date, time_id, theme_id) values (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
        preparedStatement.setLong(1, reservation.getMemberId());
        preparedStatement.setObject(2, reservation.getDate());
        preparedStatement.setLong(3, reservation.getTimeId());
        preparedStatement.setLong(4, reservation.getThemeId());
        return preparedStatement;
    }

    public void deleteReservation(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
