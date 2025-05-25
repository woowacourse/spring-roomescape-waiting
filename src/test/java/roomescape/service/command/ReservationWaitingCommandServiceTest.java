package roomescape.service.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWaitingTicket;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizationException;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaReservationWaitingTicketRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.ReservationCreateDto;

public class ReservationWaitingCommandServiceTest {

    @Mock
    private JpaReservationWaitingTicketRepository reservationWaitingTicketRepository;

    @Mock
    private JpaReservationRepository reservationRepository;

    @Mock
    private JpaReservationTimeRepository reservationTimeRepository;

    @Mock
    private JpaThemeRepository themeRepository;

    @Mock
    private JpaMemberRepository memberRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReservationWaitingCommandService reservationWaitingCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("예약 대기 생성 성공 테스트")
    @Test
    void createReservationWaiting() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;
        Long memberId = 1L;
        ReservationCreateDto requestDto = new ReservationCreateDto(date, timeId, themeId, memberId);
        
        LocalTime startAt = LocalTime.of(14, 0);
        ReservationTime reservationTime = new ReservationTime(timeId, startAt);
        Theme theme = new Theme(themeId, "공포 테마", "무서운 배경 설명", "image-url");
        Member member = new Member(memberId, "홍길동", "hong@example.com", Role.USER, "password");
        
        LocalDateTime fixedNow = LocalDateTime.of(date.minusDays(1), LocalTime.of(10, 0));
        Clock fixedClock = Clock.fixed(fixedNow.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        
        when(reservationTimeRepository.findById(timeId)).thenReturn(Optional.of(reservationTime));
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).thenReturn(true);
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        
        Reservation savedReservation = new Reservation(1L, member, date, reservationTime, theme, ReservationStatus.WAITING);
        
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        
        // when
        ReservationResponseDto response = reservationWaitingCommandService.createReservationWaiting(requestDto);
        
