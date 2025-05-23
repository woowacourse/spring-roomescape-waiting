package roomescape.reservation.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.auth.dto.LoginMember;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.ReservationAdminCreateRequest;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationReadFilteredRequest;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationSlot;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private ReservationSlotRepository reservationSlotRepository;

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservation() {
        // given
        var tomorrow = LocalDate.now().plusDays(1);
        var time = new ReservationTime(1L, LocalTime.of(10, 0));
        var theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        var member = new Member(1L, "테스트", "test@test.com", "password", RoleType.USER);
        var reservationSlot = new ReservationSlot(tomorrow, time, theme);

        when(reservationTimeRepository.findById(anyLong()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong()))
                .thenReturn(Optional.of(theme));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(member));
        when(reservationSlotRepository.findByDateAndTimeIdAndThemeId(any(), any(), anyLong()))
                .thenReturn(Optional.of(reservationSlot));
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(new Reservation(anyLong(), reservationSlot, member));

        var request = new ReservationCreateRequest(
                tomorrow,
                time.getId(),
                theme.getId()
        );

        // when
        var response = reservationService.createReservation(member.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.date()).isEqualTo(request.date()),
                () -> assertThat(response.startAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(response.themeName()).isEqualTo(theme.getName())
        );

        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("관리자가 예약을 생성한다.")
    void createReservationByAdmin() {
        // given
        var tomorrow = LocalDate.now().plusDays(1);
        var time = new ReservationTime(1L, LocalTime.of(10, 0));
        var theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        var member = new Member(1L, "테스트", "test@test.com", "password", RoleType.USER);
        var reservationSlot = new ReservationSlot(tomorrow, time, theme);

        when(reservationTimeRepository.findById(anyLong()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong()))
                .thenReturn(Optional.of(theme));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(member));
        when(reservationSlotRepository.findByDateAndTimeIdAndThemeId(any(), any(), anyLong()))
                .thenReturn(Optional.of(reservationSlot));
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(new Reservation(anyLong(), reservationSlot, member));

        var request = new ReservationAdminCreateRequest(
                LocalDate.now().plusDays(1),
                theme.getId(),
                time.getId(),
                member.getId()
        );

        // when
        var response = reservationService.createReservationByAdmin(request);

        // then
        assertAll(
                () -> assertThat(response.date()).isEqualTo(request.date()),
                () -> assertThat(response.startAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(response.themeName()).isEqualTo(theme.getName())
        );

        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("과거 날짜로 예약을 생성하면 예외가 발생한다.")
    void createReservationWithPastDate() {
        // given
        var time = new ReservationTime(1L, LocalTime.of(10, 0));
        var theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        var member = new Member(1L, "테스트", "test@test.com", "password", RoleType.USER);
        var reservationSlot = new ReservationSlot(LocalDate.now(), time, theme);

        when(reservationTimeRepository.findById(anyLong()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong()))
                .thenReturn(Optional.of(theme));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(member));
        when(reservationSlotRepository.findByDateAndTimeIdAndThemeId(any(), any(), anyLong()))
                .thenReturn(Optional.of(reservationSlot));

        var request = new ReservationCreateRequest(
                LocalDate.now().minusDays(1),
                time.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("과거 날짜는 예약할 수 없습니다.");

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("이미 예약된 시간에 예약을 생성하면 예외가 발생한다.")
    void createReservationWithDuplicateTime() {
        // given
        var time = new ReservationTime(1L, LocalTime.of(10, 0));
        var theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        var member = new Member(1L, "테스트", "test@test.com", "password", RoleType.USER);
        var date = LocalDate.now().plusDays(1);
        var reservationSlot = new ReservationSlot(date, time, theme);

        when(reservationTimeRepository.findById(anyLong()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong()))
                .thenReturn(Optional.of(theme));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(member));
        when(reservationSlotRepository.findByDateAndTimeIdAndThemeId(any(), any(), anyLong()))
                .thenReturn(Optional.of(reservationSlot));
        when(reservationRepository.existsByReservationSlot(any(ReservationSlot.class)))
                .thenReturn(true);

        var request = new ReservationCreateRequest(date, time.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(member.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 예약이 존재합니다.");

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void getAllReservations() {
        // given
        var time = new ReservationTime(1L, LocalTime.of(10, 0));
        var theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        var member = new Member(1L, "테스트", "test@test.com", "password", RoleType.USER);
        var reservationSlot = new ReservationSlot(LocalDate.now(), time, theme);
        var inDbReservations = List.of(
                new Reservation(1L, reservationSlot, member)
        );

        when(reservationRepository.findAll())
                .thenReturn(inDbReservations);

        // when
        var responses = reservationService.getAllReservations();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses.getFirst().date()).isEqualTo(LocalDate.now()),
                () -> assertThat(responses.getFirst().startAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(responses.getFirst().themeName()).isEqualTo(theme.getName()),
                () -> assertThat(responses.getFirst().memberName()).isEqualTo(member.getName())
        );
    }

    @Test
    @DisplayName("필터링된 예약을 조회한다.")
    void getFilteredReservations() {
        // given
        var date = LocalDate.now().plusDays(1);
        var time = new ReservationTime(1L, LocalTime.of(10, 0));
        var theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        var member = new Member(1L, "테스트", "test@test.com", "password", RoleType.USER);
        var reservationSlot = new ReservationSlot(LocalDate.now().plusDays(1), time, theme);
        var inDbReservations = List.of(
                new Reservation(1L, reservationSlot, member)
        );
        when(reservationRepository.findAllByReservationSlot_ThemeIdAndMemberIdAndReservationSlot_DateBetween(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(inDbReservations);

        var filterRequest = new ReservationReadFilteredRequest(
                theme.getId(),
                member.getId(),
                date,
                date
        );

        // when
        var responses = reservationService.getFilteredReservations(filterRequest);

        // then
        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses.getFirst().date()).isEqualTo(date),
                () -> assertThat(responses.getFirst().startAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(responses.getFirst().themeName()).isEqualTo(theme.getName()),
                () -> assertThat(responses.getFirst().memberName()).isEqualTo(member.getName())
        );
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void deleteReservation() {
        // when
        reservationService.deleteReservation(1L);

        // then
        verify(reservationRepository).deleteById(anyLong());
    }

    @DisplayName("예약을 삭제하고, 예약 대기를 예약으로 변환한다.")
    @Test
    void deleteAndChangeWaitingToReservation() {
        //given
        var otherMember = new Member("미소", "miso@email.com", "password", RoleType.USER);
        var theme = new Theme("테마", "설명", "썸네일");
        var time = new ReservationTime(LocalTime.of(10, 0));
        var date = LocalDate.now().plusDays(1);
        var reservationSlot = new ReservationSlot(date, time, theme);
        var reservation = new Reservation(1L, reservationSlot, otherMember);

        var member = new Member("훌라", "hula@email.com", "password", RoleType.USER);
        var waiting = new Waiting(reservationSlot, member);

        when(reservationRepository.existsById(anyLong()))
                .thenReturn(true);
        when(reservationRepository.findById(anyLong()))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.findFirstByReservationSlot(any(ReservationSlot.class)))
                .thenReturn(Optional.of(waiting));

        //when
        reservationService.deleteReservation(reservation.getId());

        //then
        verify(reservationRepository).save(any(Reservation.class));
        verify(waitingRepository).delete(any(Waiting.class));
        verify(reservationRepository).deleteById(anyLong());
    }

    @Test
    @DisplayName("유저 예약 기록을 확인한다.")
    void getReservationsByMember() {
        // given
        var date = LocalDate.now().plusDays(1);
        var time = new ReservationTime(1L, LocalTime.of(10, 0));
        var theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        var member = new Member(1L, "테스트", "test@test.com", "password", RoleType.USER);
        var reservationSlot = new ReservationSlot(date, time, theme);
        var inDbReservations = List.of(
                new Reservation(1L, reservationSlot, member)
        );
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(member));
        when(reservationRepository.findAllByMember(member))
                .thenReturn(inDbReservations);
        when(waitingRepository.findWaitingsWithRankByMemberId(anyLong()))
                .thenReturn(List.of());

        var loginMember = new LoginMember(member.getId(), member.getName(), member.getRole());

        // when
        var response = reservationService.getReservationsByMember(loginMember);

        // then
        assertThat(response).hasSize(1);
    }
}
