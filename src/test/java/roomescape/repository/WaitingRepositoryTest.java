package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.waiting.Waiting;

@Sql("/waiting-test-data.sql")
@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingRepositoryTest {

    @Autowired
    WaitingRepository waitingRepository;

    @Test
    void 예약_id로_대기_조회() {
        //given, when
        Waiting waiting = waitingRepository.findByReservationId(2L).orElseThrow();

        //then
        assertThat(waiting.getReservation().getId()).isEqualTo(2L);
    }
}
