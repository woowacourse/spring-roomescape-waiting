package roomescape.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationDetailFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ThemeFixture;

@ActiveProfiles("test")
@DataJpaTest
@Import({MemberFixture.class, ReservationFixture.class, ReservationTimeFixture.class, ThemeFixture.class, ReservationDetailFixture.class})
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
abstract class ServiceTest {

    @Autowired
    protected MemberFixture memberFixture;
    @Autowired
    protected ReservationFixture reservationFixture;
    @Autowired
    protected ReservationTimeFixture reservationTimeFixture;
    @Autowired
    protected ThemeFixture themeFixture;
    @Autowired
    protected ReservationDetailFixture reservationDetailFixture;
}
