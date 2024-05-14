package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;
import static roomescape.Fixture.VALID_RESERVATION_DATE;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.MemberRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.service.exception.PastReservationException;
import roomescape.service.request.ReservationAppRequest;
import roomescape.service.response.ReservationAppResponse;
import roomescape.service.response.ReservationTimeAppResponse;
import roomescape.service.response.ThemeAppResponse;

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
    private final long timeId = 1L;
    private final long themeId = 1L;
    private final long memberId = 1L;

    @DisplayName("예약을 저장하고, 해당 예약을 id값과 함께 반환한다.")
    @Test
    void save() {
        long reservationId = 1L;
        Reservation reservation = VALID_RESERVATION;

        when(reservationTimeRepository.findById(timeId))
            .thenReturn(Optional.of(VALID_RESERVATION_TIME));
        when(themeRepository.findById(themeId))
            .thenReturn(Optional.of(VALID_THEME));
        when(memberRepository.findById(memberId))
            .thenReturn(Optional.of(VALID_MEMBER));

        when(reservationRepository.save(any(Reservation.class)))
            .thenReturn(new Reservation(
                reservationId,
                VALID_MEMBER,
                VALID_RESERVATION_DATE,
                VALID_RESERVATION_TIME,
                VALID_THEME)
            );

        ReservationAppRequest request = new ReservationAppRequest(VALID_RESERVATION_DATE.getDate().toString(), timeId,
            themeId, memberId);
        ReservationAppResponse actual = reservationService.save(request);
        ReservationAppResponse expected = new ReservationAppResponse(
            reservationId,
            reservation.getMember().getName().getName(),
            reservation.getReservationDate(),
            ReservationTimeAppResponse.from(reservation.getReservationTime()),
            ThemeAppResponse.from(reservation.getTheme()));

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("실패: 존재하지 않는 시간,테마,사용자 ID 입력 시 예외가 발생한다.")
    @Test
    void save_TimeIdDoesntExist() {
        assertThatThrownBy(() -> reservationService.save(new ReservationAppRequest("2030-12-31", 1L, 1L, 1L)))
            .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("실패: 중복 예약을 생성하면 예외가 발생한다.")
    @Test
    void save_Duplication() {
        String rawDate = "2030-12-31";

        when(themeRepository.findById(themeId))
            .thenReturn(Optional.of(VALID_THEME));
        when(reservationTimeRepository.findById(timeId))
            .thenReturn(Optional.of(VALID_RESERVATION_TIME));
        when(memberRepository.findById(memberId))
            .thenReturn(Optional.of(VALID_MEMBER));
        when(reservationRepository.isDuplicated(LocalDate.parse(rawDate), timeId, themeId))
            .thenReturn(true);

        assertThatThrownBy(
            () -> reservationService.save(new ReservationAppRequest(rawDate, timeId, themeId, memberId)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("실패: 어제 날짜에 대한 예약을 생성하면 예외가 발생한다.")
    @Test
    void save_PastDateReservation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        when(reservationTimeRepository.findById(timeId))
            .thenReturn(Optional.of(VALID_RESERVATION_TIME));
        when(themeRepository.findById(themeId))
            .thenReturn(Optional.of(VALID_THEME));
        when(memberRepository.findById(memberId))
            .thenReturn(Optional.of(VALID_MEMBER));

        assertThatThrownBy(
            () -> reservationService.save(
                new ReservationAppRequest(yesterday.toString(), timeId, themeId, memberId))
        ).isInstanceOf(PastReservationException.class);
    }

    @DisplayName("실패: 같은 날짜에 대한 과거 시간 예약을 생성하면 예외가 발생한다.")
    @Test
    void save_TodayPastTimeReservation() {
        LocalDate today = LocalDate.now();
        String oneMinuteAgo = LocalTime.now().minusMinutes(1).toString();

        ReservationTime reservationTime = new ReservationTime(oneMinuteAgo);
        Theme theme = new Theme("방탈출1", "방탈출1을 한다.", "https://url");

        when(reservationTimeRepository.findById(timeId))
            .thenReturn(Optional.of(reservationTime));
        when(themeRepository.findById(themeId))
            .thenReturn(Optional.of(theme));
        when(memberRepository.findById(memberId))
            .thenReturn(Optional.of(VALID_MEMBER));

        assertThatThrownBy(
            () -> reservationService.save(
                new ReservationAppRequest(today.toString(), timeId, themeId, memberId))
        ).isInstanceOf(PastReservationException.class);
    }
}
