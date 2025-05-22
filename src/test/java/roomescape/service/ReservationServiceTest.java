package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.ReservationCreateDto;

class ReservationServiceTest {

    @Mock
    private JpaReservationRepository reservationRepository;

    @Mock
    private JpaReservationTimeRepository reservationTimeRepository;

    @Mock
    private JpaThemeRepository themeRepository;

    @Mock
    private JpaMemberRepository memberRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReservation() {
        ReservationCreateDto dto = new ReservationCreateDto(LocalDate.of(2025, 11, 11), 1, 1, 1);

        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "theme", "theme", "theme");
        Member member = new Member(1L, "moda", "email", Role.ADMIN, "pw");
        Reservation reservation = Reservation.createWithoutId(member, LocalDate.of(2025, 11, 11), reservationTime, theme);

        when(reservationTimeRepository.findById(any(Long.class))).thenReturn(
                Optional.of(reservationTime));
        when(themeRepository.findById(any(Long.class))).thenReturn(
                Optional.of(theme));
        when(memberRepository.findById(any(Long.class))).thenReturn(
                Optional.of(member));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(
                reservation);

        ReservationResponseDto createdReservation = reservationService.createReservation(dto);

        assertThat(createdReservation.id()).isEqualTo(1L);
        assertThat(createdReservation.member().name()).isEqualTo("moda");
        assertThat(createdReservation.date()).isEqualTo(LocalDate.of(2025, 11, 11));
        assertThat(createdReservation.time().startAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(createdReservation.theme().name()).isEqualTo("theme");
    }

}