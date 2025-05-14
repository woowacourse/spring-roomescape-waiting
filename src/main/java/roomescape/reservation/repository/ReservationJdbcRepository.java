package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.service.out.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {

    private static final String FIND_ALL_RESERVATION_QUERY = """
            SELECT r.id as id, 
                   m.id as member_id,
                   m.name as member_name,
                   m.role as role,
                   m.email as email,
                   m.password as password,
                   r.date, 
                   t.id as time_id, 
                   t.start_at as start_at,
                   th.id as theme_id, 
                   th.name as theme_name, 
                   th.description as theme_description, 
                   th.thumbnail as theme_thumbnail
            FROM reservation as r
            INNER JOIN reservation_time as t ON r.time_id = t.id
            INNER JOIN theme as th ON r.theme_id = th.id
            INNER JOIN member as m ON r.member_id = m.id""";

    private final JdbcTemplate jdbcTemplate;

    public ReservationJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        String sql = FIND_ALL_RESERVATION_QUERY + """
                ;
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                Reservation.load(
                        resultSet.getLong("id"),
                        Member.load(
                                resultSet.getLong("member_id"),
                                resultSet.getString("member_name"),
                                Role.valueOf(resultSet.getString("role")),
                                resultSet.getString("email"),
                                resultSet.getString("password")
                        ),
                        ReservationDateTime.load(
                                new ReservationDate(
                                        LocalDate.parse(resultSet.getString("date")
                                        )),
                                ReservationTime.load(
                                        resultSet.getLong("time_id"),
                                        LocalTime.parse(resultSet.getString("start_at")
                                        )
                                )),
                        Theme.load(
                                resultSet.getLong("theme_id"),
                                resultSet.getString("theme_name"),
                                resultSet.getString("theme_description"),
                                resultSet.getString("theme_thumbnail")
                        )));
    }

    @Override
    public Reservation save(Reservation reservation) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("member_id", reservation.getReserver().getId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTimeId())
                .addValue("theme_id", reservation.getTheme().getId());
        Long id = jdbcInsert.executeAndReturnKey(parameters).longValue();

        return Reservation.load(
                id,
                reservation.getReserver(),
                reservation.getReservationDatetime(),
                reservation.getTheme()
        );
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = FIND_ALL_RESERVATION_QUERY + """
                
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                        Reservation.load(
                                resultSet.getLong("id"),
                                Member.load(
                                        resultSet.getLong("member_id"),
                                        resultSet.getString("member_name"),
                                        Role.valueOf(resultSet.getString("role")),
                                        resultSet.getString("email"),
                                        resultSet.getString("password")
                                ),
                                ReservationDateTime.load(
                                        new ReservationDate(
                                                LocalDate.parse(resultSet.getString("date")
                                                )),
                                        ReservationTime.load(
                                                resultSet.getLong("time_id"),
                                                LocalTime.parse(resultSet.getString("start_at"))
                                        )),
                                Theme.load(
                                        resultSet.getLong("theme_id"),
                                        resultSet.getString("theme_name"),
                                        resultSet.getString("theme_description"),
                                        resultSet.getString("theme_thumbnail")
                                )), id)
                .stream()
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsSameDateTime(LocalDate date, Long timeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE date = ? AND time_id = ?
                )
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId);
    }

    @Override
    public boolean existReservationByTimeId(Long timeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 
                    FROM reservation 
                    WHERE time_id = ?
                )
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
    }

    @Override
    public boolean existReservationByThemeId(Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 
                    FROM reservation 
                    WHERE theme_id = ?
                )
                """;
        int count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);
        return count > 0;
    }

    @Override
    public int countReservationByThemeIdAndDuration(LocalDate from, LocalDate to, Long themeId) {
        String sql = """
                SELECT count(*)
                FROM reservation
                WHERE theme_id = ?
                AND date >= ?
                ANd date < ?
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, themeId, from, to);
    }

    @Override
    public List<Long> findReservedTimeIdsByDateAndTheme(LocalDate date, Long themeId) {
        String sql = """
                SELECT time_id
                FROM reservation
                WHERE date = ? AND theme_id = ?
                """;
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("time_id"),
                date, themeId
        );
    }

    @Override
    public List<Reservation> findFilteredReservations(Long themeId, Long memberId, LocalDate from, LocalDate to) {
        String sql = FIND_ALL_RESERVATION_QUERY + """
                
                WHERE (r.theme_id = ? OR ? IS NULL)
                    AND (r.member_id = ? OR ? IS NULL)
                    AND (r.date >= ? OR ? IS NULL)
                    AND (r.date <= ? OR ? IS NULL)
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                        Reservation.load(
                                resultSet.getLong("id"),
                                Member.load(
                                        resultSet.getLong("member_id"),
                                        resultSet.getString("member_name"),
                                        Role.valueOf(resultSet.getString("role")),
                                        resultSet.getString("email"),
                                        resultSet.getString("password")
                                ),
                                ReservationDateTime.load(
                                        new ReservationDate(
                                                LocalDate.parse(resultSet.getString("date")
                                                )),
                                        ReservationTime.load(
                                                resultSet.getLong("time_id"),
                                                LocalTime.parse(resultSet.getString("start_at"))
                                        )),
                                Theme.load(
                                        resultSet.getLong("theme_id"),
                                        resultSet.getString("theme_name"),
                                        resultSet.getString("theme_description"),
                                        resultSet.getString("theme_thumbnail")
                                )),
                themeId, themeId,
                memberId, memberId,
                from, from,
                to, to);
    }
}
