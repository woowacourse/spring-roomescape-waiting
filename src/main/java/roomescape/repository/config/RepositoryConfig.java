package roomescape.repository.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.member.MemberRepositoryImpl;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservation.ReservationRepositoryImpl;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.reservationtime.ReservationTimeRepositoryImpl;
import roomescape.repository.theme.ThemeRepository;
import roomescape.repository.theme.ThemeRepositoryImpl;

@Configuration
public class RepositoryConfig {

    @Bean
    public ReservationRepository reservationRepository(ReservationRepositoryImpl reservationRepository) {
        return reservationRepository;
    }

    @Bean
    public ReservationTimeRepository reservationTimeRepository(
            ReservationTimeRepositoryImpl reservationTimeRepository) {
        return reservationTimeRepository;
    }

    @Bean
    public ThemeRepository themeRepository(ThemeRepositoryImpl themeRepository) {
        return themeRepository;
    }

    @Bean
    public MemberRepository memberRepository(MemberRepositoryImpl memberRepository) {
        return memberRepository;
    }
}
