//package roomescape.common;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import org.springframework.jdbc.core.RowMapper;
//import roomescape.reservation.domain.Reservation;
//import roomescape.reservationTime.domain.ReservationTime;
//import roomescape.theme.domain.Theme;
//import roomescape.user.domain.Role;
//import roomescape.user.domain.User;
//
//public class RowMapperManager {
//
//    public static final RowMapper<ReservationTime> reservationTimeRowMapper = (rs, rowNum) -> mapRowToReservationTime(
//            rs);
//    public static final RowMapper<Theme> themeRowMapper = (rs, rowNum) -> mapRowToTheme(rs);
//    public static final RowMapper<User> userRowMapper = (rs, rowNum) -> mapRowToUser(rs);
//    public static final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> mapRowToReservation(rs,
//            mapRowToReservationTime(rs),
//            mapRowToTheme(rs),
//            mapRowToUser(rs));
//
//    public static ReservationTime mapRowToReservationTime(ResultSet rs) throws SQLException {
//        return new ReservationTime(
//                rs.getLong("reservation_time_id"),
//                rs.getTime("reservation_time_start_at").toLocalTime()
//        );
//    }
//
//    public static Theme mapRowToTheme(ResultSet rs) throws SQLException {
//        return new Theme(
//                rs.getLong("theme_id"),
//                rs.getString("theme_name"),
//                rs.getString("theme_description"),
//                rs.getString("theme_thumbnail")
//        );
//    }
//
//    public static User mapRowToUser(ResultSet rs) throws SQLException {
//        return new User(
//                rs.getLong("user_id"),
//                Role.findByName(rs.getString("user_role")),
//                rs.getString("user_name"),
//                rs.getString("user_email"),
//                rs.getString("user_password")
//        );
//    }
//
//    public static Reservation mapRowToReservation(ResultSet rs,
//                                                  ReservationTime reservationTime,
//                                                  Theme theme,
//                                                  User user) throws SQLException {
//        return new Reservation(
//                rs.getLong("reservation_id"),
//                rs.getDate("reservation_date").toLocalDate(),
//                reservationTime,
//                theme,
//                user
//        );
//    }
//}
