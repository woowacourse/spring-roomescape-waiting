package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.common.Constant.MATT;
import static roomescape.common.Constant.예약날짜_내일;

import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.role.Role;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.user.controller.dto.ReservationRequest;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeService reservationTimeService;

    @Mock
    private ThemeService themeService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void 예약을_생성한다() {
        Member savedMember = new Member(1L, new Name("매트"), new Email("matt.kakao"), new Password("1234"), Role.MEMBER);
        ReservationTime savedReservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme savedTheme = new Theme(1L, "test", "test", "test");
        Reservation savedReservation = new Reservation(1L, 예약날짜_내일.getDate(), savedReservationTime, savedTheme,
                savedMember, ReservationStatus.RESERVATION);
        when(reservationRepository.existsByReservationDateAndReservationTimeId(any(), any())).thenReturn(false);
        when(memberService.findById(any(Long.class))).thenReturn(savedMember);
        when(reservationTimeService.getReservationTime(any(Long.class))).thenReturn(savedReservationTime);
        when(themeService.getTheme(any(Long.class))).thenReturn(savedTheme);
        when(reservationRepository.save(any())).thenReturn(savedReservation);

        ReservationRequest reservationRequest = new ReservationRequest(
                예약날짜_내일.getDate(),
                savedReservationTime.getId(),
                savedTheme.getId()
        );

        ReservationResponse response = reservationService.create(1L, reservationRequest);
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
        when(reservationRepository.existsByReservationDateAndReservationTimeId(any(), any())).thenReturn(true);

        ReservationRequest request = new ReservationRequest(
                예약날짜_내일.getDate(),
                savedReservationTime.getId(),
                savedTheme.getId()
        );
        assertThatThrownBy(() -> reservationService.create(MATT.getId(), request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_예약을_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationService.deleteById(3L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
