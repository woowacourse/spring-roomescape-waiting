package roomescape.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.global.auth.service.MyPasswordEncoder;
import roomescape.member.repository.JpaMemberRepository;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.repository.JpaReservationRepository;
import roomescape.reservation.repository.JpaWaitingReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingReservationRepository;
import roomescape.reservationtime.repository.JpaReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.repository.JpaThemeRepository;
import roomescape.theme.repository.ThemeRepository;

@Configuration
public class TestConfig {

    @Bean
    public MyPasswordEncoder myPasswordEncoder() {
        return new MyPasswordEncoder();
    }

}
