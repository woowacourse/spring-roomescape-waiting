package roomescape.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.enums.Role;
import roomescape.dto.admin.AdminReservationRequest;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.search.SearchConditionsRequest;
import roomescape.exception.reservation.ReservationAlreadyExistsException;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.theme.ThemeNotFoundException;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;
import roomescape.repository.waiting.WaitingRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository timeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private WaitingRepository waitingRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @DisplayName("예약 시간이 존재하지 않으면 예약을 생성할 수 없다")
    @Test
    void timeNotFound() {
        // given
        LocalDate today = LocalDate.now();
        Long timeId = 1L;
        Long themeId = 1L;

        ReservationRequest request = new ReservationRequest(today, timeId, themeId);

        // when
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> reservationService.create(request, new Member(1L, "슬링키", "이메일", "비밀번호",
                Role.ADMIN)))
                .isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @DisplayName("테마가 존재하지 않으면 예약을 생성할 수 없다")
    @Test
    void themeNotFound() {
        // given
        LocalDate today = LocalDate.now();
        Long timeId = 1L;
        Long themeId = 1L;
        String name = "에드";
        ReservationRequest request = new ReservationRequest(today, timeId, themeId);

        // when
        when(timeRepository.findById(timeId)).thenReturn(
                Optional.of(new ReservationTime(timeId, LocalTime.now().plusHours(1))));

        when(themeRepository.findById(themeId)).thenReturn(Optional.empty());
        //then
        assertThatThrownBy(() -> reservationService.create(request, new Member(1L, "슬링키", "이메일", "비밀번호", Role.ADMIN)))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("이미 예약이 존재하는 날짜, 시간에는 예약을 생성할 수 없다")
    @Test
    void alreadyExists() {
        // given
        LocalDate today = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;
        String name = "에드";
        ReservationRequest request = new ReservationRequest(today, timeId, themeId);

        // when
        ReservationTime time = new ReservationTime(timeId, LocalTime.now().plusHours(1));
        when(timeRepository.findById(timeId)).thenReturn(
                Optional.of(time));

        when(themeRepository.findById(themeId)).thenReturn(
                Optional.of(new Theme(themeId, "test", "test", "test")));
        when(reservationRepository.existsByDateAndTimeId(today, time.getId())).thenReturn(true);

        //then
        assertThatThrownBy(() -> reservationService.create(request, new Member(1L, "슬링키", "이메일", "비밀번호", Role.ADMIN)))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @DisplayName("관리자가 정상적으로 예약을 생성할 수 있다")
    @Test
    void createByAdminSuccess() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 2L;
        Long themeId = 2L;
        Long memberId = 2L;

        AdminReservationRequest request = new AdminReservationRequest(date, timeId, themeId, memberId);

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "admin@a.com", "pw", Role.ADMIN);
        Reservation reservation = new Reservation(99L, date, time, theme, member);

        // when
        when(timeRepository.findById(anyLong())).thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(reservationRepository.save(any())).thenReturn(reservation);

        // then
        ReservationResponse response = reservationService.createByAdmin(request);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.member().name()).isEqualTo("관리자");
        assertThat(response.theme().name()).isEqualTo("SF 테마");
        assertThat(response.date()).isEqualTo(date);
    }

    @DisplayName("일반 사용자가 정상적으로 예약을 생성할 수 있다")
    @Test
    void createReservationByUserSuccess() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 3L;
        Long themeId = 3L;
        Member member = new Member(3L, "일반유저", "user@a.com", "pw", Role.USER);
        ReservationRequest request = new ReservationRequest(date, timeId, themeId);
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(11, 0));
        Theme theme = new Theme(themeId, "일반 테마", "설명", "url");
        Reservation reservation = new Reservation(100L, date, time, theme, member);

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateAndTimeId(date, timeId)).thenReturn(false);
        when(reservationRepository.save(any())).thenReturn(reservation);

        // when
        ReservationResponse response = reservationService.create(request, member);

        // then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.member().name()).isEqualTo("일반유저");
        assertThat(response.theme().name()).isEqualTo("일반 테마");
        assertThat(response.date()).isEqualTo(date);
    }

    @DisplayName("과거 시간에 예약을 시도하면 예외가 발생한다")
    @Test
    void reservationInPastException() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;
        Member member = new Member(1L, "유저", "이메일", "비밀번호", Role.USER);
        ReservationRequest request = new ReservationRequest(yesterday, timeId, themeId);
        ReservationTime time = new ReservationTime(timeId, LocalTime.now());
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(new Theme(themeId, "테마", "설명", "url")));

        // then
        assertThatThrownBy(() -> reservationService.create(request, member))
                .isInstanceOf(roomescape.exception.reservation.ReservationInPastException.class);
    }

    @DisplayName("예약 전체 목록을 조회할 수 있다")
    @Test
    void getAllReservations() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 3L;
        Long themeId = 3L;
        Member member = new Member(3L, "일반유저", "user@a.com", "pw", Role.USER);
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(11, 0));
        Theme theme = new Theme(themeId, "일반 테마", "설명", "url");
        Reservation reservation = new Reservation(100L, date, time, theme, member);
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));

        // when
        List<ReservationResponse> responses = reservationService.getAll();

        // then
        assertThat(responses).hasSize(1);
    }

    @DisplayName("예약 삭제 시 대기자가 있으면 대기자를 예약으로 승격한다")
    @Test
    void deleteById_promoteWaiting() {

        // given
        Long reservationId = 1L;
        LocalDate date = LocalDate.now().plusDays(2);
        ReservationTime time = new ReservationTime(1L, LocalTime.now());
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Member member = new Member(1L, "유저", "이메일", "비밀번호", Role.USER);
        Reservation reservation = new Reservation(reservationId, date, time, theme, member);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(waitingRepository.existsByDateAndTimeIdAndThemeId(any(), any(), any())).thenReturn(true);
        Waiting waiting = new Waiting(1L, date, time, theme, member);
        when(waitingRepository.findFirstWaitingByDateAndTimeIdAndThemeId(any(), any(), any())).thenReturn(waiting);

        // when
        reservationService.deleteById(reservationId);

        // then
        verify(reservationRepository).deleteById(reservationId);
        verify(reservationRepository).save(any());
        verify(waitingRepository).deleteById(any());
    }

    @DisplayName("조건별 예약 조회가 가능하다")
    @Test
    void getReservationsByConditions() {
        // given
        SearchConditionsRequest cond = new SearchConditionsRequest(1L, 1L, LocalDate.now(),
                LocalDate.now().plusDays(1));
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 3L;
        Long themeId = 3L;
        Member member = new Member(3L, "일반유저", "user@a.com", "pw", Role.USER);
        ReservationRequest request = new ReservationRequest(date, timeId, themeId);
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(11, 0));
        Theme theme = new Theme(themeId, "일반 테마", "설명", "url");
        Reservation reservation = new Reservation(100L, date, time, theme, member);

        when(reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(List.of(reservation));

        // when
        List<ReservationResponse> responses = reservationService.getReservationsByConditions(cond);

        // then
        assertThat(responses).hasSize(1);
    }

    @DisplayName("회원별 예약 조회가 가능하다")
    @Test
    void getReservationByMember() {
        // given
        Member member = new Member(1L, "유저", "이메일", "비밀번호", Role.USER);

        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 3L;
        Long themeId = 3L;
        Member reservedMember = new Member(3L, "일반유저", "user@a.com", "pw", Role.USER);
        ReservationRequest request = new ReservationRequest(date, timeId, themeId);
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(11, 0));
        Theme theme = new Theme(themeId, "일반 테마", "설명", "url");
        Reservation reservation = new Reservation(100L, date, time, theme, reservedMember);
        when(reservationRepository.findAllByMember(member)).thenReturn(List.of(reservation));

        // when
        List<roomescape.dto.reservation.MemberReservationResponse> responses = reservationService.getReservationByMember(
                member);

        // then
        assertThat(responses).hasSize(1);
    }
}
