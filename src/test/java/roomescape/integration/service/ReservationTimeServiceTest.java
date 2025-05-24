package roomescape.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.common.Constant.FIXED_CLOCK;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.ServiceTestBase;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.service.ReservationTimeService;
import roomescape.service.request.AvailableReservationTimeRequest;
import roomescape.service.request.CreateReservationTimeRequest;

class ReservationTimeServiceTest extends ServiceTestBase {

    @Autowired
    private ReservationTimeService service;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Test
    void 예약시간을_생성할_수_있다() {
        // given
        var startAt = LocalTime.of(10, 0);
        var request = new CreateReservationTimeRequest(startAt);

        // when
        var response = service.createReservationTime(request);

        // then
        assertThat(response.startAt()).isEqualTo(startAt);
    }

    @Test
    void 중복된_예약시간은_생성할_수_없다() {
        // given
        var startAt = LocalTime.of(10, 0);
        reservationTimeDbFixture.예약시간(startAt);
        var request = new CreateReservationTimeRequest(startAt);

        // when // then
        assertThatThrownBy(() -> service.createReservationTime(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 모든_예약시간을_조회할_수_있다() {
        // given
        reservationTimeDbFixture.예약시간_10시();
        reservationTimeDbFixture.예약시간(LocalTime.of(11, 0));

        // when
        var result = service.findAllReservationTimes();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 예약이_없는_시간은_삭제할_수_있다() {
        // given
        var time = reservationTimeDbFixture.예약시간_10시();

        // when
        service.deleteReservationTimeById(time.getId());

        // then
        assertThat(service.findAllReservationTimes()).isEmpty();
    }

    @Test
    void 예약이_존재하는_시간은_삭제할_수_없다() {
        // given
        var time = reservationTimeDbFixture.예약시간_10시();
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationDateTime = new ReservationDateTime(
                new ReservationDate(LocalDate.of(2025, 5, 5)), time, FIXED_CLOCK);
        var theme = themeDbFixture.공포();
        reservationDbFixture.예약_생성(reservationDateTime.getReservationDate(), time, theme, member);

        // when // then
        assertThatThrownBy(() -> service.deleteReservationTimeById(time.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_예약시간은_삭제할_수_없다() {
        // when // then
        assertThatThrownBy(() -> service.deleteReservationTimeById(1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 예약시간을_ID로_조회할_수_있다() {
        // given
        var time = reservationTimeDbFixture.예약시간_10시();

        // when
        var found = service.getReservationTime(time.getId());

        // then
        assertThat(found.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 예약시간_ID로_조회시_없으면_예외() {
        // when // then
        assertThatThrownBy(() -> service.getReservationTime(1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 예약가능한_시간들을_조회할_수_있다() {
        // given
        reservationTimeDbFixture.예약시간_10시();
        reservationTimeDbFixture.예약시간_11시();

        // when
        var result = service.findAvailableReservationTimes(
                new AvailableReservationTimeRequest(LocalDate.of(2025, 5, 5), 1L));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.get(0).startAt()).isEqualTo(LocalTime.of(10, 0));
            softly.assertThat(result.get(0).isReserved()).isFalse();
        });
    }
}
