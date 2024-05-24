package roomescape.fixture;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;

public class fixture {
    public static final Member ADMIN_MEMBER = new Member(
            1L, "어드민", "testDB@email.com", "1234", Role.ADMIN);
    public static final Member USER_MEMBER = new Member(
            2L, "사용자", "test2DB@email.com", "1234", Role.USER);

    public static final Theme THEME_ONE = new Theme(
            1L, "레벨1 탈출", "우테코 레벨2를 탈출하는 내용입니다",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_TWO = new Theme(
            2L, "레벨2 탈출", "우테코 레벨3를 탈출하는 내용입니다",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

    public static final LocalDate FROM_DATE = LocalDate.of(2024, 5, 18);
    public static final LocalDate TO_DATE = LocalDate.of(2024, 5, 20);

    public static final TimeSlot TIME_ONE = new TimeSlot(
            1L, LocalTime.of(10, 0));
    public static final TimeSlot TIME_TWO = new TimeSlot(
            2L, LocalTime.of(11, 0));
}
