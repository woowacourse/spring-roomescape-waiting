package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.IllegalAuthorizationException;
import roomescape.exception.IllegalReservationDateTimeRequestException;
import roomescape.member.dao.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.reservation.dao.ReservationContentRepository;
import roomescape.reservation.dao.ReservationRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationContent;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.theme.dao.ThemeRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.dao.TimeRepository;
import roomescape.time.domain.Time;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private final Time time = new Time(1L, LocalTime.of(12, 0));
    private final Theme theme = new Theme(1L, "그켬미", "켬미 방탈출", "thumbnail");
    private final ReservationContent reservationContent = new ReservationContent(LocalDate.MAX, time, theme);
    private final Member member = new Member(1L,"켬미", "kyummi@email.com", "pass");
    private final Reservation reservation = new Reservation(1L, member, reservationContent, LocalDateTime.now());

    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationContentRepository contentRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private TimeRepository timeRepository;

    @Test
    @DisplayName("예약을 추가한다.")
    void addReservation() {
        Mockito.when(reservationRepository.save(any()))
                .thenReturn(reservation);
        Mockito.when(timeRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservation.getTime()));
        Mockito.when(themeRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservation.getTheme()));
        Mockito.when(
                        contentRepository.findByThemeAndTimeAndDate(any(Theme.class), any(Time.class), any(LocalDate.class)))
                .thenReturn(Optional.of(reservationContent));

        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate(), reservation.getMember(),
                reservation.getTime()
                        .getId(), reservation.getTheme()
                .getId());
        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest);

        assertThat(reservationResponse.id()).isEqualTo(1);
    }

    @Test
    @DisplayName("과거의 날짜를 예약하려고 시도하는 경우 에외를 던진다.")
    void validation_ShouldThrowException_WhenReservationDateIsPast() {

        Mockito.when(timeRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservation.getTime()));

        Mockito.when(themeRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservation.getTheme()));

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.MIN, reservation.getMember(),
                reservation.getTimeId(), reservation.getThemeId());

        assertThatThrownBy(() -> reservationService.addReservation(reservationRequest)).isInstanceOf(
                IllegalReservationDateTimeRequestException.class);

    }

    @Test
    @DisplayName("예약을 찾는다.")
    void findReservations() {
        Mockito.when(reservationRepository.findAllByOrderByReservationContent_Date())
                .thenReturn(List.of(reservation));

        List<ReservationResponse> reservationResponses = reservationService.findReservations();

        assertThat(reservationResponses).hasSize(1);
    }

    @Test
    @DisplayName("자신이 요청한 예약을 정상적으로 지운다.")
    void cancelMyReservation_ShouldRemoveReservation_WhenRequestFromReservationOwner() {
        Mockito.doNothing()
                .when(reservationRepository)
                .deleteById(reservation.getId());
        Mockito.when(reservationRepository.findById(reservation.getMemberId()))
                .thenReturn(Optional.of(reservation));

        assertThatCode(() -> reservationService.cancelMyReservation(reservation.getId(),
                new MemberProfileInfo(member.getId(), member.getName(), member.getEmail()))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("자신의 요청하지 않은 예약을 취소 요청할 경우, 예외를 던진다.")
    void cancelMyReservation_ShouldThrowException_WhenRequestNotFromReservationOwner() {

        Mockito.when(reservationRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(reservation));
        MemberProfileInfo memberProfileInfo = new MemberProfileInfo(0L, member.getName(), member.getEmail());

        Long reservationId = reservation.getId();
        assertThatThrownBy(() -> reservationService.cancelMyReservation(reservationId,
                memberProfileInfo)).isInstanceOf(
                IllegalAuthorizationException.class);
    }

}
