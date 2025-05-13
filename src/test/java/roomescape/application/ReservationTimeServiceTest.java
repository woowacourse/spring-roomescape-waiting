package roomescape.application;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.BaseTest;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.fixture.MemberDbFixture;
import roomescape.fixture.ReservationDbFixture;
import roomescape.fixture.ReservationTimeDbFixture;
import roomescape.fixture.ThemeDbFixture;
import roomescape.presentation.dto.request.AvailableReservationTimeRequest;
import roomescape.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.presentation.dto.response.ReservationTimeResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTimeServiceTest extends BaseTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Test
    void 예약시간을_모두_조회한다() {
        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
        List<ReservationTimeResponse> responses = reservationTimeService.getReservationTimes();

        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses.getFirst().startAt()).isEqualTo(time.getStartAt().toString())
        );
    }

    @Test
    void 예약시간을_생성한다() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));

        ReservationTimeResponse response = reservationTimeService.createReservationTime(request);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.startAt()).isEqualTo(request.startAt().toString())
        );
    }

    @Test
    void 이미_존재하는_시간을_생성하면_예외가_발생한다() {
        reservationTimeDbFixture.예약시간_10시();
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(
                LocalTime.of(10, 0));

        assertThatThrownBy(() -> reservationTimeService.createReservationTime(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약시간을_삭제한다() {
        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
        reservationTimeService.deleteReservationTimeById(time.getId());

        List<ReservationTimeResponse> responses = reservationTimeService.getReservationTimes();

        assertThat(responses).hasSize(0);
    }

    @Test
    void 존재하지_않는_예약시간을_삭제하면_예외가_발생한다() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(3L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 이미_해당_시간의_예약이_존재할때_삭제하면_예외가_발생한다() {
        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.한스_사용자();
        reservationDbFixture.예약_한스_25_4_22_10시_공포(member, time, theme);

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(time.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 아이디로_시간을_조회한다() {
        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
        ReservationTime findTime = reservationTimeService.findReservationTimeById(time.getId());

        assertThat(time).isEqualTo(findTime);
    }

    @Test
    void 존재하지_않는_아이디로_조회하면_예외가_발생한다() {
        Long notExistId = 2L;
        assertThatThrownBy(() -> reservationTimeService.findReservationTimeById(notExistId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 예약가능한_시간을_조회한다() {
        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
        AvailableReservationTimeRequest request = new AvailableReservationTimeRequest(LocalDate.of(2025, 5, 20), time.getId());

        List<AvailableReservationTimeResponse> responses = reservationTimeService.getAvailableReservationTimes(request);

        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses.getFirst().startAt()).isEqualTo(time.getStartAt().toString())
        );
    }
}