        // then
        assertEquals(savedReservation.getId(), response.id());
        assertEquals(savedReservation.getDate(), response.date());
        assertEquals(savedReservation.getTheme().getId(), response.theme().id());
        assertEquals(savedReservation.getMember().getName(), response.member().name());
        verify(reservationWaitingTicketRepository).save(any(ReservationWaitingTicket.class));
    }
    
    @DisplayName("존재하지 않는 예약 시간으로 예약 대기 생성 시 예외 발생")
    @Test
    void createReservationWaitingWithNonExistentTime() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 999L;
        Long themeId = 1L;
        Long memberId = 1L;
        ReservationCreateDto requestDto = new ReservationCreateDto(date, timeId, themeId, memberId);
        
        when(reservationTimeRepository.findById(timeId)).thenReturn(Optional.empty());
        
        // when & then
        assertThrows(NotFoundException.class, () -> reservationWaitingCommandService.createReservationWaiting(requestDto));
    }
    
    @DisplayName("존재하지 않는 테마로 예약 대기 생성 시 예외 발생")
    @Test
    void createReservationWaitingWithNonExistentTheme() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 999L;
        Long memberId = 1L;
        ReservationCreateDto requestDto = new ReservationCreateDto(date, timeId, themeId, memberId);
        
        LocalTime startAt = LocalTime.of(14, 0);
        ReservationTime reservationTime = new ReservationTime(timeId, startAt);
        
        LocalDateTime fixedNow = LocalDateTime.of(date.minusDays(1), LocalTime.of(10, 0));
        Clock fixedClock = Clock.fixed(fixedNow.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        
        when(reservationTimeRepository.findById(timeId)).thenReturn(Optional.of(reservationTime));
        when(themeRepository.findById(themeId)).thenReturn(Optional.empty());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        
        // when & then
        assertThrows(NotFoundException.class, () -> reservationWaitingCommandService.createReservationWaiting(requestDto));
    }
    
    @DisplayName("존재하지 않는 회원으로 예약 대기 생성 시 예외 발생")
    @Test
    void createReservationWaitingWithNonExistentMember() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;
        Long memberId = 999L;
        ReservationCreateDto requestDto = new ReservationCreateDto(date, timeId, themeId, memberId);
        
        LocalTime startAt = LocalTime.of(14, 0);
        ReservationTime reservationTime = new ReservationTime(timeId, startAt);
        Theme theme = new Theme(themeId, "공포 테마", "무서운 배경 설명", "image-url");
        
        LocalDateTime fixedNow = LocalDateTime.of(date.minusDays(1), LocalTime.of(10, 0));
        Clock fixedClock = Clock.fixed(fixedNow.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        
        when(reservationTimeRepository.findById(timeId)).thenReturn(Optional.of(reservationTime));
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        
        // when & then
        assertThrows(NotFoundException.class, () -> reservationWaitingCommandService.createReservationWaiting(requestDto));
    }
    
    @DisplayName("예약이 존재하지 않는 경우 예약 대기 생성 시 예외 발생")
    @Test
    void createReservationWaitingWithoutExistingReservation() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;
        Long memberId = 1L;
        ReservationCreateDto requestDto = new ReservationCreateDto(date, timeId, themeId, memberId);
        
        LocalTime startAt = LocalTime.of(14, 0);
        ReservationTime reservationTime = new ReservationTime(timeId, startAt);
        Theme theme = new Theme(themeId, "공포 테마", "무서운 배경 설명", "image-url");
        Member member = new Member(memberId, "홍길동", "hong@example.com", Role.USER, "password");
        
        LocalDateTime fixedNow = LocalDateTime.of(date.minusDays(1), LocalTime.of(10, 0));
        Clock fixedClock = Clock.fixed(fixedNow.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        
        when(reservationTimeRepository.findById(timeId)).thenReturn(Optional.of(reservationTime));
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).thenReturn(false);
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        
        // when & then
        assertThrows(IllegalArgumentException.class, () -> reservationWaitingCommandService.createReservationWaiting(requestDto));
    }
    
    @DisplayName("예약 대기 삭제 성공 테스트")
    @Test
    void deleteReservationWaiting() {
        // given
        Long reservationId = 1L;
        Long memberId = 1L;
        LoginInfo loginInfo = new LoginInfo(memberId, "홍길동","email",  Role.USER);
        
        Member member = new Member(memberId, "홍길동", "hong@example.com", Role.USER, "password");
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(14, 0));
        Theme theme = new Theme(1L, "공포 테마", "무서운 배경 설명", "image-url");
        
        Reservation reservation = new Reservation(reservationId, member, date, reservationTime, theme, ReservationStatus.WAITING);
        
        ReservationWaitingTicket waitingTicket = new ReservationWaitingTicket(reservation);
        
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationWaitingTicketRepository.findByReservationId(reservationId)).thenReturn(Optional.of(waitingTicket));
        
        // when
        reservationWaitingCommandService.deleteReservationWaiting(reservationId, loginInfo);
        
        // then
        verify(reservationWaitingTicketRepository).delete(waitingTicket);
        verify(reservationRepository).delete(reservation);
    }
    
    @DisplayName("존재하지 않는 예약 대기 삭제 시 예외 발생")
    @Test
    void deleteNonExistentReservationWaiting() {
        // given
        Long reservationId = 999L;
        Long memberId = 1L;
        LoginInfo loginInfo = new LoginInfo(memberId, "홍길동","email",  Role.USER);
        
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());
        
        // when & then
        assertThrows(NotFoundException.class, () -> reservationWaitingCommandService.deleteReservationWaiting(reservationId, loginInfo));
    }
    
    @DisplayName("권한 없는 사용자가 예약 대기 삭제 시 예외 발생")
    @Test
    void deleteReservationWaitingWithoutAuthorization() {
        // given
        Long reservationId = 1L;
        Long memberId = 1L;
        Long otherMemberId = 2L;
        LoginInfo loginInfo = new LoginInfo(otherMemberId, "김철수", "email", Role.USER);
        
        Member member = new Member(memberId, "홍길동", "hong@example.com", Role.USER, "password");
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(14, 0));
        Theme theme = new Theme(1L, "공포 테마", "무서운 배경 설명", "image-url");
        
        Reservation reservation = new Reservation(reservationId, member, date, reservationTime, theme, ReservationStatus.WAITING);
        
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        
        // when & then
        assertThrows(UnauthorizationException.class, () -> reservationWaitingCommandService.deleteReservationWaiting(reservationId, loginInfo));
    }
}