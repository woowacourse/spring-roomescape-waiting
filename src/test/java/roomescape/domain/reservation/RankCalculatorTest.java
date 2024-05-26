package roomescape.domain.reservation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.fixture.ReservationFixture;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class RankCalculatorTest {

    @Autowired
    RankCalculator rankCalculator;

    @DisplayName("예약 대기 순번을 계산한다.")
    @Test
    void calculate() {
        // given
        Reservation reservation = ReservationFixture.createWaiting();
        // when
        int rank = rankCalculator.calculate(reservation);
        //then
        Assertions.assertThat(rank).isEqualTo(1);
    }
}
