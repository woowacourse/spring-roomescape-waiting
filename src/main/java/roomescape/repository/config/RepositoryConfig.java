package roomescape.repository.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import roomescape.repository.member.JpaMemberRepositoryAdapter;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.reservation.JpaReservationRepositoryAdapter;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.JpaReservationTimeRepositoryAdapter;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.JpaThemeRepositoryAdapter;
import roomescape.repository.theme.ThemeRepository;

@Configuration
public class RepositoryConfig {

    @Bean
    public ReservationRepository reservationRepository(JpaReservationRepositoryAdapter reservationRepository) {
        return reservationRepository;
    }

    @Bean
    public ReservationTimeRepository reservationTimeRepository(
            JpaReservationTimeRepositoryAdapter reservationTimeRepository) {
        return reservationTimeRepository;
    }

    @Bean
    public ThemeRepository themeRepository(JpaThemeRepositoryAdapter themeRepository) {
        return themeRepository;
    }

    @Bean
    public MemberRepository memberRepository(JpaMemberRepositoryAdapter memberRepository) {
        return memberRepository;
    }
}
