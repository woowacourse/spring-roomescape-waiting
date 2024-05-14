package roomescape.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_REQUEST;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_RESPONSE;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import roomescape.dto.ReservationTimeResponse;
import roomescape.service.AvailableTimeService;
import roomescape.service.ReservationTimeService;

@SpringBootTest
class ReservationTimeControllerTest {
    @MockBean
    private ReservationTimeService reservationTimeService;
    @MockBean
    private AvailableTimeService availableTimeService;
    @Autowired
    private ReservationTimeController reservationTimeController;

    @Test
    @DisplayName("예약 시간 정상 동작 시 API 명세대로 응답이 생성되는지 확인")
    void save() {
        Mockito.when(reservationTimeService.save(DEFAULT_REQUEST))
                .thenReturn(DEFAULT_RESPONSE);

        ResponseEntity<ReservationTimeResponse> response = reservationTimeController.save(DEFAULT_REQUEST);
        assertAll(
                () -> Assertions.assertThat(response.getStatusCode())
                        .isEqualTo(HttpStatusCode.valueOf(201)),
                () -> Assertions.assertThat(response.getBody())
                        .isEqualTo(DEFAULT_RESPONSE)
        );
    }

    @Test
    @DisplayName("예약 시간 삭제 정상 동작 시 API 명세대로 응답이 생성되는지 확인")
    void delete() {
        ResponseEntity<Void> response = reservationTimeController.delete(DEFAULT_RESPONSE.id());

        Assertions.assertThat(response.getStatusCode())
                .isEqualTo(HttpStatusCode.valueOf(204));
    }
}
