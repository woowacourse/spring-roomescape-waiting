package roomescape.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import roomescape.core.domain.Member;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Role;
import roomescape.core.domain.Theme;
import roomescape.core.repository.MemberRepository;
import roomescape.core.repository.ReservationTimeRepository;
import roomescape.core.repository.ThemeRepository;

@Component
@Profile("test")
public class TestFixture {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    public void initTestData() {
        persistMember();
        persistTheme();
        persistOneMinuteAfterReservationTime();
        persistOneMinuteBeforeReservationTime();
    }

    public void persistMember() {
        memberRepository.save(getMember());
    }

    public static Member getMember() {
        return new Member("리건", "test@email.com", "password", Role.ADMIN);
    }

    public void persistTheme() {
        themeRepository.save(getTheme());
    }

    public static Theme getTheme() {
        return new Theme("테마", "테마 설명", "테마 이미지");
    }

    public void persistOneMinuteAfterReservationTime() {
        reservationTimeRepository.save(getOneMinuteAfterReservationTime());
    }

    public static ReservationTime getOneMinuteAfterReservationTime() {
        return new ReservationTime(LocalTime.now().plusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    public void persistOneMinuteBeforeReservationTime() {
        reservationTimeRepository.save(getOneMinuteBeforeReservationTime());
    }

    public static ReservationTime getOneMinuteBeforeReservationTime() {
        return new ReservationTime(LocalTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    public static String getTomorrowDate() {
        return LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);
    }

    public static String getDayAfterTomorrowDate() {
        return LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_DATE);
    }

    public static String getTodayDate() {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    }

    public static String getEmail() {
        return "test@email.com";
    }

    public static String getPassword() {
        return "password";
    }
}
