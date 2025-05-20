package roomescape.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.auth.service.MyPasswordEncoder;
import roomescape.member.repository.JpaMemberRepository;
import roomescape.member.repository.JpaMemberRepositoryAdapter;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.repository.JpaReservationRepository;
import roomescape.reservation.repository.JpaReservationRepositoryAdapter;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.repository.JpaReservationTimeRepository;
import roomescape.reservationtime.repository.JpaReservationTimeRepositoryAdapter;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.repository.JpaThemeRepository;
import roomescape.theme.repository.JpaThemeRepositoryAdapter;
import roomescape.theme.repository.ThemeRepository;

@Configuration
public class TestConfig {

    @Bean
    public MyPasswordEncoder myPasswordEncoder() {
        return new MyPasswordEncoder();
    }

    @Bean
    public ThemeRepository themeRepository(final JpaThemeRepository jpaThemeRepository) {
        return new JpaThemeRepositoryAdapter(jpaThemeRepository);
    }

    @Bean
    public ReservationRepository reservationRepository(final JpaReservationRepository jpaReservationRepository) {
        return new JpaReservationRepositoryAdapter(jpaReservationRepository);
    }

    @Bean
    public ReservationTimeRepository reservationTimeRepository(
            final JpaReservationTimeRepository jpaReservationTimeRepository) {
        return new JpaReservationTimeRepositoryAdapter(jpaReservationTimeRepository);
    }

    @Bean
    public MemberRepository memberRepository(final JpaMemberRepository jpaMemberRepository) {
        return new JpaMemberRepositoryAdapter(jpaMemberRepository);
    }
}
