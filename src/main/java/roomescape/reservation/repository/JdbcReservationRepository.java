package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> new Reservation(
            resultSet.getLong("id"),
            resultSet.getObject("date", LocalDate.class),
            new ReservationTime(
                    resultSet.getLong("time_id"),
                    resultSet.getObject("time_value", LocalTime.class)
            ),
            resultSet.getLong("theme_id"),
            resultSet.getLong("member_id")
    );

    @Override
    public Reservation save(Reservation reservation) {
        String sql = """
                INSERT INTO reservation (date, time_id, theme_id, member_id) 
                VALUES (:date, :timeId, :themeId, :memberId)
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getThemeId())
                .addValue("memberId", reservation.getMemberId());

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        return new Reservation(
                keyHolder.getKey().longValue(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getThemeId(),
                reservation.getMemberId()
        );
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT 
                    r.id,
                    r.date,
                    r.time_id,
                    rt.start_at as time_value,
                    r.theme_id, 
                    r.member_id 
                FROM reservation r 
                LEFT JOIN reservation_time rt ON r.time_id = rt.id 
                LEFT JOIN theme t ON r.theme_id = t.id 
                LEFT JOIN member m ON r.member_id = m.id
                """;

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Reservation> findAllByTimeId(Long id) {
        String sql = """
                SELECT 
                    r.id, 
                    r.date, 
                    r.time_id,
                    rt.start_at as time_value,
                    r.theme_id,
                    r.member_id
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id = rt.id
                WHERE rt.id = :id
                ORDER BY r.date
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<Reservation> findAllFiltered(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        String sql = """
                SELECT 
                    r.id,
                    r.date,
                    r.time_id,
                    rt.start_at as time_value,
                    r.theme_id, 
                    r.member_id 
                FROM reservation r 
                LEFT JOIN reservation_time rt ON r.time_id = rt.id 
                LEFT JOIN theme t ON r.theme_id = t.id 
                LEFT JOIN member m ON r.member_id = m.id
                WHERE r.theme_id = :themeId
                AND r.member_id = :memberId
                AND r.date >= :dateFrom
                AND r.date <= :dateTo
                ORDER BY r.date, rt.start_at
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("memberId", memberId)
                .addValue("dateFrom", dateFrom)
                .addValue("dateTo", dateTo);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        int rowCount = jdbcTemplate.update(sql, params);
        return rowCount > 0;
    }

    @Override
    public boolean existsByDateAndTimeId(LocalDate date, Long timeId) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE date = :date
                AND time_id = :timeId
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", timeId);

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
}
