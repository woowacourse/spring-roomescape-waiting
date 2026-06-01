package roomescape.fixture;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.SlotDao;
import roomescape.dao.ThemeDao;

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
            ReservationDao reservationDao
    ) {
        return new FixtureGenerator(
                themeDao,
                reservationTimeDao,
                slotDao,
                reservationDao
        );
    }
}
