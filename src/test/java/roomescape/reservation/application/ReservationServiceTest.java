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
    private LocalDate tomorrow;

    @BeforeEach
    void setUp() {
        this.miaReservationTime = reservationTimeService.create(new ReservationTime(MIA_RESERVATION_TIME));
        this.wootecoTheme = themeService.create(WOOTECO_THEME());
        this.mia = memberService.create(USER_MIA());
        this.tommy = memberService.create(USER_TOMMY());
        this.tomorrow = LocalDate.now().plusDays(1);
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
    @DisplayName("이미 예약이 된 테마를 예약 대기를 신청하면 예외가 발생한다.")
    void invalidWaitingReservation1() {
        Reservation reservation = USER_RESERVATION(mia, tomorrow, miaReservationTime, wootecoTheme);
        reservationService.createReservation(reservation);

        assertThatThrownBy(() -> reservationService.createWaitingReservation(reservation))
                .isInstanceOf(ViolationException.class)
                .hasMessageContaining("이미 예약 또는 대기를 신청하셨습니다.");
    }

    @Test
    @DisplayName("이미 예약 대기 신청이 된 테마를 다시 예약 대기를 신청하면 예외가 발생한다.")
    void invalidWaitingReservation2() {
        Reservation reservation = USER_RESERVATION(mia, tomorrow, miaReservationTime, wootecoTheme);
        reservationService.createWaitingReservation(reservation);

        assertThatThrownBy(() -> reservationService.createWaitingReservation(reservation))
                .isInstanceOf(ViolationException.class)
                .hasMessageContaining("이미 예약 또는 대기를 신청하셨습니다.");
    }

    @Test
    @DisplayName("예약 대기를 요청한 사용자가 예약 대기를 삭제할 수 있다.")
    void deleteWaitingReservation() {
        Reservation reservation = USER_RESERVATION(mia, tomorrow, miaReservationTime, wootecoTheme, ReservationStatus.BOOKING);
        Reservation createdReservation = reservationService.createWaitingReservation(reservation);

        reservationService.deleteWaitingReservation(createdReservation.getId(), mia);
        List<Reservation> reservations = reservationService.findAllByMember(mia);

        assertThat(reservations).hasSize(0);
    }

    @Test
    @DisplayName("관리자가 특정 사용자의 예약 대기를 삭제할 수 있다.")
    void deleteWaitingReservationByAdmin() {
        Reservation reservation = USER_RESERVATION(mia, tomorrow, miaReservationTime, wootecoTheme, ReservationStatus.BOOKING);
        Reservation createdReservation = reservationService.createWaitingReservation(reservation);

        reservationService.deleteWaitingReservation(createdReservation.getId(), USER_ADMIN());
        List<Reservation> reservations = reservationService.findAllByMember(mia);

        assertThat(reservations).hasSize(0);
    }

    @Test
    @DisplayName("예약 대기 삭제 시 예약 요청한 사용자가 아니라면 예외가 발생한다.")
    void invalidDeleteWaitingReservation() {
        Reservation reservation = USER_RESERVATION(mia, tomorrow, miaReservationTime, wootecoTheme, ReservationStatus.BOOKING);
        Reservation createdReservation = reservationService.createWaitingReservation(reservation);

        assertThatThrownBy(() -> reservationService.deleteWaitingReservation(createdReservation.getId(), tommy))
                .isInstanceOf(ViolationException.class)
                .hasMessageContaining("예약 대기 삭제는 예약을 한 사용자 또는 관리자만 가능합니다.");
    }
}
