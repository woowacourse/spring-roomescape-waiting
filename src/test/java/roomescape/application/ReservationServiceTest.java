package roomescape.application;

import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.domain.ReservationStatus.RESERVED;
import static roomescape.domain.ReservationStatus.WAITING;
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
import org.springframework.context.MessageSource;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.ReservationServiceResponse;
import roomescape.application.dto.ReservationStatusServiceResponse;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;
import roomescape.domain.entity.Waiting;
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
    @Mock
    private WaitingService waitingService;
    @Mock
    private MessageSource messageSource;

    @DisplayName("사용자 예약 목록 조회 성공")
    @Test
    void getMyReservationsByMember() {
        //given
        Long memberId = 2L;
        Member member = Member.withId(memberId, "브라운", "brown@email.com", "brown", USER);

        Theme theme = Theme.withId(1L, "테마1", "테마1입니다.", "썸네일1");
        ReservationTime reservationTime = ReservationTime.withId(1L, LocalTime.of(10, 0));
        GameSchedule reservationSchedule = GameSchedule.withId(1L, LocalDate.now().plusDays(1), reservationTime, theme);

        Reservation reservation1 = Reservation.withId(1L, member, reservationSchedule, RESERVED);
        Reservation reservation2 = Reservation.withId(2L, member, reservationSchedule, RESERVED);

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation1);
        reservations.add(reservation2);

        Mockito.doReturn(reservations).when(reservationRepository).findByMemberId(memberId);

        //when
        List<ReservationStatusServiceResponse> dtos = reservationService.getReservationsByMember(memberId);

        //then
        assertAll(
                () -> Assertions.assertThat(dtos).hasSize(2),
                () -> Assertions.assertThat(dtos.get(0).reservationId()).isEqualTo(reservation1.getId()),
                () -> Assertions.assertThat(dtos.get(1).reservationId()).isEqualTo(reservation2.getId())
        );
    }

    @DisplayName("사용자 예약 목록에 예약 대기 목록도 포함하여 조회 성공")
    @Test
    void getReservationsAndWaitingsByMember() {
        //given
        Long memberId = 2L;
        Member member = Member.withId(memberId, "브라운", "brown@email.com", "brown", USER);

        Theme theme = Theme.withId(1L, "테마1", "테마1입니다.", "썸네일1");
        ReservationTime time1 = ReservationTime.withId(1L, LocalTime.of(10, 0));
        ReservationTime time2 = ReservationTime.withId(2L, LocalTime.of(11, 0));
        GameSchedule reservationSchedule1 = GameSchedule.withId(1L, LocalDate.now().plusDays(1), time1, theme);
        GameSchedule reservationSchedule2 = GameSchedule.withId(2L, LocalDate.now().plusDays(2), time1, theme);
        GameSchedule waitingSchedule = GameSchedule.withId(3L, LocalDate.now().plusDays(1), time2, theme);

        Reservation reservation1 = Reservation.withId(1L, member, reservationSchedule1, RESERVED);
        Reservation reservation2 = Reservation.withId(2L, member, reservationSchedule2, RESERVED);
        Waiting waiting = Waiting.withId(3L, member, waitingSchedule, WAITING);

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation1);
        reservations.add(reservation2);
        Mockito.doReturn(reservations).when(reservationRepository).findByMemberId(memberId);

        ReservationStatusServiceResponse waitingDto = new ReservationStatusServiceResponse(
                waiting.getId(),
                waiting.getGameSchedule().getTheme().getName(),
                waiting.getGameSchedule().getDate(),
                waiting.getGameSchedule().getTime().getStartAt(),
                waiting.getStatus().name()
        );
        Mockito.doReturn(List.of(waitingDto)).when(waitingService).getWaitingsByMember(memberId);

        //when
        List<ReservationStatusServiceResponse> dtos = reservationService.getMyReservationsByMember(memberId);

        //then
        assertAll(
                () -> Assertions.assertThat(dtos).hasSize(3),
                () -> Assertions.assertThat(dtos.get(0).reservationId()).isEqualTo(reservation1.getId()),
                () -> Assertions.assertThat(dtos.get(1).reservationId()).isEqualTo(reservation2.getId()),
                () -> Assertions.assertThat(dtos.get(2).reservationId()).isEqualTo(waiting.getId())
        );
    }

    @DisplayName("사용자 예약 추가")
    @Test
    void registerReservation() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = ReservationTime.withId(1L, LocalTime.of(10, 0));
        Theme theme = Theme.withId(1L, "테마1", "테마1입니다.", "썸네일1");

        GameSchedule gameSchedule = GameSchedule.withId(1L, date, time, theme);
        Mockito.doReturn(gameSchedule).when(gameScheduleService).getGameScheduleForReservation(
                date,
                time.getId(),
                theme.getId()
        );

        Member member = Member.withId(1L, "브라운", "brown@email.com", "brown", USER);
        Mockito.doReturn(member).when(memberService).getMemberEntityById(member.getId());

        Reservation reservation = Reservation.withId(1L, member, gameSchedule, RESERVED);
        Mockito.doReturn(reservation).when(reservationRepository).save(Mockito.any(Reservation.class));

        //when
        ReservationServiceResponse reservationServiceResponse = reservationService.registerReservation(
                new ReservationCreateServiceRequest(date, theme.getId(), time.getId(), member.getId())
        );

        //then
        Assertions.assertThat(reservationServiceResponse).isEqualTo(ReservationServiceResponse.from(reservation));
    }
}
