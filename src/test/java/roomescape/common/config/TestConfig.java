package roomescape.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.reservationslot.domain.repository.ReservationSlotRepository;
import roomescape.reservationslot.infrastructure.JpaReservationSlotRepository;
import roomescape.reservationslot.infrastructure.JpaReservationSlotRepositoryAdapter;
import roomescape.common.security.application.MyPasswordEncoder;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.member.infrastructure.JpaMemberRepositoryAdapter;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.reservationtime.infrastructure.JpaReservationTimeRepository;
import roomescape.reservationtime.infrastructure.JpaReservationTimeRepositoryAdapter;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepositoryAdapter;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepositoryAdapter;

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
    public ReservationSlotRepository reservationSlotRepository(final JpaReservationSlotRepository jpaReservationSlotRepository) {
        return new JpaReservationSlotRepositoryAdapter(jpaReservationSlotRepository);
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
    public ReservationRepository reservationRepository(final JpaReservationRepository jpaReservationRepository) {
        return new JpaReservationRepositoryAdapter(jpaReservationRepository);
    }
}
