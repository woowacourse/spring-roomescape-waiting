package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;

public class DynamicReservationSelectQuery {

    private static class Condition {
        private String conditionQuery;
        private Object value;

        public Condition(final String conditionQuery, final Object value) {
            this.conditionQuery = conditionQuery;
            this.value = value;
        }

        public String getConditionQuery() {
            return conditionQuery;
        }

        public Object getValue() {
            return value;
        }
    }

    private final String selectQuery = """
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
    private final List<Condition> conditions = new ArrayList<>();
    private final RowMapper mapper = (resultSet, rowNum) ->
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
                    ));
    private final JdbcTemplate jdbcTemplate;

    public DynamicReservationSelectQuery(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DynamicReservationSelectQuery addMemberCondition(final Long memberId) {
        if (memberId != null) {
            conditions.add(new Condition("mb.id = ?", memberId));
        }
        return this;
    }

    public DynamicReservationSelectQuery addThemeCondition(final Long themeId) {
        if (themeId != null) {
            conditions.add(new Condition("th.id = ?", themeId));
        }
        return this;
    }

    public DynamicReservationSelectQuery addFromDateCondition(final LocalDate startDate) {
        if (startDate != null) {
            conditions.add(new Condition(
                    "PARSEDATETIME(r.date, 'yyyy-MM-dd') >= PARSEDATETIME(?, 'yyyy-MM-dd')",
                    startDate
            ));
        }
        return this;
    }

    public DynamicReservationSelectQuery addToDateCondition(final LocalDate endDate) {
        if (endDate != null) {
            conditions.add(new Condition(
                    "PARSEDATETIME(r.date, 'yyyy-MM-dd') <= PARSEDATETIME(?, 'yyyy-MM-dd')",
                    endDate
            ));
        }
        return this;
    }

    public List<Reservation> findAllReservations() {
        if (conditions.isEmpty()) {
            return jdbcTemplate.query(selectQuery, mapper);
        }
        return jdbcTemplate.query(containsWhereConditionQuery(), mapper, conditionValues());
    }

    private String containsWhereConditionQuery() {
        return selectQuery + " where " + joinConditions() + ";";
    }

    private Object[] conditionValues() {
        return conditions.stream().map(Condition::getValue).toArray();
    }

    private String joinConditions() {
        return conditions.stream().map(Condition::getConditionQuery).collect(Collectors.joining(" and "));
    }
}
