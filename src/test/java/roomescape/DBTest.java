package roomescape;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.auth.AuthService;
import roomescape.service.auth.JwtConfig;
import roomescape.service.auth.JwtTokenManager;
import roomescape.service.auth.TokenCookieManager;

@DataJpaTest
@Import(DBTest.DBTestConfig.class)
public class DBTest {

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected ReservationTimeRepository timeRepository;

    @Autowired
    protected ThemeRepository themeRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected AuthService authService;

    @Autowired
    protected ReservationService reservationService;

    @Autowired
    protected ReservationTimeService timeService;

    @Autowired
    protected ThemeService themeService;

    @Autowired
    protected MemberService memberService;

    @TestConfiguration
    @Import(JwtConfig.class)
    static class DBTestConfig {

        @Bean
        public AuthService authService(MemberRepository memberRepository, JwtTokenManager jwtTokenManager,
                                       TokenCookieManager tokenCookieManager) {
            return new AuthService(memberRepository, jwtTokenManager, tokenCookieManager);
        }

        @Bean
        public ReservationService reservationService(ReservationRepository reservationRepository,
                                                     ReservationTimeRepository timeRepository,
                                                     ThemeRepository themeRepository,
                                                     MemberRepository memberRepository) {
            return new ReservationService(reservationRepository, timeRepository, themeRepository, memberRepository);
        }

        @Bean
        public ReservationTimeService timeService(ReservationTimeRepository timeRepository,
                                                  ReservationRepository reservationRepository) {
            return new ReservationTimeService(timeRepository, reservationRepository);
        }

        @Bean
        public ThemeService themeService(ThemeRepository themeRepository) {
            return new ThemeService(themeRepository);
        }

        @Bean
        public MemberService memberService(MemberRepository memberRepository) {
            return new MemberService(memberRepository);
        }
    }
}
