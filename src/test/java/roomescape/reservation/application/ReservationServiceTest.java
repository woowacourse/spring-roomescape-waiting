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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
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
        Reservation createdReservation = reservationService.create(reservation);

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
    void createDuplicatedReservation() {
        // given
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia));

        Reservation duplicatedReservation = new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme);

        // when & then
        assertThatThrownBy(() -> reservationService.create(duplicatedReservation))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // given
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia));
        reservationService.create(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy));

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
        Reservation miaReservation = reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia));
        Reservation tommyReservation = reservationService.create(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy));

        // when
        List<Reservation> reservations = reservationService.findAllByMemberIdAndThemeIdAndDateBetween(
                miaReservation.getMemberId(), miaReservation.getThemeId(), miaReservation.getDate(), tommyReservation.getDate());

        // then
        assertThat(reservations).hasSize(1)
                .extracting(Reservation::getMemberName)
                .containsExactly(MIA_NAME);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void delete() {
        // given
        Reservation reservation = reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia));

        // when & then
        assertThatCode(() -> reservationService.delete(reservation.getId()))
                .doesNotThrowAnyException();
    }
}
