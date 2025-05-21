package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.exception.impl.BadRequestException;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.member.application.dto.MemberResponse;
import roomescape.reservation.application.dto.AdminReservationRequest;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.MemberWaitingRequest;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.ReservationTimeResponse;
import roomescape.theme.application.dto.ThemeResponse;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
class ReservationCommandServiceTest {

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Test
    void 예약을_추가한다() {
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L);

        assertThat(reservationCommandService.addMemberReservation(request, 1L)).isEqualTo(
                new ReservationResponse(
                        9L,
                        LocalDate.now().plusDays(1),
                        new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                        new ThemeResponse(1L, "인터스텔라", "시공간을 넘나들며 인류의 미래를 구해야 하는 극한의 두뇌 미션, 인터스텔라 방탈출!",
                                "https://upload.wikimedia.org/wikipedia/ko/b/b7/%EC%9D%B8%ED%84%B0%EC%8A%A4%ED%85%94%EB%9D%BC.jpg?20150905075839"),
                        new MemberResponse(1L, "엠제이")));
    }

    @Test
    void 예약을_삭제한다() {
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L);
        reservationCommandService.addMemberReservation(request, 1L);

        final Long id = 9L;
        assertThatCode(() -> reservationCommandService.deleteReservationById(id))
                .doesNotThrowAnyException();
    }

    @Test
    void 존재하지_않는_예약은_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationCommandService.deleteReservationById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    void 대기_예약을_확정_예약으로_변경한다() {
        reservationCommandService.deleteReservationById(8L);

        assertThatCode(() -> reservationCommandService.confirmReservation(2L))
                .doesNotThrowAnyException();
    }

    @Test
    void 존재하지_않는_예약은_상태를_변경할_수_없다() {
        assertThatThrownBy(() -> reservationCommandService.confirmReservation(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    void 과거_시간에는_예약할_수_없다() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        MemberReservationRequest request = new MemberReservationRequest(pastDate, 1L, 1L);

        assertThatThrownBy(() -> reservationCommandService.addMemberReservation(request, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("현재보다 과거의 날짜로 예약할 수 없습니다.");
    }

    @Test
    void 관리자가_예약을_추가한다() {
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now().plusDays(1), 1L, 1L, 1L);

        ReservationResponse response = reservationCommandService.addAdminReservation(request);
        assertThat(response.date()).isEqualTo(request.date());
        assertThat(response.member().id()).isEqualTo(1L);
    }

    @Test
    void 중복된_예약이나_대기는_불가하다() {
        MemberReservationRequest reservation = new MemberReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
        reservationCommandService.addMemberReservation(reservation, 1L);

        MemberWaitingRequest waiting = new MemberWaitingRequest(LocalDate.now().plusDays(1), 1L, 1L);
        assertThatThrownBy(() -> reservationCommandService.addMemberWaiting(waiting, 1L))
                .isInstanceOf(roomescape.common.exception.impl.ConflictException.class)
                .hasMessage("이미 예약 확정 및 대기 건수가 있습니다.");
    }

    @Test
    void 본인의_대기가_아니면_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationCommandService.deleteOwnWaitingById(2L, 999L))
                .isInstanceOf(roomescape.common.exception.impl.BadRequestException.class)
                .hasMessage("사용자 본인의 예약이 아닙니다.");
    }

    @Test
    void 이미_예약된_시간은_확정할_수_없다() {
        assertThatThrownBy(() -> reservationCommandService.confirmReservation(1L))
                .isInstanceOf(roomescape.common.exception.impl.ConflictException.class)
                .hasMessage("이미 예약 확정된 건이 있습니다.");
    }

}
