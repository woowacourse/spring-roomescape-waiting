package roomescape.reservation.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.ServiceTest;
import roomescape.global.exception.ViolationException;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.response.AvailableReservationTimeResponse;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.*;
import static roomescape.TestFixture.MIA_RESERVATION;

class ReservationTimeServiceTest extends ServiceTest {
    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void create() {
        // given
        ReservationTime reservationTime = new ReservationTime(MIA_RESERVATION_TIME);

        // when
        ReservationTime createdReservationTime = reservationTimeService.create(reservationTime);

        // then
        assertThat(createdReservationTime.getId()).isNotNull();
    }

    @Test
    @DisplayName("예약 시간 목록을 조회한다.")
    void findAll() {
        // given
        reservationTimeService.create(new ReservationTime(MIA_RESERVATION_TIME));

        // when
        List<ReservationTime> reservationTimes = reservationTimeService.findAll();

        // then
        assertThat(reservationTimes).hasSize(1)
                .extracting(ReservationTime::getStartAt)
                .contains(MIA_RESERVATION_TIME);
    }

    @Test
    @DisplayName("예약 시간을 삭제한다.")
    void delete() {
        // given
        ReservationTime reservationTime = reservationTimeService.create(new ReservationTime(MIA_RESERVATION_TIME));

        // when & then
        assertThatCode(() -> reservationTimeService.delete(reservationTime.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("삭제하려는 예약 시간에 예약이 존재할 경우 예외가 발생한다.")
    void validateHasReservation() {
        // given
        ReservationTime reservationTime = reservationTimeService.create(new ReservationTime(MIA_RESERVATION_TIME));
        Theme theme = themeService.create(WOOTECO_THEME());
        Member member = memberService.create(USER_MIA());
        reservationService.create(MIA_RESERVATION(reservationTime, theme, member));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(reservationTime.getId()))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("선택한 날짜와 테마로 예약 가능한 시간 목록을 조회한다.")
    void findAvailableReservationTimes() {
        // given
        ReservationTime miaReservationTime = reservationTimeService.create(new ReservationTime(MIA_RESERVATION_TIME));
        Theme theme = themeService.create(WOOTECO_THEME());
        Member mia = memberService.create(USER_MIA());
        Reservation miaReservation = reservationService.create(MIA_RESERVATION(miaReservationTime, theme, mia));

        reservationTimeService.create(new ReservationTime(LocalTime.of(16, 0)));

        // when
        List<AvailableReservationTimeResponse> availableReservationTimes
                = reservationTimeService.findAvailableReservationTimes(MIA_RESERVATION_DATE, miaReservation.getThemeId());

        // then
        assertAll(() -> {
            assertThat(isReserved(availableReservationTimes, MIA_RESERVATION_TIME)).isTrue();
            assertThat(isReserved(availableReservationTimes, LocalTime.of(16, 0))).isFalse();
        });
    }

    private boolean isReserved(List<AvailableReservationTimeResponse> availableReservationTimes, LocalTime time) {
        return availableReservationTimes.stream()
                .filter(response -> response.startAt().equals(time))
                .findFirst()
                .get()
                .isReserved();
    }
}
