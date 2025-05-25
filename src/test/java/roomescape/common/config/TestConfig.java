package roomescape.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.bookingslot.domain.repository.BookingSlotRepository;
import roomescape.bookingslot.infrastructure.JpaBookingSlotRepository;
import roomescape.bookingslot.infrastructure.JpaBookingSlotRepositoryAdapter;
import roomescape.common.security.application.MyPasswordEncoder;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.member.infrastructure.JpaMemberRepositoryAdapter;
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
    public BookingSlotRepository bookingSlotRepository(final JpaBookingSlotRepository jpaBookingSlotRepository) {
        return new JpaBookingSlotRepositoryAdapter(jpaBookingSlotRepository);
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
