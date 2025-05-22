package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static roomescape.reservation.fixture.MemberFixture.MATT;
import static roomescape.reservation.fixture.ReservationDateFixture.예약날짜_내일;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.role.Role;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.request.ReservationRequest;
import roomescape.reservation.controller.request.WaitingCreateRequest;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.controller.response.WaitingResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.service.exception.MemberAlreadyHasThisReservationException;
import roomescape.reservation.service.exception.WaitingDuplicateException;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private ReservationTimeService reservationTimeService;

    @Mock
    private ThemeService themeService;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    @Test
    void 예약을_생성한다() {
        Member savedMember = new Member(1L, new Name("매트"), new Email("matt.kakao"), new Password("1234"), Role.MEMBER);
        ReservationTime savedReservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme savedTheme = new Theme(1L, "test", "test", "test");
        Reservation savedReservation = new Reservation(1L, 예약날짜_내일.getDate(), savedReservationTime, savedTheme,
                savedMember);
        when(reservationRepository.existsByReservationDateAndReservationTimeIdAndThemeId(any(), any(),
                any())).thenReturn(false);
        when(memberService.findById(any(Long.class))).thenReturn(savedMember);
        when(reservationTimeService.findById(any(Long.class))).thenReturn(savedReservationTime);
        when(themeService.findById(any(Long.class))).thenReturn(savedTheme);
        when(reservationRepository.save(any())).thenReturn(savedReservation);

        ReservationRequest reservationRequest = new ReservationRequest(
                예약날짜_내일.getDate(),
                savedReservationTime.getId(),
                savedTheme.getId()
        );

        ReservationResponse response = reservationWaitingService.createReservation(1L, reservationRequest);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.member().getName()).isEqualTo(MATT.getName());
        assertThat(response.date()).isEqualTo(예약날짜_내일.getDate());
        assertThat(response.time()).isEqualTo(
                new ReservationTimeResponse(savedReservationTime.getId(),
                        savedReservationTime.getStartAt().toString()));
    }

    @Test
    void 예약이_존재하면_예약을_생성할_수_없다() {
        ReservationTime savedReservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme savedTheme = new Theme(1L, "test", "test", "test");
        when(reservationRepository.existsByReservationDateAndReservationTimeIdAndThemeId(any(), any(),
                any())).thenReturn(true);

        ReservationRequest request = new ReservationRequest(
                예약날짜_내일.getDate(),
                savedReservationTime.getId(),
                savedTheme.getId()
        );
        assertThatThrownBy(() -> reservationWaitingService.createReservation(MATT.getId(), request))
                .isInstanceOf(IllegalArgumentException.class);
    }

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
        when(reservationRepository.findByThemeIdAndReservationTimeIdAndReservationDate_reservationDate(
                eq(themeId), eq(timeId), eq(reservationDate)))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.existsBySameReservation(eq(waitedMemberId), eq(themeId), eq(timeId),
                eq(reservationDate)))
                .thenReturn(false);
        when(waitingRepository.save(any(Waiting.class))).thenReturn(expectedSavedWaiting);

        // then
        WaitingResponse response = reservationWaitingService.createWaiting(waitedMemberResponse, request);
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

        when(reservationRepository.findByThemeIdAndReservationTimeIdAndReservationDate_reservationDate(
                eq(themeId), eq(timeId), eq(reservationDate)))
                .thenReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.createWaiting(memberResponse, request))
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

        when(reservationRepository.findByThemeIdAndReservationTimeIdAndReservationDate_reservationDate(
                eq(themeId), eq(timeId), eq(reservationDate)))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.existsBySameReservation(eq(memberId), eq(themeId), eq(timeId), eq(reservationDate)))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.createWaiting(waitedMemberResponse, request))
                .isInstanceOf(WaitingDuplicateException.class)
                .hasMessageContaining("이미 대기열에 등록되어 있습니다.");
    }

    @Test
    void 존재하지_않는_예약을_삭제할_수_없다() {
        when(reservationRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.deleteReservationAndUpdateWaiting(3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 예약을 찾을 수 없습니다.");
    }

    @Test
    void 예약을_삭제하고_대기자가_없으면_예약만_삭제한다() {
        // given
        Long reservationId = 1L;
        Member member = new Member(1L, new Name("예약자"), new Email("test@test.com"), Role.MEMBER);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "theme", "desc", "img");
        Reservation reservation = new Reservation(reservationId, LocalDate.now(), reservationTime, theme, member);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(waitingRepository.findFirstOrderById(eq(theme.getId()), eq(reservationTime.getId()),
                eq(reservation.getDate())))
                .thenReturn(Optional.empty());

        // when
        reservationWaitingService.deleteReservationAndUpdateWaiting(reservationId);

        // then
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    void 예약을_삭제하고_대기자가_있으면_대기자를_예약자로_변경한다() {
        // given
        Long reservationId = 1L;
        Member reservedMember = new Member(1L, new Name("예약자"), new Email("reserved@test.com"), Role.MEMBER);
        Member waitingMember = new Member(2L, new Name("대기자"), new Email("waiting@test.com"), Role.MEMBER);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "theme", "desc", "img");
        Reservation reservation = new Reservation(reservationId, LocalDate.now(), reservationTime, theme,
                reservedMember);
        Waiting waiting = new Waiting(1L, reservation, waitingMember);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(waitingRepository.findFirstOrderById(eq(theme.getId()), eq(reservationTime.getId()),
                eq(reservation.getDate())))
                .thenReturn(Optional.of(waiting));

        // when
        reservationWaitingService.deleteReservationAndUpdateWaiting(reservationId);

        // then
        verify(waitingRepository).deleteById(waiting.getId());
        verify(reservationRepository).save(any(Reservation.class));
        assertThat(reservation.getMember()).isEqualTo(waitingMember);
    }
} 
