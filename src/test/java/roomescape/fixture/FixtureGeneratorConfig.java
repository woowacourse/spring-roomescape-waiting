package roomescape.fixture;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.SlotDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;

@TestConfiguration
public class FixtureGeneratorConfig {

    @Bean
    public ApiFixtureGenerator apiFixtureGenerator() {
        return new ApiFixtureGenerator();
    }

    @Bean
    public FixtureGenerator fixtureGenerator(
            ThemeDao themeDao,
            ReservationTimeDao reservationTimeDao,
            SlotDao slotDao,
            ReservationDao reservationDao,
            WaitingDao waitingDao
    ) {
        return new FixtureGenerator(
                themeDao,
                reservationTimeDao,
                slotDao,
                reservationDao,
                waitingDao
        );
    }
}
