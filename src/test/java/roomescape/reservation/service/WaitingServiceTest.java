package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.role.Role;
import roomescape.reservation.controller.request.WaitingCreateRequest;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.controller.response.WaitingResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.service.exception.MemberAlreadyHasThisReservationException;
import roomescape.reservation.service.exception.WaitingDuplicateException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    WaitingRepository waitingRepository;

    @Mock
    ReservationService reservationService;

    @InjectMocks
    WaitingService waitingService;

    @Test
    void 예약대기를_정상적으로_생성한다() {

        // given
        LocalDate reservationDate = LocalDate.of(2025, 12, 11);
        Long themeId = 1L;
        Long timeId = 1L;
        Long reservedMemberId = 1L;
        Long waitedMemberId = 2L;

        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.of(10, 10));
        Theme theme = new Theme(themeId, "test", "test", "test");
        Member reservedMember = new Member(reservedMemberId, new Name("예약자"), new Email("reserved@test.com"),
                Role.MEMBER);
        Member waitedMember = new Member(waitedMemberId, new Name("대기자"), new Email("waited@test.com"), Role.MEMBER);

        Reservation reservation = new Reservation(1L, reservationDate, reservationTime, theme, reservedMember);
        Waiting expectedSavedWaiting = new Waiting(1L, reservation, waitedMember);
        MemberResponse waitedMemberResponse = MemberResponse.from(waitedMember);
        WaitingCreateRequest request = new WaitingCreateRequest(reservationDate, themeId, timeId);

        // when
        when(reservationService.findByReservationInfo(eq(themeId), eq(timeId), eq(reservationDate)))
                .thenReturn(ReservationResponse.from(reservation));
        when(reservationService.findById(eq(reservation.getId()))).thenReturn(reservation);
        when(waitingRepository.existsBySameReservation(eq(waitedMemberId), eq(themeId), eq(timeId),
                eq(reservationDate)))
                .thenReturn(false);
        when(waitingRepository.save(any(Waiting.class))).thenReturn(expectedSavedWaiting);

        // then
        WaitingResponse response = waitingService.create(waitedMemberResponse, request);
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void 예약자는_자신의_예약에_대기를_등록할_수_없다() {

        // given
        Long memberId = 1L;
        LocalDate reservationDate = LocalDate.of(2025, 12, 11);
        Long themeId = 1L;
        Long timeId = 1L;

        Member member = new Member(memberId, new Name("예약자"), new Email("test@test.com"), Role.MEMBER);
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "theme", "desc", "img");
        Reservation reservation = new Reservation(1L, reservationDate, reservationTime, theme, member);

        MemberResponse memberResponse = MemberResponse.from(member);
        WaitingCreateRequest request = new WaitingCreateRequest(reservationDate, themeId, timeId);

        when(reservationService.findByReservationInfo(eq(themeId), eq(timeId), eq(reservationDate)))
                .thenReturn(ReservationResponse.from(reservation));
        when(reservationService.findById(eq(reservation.getId()))).thenReturn(reservation);

        // when & then
        assertThatThrownBy(() -> waitingService.create(memberResponse, request))
                .isInstanceOf(MemberAlreadyHasThisReservationException.class)
                .hasMessageContaining("이미 해당 예약이 등록되셨습니다.");
    }

    @Test
    void 이미_대기열에_등록된_경우_예외가_발생한다() {

        // given
        Long memberId = 2L;
        LocalDate reservationDate = LocalDate.of(2025, 12, 11);
        Long themeId = 1L;
        Long timeId = 1L;

        Member reservedMember = new Member(1L, new Name("예약자"), new Email("reserved@test.com"), Role.MEMBER);
        Member waitedMember = new Member(memberId, new Name("대기자"), new Email("waited@test.com"), Role.MEMBER);
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "theme", "desc", "img");
        Reservation reservation = new Reservation(1L, reservationDate, reservationTime, theme, reservedMember);

        MemberResponse waitedMemberResponse = MemberResponse.from(waitedMember);
        WaitingCreateRequest request = new WaitingCreateRequest(reservationDate, themeId, timeId);

        when(reservationService.findByReservationInfo(eq(themeId), eq(timeId), eq(reservationDate)))
                .thenReturn(ReservationResponse.from(reservation));
        when(reservationService.findById(eq(reservation.getId()))).thenReturn(reservation);
        when(waitingRepository.existsBySameReservation(eq(memberId), eq(themeId), eq(timeId), eq(reservationDate)))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> waitingService.create(waitedMemberResponse, request))
                .isInstanceOf(WaitingDuplicateException.class)
                .hasMessageContaining("이미 대기열에 등록되어 있습니다.");
    }
}
