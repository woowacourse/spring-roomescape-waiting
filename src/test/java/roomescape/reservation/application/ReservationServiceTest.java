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
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.WaitingReservation;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.TestFixture.MIA_NAME;
import static roomescape.TestFixture.MIA_RESERVATION;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;
import static roomescape.TestFixture.TOMMY_NAME;
import static roomescape.TestFixture.TOMMY_RESERVATION;
import static roomescape.TestFixture.TOMMY_RESERVATION_DATE;
import static roomescape.TestFixture.TOMMY_RESERVATION_TIME;
import static roomescape.TestFixture.USER_MIA;
import static roomescape.TestFixture.USER_TOMMY;
import static roomescape.TestFixture.WOOTECO_THEME;
import static roomescape.TestFixture.WOOTECO_THEME_NAME;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

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
        Reservation reservation = MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING);

        // when
        Reservation createdReservation = reservationService.create(reservation);

        // then
        assertSoftly(softly -> {
            softly.assertThat(createdReservation.getId()).isNotNull();
            softly.assertThat(createdReservation.getStatus()).isEqualTo(BOOKING);
        });
    }

    @ParameterizedTest
    @MethodSource("invalidReservationDate")
    @DisplayName("예약 날짜는 현재 날짜 이후이다.")
    void validateDate(LocalDate invalidDate) {
        // given
        Reservation reservation = new Reservation(
                USER_MIA(), invalidDate, new ReservationTime(MIA_RESERVATION_TIME), WOOTECO_THEME(), BOOKING);

        // when & then
        assertThatThrownBy(() -> reservationService.create(reservation))
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
    void createWithOverflowCapacity() {
        // given
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));

        Reservation duplicatedReservation = new Reservation(
                tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING);

        // when & then
        assertThatThrownBy(() -> reservationService.create(duplicatedReservation))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("동일한 테마, 날짜, 시간에 예약이 있다면 대기 예약을 한다.")
    void createWaitingReservation() {
        // given
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));

        Reservation waitingReservation = new Reservation(
                tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING);

        // when
        Reservation createdReservation = reservationService.create(waitingReservation);

        // then
        assertSoftly(softly -> {
            softly.assertThat(createdReservation.getId()).isNotNull();
            softly.assertThat(createdReservation.getStatus()).isEqualTo(WAITING);
        });
    }

    @Test
    @DisplayName("동일한 테마, 날짜, 시간에 예약이 없다면 대기할 수 없다.")
    void createInvalidWaitingReservation() {
        // given
        Reservation invalidWaitingReservation = new Reservation(
                tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING);

        // when & then
        assertThatThrownBy(() -> reservationService.create(invalidWaitingReservation))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("사용자는 중복된 예약을 할 수 없다.")
    void createDuplicatedReservation() {
        // given
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));

        // when & then
        assertThatThrownBy(() -> reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING)))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // given
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        reservationService.create(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy, BOOKING));

        // when
        List<Reservation> reservations = reservationService.findAll();

        // then
        assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(2)
                    .extracting(Reservation::getMemberName)
                    .contains(MIA_NAME, TOMMY_NAME);
            softly.assertThat(reservations).extracting(Reservation::getTime)
                    .extracting(ReservationTime::getStartAt)
                    .contains(MIA_RESERVATION_TIME, TOMMY_RESERVATION_TIME);
            softly.assertThat(reservations).extracting(Reservation::getTheme)
                    .extracting(Theme::getName)
                    .contains(WOOTECO_THEME_NAME, WOOTECO_THEME_NAME);
        });
    }

    @Test
    @DisplayName("예약자, 테마, 날짜로 예약 목록을 조회한다.")
    void findAllByMemberIdAndThemeIdAndDateBetween() {
        // given
        Reservation miaReservation = reservationService.create(
                MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        Reservation tommyReservation = reservationService.create(
                TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy, BOOKING));

        // when
        List<Reservation> reservations = reservationService.findReservationsByMemberIdAndThemeIdAndDateBetween(
                miaReservation.getMember().getId(), miaReservation.getTheme().getId(),
                miaReservation.getDate(), tommyReservation.getDate());

        // then
        assertThat(reservations).hasSize(1)
                .extracting(Reservation::getMemberName)
                .containsExactly(MIA_NAME);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void delete() {
        // given
        Reservation reservation = reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));

        // when & then
        assertThatCode(() -> reservationService.deleteReservation(reservation.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약을 삭제하면 첫 번째 대기 예약 상태가 예약 중으로 바뀐다.")
    void deleteAndChangeToBooking() {
        // given
        Reservation reservationInBooking = reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        Reservation reservationInWaiting = reservationService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING));

        // when
        reservationService.deleteReservation(reservationInBooking.getId());

        // then
        List<Reservation> changedBookings = reservationService.findReservationsInBookingByMember(tommy);
        List<WaitingReservation> changedWaitings = reservationService.findWaitingReservationsWithPreviousCountByMember(tommy);
        assertSoftly(softly -> {
            softly.assertThat(changedBookings).hasSize(1)
                    .extracting(Reservation::getId)
                    .contains(reservationInWaiting.getId());
            softly.assertThat(changedWaitings).hasSize(0);
        });
    }

    @Test
    @DisplayName("사용자의 예약 목록을 조회한다.")
    void findAllInBookingByMember() {
        // given
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        reservationService.create(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy, BOOKING));

        // when
        List<Reservation> reservations = reservationService.findReservationsInBookingByMember(mia);

        // then
        assertThat(reservations).hasSize(1)
                .extracting(Reservation::getMember)
                .extracting(Member::getId)
                .contains(mia.getId());
    }

    @Test
    @DisplayName("사용자의 대기 예약 목록을 이전 대기 갯수와 함께 조회한다.")
    void findAllInWaitingWithPreviousCountByMember() {
        // given
        reservationService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING));
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING));

        // when
        List<WaitingReservation> waitingReservations = reservationService.findWaitingReservationsWithPreviousCountByMember(mia);

        // then
        assertThat(waitingReservations).hasSize(1)
                .extracting(WaitingReservation::getPreviousCount)
                .contains(0L);
    }

    @Test
    @DisplayName("사용자 본인의 대기 예약을 취소한다.")
    void deleteMyWaitingReservation() {
        // given
        reservationService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING));

        Reservation waitingReservation = reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING));

        // when
        reservationService.deleteWaitingReservationByMember(waitingReservation.getId(), mia);

        // then
        List<WaitingReservation> waitingReservations = reservationService.findWaitingReservationsWithPreviousCountByMember(mia);
        assertThat(waitingReservations).hasSize(0);
    }

    @Test
    @DisplayName("다른 사용자의 대기 예약을 취소할 수 없다.")
    void deleteMyWaitingReservationWithoutOwnerShip() {
        // given
        reservationService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING));

        Reservation miaWaitingReservation = reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING));

        // when & then
        assertThatThrownBy(() -> reservationService.deleteWaitingReservationByMember(miaWaitingReservation.getId(), tommy))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("사용자는 확정된 예약을 취소할 수 없다.")
    void deleteMyReservationInBooking() {
        // given
        Reservation reservation = reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));

        // when & then
        assertThatThrownBy(() -> reservationService.deleteWaitingReservationByMember(reservation.getId(), mia))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("대기 중인 모든 예약 목록을 조회한다.")
    void findAllInWaitingWithDetails() {
        // given
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        reservationService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING));

        reservationService.create(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy, BOOKING));
        reservationService.create(new Reservation(mia, TOMMY_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING));

        // when
        List<Reservation> reservations = reservationService.findWaitingReservations();

        // then
        assertThat(reservations).hasSize(2)
                .extracting(Reservation::getMemberName)
                .contains(TOMMY_NAME, MIA_NAME);
    }
}
