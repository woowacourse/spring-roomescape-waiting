package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.fixture.config.TestConfig;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.ui.dto.response.ReservationStatusResponse;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReservationStatusServiceTest {

    @Autowired
    private ReservationStatusService reservationStatusService;

    @Test
    void 예약_상태_목록을_조회한다() {
        // when
        final List<ReservationStatusResponse> responses = reservationStatusService.findAll();

        // then
        assertThat(responses).hasSize(ReservationStatus.values().length);
    }
}
