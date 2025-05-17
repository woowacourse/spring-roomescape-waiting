package roomescape.reservation.infrastructure.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationId;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.user.domain.UserId;

import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationRowMapper implements RowMapper<Reservation> {

    public static final ReservationRowMapper INSTANCE = new ReservationRowMapper();

    @Override
    public Reservation mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Reservation.withId(
                ReservationId.from(resultSet.getLong("id")),
                UserId.from(resultSet.getLong("user_id")),
                ReservationDate.from(resultSet.getDate("date").toLocalDate()),
                ReservationTime.withId(
                        ReservationTimeId.from(resultSet.getLong("time_id")),
                        resultSet.getTime("start_at").toLocalTime()
                ),
                Theme.withId(
                        ThemeId.from(resultSet.getLong("theme_id")),
                        ThemeName.from(resultSet.getString("theme_name")),
                        ThemeDescription.from(resultSet.getString("description")),
                        ThemeThumbnail.from(resultSet.getString("thumbnail"))
                )
        );
    }
}
