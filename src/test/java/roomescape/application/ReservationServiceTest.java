package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import roomescape.BasicAcceptanceTest;
import roomescape.TestFixtures;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Status;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.LoginMember;
import roomescape.dto.MyReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;

@Sql("/setting-big-reservation.sql")
class ReservationServiceTest extends BasicAcceptanceTest {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("사용자 요청으로 들어온 에약이 예외 조건에 해당되지 않을 때 해당 예약을 저장한다.")
    @Test
    void saveByClient() {
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        LoginMember loginMember = new LoginMember(1L, "찰리");
        ReservationRequest reservationRequest = new ReservationRequest(tomorrow, 1L, 1L);
        reservationService.saveByClient(loginMember, reservationRequest);

        List<ReservationResponse> reservationResponses = reservationService.findAllByStatus(Status.RESERVATION);

        assertThat(reservationResponses).isEqualTo(TestFixtures.RESERVATION_RESPONSES_2);
    }

    @DisplayName("관리자 요청으로 들어온 에약이 예외 조건에 해당되지 않을 때 해당 예약을 저장한다.")
    @Test
    void saveByAdmin() {
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(1L, tomorrow, 1L, 1L);
        reservationService.saveByAdmin(adminReservationRequest);

        List<ReservationResponse> reservationResponses = reservationService.findAllByStatus(Status.RESERVATION);

        assertThat(reservationResponses).isEqualTo(TestFixtures.RESERVATION_RESPONSES_2);
    }

    @DisplayName("해당 id의 예약을 삭제한다.")
    @Test
    void deleteById() {
        reservationService.deleteById(1L);
        List<ReservationResponse> reservationResponses = reservationService.findAllByStatus(Status.RESERVATION);

        assertThat(reservationResponses).isEqualTo(TestFixtures.RESERVATION_RESPONSES_3);
    }

    @DisplayName("예약 삭제 요청시 예약이 존재하지 않으면 예외를 발생시킨다.")
    @Test
    void invalidNotExistReservation() {
        assertThatThrownBy(() -> reservationService.deleteById(99L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(String.format("존재하지 않는 예약입니다. 요청 예약 id:%d", 99));
    }

    @DisplayName("조건을 만족하는 예약을 반환한다.")
    @Test
    void findByCriteria() {
        List<ReservationResponse> reservationResponses = reservationService.findByCriteria(
                TestFixtures.RESERVATION_CRITERIA_REQUEST
        );

        assertThat(reservationResponses).isEqualTo(TestFixtures.RESERVATION_RESPONSES_1);
    }

    @DisplayName("해당 멤버의 예약 목록을 반환한다.")
    @Test
    void findMyReservations() {
        List<MyReservationResponse> myReservationResponses = reservationService.findMyReservations(1L);

        assertThat(myReservationResponses).isEqualTo(TestFixtures.MY_RESERVATION_RESPONSES);
    }

    @TestFactory
    @DisplayName("예약을 삭제할 시 1번째 예약 대기를 예약으로 변경한다.")
    Stream<DynamicTest> updateWaitingToReservation() {
        AtomicReference<ReservationResponse> firstReservationResponse = new AtomicReference<>();
        AtomicReference<ReservationResponse> secondReservationResponse = new AtomicReference<>();
        return Stream.of(
                dynamicTest("예약 대기를 조회한다. (총 0개)", () -> assertThat(reservationService.findAllByStatus(Status.WAITING)).isEmpty()),
                dynamicTest("예약을 추가한다.", () -> firstReservationResponse.set(reservationService.saveByAdmin(TestFixtures.ADMIN_RESERVATION_REQUEST_1))),
                dynamicTest("저장되어 있는 예약과 동일한 예약을 추가한다.", () -> secondReservationResponse.set(reservationService.saveByAdmin(TestFixtures.ADMIN_RESERVATION_REQUEST_2))),
                dynamicTest("예약의 상태를 확인한다 (RESERVATION)", () -> assertThat(reservationRepository.findById(secondReservationResponse.get().id()).orElseThrow().getStatus()).isEqualTo(Status.WAITING)),
                dynamicTest("예약을 삭제한다.", () -> reservationService.deleteById(firstReservationResponse.get().id())),
                dynamicTest("예약의 상태를 확인한다 (WAITING)", () -> assertThat(reservationRepository.findById(secondReservationResponse.get().id()).orElseThrow().getStatus()).isEqualTo(Status.RESERVATION))
        );
    }
}
