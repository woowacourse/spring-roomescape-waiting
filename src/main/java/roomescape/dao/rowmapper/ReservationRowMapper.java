package roomescape.dao.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.Reservation;

@Component
public class ReservationRowMapper implements RowMapper<Reservation> {
    private final MemberRowMapper memberRowMapper;
    private final ReservationTimeRowMapper reservationTimeRowMapper;
    private final ThemeRowMapper themeRowMapper;

    public ReservationRowMapper(MemberRowMapper memberRowMapper,
                                ReservationTimeRowMapper reservationTimeRowMapper,
                                ThemeRowMapper themeRowMapper) {
        this.memberRowMapper = memberRowMapper;
        this.reservationTimeRowMapper = reservationTimeRowMapper;
        this.themeRowMapper = themeRowMapper;
    }

    @Override
    public Reservation mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Reservation(
                resultSet.getLong("id"),
                LocalDate.parse(resultSet.getString("date")),
                memberRowMapper.mapJoinedRow(resultSet),
                reservationTimeRowMapper.mapJoinedRow(resultSet),
                themeRowMapper.mapJoinedRow(resultSet)
        );
    }
}
