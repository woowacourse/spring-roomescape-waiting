package roomescape.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.ReservationFixture.DEFAULT_REQUEST;
import static roomescape.fixture.ReservationFixture.DEFAULT_RESERVATION;
import static roomescape.fixture.ReservationFixture.DEFAULT_RESPONSE;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import roomescape.dto.ReservationResponse;
import roomescape.service.ReservationService;

@SpringBootTest
class ReservationControllerTest {
    @MockBean
    private ReservationService reservationService;
    @Autowired
    private ReservationController reservationController;

    @Test
    @DisplayName("예약 생성 정상 동작 시 API 명세대로 응답이 생성되는지 확인")
    void saveReservation() {
        Mockito.when(reservationService.save(DEFAULT_REQUEST))
                .thenReturn(DEFAULT_RESPONSE);

        ResponseEntity<ReservationResponse> response = reservationController.saveReservation(
                DEFAULT_RESERVATION.getId(), DEFAULT_REQUEST);
        assertAll(
                () -> Assertions.assertThat(response.getStatusCode())
                        .isEqualTo(HttpStatusCode.valueOf(201)),
                () -> Assertions.assertThat(response.getBody())
                        .isEqualTo(DEFAULT_RESPONSE)
        );
    }

    @Test
    @DisplayName("예약 삭제 정상 동작 시 API 명세대로 응답이 생성되는지 확인")
    void delete() {
        ResponseEntity<Void> response = reservationController.delete(DEFAULT_RESERVATION.getId());
        Assertions.assertThat(response.getStatusCode())
                .isEqualTo(HttpStatusCode.valueOf(204));
    }
}
