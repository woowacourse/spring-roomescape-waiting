package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialReservationFixture.NO_RESERVATION_DATE;
import static roomescape.InitialReservationFixture.RESERVATION_1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exceptions.NotFoundException;
import roomescape.member.dto.MemberRequest;
import roomescape.reservation.dto.ReservationRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Test
    @DisplayName("예약이 없는데 예약 대기를 신청하는 경우 예외가 발생한다.")
    void throwExceptionWhenAddWaitingWithoutReservation() {
        ReservationRequest reservationRequest = new ReservationRequest(
                NO_RESERVATION_DATE,
                RESERVATION_1.getReservationTime().getId(),
                RESERVATION_1.getTheme().getId()
        );
        MemberRequest memberRequest = new MemberRequest(MEMBER_1);
        assertThatThrownBy(() -> waitingService.addWaiting(reservationRequest, memberRequest))
                .isInstanceOf(NotFoundException.class);
    }
}
