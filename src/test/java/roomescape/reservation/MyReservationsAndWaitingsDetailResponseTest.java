package roomescape.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.application.readmodel.ReservationReadModel;
import roomescape.reservation.dto.response.MyReservationsAndWaitingsDetailResponse;
import roomescape.reservationtime.dto.response.TimeInformation;
import roomescape.theme.dto.response.ThemeFindResponse;
import roomescape.waiting.application.readmodel.WaitingReadModel;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class MyReservationsAndWaitingsDetailResponseTest {

    @Test
    @DisplayName("status가 RESERVED이면 waitingOrder는 null이고 id는 reservation id를 사용한다.")
    void ReservationDetailFindResponse_contract_1() {
        ReservationReadModel readModel = new ReservationReadModel(
                101L,
                "member",
                LocalDate.of(2026, 5, 5),
                1L, "theme", "desc", "thumb",
                1L, LocalTime.of(10, 0)
        );

        MyReservationsAndWaitingsDetailResponse response = MyReservationsAndWaitingsDetailResponse.from(readModel);

        assertThat(response.status()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.waitingOrder()).isNull();
    }

    @Test
    @DisplayName("status가 WAITING이면 waitingOrder는 값이 있어야 하고 id는 waiting id를 사용한다.")
    void ReservationDetailFindResponse_contract_2() {
        WaitingReadModel readModel = new WaitingReadModel(
                202L,
                "member",
                LocalDate.of(2026, 5, 5),
                1L, "theme", "desc", "thumb",
                1L, LocalTime.of(10, 0),
                3L
        );

        MyReservationsAndWaitingsDetailResponse response = MyReservationsAndWaitingsDetailResponse.from(readModel);

        assertThat(response.status()).isEqualTo(ReservationStatus.WAITING);
        assertThat(response.id()).isEqualTo(202L);
        assertThat(response.waitingOrder()).isEqualTo(3L);
    }
}
