package roomescape.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import roomescape.core.domain.Member;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Role;
import roomescape.core.domain.Theme;

public class TestFixture {
    public static String getTomorrowDate() {
        return LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);
    }

    public static String getDayAfterTomorrowDate() {
        return LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_DATE);

    }

    public static String getEmail() {
        return "test@email.com";
    }

    public static String getPassword() {
        return "password";
    }

    public static Member getMember() {
        return new Member("리건", "test@email.com", "password", Role.ADMIN);
    }

    public static Theme getTheme() {
        return new Theme("테마", "테마 설명", "테마 이미지");
    }

    public static ReservationTime getOneMinuteAfterReservationTime() {
        return new ReservationTime(LocalTime.now().plusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    public static ReservationTime getOneMinuteBeforeReservationTime() {
        return new ReservationTime(LocalTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm")));
    }
}
