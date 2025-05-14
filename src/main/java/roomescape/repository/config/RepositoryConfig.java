package roomescape.repository.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.member.MemberRepositoryImpl;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservation.ReservationRepositoryImpl;
import roomescape.repository.reservationmember.ReservationMemberRepository;
import roomescape.repository.reservationmember.legacy.JdbcReservationMemberRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.reservationtime.ReservationTimeRepositoryImpl;
import roomescape.repository.theme.ThemeRepository;
import roomescape.repository.theme.ThemeRepositoryImpl;

@Configuration
public class RepositoryConfig {

    @Bean
    public ReservationRepository reservationRepository(@Autowired ReservationRepositoryImpl reservationRepository) {
        return reservationRepository;
    }

    @Bean
    public ReservationTimeRepository reservationTimeRepository(
            @Autowired ReservationTimeRepositoryImpl reservationTimeRepository) {
        return reservationTimeRepository;
    }

    @Bean
    public ThemeRepository themeRepository(@Autowired ThemeRepositoryImpl themeRepository) {
        return themeRepository;
    }

    @Bean
    public MemberRepository memberRepository(@Autowired MemberRepositoryImpl memberRepository) {
        return memberRepository;
    }

    @Bean
    public ReservationMemberRepository reservationMemberRepository(
            @Autowired JdbcTemplate jdbcTemplate) {
        return new JdbcReservationMemberRepository(jdbcTemplate);
    }
}
