package roomescape.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.common.security.application.MyPasswordEncoder;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.member.infrastructure.JpaMemberRepositoryAdapter;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepositoryAdapter;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.reservationtime.infrastructure.JpaReservationTimeRepository;
import roomescape.reservationtime.infrastructure.JpaReservationTimeRepositoryAdapter;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepositoryAdapter;
import roomescape.waiting.domain.repository.WaitingRepository;
import roomescape.waiting.infrastructure.JpaWaitingRepository;
import roomescape.waiting.infrastructure.JpaWaitingRepositoryAdapter;

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

    @Bean
    public WaitingRepository waitingRepository(final JpaWaitingRepository jpaWaitingRepository) {
        return new JpaWaitingRepositoryAdapter(jpaWaitingRepository);
    }
}
