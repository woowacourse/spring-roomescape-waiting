package roomescape.controller;

import static org.junit.jupiter.api.Assertions.assertAll;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.fixture.ReservationFixture;
import roomescape.service.ReservationService;

@SpringBootTest
class AdminControllerTest {
    @MockBean
    private ReservationService reservationService;
    @Autowired
    private AdminController adminController;

    @Test
    @DisplayName("관리자 예약 생성정상 동작시 API 명세대로 응답이 생성되는지 확인")
    void saveReservation() {
        //given
        ReservationRequest reservationRequest = ReservationFixture.DEFAULT_REQUEST;
        ReservationResponse expected = ReservationFixture.DEFAULT_RESPONSE;

        Mockito.when(reservationService.save(reservationRequest))
                .thenReturn(expected);
        //when
        ResponseEntity<ReservationResponse> response = adminController.saveReservation(reservationRequest);
        //then
        assertAll(
                () -> Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201)),
                () -> Assertions.assertThat(response.getBody()).isEqualTo(expected)
        );
    }
}
