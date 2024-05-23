package roomescape.utils;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import roomescape.core.domain.Member;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Role;
import roomescape.core.domain.Theme;
import roomescape.core.repository.MemberRepository;
import roomescape.core.repository.ReservationRepository;
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

    @Autowired
    private ReservationRepository reservationRepository;

    @Transactional
    public void initTestData() {
        persistMember();
        persistTheme("테마");
        persistReservationTimeAfterMinute(1);
        persistReservationTimeBeforeMinute(1);
        persistReservationWithDateAndTimeAndTheme(getTodayDate(), 1L, 1L);
    }

    @Transactional
    public void persistMember() {
        memberRepository.save(getMember());
    }

    public static Member getMember() {
        return new Member("리건", "test@email.com", "password", Role.ADMIN);
    }

    @Transactional
    public void persistTheme(final String name) {
        themeRepository.save(getTheme(name));
    }

    public static Theme getTheme(final String name) {
        return new Theme(name, "테마 설명", "테마 이미지");
    }

    @Transactional
    public ReservationTime persistReservationTimeAfterMinute(final long minute) {
        return reservationTimeRepository.save(getReservationTimeAfterMinute(minute));
    }

    public static ReservationTime getReservationTimeAfterMinute(final long minute) {
        return new ReservationTime(LocalTime.now().plusMinutes(minute)
                .format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @Transactional
    public ReservationTime persistReservationTimeBeforeMinute(final long minute) {
        return reservationTimeRepository.save(getReservationTimeBeforeMinute(minute));
    }

    public static ReservationTime getReservationTimeBeforeMinute(final long minute) {
        return new ReservationTime(LocalTime.now().minusMinutes(minute)
                .format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @Transactional
    public void persistReservationWithDateAndTimeAndTheme(final String date, final long timeId, final long themeId) {
        final Member member = memberRepository.findById(1L)
                .orElseThrow(IllegalArgumentException::new);
        final ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(IllegalArgumentException::new);
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(IllegalArgumentException::new);

        reservationRepository.save(new Reservation(member, date, time, theme));
    }

    public static String getTomorrowDate() {
        return LocalDate.now()
                .plusDays(1)
                .format(DateTimeFormatter.ISO_DATE);
    }

    public static String getDayAfterTomorrowDate() {
        return LocalDate.now()
                .plusDays(2)
                .format(DateTimeFormatter.ISO_DATE);
    }

    public static String getTodayDate() {
        return LocalDate.now()
                .format(DateTimeFormatter.ISO_DATE);
    }

    public static String getEmail() {
        return "test@email.com";
    }

    public static String getPassword() {
        return "password";
    }
}
