package roomescape.service.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationCreateServiceTest extends ReservationServiceTest {
    @Autowired
    private ReservationCreateService reservationCreateService;

    @DisplayName("어드민이 새로운 예약을 저장한다.")
    @Test
    void createAdminReservation() {
        //given
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(reservationDetail.getDate(), admin.getId(),
                reservationDetail.getReservationTime().getId(), theme.getId());

        //when
        ReservationResponse result = reservationCreateService.createAdminReservation(adminReservationRequest);

        //then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.time().id()).isEqualTo(reservationDetail.getReservationTime().getId()),
                () -> assertThat(result.theme().id()).isEqualTo(theme.getId()),
                () -> assertThat(result.status()).isEqualTo(ReservationStatus.RESERVED.getDescription())
        );
    }

    @DisplayName("사용자가 새로운 예약을 저장한다.")
    @Test
    void createMemberReservation() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());

        //when
        ReservationResponse result = reservationCreateService.createMemberReservation(reservationRequest, member.getId());

        //then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.time().id()).isEqualTo(reservationDetail.getReservationTime().getId()),
                () -> assertThat(result.theme().id()).isEqualTo(theme.getId()),
                () -> assertThat(result.status()).isEqualTo(ReservationStatus.RESERVED.getDescription())
        );
    }

    @DisplayName("사용자가 새로운 예약 대기를 저장한다.")
    @Test
    void createMemberWaiting() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());
        reservationCreateService.createMemberReservation(reservationRequest, member.getId());

        ReservationRequest anotherReservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());

        //when
        ReservationResponse result = reservationCreateService.createMemberReservation(anotherReservationRequest, anotherMember.getId());

        //then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.time().id()).isEqualTo(reservationDetail.getReservationTime().getId()),
                () -> assertThat(result.theme().id()).isEqualTo(theme.getId()),
                () -> assertThat(result.status()).isEqualTo(ReservationStatus.WAITING.getDescription())
        );
    }

    @DisplayName("사용자가 이미 예약인 상태에서 예약 요청을 한다면 예외가 발생한다.")
    @Test
    void cannotCreateByExistingMemberReservation() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());
        reservationCreateService.createMemberReservation(reservationRequest, member.getId());

        //when & then
        assertThatThrownBy(() -> reservationCreateService.createMemberReservation(reservationRequest, member.getId()))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이미 예약(대기) 상태입니다.");
    }

    @DisplayName("사용자가 이미 예약 대기인 상태에서 예약 요청을 한다면 예외가 발생한다.")
    @Test
    void cannotCreateByExistingMemberWaiting() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());
        reservationCreateService.createMemberReservation(reservationRequest, member.getId());
        ReservationRequest anotherReservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());
        reservationCreateService.createMemberReservation(anotherReservationRequest, anotherMember.getId());

        //when & then
        assertThatThrownBy(() -> reservationCreateService.createMemberReservation(reservationRequest, anotherMember.getId()))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이미 예약(대기) 상태입니다.");
    }

    @DisplayName("존재하지 않는 시간으로 예약을 추가하면 예외를 발생시킨다.")
    @Test
    void cannotCreateByUnknownTime() {
        //given
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(reservationDetail.getDate(), member.getId(), 0L,
                theme.getId());

        //when & then
        assertThatThrownBy(() -> reservationCreateService.createAdminReservation(adminReservationRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("더이상 존재하지 않는 시간입니다.");
    }

    @DisplayName("존재하지 않는 테마로 예약을 추가하면 예외를 발생시킨다.")
    @Test
    void cannotCreateByUnknownTheme() {
        //given
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(reservationDetail.getDate(), member.getId(),
                reservationDetail.getReservationTime().getId(), 0L);

        //when & then
        assertThatThrownBy(() -> reservationCreateService.createAdminReservation(adminReservationRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("더이상 존재하지 않는 테마입니다.");
    }

}
