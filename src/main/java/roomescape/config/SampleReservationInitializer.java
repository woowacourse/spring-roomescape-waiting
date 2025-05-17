package roomescape.config;

import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;

@Component
@Profile("local")
public class SampleReservationInitializer implements CommandLineRunner {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public SampleReservationInitializer(
            ReservationRepository reservationRepository,
            MemberRepository memberRepository,
            ReservationTimeRepository timeRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    @Override
    public void run(String... args) {
        LocalDate baseDate = LocalDate.now().minusDays(3);

        save(3L, baseDate, 3L, 9L);
        save(7L, baseDate, 9L, 7L);
        save(2L, baseDate.minusDays(1), 2L, 16L);
        save(9L, baseDate.plusDays(3), 3L, 19L);
        save(5L, baseDate.plusDays(2), 10L, 5L);
        save(1L, baseDate, 9L, 16L);
        save(6L, baseDate.minusDays(1), 7L, 20L);
        save(4L, baseDate.plusDays(2), 7L, 11L);
        save(8L, baseDate.plusDays(3), 7L, 13L);
        save(10L, baseDate, 5L, 4L);
        save(2L, baseDate.plusDays(1), 4L, 2L);
        save(5L, baseDate.plusDays(4), 8L, 15L);
        save(9L, baseDate.plusDays(5), 1L, 7L);
        save(7L, baseDate.minusDays(1), 6L, 11L);
        save(1L, baseDate.plusDays(4), 2L, 5L);
        save(3L, baseDate.plusDays(3), 10L, 18L);
        save(6L, baseDate.plusDays(2), 5L, 20L);
        save(8L, baseDate.plusDays(1), 9L, 3L);
        save(4L, baseDate.plusDays(5), 7L, 7L);
        save(10L, baseDate.plusDays(4), 11L, 13L);

        saveWaiting(1L, baseDate, 1L, 1L);
        saveWaiting(2L, baseDate, 1L, 1L);
        saveWaiting(3L, baseDate, 1L, 1L);
        saveWaiting(4L, baseDate, 1L, 1L);
        saveWaiting(5L, baseDate, 1L, 1L);
    }

    private void save(Long memberId, LocalDate date, Long timeId, Long themeId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        ReservationTime time = timeRepository.findById(timeId).orElseThrow();
        Theme theme = themeRepository.findById(themeId).orElseThrow();

        Reservation reservation = Reservation.createNew(member, date, time, theme);
        reservationRepository.save(reservation);
    }

    private void saveWaiting(Long memberId, LocalDate date, Long timeId, Long themeId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        ReservationTime time = timeRepository.findById(timeId).orElseThrow();
        Theme theme = themeRepository.findById(themeId).orElseThrow();

        Reservation reservation = Reservation.createWaiting(member, date, time, theme);
        reservationRepository.save(reservation);
    }
}
