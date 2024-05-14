package roomescape.reservation.dao;

import java.sql.ResultSet;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;

@Repository
public class ReservationDao implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId());
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(
                id,
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }


    @Override
    public void delete(long reservationId) {
        String sql = "DELETE FROM reservation WHERE id = ?;";
        jdbcTemplate.update(sql, reservationId);
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        String sql = """
                SELECT 1
                FROM reservation as r 
                INNER JOIN reservation_time as t ON r.time_id = t.id 
                INNER JOIN theme as th ON r.theme_id = th.id 
                WHERE t.id = ? 
                LIMIT 1;
                """;

        return jdbcTemplate.query(sql, ResultSet::next, timeId);
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        String sql = """
                SELECT 1
                FROM reservation as r 
                INNER JOIN reservation_time as t ON r.time_id = t.id 
                INNER JOIN theme as th ON r.theme_id = th.id 
                WHERE th.id = ? 
                LIMIT 1;
                """;

        return jdbcTemplate.query(sql, ResultSet::next, themeId);
    }

    @Override
    public boolean existsBy(LocalDate date, ReservationTime time, Theme theme) {
        String sql = """
                SELECT 1
                FROM reservation as r 
                INNER JOIN reservation_time as t ON r.time_id = t.id 
                INNER JOIN theme as th ON r.theme_id = th.id 
                WHERE date = ? AND time_id = ? AND theme_id = ?
                LIMIT 1;
                """;

        return jdbcTemplate.query(sql, ResultSet::next, date, time.getId(), theme.getId());
    }
}
