package roomescape.global.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeRepository;

import java.time.LocalTime;
import java.util.List;

@Profile({"default", "!test"})
@Component
@Transactional
public class DatabaseInitializer implements ApplicationRunner {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public DatabaseInitializer(ReservationTimeRepository reservationTimeRepository,
                               ThemeRepository themeRepository,
                               MemberRepository memberRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        reservationTimeRepository.saveAll(List.of(
                new ReservationTime(LocalTime.of(13, 0)),
                new ReservationTime(LocalTime.of(14, 0))
        ));
        themeRepository.saveAll(List.of(
                new Theme("호러", "매우 무섭습니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
                new Theme("추리", "매우 어렵습니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
        ));
        memberRepository.saveAll(List.of(
                new Member("어드민", "admin@admin.com", "1234", Role.ADMIN),
                new Member("미아", "mia@mia.com", "1234", Role.USER)
        ));
    }
}
