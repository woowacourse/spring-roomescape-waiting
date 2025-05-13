package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationRepositoryImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Reservation> findAll() {
        String sql = """
                select r.id as reservation_id, 
                       r.date, 
                       mb.id as member_id,
                       mb.name as member_name,
                       mb.email as member_email,
                       mb.password as member_password,
                       mb.role as member_role,
                       t.id as time_id, 
                       t.start_at as time_value, 
                       th.id as theme_id, 
                       th.name as theme_name, 
                       th.description as theme_description, 
                       th.thumbnail as theme_thumbnail
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join member as mb on r.member_id = mb.id
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new Reservation(
                        resultSet.getLong("id"),
                        new Member(
                                resultSet.getLong("member_id"),
                                new MemberName(resultSet.getString("member_name")),
                                new MemberEmail(resultSet.getString("member_email")),
                                new MemberEncodedPassword(resultSet.getString("member_password")),
                                MemberRole.from(resultSet.getString("member_role"))
                        ),
                        new ReservationDate(resultSet.getString("date")),
                        new ReservationTime(
                                resultSet.getLong("time_id"),
                                LocalTime.parse(resultSet.getString("time_value"))
                        ),
                        new Theme(
                                resultSet.getLong("theme_id"),
                                new ThemeName(resultSet.getString("theme_name")),
                                new ThemeDescription(resultSet.getString("theme_description")),
                                new ThemeThumbnail(resultSet.getString("theme_thumbnail"))
                        )));
    }

    public Reservation save(
            final Member member,
            final ReservationDateTime reservationDateTime,
            final Theme theme
    ) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("member_id", member.getId())
                .addValue("date", reservationDateTime.getReservationDate().getDate())
                .addValue("time_id", reservationDateTime.getReservationTime().getId())
                .addValue("theme_id", theme.getId());
        Long id = jdbcInsert.executeAndReturnKey(parameters).longValue();

        return new Reservation(id, member, reservationDateTime.getReservationDate(),
                new ReservationTime(reservationDateTime.getReservationTime().getId(),
                        reservationDateTime.getReservationTime().getStartAt()),
                theme);
    }

    public Optional<Reservation> findById(final Long id) {
        String sql = """
                select r.id as reservation_id, 
                       r.date, 
                       mb.id as member_id,
                       mb.name as member_name,
                       mb.email as member_email,
                       mb.password as member_password,
                       mb.role as member_role,
                       t.id as time_id, 
                       t.start_at as time_value, 
                       th.id as theme_id, 
                       th.name as theme_name, 
                       th.description as theme_description, 
                       th.thumbnail as theme_thumbnail
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join member as mb on r.member_id = mb.id
                where r.id = ?
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                        new Reservation(
                                resultSet.getLong("id"),
                                new Member(
                                        resultSet.getLong("member_id"),
                                        new MemberName(resultSet.getString("member_name")),
                                        new MemberEmail(resultSet.getString("member_email")),
                                        new MemberEncodedPassword(resultSet.getString("member_password")),
                                        MemberRole.from(resultSet.getString("member_role"))
                                ),
                                new ReservationDate(resultSet.getString("date")),
                                new ReservationTime(
                                        resultSet.getLong("time_id"),
                                        LocalTime.parse(resultSet.getString("time_value"))
                                ),
                                new Theme(
                                        resultSet.getLong("theme_id"),
                                        new ThemeName(resultSet.getString("theme_name")),
                                        new ThemeDescription(resultSet.getString("theme_description")),
                                        new ThemeThumbnail(resultSet.getString("theme_thumbnail"))
                                )), id)
                .stream()
                .findFirst();
    }

    public void deleteById(final Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existSameDateTime(final ReservationDate reservationDate, final Long timeId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE date = ? AND time_id = ?)";
        return jdbcTemplate.queryForObject(sql, boolean.class, reservationDate.getDate(), timeId);
    }

    public boolean existReservationByTimeId(final Long timeId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE time_id = ?)";
        return jdbcTemplate.queryForObject(sql, boolean.class, timeId);
    }

    public boolean existReservationByThemeId(final Long themeId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, boolean.class, themeId);
    }

    public List<Reservation> findAllWithCondition(
            final Long memberId,
            final Long themeId,
            final LocalDate fromDate,
            final LocalDate toDate
    ) {
        return new DynamicReservationSelectQuery(jdbcTemplate)
                .addMemberCondition(memberId)
                .addThemeCondition(themeId)
                .addFromDateCondition(fromDate)
                .addToDateCondition(toDate)
                .findAllReservations();
    }
}
