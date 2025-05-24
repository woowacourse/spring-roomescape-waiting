package roomescape.waiting.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.auth.dto.LoginMember;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ForbiddenException;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.entity.ReservationSlot;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.theme.entity.Theme;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.waiting.service.WaitingService;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @InjectMocks
    private WaitingService waitingService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReservationSlotRepository reservationSlotRepository;

    private LocalDate date;
    private Theme theme;
    private ReservationTime time;
    private ReservationSlot reservationSlot;
    private Waiting waiting;

    private Member member;
    private Member otherMember;
    private Member admin;
    private LoginMember loginMember;

    @BeforeEach
    void setUp() {
        date = LocalDate.now().plusDays(1);
        time = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "테마", "설명", "썸네일");
        reservationSlot = new ReservationSlot(1L, date, time, theme);

        member = new Member(1L, "훌라", "hula@email.com", "password", RoleType.USER);
        otherMember = new Member(2L, "미소", "miso@email.com", "password", RoleType.USER);
        admin = new Member(3L, "admin", "admin@email.com", "password", RoleType.ADMIN);
        loginMember = new LoginMember(member.getId(), member.getName(), member.getRole());

        waiting = new Waiting(1L, reservationSlot, member);
    }

    @Test
    @DisplayName("예약 대기를 생성할 수 있다.")
    void createWaiting() {
        //given
        when(reservationRepository.existsByReservationSlot(reservationSlot))
                .thenReturn(true);
        when(reservationRepository.existsByReservationSlotAndMemberId(any(), anyLong()))
                .thenReturn(false);
        when(waitingRepository.existsByReservationSlotAndMemberId(any(), anyLong()))
                .thenReturn(false);


        when(waitingRepository.save(any()))
                .thenReturn(waiting);
        when(waitingRepository.countByReservationSlotAndIdLessThan(any(), any()))
                .thenReturn(1L);

        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(member));
        when(reservationSlotRepository.findByDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(Optional.of(reservationSlot));

        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        //when
        var response = waitingService.createWaiting(loginMember, request);

        //then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("예약이 존재하지 않는다면 예외를 던진다.")
    void cantCreateWaitingWhenNotReserved() {
        //given
        when(reservationSlotRepository.findByDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(Optional.of(reservationSlot));
        when(reservationRepository.existsByReservationSlot(reservationSlot))
                .thenReturn(false);
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(member));

        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        //when & then
        assertThatThrownBy(() -> waitingService.createWaiting(loginMember, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("중복된 본인의 예약이 존재한다면 예외를 던진다.")
    void cantCreateWaitingWhenAlreadyReserved() {
        //given
        when(reservationSlotRepository.findByDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(Optional.of(reservationSlot));
        when(reservationRepository.existsByReservationSlot(any()))
                .thenReturn(true);
        when(reservationRepository.existsByReservationSlotAndMemberId(any(), anyLong()))
                .thenReturn(true);
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(member));

        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        //when & then
        assertThatThrownBy(() -> waitingService.createWaiting(loginMember, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("중복된 본인의 예약 대기가 존재한다면 예외를 던진다.")
    void cantCreateWaitingWhenAlreadyWaiting() {
        //given
        when(reservationSlotRepository.findByDateAndTimeIdAndThemeId(any(), any(), anyLong()))
                .thenReturn(Optional.of(reservationSlot));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(member));
        when(reservationRepository.existsByReservationSlot(any()))
                .thenReturn(true);
        when(reservationRepository.existsByReservationSlotAndMemberId(any(), anyLong()))
                .thenReturn(false);
        when(waitingRepository.existsByReservationSlotAndMemberId(any(), anyLong()))
                .thenReturn(true);

        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        //when & then
        assertThatThrownBy(() -> waitingService.createWaiting(loginMember, request))
                .isInstanceOf(BadRequestException.class);
    }

    @DisplayName("본인의 예약 대기가 아니라면, 삭제 시 예외를 발생한다.")
    @Test
    void cantDeleteWaitingWhenNotMine() {
        //given
        var otherMemberWaiting = new Waiting(1L, reservationSlot, otherMember);

        when(waitingRepository.findById(anyLong()))
                .thenReturn(Optional.of(otherMemberWaiting));

        //when & then
        assertThatThrownBy(() -> waitingService.deleteWaiting(otherMemberWaiting.getId(), loginMember))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("어드민이라면, 본인 외의 예약 대기를 삭제할 수 있다.")
    @Test
    void deleteWaitingWhenAdmin() {
        //given
        var otherMemberWaiting = new Waiting(1L, reservationSlot, otherMember);
        var adminLoginMember = new LoginMember(admin.getId(), admin.getPassword(), admin.getRole());

        when(waitingRepository.findById(anyLong()))
                .thenReturn(Optional.of(otherMemberWaiting));

        // when & then
        assertThatCode(() -> waitingService.deleteWaiting(otherMemberWaiting.getId(), adminLoginMember))
                .doesNotThrowAnyException();

        verify(waitingRepository).deleteById(anyLong());
    }

    @DisplayName("예약이 존재할 때, 예약 대기를 승인한다.")
    @Test
    void acceptWaitingWhenAlreadyReserved() {
        //given
        when(waitingRepository.findById(anyLong()))
                .thenReturn(Optional.of(waiting));

        //when & then
        assertThatCode(() -> waitingService.acceptWaiting(waiting.getId()))
                .doesNotThrowAnyException();
    }
}
