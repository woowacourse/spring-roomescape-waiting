package roomescape.reservation.acceptance;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.facade.ReservationFacadeService;
import roomescape.reservation.repository.ReservationRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/data-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationAcceptanceTest {

    @Autowired
    private ReservationFacadeService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @TestFactory
    @DisplayName("예약이 취소될때 대기가 있는 경우 자동으로 첫번째의 대기자가 예약 상태가 된다.")
    Stream<DynamicTest> changeToReservationStatus_WhenReservationIsDeleted() {
        List<ReservationResponse> reservations = reservationService.findReservations();
        long reservationId = reservations.size();

        LocalDate date = LocalDate.now().plusDays(3);

        return Stream.of(
                dynamicTest("예약을 진행한다.", () -> {
                    ReservationRequest reservationRequest = new ReservationRequest(
                            date, 1, 5);

                    ReservationResponse reservationResponse = reservationService.addReservation(
                            reservationRequest, 3);

                    assertAll(
                            () -> assertEquals(reservationResponse.date(), date),
                            () -> assertEquals(reservationResponse.memberName(), "일반 멤버 아서"),
                            () -> assertEquals(reservationResponse.id(), reservationId + 1)
                    );
                }),
                dynamicTest("대기를 추가한다.", () -> {
                    ReservationRequest reservationRequest = new ReservationRequest(
                            date, 1, 5);

                    ReservationResponse reservationResponse = reservationService.addWaitingReservation(
                            reservationRequest, 7);

                    assertAll(
                            () -> assertEquals(reservationResponse.date(), date),
                            () -> assertEquals(reservationResponse.memberName(), "일반 멤버 리브"),
                            () -> assertEquals(reservationResponse.id(), reservationId + 2)
                    );
                }),
                dynamicTest("예약을 삭제한다.", () -> {
                    reservationService.removeReservation(reservationId + 1);

                    Optional<Reservation> reservation = reservationRepository.findById(reservationId + 1);
                    assertTrue(reservation.isEmpty());
                }),
                dynamicTest("첫번째 대기자가 자동으로 예약 상태가 된다.", () -> {
                    Reservation reservation = reservationRepository.findById(reservationId + 2).get();

                    assertAll(
                            () -> assertEquals(reservation.getId(), reservationId + 2),
                            () -> assertEquals(reservation.getReservationStatus(),
                                    ReservationStatus.RESERVED.getStatus())
                    );
                })
        );
    }
}
