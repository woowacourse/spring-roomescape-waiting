package roomescape.application;

import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.domain.Role.USER;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.application.dto.MemberDto;
import roomescape.application.dto.ReservationWaitingDto;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TimeService timeService;
    @Mock
    private ThemeService themeService;
    @Mock
    private MemberService memberService;

    @DisplayName("사용자 예약 목록 조회 성공")
    @Test
    void getReservationsByMember() {
        //given
        Long memberId = 2L;
        Member member = Member.of(memberId, "브라운", "brown@email.com", "brown", USER);
        Mockito.doReturn(MemberDto.from(member)).when(memberService).getMemberById(memberId);

        Theme theme = Theme.of(1L, "테마1", "테마1입니다.", "썸네일1");
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        Waiting waiting = new Waiting(ReservationStatus.RESERVED);

        Reservation reservation1 = Reservation.of(1L, member, theme, LocalDate.now().plusDays(1), time, waiting);
        Reservation reservation2 = Reservation.of(2L, member, theme, LocalDate.now().plusDays(1), time, waiting);

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation1);
        reservations.add(reservation2);
        Mockito.doReturn(reservations).when(reservationRepository).findByMember(Mockito.any(Member.class));

        //when
        List<ReservationWaitingDto> dtos = reservationService.getReservationsByMember(memberId);

        //then
        assertAll(
                () -> Assertions.assertThat(dtos).hasSize(2),
                () -> Assertions.assertThat(dtos.get(0).reservationId()).isEqualTo(reservation1.getId()),
                () -> Assertions.assertThat(dtos.get(1).reservationId()).isEqualTo(reservation2.getId())
        );
    }
}
