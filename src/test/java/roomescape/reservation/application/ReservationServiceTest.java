package roomescape.reservation.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.ServiceTest;
import roomescape.global.exception.ViolationException;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static roomescape.TestFixture.*;

class ReservationServiceTest extends ServiceTest {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private MemberService memberService;

    private ReservationTime miaReservationTime;
    private Theme wootecoTheme;
    private Member mia;
    private Member tommy;

    @BeforeEach
    void setUp() {
        this.miaReservationTime = reservationTimeService.create(new ReservationTime(MIA_RESERVATION_TIME));
        this.wootecoTheme = themeService.create(WOOTECO_THEME());
        this.mia = memberService.create(USER_MIA());
        this.tommy = memberService.create(USER_TOMMY());
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void create() {
        // given
        Reservation reservation = MIA_RESERVATION(miaReservationTime, wootecoTheme, mia);

        // when
        Reservation createdReservation = reservationService.createReservation(reservation);

        // then
        assertThat(createdReservation.getId()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("invalidReservationDate")
    @DisplayName("예약 날짜는 현재 날짜 이후이다.")
    void validateDate(LocalDate invalidDate) {
        // given
        Reservation reservation = new Reservation(
                USER_MIA(), invalidDate, new ReservationTime(MIA_RESERVATION_TIME), WOOTECO_THEME());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(reservation))
                .isInstanceOf(ViolationException.class);
    }

    private static Stream<LocalDate> invalidReservationDate() {
        return Stream.of(
                LocalDate.now(),
                LocalDate.now().minusDays(1L)
        );
    }

    @Test
    @DisplayName("동일한 테마, 날짜, 시간에 한 팀만 예약할 수 있다.")
    void createDuplicatedReservation() {
        // given
        reservationService.createReservation(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia));

        Reservation duplicatedReservation = new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(duplicatedReservation))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // given
        reservationService.createReservation(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia));
        reservationService.createReservation(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy));

        // when
        List<Reservation> reservations = reservationService.findAll();

        // then
        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("예약자, 테마, 날짜로 예약 목록을 조회한다.")
    void findAllByMemberIdAndThemeIdAndDateBetween() {
        // given
        Reservation miaReservation = reservationService.createReservation(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia));
        Reservation tommyReservation = reservationService.createReservation(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy));

        // when
        List<Reservation> reservations = reservationService.findAllByMemberAndThemeAndDateBetween(
                miaReservation.getMember(), miaReservation.getTheme(), miaReservation.getDate(), tommyReservation.getDate());

        // then
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void delete() {
        // given
        Reservation reservation = reservationService.createReservation(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia));

        // when & then
        assertThatCode(() -> reservationService.deleteReservation(reservation.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("사용자의 예약 목록을 조회한다.")
    void findAllByMember() {
        // given
        reservationService.createReservation(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia));
        reservationService.createReservation(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy));

        // when
        List<Reservation> reservations = reservationService.findAllByMember(mia);

        // then
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("예약 삭제 시 예약 대기가 있다면 첫번째 예약 대기 순서가 예약 확정된다.")
    void changeWaitingToBooking() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Reservation miaReservation = reservationService.createReservation(new Reservation(mia, tomorrow, miaReservationTime, wootecoTheme, ReservationStatus.BOOKING));
        reservationService.createWaitingReservation(new Reservation(tommy, tomorrow, miaReservationTime, wootecoTheme, ReservationStatus.WAITING));

        reservationService.deleteReservation(miaReservation.getId());
        Reservation reservation = reservationService.findAllByMember(tommy).get(0);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.BOOKING);
    }

    @Test
    @DisplayName("이미 예약이 된 테마를 예약 대기를 신청하면 예외가 발생한다.")
    void invalidWaitingReservation1() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        final Reservation reservation = new Reservation(mia, tomorrow, miaReservationTime, wootecoTheme);
        reservationService.createReservation(reservation);

        assertThatThrownBy(() -> reservationService.createWaitingReservation(reservation))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("이미 예약 대기 신청이 된 테마를 다시 예약 대기를 신청하면 예외가 발생한다.")
    void invalidWaitingReservation2() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        final Reservation reservation = new Reservation(mia, tomorrow, miaReservationTime, wootecoTheme, ReservationStatus.WAITING);
        reservationService.createWaitingReservation(reservation);

        assertThatThrownBy(() -> reservationService.createWaitingReservation(reservation))
                .isInstanceOf(ViolationException.class);
    }
}
