package roomescape;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.RoomTheme;

public class TestFixture {
    public static String VALID_STRING_DATE = LocalDate.now().plusDays(1).toString();
    public static LocalDate DATE_AFTER_1DAY = LocalDate.parse(VALID_STRING_DATE);
    public static LocalDate DATE_AFTER_2DAY = LocalDate.now().plusDays(2);
    public static String VALID_STRING_TIME = "10:00";
    public static LocalTime TIME = LocalTime.parse(VALID_STRING_TIME);
    public static ReservationTime RESERVATION_TIME_10AM = new ReservationTime(TIME);
    public static ReservationTime RESERVATION_TIME_11AM = new ReservationTime(LocalTime.parse("11:00"));
    public static ReservationTime RESERVATION_TIME_12AM = new ReservationTime(LocalTime.parse("12:00"));
    public static Member MEMBER_BROWN = new Member("브라운", "brown@gmail.com", "brown", Role.MEMBER);
    public static Member ADMIN_ZEZE = new Member("제제", "zeze@gmail.com", "zeze", Role.ADMIN);
    public static RoomTheme ROOM_THEME1 = new RoomTheme("레벨 1 탈출",
            "우테코 레벨1를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static RoomTheme ROOM_THEME2 = new RoomTheme("레벨 2 탈출",
            "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
}
