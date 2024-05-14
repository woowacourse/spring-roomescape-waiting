package roomescape.reservation.dao;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.MemberReservationRepository;

@Repository
public class MemberReservationDao implements MemberReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<MemberReservation> rowMapper = (ResultSet resultSet, int rowNum) -> {
        return new MemberReservation(
                resultSet.getLong("member_reservation_id"),
                new Member(
                        resultSet.getLong("member_id"),
                        resultSet.getString("member_name")
                ),
                new Reservation(
                        resultSet.getLong("reservation_id"),
                        resultSet.getDate("date").toLocalDate(),
                        new ReservationTime(resultSet.getLong("time_id"),
                                resultSet.getTime("time_value").toLocalTime()
                        ),
                        new Theme(resultSet.getLong("theme_id"),
                                resultSet.getString("theme_name"),
                                resultSet.getString("description"),
                                resultSet.getString("thumbnail"))
                )
        );
    };

    private final ResultSetExtractor<Optional<MemberReservation>> optionalResultSetExtractor = (ResultSet resultSet) -> {
        if (resultSet.next()) {
            MemberReservation memberReservation = new MemberReservation(
                    resultSet.getLong("member_reservation_id"),
                    new Member(
                            resultSet.getLong("member_id"),
                            resultSet.getString("member_name")
                    ),
                    new Reservation(
                            resultSet.getLong("reservation_id"),
                            resultSet.getDate("date").toLocalDate(),
                            new ReservationTime(resultSet.getLong("time_id"),
                                    resultSet.getTime("time_value").toLocalTime()
                            ),
                            new Theme(resultSet.getLong("theme_id"),
                                    resultSet.getString("theme_name"),
                                    resultSet.getString("description"),
                                    resultSet.getString("thumbnail"))
                    )
            );
            return Optional.of(memberReservation);
        } else {
            return Optional.empty();
        }
    };

    public MemberReservationDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("member_reservation")
                .usingGeneratedKeyColumns("id");
        ;
    }

    @Override
    public Optional<MemberReservation> findById(long id) {
        String sql = """
                SELECT mr.id AS member_reservation_id, r.id AS reservation_id, r.date, t.id AS time_id, t.start_at AS time_value, 
                th.id AS theme_id, th.name AS theme_name, th.description, th.thumbnail, m.id AS member_id, m.name AS member_name 
                FROM reservation AS r 
                INNER JOIN reservation_time AS t ON r.time_id = t.id 
                INNER JOIN theme AS th ON r.theme_id = th.id 
                INNER JOIN member_reservation AS mr ON mr.reservation_id = r.id 
                INNER JOIN member AS m ON m.id = mr.member_id
                WHERE mr.id = ?;
                """;

        return jdbcTemplate.query(sql, optionalResultSetExtractor, id);
    }

    @Override
    public List<MemberReservation> findBy(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT mr.id AS member_reservation_id, r.id AS reservation_id, r.date, t.id AS time_id, t.start_at AS time_value,
                th.id AS theme_id, th.name AS theme_name, th.description, th.thumbnail, m.id AS member_id, m.name AS member_name
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                INNER JOIN member_reservation AS mr ON mr.reservation_id = r.id
                INNER JOIN member AS m ON m.id = mr.member_id
                """;

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        addCondition(memberId, conditions, "m.id = ?", params);
        addCondition(themeId, conditions, "th.id = ?", params);
        addCondition(startDate, conditions, "r.date >= ?", params);
        addCondition(endDate, conditions, "r.date <= ?", params);

        if (!conditions.isEmpty()) {
            sql += " WHERE " + String.join(" AND ", conditions);
        }

        sql += " ORDER BY r.date, t.start_at;";
        return jdbcTemplate.query(sql, rowMapper, params.toArray());
    }

    private void addCondition(Object param, List<String> conditions, String sql, List<Object> params) {
        if (param == null) {
            return;
        }

        conditions.add(sql);
        params.add(param);
    }

    @Override
    public void deleteById(long memberReservationId) {
        String sql = "DELETE FROM member_reservation WHERE id = ?;";
        jdbcTemplate.update(sql, memberReservationId);
    }

    @Override
    public void deleteByReservationId(long reservationId) {
        String sql = "DELETE FROM member_reservation WHERE reservation_id = ?";
        jdbcTemplate.update(sql, reservationId);
    }
    
    @Override
    public boolean existBy(LocalDate date, ReservationTime time, Theme theme) {
        String sql = """
                SELECT 1
                FROM member_reservation as mr
                WHERE mr.reservation_id = (
                    SELECT r.id
                    FROM reservation AS r 
                    INNER JOIN reservation_time AS t ON r.time_id = t.id 
                    INNER JOIN theme AS th ON r.theme_id = th.id 
                    INNER JOIN member_reservation AS mr ON mr.reservation_id = r.id 
                    INNER JOIN member AS m ON m.id = mr.member_id 
                    WHERE date = ? AND time_id = ? AND theme_id = ?
                    LIMIT 1
                );
                """;

        return jdbcTemplate.query(sql, ResultSet::next, date, time.getId(), theme.getId());
    }

    @Override
    public long save(Member member, Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource().addValue("member_id", member.getId())
                .addValue("reservation_id", reservation.getId());
        return simpleJdbcInsert.executeAndReturnKey(params).longValue();
    }
}
