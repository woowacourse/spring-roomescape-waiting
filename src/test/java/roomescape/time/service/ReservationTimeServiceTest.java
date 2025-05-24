package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.common.CleanUp;
import roomescape.fixture.db.MemberDbFixture;
import roomescape.fixture.db.ReservationDateTimeDbFixture;
import roomescape.fixture.db.ReservationTimeDbFixture;
import roomescape.fixture.db.ThemeDbFixture;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.controller.request.AvailableReservationTimeRequest;
import roomescape.time.controller.request.ReservationTimeCreateRequest;
import roomescape.time.controller.response.AvailableReservationTimeResponse;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;
    @Autowired
    private MemberDbFixture memberDbFixture;
    @Autowired
    private ReservationDateTimeDbFixture reservationDateTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 예약시간을_생성한다() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));

        ReservationTimeResponse response = reservationTimeService.open(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.startAt()).isEqualTo("10:00");
    }

    @Test
    void 이미_존재하는_시간은_추가할_수_없다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(reservationTime.getStartAt());

        assertThatThrownBy(() -> reservationTimeService.open(request)).isInstanceOf(InvalidArgumentException.class)
                .hasMessage("이미 존재하는 예약 시간입니다.");
    }

    @Test
    void 예약시간을_모두_조회한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();

        List<ReservationTimeResponse> responses = reservationTimeService.getAll();

        assertThat(responses.get(0).startAt()).isEqualTo(reservationTime.getStartAt().toString());
    }

    @Test
    void 예약시간을_삭제한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();

        reservationTimeService.deleteById(reservationTime.getId());

        assertThat(reservationTimeService.getAll()).hasSize(0);
    }

    @Test
    void 존재하지_않는_예약시간을_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("예약 시간을 찾을 수 없습니다.");
    }

    @Test
    void 이미_해당_시간의_예약이_존재한다면_삭제할_수_없다() {
        Member reserver = memberDbFixture.유저1_생성();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Theme theme = themeDbFixture.공포();

        Reservation reservation = reservationRepository.save(Reservation.reserve(reserver, reservationDateTime, theme));
        assertThatThrownBy(
                () -> reservationTimeService.deleteById(reservation.getReservationTime().getId())).isInstanceOf(
                InvalidArgumentException.class).hasMessage("해당 시간에 이미 예약이 존재하여 삭제할 수 없습니다.");
    }

    @Test
    void 예약_가능한_시간을_조회한다() {
        Member reserver = memberDbFixture.유저1_생성();
        ReservationDateTime reservationDateTime1 = reservationDateTimeDbFixture._7일전_열시();
        ReservationDateTime reservationDateTime2 = reservationDateTimeDbFixture.내일_열한시();
        Theme theme = themeDbFixture.공포();

        Reservation reservation1 = reservationRepository.save(
                Reservation.reserve(reserver, reservationDateTime1, theme));
        reservationRepository.save(Reservation.reserve(reserver, reservationDateTime2, theme));

        AvailableReservationTimeRequest request = new AvailableReservationTimeRequest(reservation1.getDate(),
                reservation1.getTimeId());

        List<AvailableReservationTimeResponse> responses = reservationTimeService.getAvailableReservationTimes(request);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(responses.get(0).id()).isNotNull();
        softly.assertThat(responses.get(0).startAt()).isEqualTo(reservation1.getReservationTime().getStartAt());
        softly.assertThat(responses.get(0).isReserved()).isEqualTo(true);
        softly.assertThat(responses.get(1).isReserved()).isEqualTo(false);

        softly.assertAll();
    }
}
