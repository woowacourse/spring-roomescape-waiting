package roomescape.service.booking.waiting.module;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.waiting.WaitingResponse;

@Sql("/waiting-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingSearchServiceTest {

    @Autowired
    WaitingSearchService waitingSearchService;

    @Test
    void 모든_예약_대기_조회() {
        //when
        List<WaitingResponse> allWaitingReservations = waitingSearchService.findAllWaitingReservations();

        //then
        assertThat(allWaitingReservations).hasSize(2);
    }
}
