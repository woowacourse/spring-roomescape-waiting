package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.application.dto.ReservationCreateDto;
import roomescape.application.dto.ReservationDto;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.AuthorizationException;
import roomescape.exception.NotFoundException;
import roomescape.testFixture.StubHelper;

@ExtendWith(MockitoExtension.class)
public class ReservationCommandServiceTest {

    @InjectMocks
    private ReservationCommandService reservationService;

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TimeService timeService;
    @Mock
    private ThemeService themeService;
    @Mock
    private MemberService memberService;
    @Mock
    private ClockProvider clockProvider;

    @DisplayName("사용자 예약 추가")
    @Test
    void registerReservation() {
        //given
        Theme theme = StubHelper.stubTheme(1L, themeService);
        ReservationTime time = StubHelper.stubTime(1L, timeService);
        Member member = StubHelper.stubMember(1L, memberService);
        Status status = Mockito.mock(Status.class);

        Reservation reservation = Reservation.of(1L, member, theme, LocalDate.now().plusDays(1), time, status);
        Mockito.doReturn(reservation).when(reservationRepository).save(Mockito.any(Reservation.class));
        Mockito.doReturn(LocalDateTime.now()).when(clockProvider).now();

        //when
        ReservationDto reservationDto = reservationService.registerReservation(new ReservationCreateDto(
                reservation.getDate(),
                theme.getId(),
                time.getId(),
                member.getId()
        ));

        //then
        Assertions.assertThat(reservationDto).isEqualTo(ReservationDto.from(reservation));
    }

    @DisplayName("예약이 가능한 일시에 예약 대기를 등록할 수 없다")
    @Test
    void rejectedWaitingByUser() {
        // given

        // when

        // then

    }

    @DisplayName("이미 해당 일시에 예약이 존재한다면 예약 대기를 등록한다")
    @Test
    void registerWaitingByUser() {
        // given

        // when

        // then

    }

    @DisplayName("관리자가 다른 사람의 예약을 삭제할 수 있다")
    @Test
    void deleteWaitingByAdmin() {
        // given
        Member admin = Mockito.mock(Member.class);
        Reservation reservation = Mockito.mock(Reservation.class);

        Mockito.when(admin.isAdmin()).thenReturn(true);
        Mockito.when(memberService.getMemberEntityById(1L)).thenReturn(admin);
        Mockito.when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        // when
        reservationService.cancelReservation(100L, 1L);

        // then
        Mockito.verify(reservation).cancel();
    }

    @DisplayName("본인은 자신의 예약을 삭제할 수 있다")
    @Test
    void deleteWaitingBySelf() {
        // given
        Member user = Mockito.mock(Member.class);
        Reservation reservation = Mockito.mock(Reservation.class);

        Mockito.when(user.isAdmin()).thenReturn(false);
        Mockito.when(user.isNotEqual(Mockito.any())).thenReturn(false);
        Mockito.when(memberService.getMemberEntityById(1L)).thenReturn(user);
        Mockito.when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        Mockito.when(reservation.getMember()).thenReturn(Mockito.mock(Member.class));

        // when
        reservationService.cancelReservation(100L, 1L);

        // then
        Mockito.verify(reservation).cancel();
    }

    @DisplayName("다른 사람의 예약은 삭제할 수 없다")
    @Test
    void deleteWaitingUnauthorized() {
        // given
        Member user = Mockito.mock(Member.class);
        Reservation reservation = Mockito.mock(Reservation.class);

        Mockito.when(user.isAdmin()).thenReturn(false);
        Mockito.when(user.isNotEqual(Mockito.any())).thenReturn(true);
        Mockito.when(memberService.getMemberEntityById(1L)).thenReturn(user);
        Mockito.when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        Mockito.when(reservation.getMember()).thenReturn(Mockito.mock(Member.class));

        // then
        assertThatThrownBy(() -> reservationService.cancelReservation(100L, 1L))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
    }

    @DisplayName("예약이 존재하지 않으면 예외를 던진다")
    @Test
    void deleteWaitingNotFound() {
        // given
        Member member = Mockito.mock(Member.class);
        Mockito.when(memberService.getMemberEntityById(1L)).thenReturn(member);
        Mockito.when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> reservationService.cancelReservation(999L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("삭제하려는 예약 id가 존재하지 않습니다. id: 999");
    }
}
