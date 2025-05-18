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
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.ReservationTimeResponse;
import roomescape.reservation.domain.BookingStatus;
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
                LocalDate.now().plusDays(1), 1L, 1L, BookingStatus.RESERVED);

        assertThat(reservationCommandService.addMemberReservation(request, 1L)).isEqualTo(
                new ReservationResponse(
                        12L,
                        LocalDate.now().plusDays(1),
                        new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                        new ThemeResponse(1L, "인터스텔라", "시공간을 넘나들며 인류의 미래를 구해야 하는 극한의 두뇌 미션, 인터스텔라 방탈출!",
                                "https://upload.wikimedia.org/wikipedia/ko/b/b7/%EC%9D%B8%ED%84%B0%EC%8A%A4%ED%85%94%EB%9D%BC.jpg?20150905075839"),
                        new MemberResponse(1L, "엠제이")));
    }

    @Test
    void 예약을_삭제한다() {
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, BookingStatus.RESERVED);
        reservationCommandService.addMemberReservation(request, 1L);

        final Long id = 7L;
        assertThatCode(() -> reservationCommandService.deleteById(id)).doesNotThrowAnyException();
    }

    @Test
    void 존재하지_않는_예약은_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationCommandService.deleteById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    void 다른_사람의_예약은_삭제할_수_없다() {
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, BookingStatus.RESERVED);
        reservationCommandService.addMemberReservation(request, 1L);

        assertThatThrownBy(() -> reservationCommandService.deleteById(7L, 999L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("사용자 본인의 예약이 아닙니다.");
    }

    @Test
    void 대기_예약을_확정_예약으로_변경한다() {
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, BookingStatus.WAITING);
        reservationCommandService.addMemberReservation(request, 1L);

        assertThatCode(() -> reservationCommandService.updateStatus(12L))
                .doesNotThrowAnyException();
    }

    @Test
    void 대기_상태가_아닌_예약은_상태를_변경할_수_없다() {
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, BookingStatus.RESERVED);
        reservationCommandService.addMemberReservation(request, 1L);

        assertThatThrownBy(() -> reservationCommandService.updateStatus(12L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("대기 중인 예약이 아닙니다.");
    }

    @Test
    void 동일_시간에_확정된_예약이_있으면_상태를_변경할_수_없다() {
        final MemberReservationRequest request1 = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, BookingStatus.RESERVED);
        final MemberReservationRequest request2 = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, BookingStatus.WAITING);
        reservationCommandService.addMemberReservation(request1, 1L);
        reservationCommandService.addMemberReservation(request2, 2L);

        assertThatThrownBy(() -> reservationCommandService.updateStatus(13L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 동일한 시간에 예약된 건이 있습니다.");
    }

    @Test
    void 존재하지_않는_예약은_상태를_변경할_수_없다() {
        assertThatThrownBy(() -> reservationCommandService.updateStatus(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("선택한 예약이 존재하지 않습니다.");
    }
}
