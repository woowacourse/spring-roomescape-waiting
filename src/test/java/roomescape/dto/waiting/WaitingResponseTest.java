package roomescape.dto.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;
import roomescape.repository.WaitingRepository;

@Sql("/waiting-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingResponseTest {

    @Autowired
    WaitingRepository waitingRepository;

    @Test
    @Transactional
    void 예약_대기_응답_셍성() {
        //given
        Waiting waiting = waitingRepository.findById(1L).orElseThrow();
        Reservation reservation = waiting.getReservation();

        //when
        WaitingResponse waitingResponse = WaitingResponse.from(waiting);

        //then
        String orderWaiting = String.format("%d번째 예약 대기", waiting.getWaitingOrderValue());
        assertAll(
                () -> assertThat(waitingResponse.waitingId()).isEqualTo(waiting.getId()),
                () -> assertThat(waitingResponse.member()).isEqualTo(reservation.getMember().getName()),
                () -> assertThat(waitingResponse.theme()).isEqualTo(reservation.getTheme().getThemeName()),
                () -> assertThat(waitingResponse.date()).isEqualTo(reservation.getDate()),
                () -> assertThat(waitingResponse.startAt()).isEqualTo(reservation.getTime().getStartAt())
        );
    }
}
