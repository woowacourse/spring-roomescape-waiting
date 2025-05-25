package roomescape.application;

import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.domain.ReservationStatus.RESERVED;
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
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.ReservationServiceResponse;
import roomescape.application.dto.ReservationStatusServiceResponse;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;
import roomescape.domain.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private GameScheduleService gameScheduleService;
    @Mock
    private MemberService memberService;

    @DisplayName("사용자 예약 목록 조회 성공")
    @Test
    void getReservationsByMember() {
        //given
        Long memberId = 2L;
        Member member = stubMember(memberId);

        Theme theme = Theme.of(1L, "테마1", "테마1입니다.", "썸네일1");
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        GameSchedule gameSchedule = GameSchedule.of(1L, LocalDate.now().plusDays(1), time, theme);

        Reservation reservation1 = Reservation.of(1L, member, gameSchedule, RESERVED);
        Reservation reservation2 = Reservation.of(2L, member, gameSchedule, RESERVED);

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation1);
        reservations.add(reservation2);

        Mockito.doReturn(reservations).when(reservationRepository).findByMember(Mockito.any(Member.class));

        //when
        List<ReservationStatusServiceResponse> dtos = reservationService.getReservationsByMember(memberId);

        //then
        assertAll(
                () -> Assertions.assertThat(dtos).hasSize(2),
                () -> Assertions.assertThat(dtos.get(0).reservationId()).isEqualTo(reservation1.getId()),
                () -> Assertions.assertThat(dtos.get(1).reservationId()).isEqualTo(reservation2.getId())
        );
    }

    @DisplayName("사용자 예약 추가")
    @Test
    void registerReservation() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        Theme theme = Theme.of(1L, "테마1", "테마1입니다.", "썸네일1");

        GameSchedule gameSchedule = GameSchedule.of(1L, date, time, theme);
        Mockito.doReturn(gameSchedule).when(gameScheduleService).getGameScheduleForReservation(
                date,
                time.getId(),
                theme.getId()
        );

        Member member = stubMember(1L);
        Reservation reservation = Reservation.of(1L, member, gameSchedule, RESERVED);
        Mockito.doReturn(reservation).when(reservationRepository).save(Mockito.any(Reservation.class));

        //when
        ReservationServiceResponse reservationServiceResponse = reservationService.registerReservation(
                new ReservationCreateServiceRequest(date, theme.getId(), time.getId(), member.getId())
        );

        //then
        Assertions.assertThat(reservationServiceResponse).isEqualTo(ReservationServiceResponse.from(reservation));
    }

    private Member stubMember(long memberId) {
        Member member = Member.of(memberId, "브라운", "brown@email.com", "brown", USER);
        Mockito.doReturn(member).when(memberService).getMemberEntityById(memberId);
        return member;
    }
}
