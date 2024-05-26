package roomescape.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.exception.reservation.DateTimePassedException;
import roomescape.exception.reservation.ReservationConflictException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.exception.reservation.TimeNotFoundException;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@Import(ReservationService.class)
@DataJpaTest
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    private final Member sampleMember = new Member("t1@t1.com", "123", "러너덕", "MEMBER");
    private final ReservationTime sampleTime = new ReservationTime("11:00");
    private final Theme sampleTheme = new Theme("공포", "공포는 무서워", "hi.jpg");
    private final LocalDate sampleDate = LocalDate.parse("2025-11-30");

    @DisplayName("저장되어있지 않은 예약 시간에 예약을 시도하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_unsaved_time() {
        memberRepository.save(sampleMember);
        themeRepository.save(sampleTheme);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2025-11-30", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(TimeNotFoundException.class)
                .hasMessage("예약 하려는 시간이 저장되어 있지 않습니다.");
    }

    @DisplayName("이미 지나간 날짜에 예약을 시도하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_before_date() {
        memberRepository.save(sampleMember);
        reservationTimeRepository.save(sampleTime);
        themeRepository.save(sampleTheme);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2024-05-07", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(DateTimePassedException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약은 불가능합니다.");
    }

    @DisplayName("같은 테마를 같은 시간에 예약을 시도하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_same_theme_and_date_time() {
        memberRepository.save(sampleMember);
        reservationTimeRepository.save(sampleTime);
        themeRepository.save(sampleTheme);
        Reservation reservation1 = new Reservation(sampleMember, sampleTheme, sampleDate, sampleTime);
        reservationRepository.save(reservation1);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2025-11-30", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(ReservationConflictException.class);
    }

    @DisplayName("예약을 정상적으로 생성한다.")
    @Test
    void success_create_reservation() {
        reservationTimeRepository.save(sampleTime);
        themeRepository.save(sampleTheme);
        memberRepository.save(sampleMember);

        ReservationCreate reservationDto = new ReservationCreate("t1@t1.com", 1L, "2025-11-30", 1L);

        assertThatNoException()
                .isThrownBy(() -> reservationService.createReservation(reservationDto));
    }


    @DisplayName("예약 삭제 시 저장되어있지 않은 아이디면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_not_saved_reservation_id() {
        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @DisplayName("예약을 정상적으로 삭제한다.")
    @Test
    void success_delete_reservation() {
        Reservation reservation = new Reservation(sampleMember, sampleTheme, sampleDate, sampleTime);
        memberRepository.save(sampleMember);
        reservationTimeRepository.save(sampleTime);
        themeRepository.save(sampleTheme);
        reservationRepository.save(reservation);

        assertThatNoException()
                .isThrownBy(() -> reservationService.deleteReservation(reservation.getId()));
    }

    @Test
    @DisplayName("예약 삭제 시 대기가 있을 경우 자동으로 예약을 생성한다.")
    void autoCreateReservationWhenDeletingIfWaitingExist() {
        // given
        Member member = memberRepository.save(sampleMember);
        Member member2 = memberRepository.save(new Member("t2@t2.com", "123", "안돌", "MEMBER"));
        ReservationTime time = reservationTimeRepository.save(sampleTime);
        Theme theme = themeRepository.save(sampleTheme);
        Reservation reservation = reservationRepository.save(new Reservation(member, theme, sampleDate, time));
        Waiting waiting = waitingRepository.save(new Waiting(reservation, member2, LocalDateTime.now()));

        // when
        Long reservationId = reservation.getId();
        reservationService.deleteReservation(reservationId);
        List<Reservation> actualReservation = reservationRepository.findAllByMemberEmail(member2.getEmail());
        List<Waiting> actualWaiting = waitingRepository.findByReservationId(reservationId);

        // then
        assertThat(actualReservation).hasSize(1);
        assertThat(actualWaiting).isEmpty();
    }

    @Test
    @DisplayName("예약과 예약 대기가 동시에 존재하는 멤버에 대해 예약을 조회한다.")
    void findReservationsByMemberEmailWithWaiting() {
        // given
        Member member = memberRepository.save(sampleMember);
        Member member2 = memberRepository.save(new Member("t2@t2.com", "123", "안돌", "MEMBER"));
        ReservationTime time = reservationTimeRepository.save(sampleTime);
        Theme theme = themeRepository.save(sampleTheme);
        Reservation reservation = reservationRepository.save(new Reservation(member, theme, sampleDate, time));
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        waitingRepository.save(new Waiting(reservation, member2, LocalDateTime.now()));
        reservationRepository.save(new Reservation(member2, theme, tomorrow, time));

        // when
        List<ReservationResponse> actual = reservationService.findReservationsByMemberEmail(member2.getEmail());

        // then
        assertThat(actual).hasSize(2);
    }
}
