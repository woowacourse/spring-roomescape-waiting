package roomescape.reservation.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.ServiceTest;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;
import static roomescape.TestFixture.WOOTECO_THEME;

class WaitingServiceTest extends ServiceTest {
    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ReservationService reservationService;

    private ReservationTime reservationTime;
    private LocalDate tomorrow;
    private Theme theme;
    private Member roro;
    private Member sudal;

    @BeforeEach
    void setUp() {
        this.reservationTime = reservationTimeService.create(new ReservationTime(MIA_RESERVATION_TIME));
        this.tomorrow = LocalDate.now().plusDays(1);
        this.theme = themeService.create(WOOTECO_THEME());
        this.roro = memberService.create(new Member("roro", "1234@email.com", "1234", Role.USER));
        this.sudal = memberService.create(new Member("sudal", "5678@email.com", "1234", Role.USER));
    }

    @Test
    @DisplayName("예약 대기 순서를 알 수 있다.")
    void findRankByReservation() {
        Reservation roroReservation = new Reservation(roro, tomorrow, reservationTime, theme, ReservationStatus.WAITING);
        Reservation sudalReservation = new Reservation(sudal, tomorrow, reservationTime, theme, ReservationStatus.WAITING);
        reservationService.createWaitingReservation(roroReservation);
        reservationService.createWaitingReservation(sudalReservation);

        Long roroRank = waitingService.findRankByReservation(roroReservation);
        Long sudalRank = waitingService.findRankByReservation(sudalReservation);

        assertThat(roroRank).isEqualTo(1);
        assertThat(sudalRank).isEqualTo(2);
    }

    @Test
    @DisplayName("전체 예약 대기를 조회할 수 있다.")
    void findAll() {
        Reservation roroReservation = new Reservation(roro, tomorrow, reservationTime, theme, ReservationStatus.WAITING);
        Reservation sudalReservation = new Reservation(sudal, tomorrow, reservationTime, theme, ReservationStatus.WAITING);
        reservationService.createWaitingReservation(roroReservation);
        reservationService.createWaitingReservation(sudalReservation);

        List<Waiting> waitings = waitingService.findAll();

        assertThat(waitings).hasSize(2);
    }

    @Test
    @DisplayName("예약 대기를 삭제할 수 있다.")
    void delete() {
        Reservation roroReservation = new Reservation(roro, tomorrow, reservationTime, theme, ReservationStatus.WAITING);
        Reservation sudalReservation = new Reservation(sudal, tomorrow, reservationTime, theme, ReservationStatus.WAITING);
        reservationService.createWaitingReservation(roroReservation);
        reservationService.createWaitingReservation(sudalReservation);

        waitingService.delete(1L);
        List<Waiting> waitings = waitingService.findAll();

        assertThat(waitings).hasSize(1);
    }
}
