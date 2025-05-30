package roomescape.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationScheduleDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationScheduleRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@TestConfiguration
public class RepositoryTestConfig {

    @Bean
    public ThemeDbFixture themeDbFixture(final ThemeRepository repository) {
        return new ThemeDbFixture(repository);
    }

    @Bean
    public ReservationDbFixture reservationDbFixture(final ReservationRepository repository) {
        return new ReservationDbFixture(repository);
    }

    @Bean
    public ReservationTimeDbFixture reservationTimeDbFixture(final ReservationTimeRepository repository) {
        return new ReservationTimeDbFixture(repository);
    }

    @Bean
    public MemberDbFixture memberDbFixture(final MemberRepository repository) {
        return new MemberDbFixture(repository);
    }

    @Bean
    public ReservationScheduleDbFixture reservationScheduleDbFixture(final ReservationScheduleRepository repository) {
        return new ReservationScheduleDbFixture(repository);
    }

    @Bean
    @Primary
    public PasswordEncoder passwordTestEncoder() {
        return new BCryptPasswordEncoder();
    }
}
