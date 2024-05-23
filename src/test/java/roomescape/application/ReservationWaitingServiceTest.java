package roomescape.application;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.dto.response.MyReservationResponse;
import roomescape.application.dto.response.ReservationStatus;

class ReservationWaitingServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Test
    @Sql("/waitings.sql")
    @DisplayName("회원 아이디로 예약과 예약 대기들을 예약 대기 순번을 포함해서 조회한다.")
    void getMyReservationWithRanks() {
        List<MyReservationResponse> responses = reservationWaitingService
                .getMyReservationAndWaitingWithRanks(1L);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(3);

            softly.assertThat(responses.get(0).id()).isEqualTo(1);
            softly.assertThat(responses.get(0).rank()).isEqualTo(0);
            softly.assertThat(responses.get(0).status()).isEqualTo(ReservationStatus.RESERVED);

            softly.assertThat(responses.get(1).id()).isEqualTo(5);
            softly.assertThat(responses.get(1).rank()).isEqualTo(1);
            softly.assertThat(responses.get(1).status()).isEqualTo(ReservationStatus.WAITING);

            softly.assertThat(responses.get(2).id()).isEqualTo(3);
            softly.assertThat(responses.get(2).rank()).isEqualTo(0);
            softly.assertThat(responses.get(2).status()).isEqualTo(ReservationStatus.RESERVED);
        });
    }
}
