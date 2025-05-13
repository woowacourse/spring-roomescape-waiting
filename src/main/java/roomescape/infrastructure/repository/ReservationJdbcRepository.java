package roomescape.infrastructure.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationDateTime;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final static RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) ->
            Reservation.create(
                    resultSet.getLong("reservation_id"),
                    Member.create(
                            resultSet.getLong("member_id"),
                            resultSet.getString("member_name"),
                            Role.valueOf(resultSet.getString("member_role")),
                            resultSet.getString("member_email"),
                            resultSet.getString("member_password")
                    ),
                    LocalDate.parse(resultSet.getString("date")),
                    ReservationTime.create(
                            resultSet.getLong("time_id"),
                            LocalTime.parse(resultSet.getString("time_value"))
                    ),
                    Theme.create(
                            resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("theme_description"),
                            resultSet.getString("theme_thumbnail")
                    ));

    public ReservationJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public List<Reservation> findAll() {
        String sql = """
                select r.id as reservation_id, 
                       r.date, 
                       t.id as time_id, 
                       t.start_at as time_value, 
                       th.id as theme_id, 
                       th.name as theme_name, 
                       th.description as theme_description, 
                       th.thumbnail as theme_thumbnail,
                       m.id as member_id, 
                       m.name as member_name, 
                       m.role as member_role,
                       m.email as member_email, 
                       m.password as member_password
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join member as m on r.member_id = m.id
                """;

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public Reservation save(Member member, ReservationDateTime reservationDateTime, Theme theme) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date", reservationDateTime.reservationDate().getDate())
                .addValue("time_id", reservationDateTime.reservationTime().getId())
                .addValue("theme_id", theme.getId())
                .addValue("member_id", member.getId());
        Long id = jdbcInsert.executeAndReturnKey(parameters).longValue();

        return Reservation.create(id, member, reservationDateTime.reservationDate().getDate(),
                ReservationTime.create(reservationDateTime.reservationTime().getId(),
                        reservationDateTime.reservationTime().getStartAt()),
                theme);
    }

    public void deleteById(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Optional<Reservation> findById(Long id) {
        String sql = """
                select r.id as reservation_id, 
                       r.date, 
                       t.id as time_id, 
                       t.start_at as time_value, 
                       th.id as theme_id, 
                       th.name as theme_name, 
                       th.description as theme_description, 
                       th.thumbnail as theme_thumbnail,
                       m.id as member_id, 
                       m.name as member_name, 
                       m.role as member_role,
                       m.email as member_email, 
                       m.password as member_password
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join member as m on r.member_id = m.id
                where r.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByDateTimeAndTheme(ReservationDate reservationDate, Long timeId, Long themeId) {
        String sql = "SELECT 1 FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ? LIMIT 1";
        List<Integer> results = jdbcTemplate.queryForList(sql,
                Integer.class,
                reservationDate.getDate(), timeId, themeId);
        return !results.isEmpty();
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT 1 FROM reservation WHERE time_id = ? LIMIT 1";
        List<Integer> results = jdbcTemplate.queryForList(sql, Integer.class, timeId);
        return !results.isEmpty();
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT 1 FROM reservation WHERE theme_id = ? LIMIT 1";
        List<Integer> results = jdbcTemplate.queryForList(sql, Integer.class, themeId);
        return !results.isEmpty();
    }

    @Override
    public List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        String sql = """
                select r.id as reservation_id, 
                       r.date, 
                       t.id as time_id, 
                       t.start_at as time_value, 
                       th.id as theme_id, 
                       th.name as theme_name, 
                       th.description as theme_description, 
                       th.thumbnail as theme_thumbnail,
                       m.id as member_id, 
                       m.name as member_name, 
                       m.role as member_role,
                       m.email as member_email, 
                       m.password as member_password
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join member as m on r.member_id = m.id
                where r.theme_id = ? AND r.member_id = ? AND (r.date BETWEEN ? AND ?)
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, themeId, memberId, dateFrom, dateTo);
    }

}
