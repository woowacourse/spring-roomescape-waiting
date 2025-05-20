package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
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
import roomescape.fixture.entity.ReservationDateFixture;
import roomescape.fixture.db.ReservationDateTimeDbFixture;
import roomescape.fixture.db.ReservationTimeDbFixture;
import roomescape.fixture.db.ThemeDbFixture;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;
    @Autowired
    private MemberDbFixture memberDbFixture;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationDateTimeDbFixture reservationDateTimeDbFixture;

    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 예약을_생성한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();
        Theme theme = themeDbFixture.공포();
        Member reserver = memberDbFixture.유저1_생성();
        LocalDate date = ReservationDateFixture.예약날짜_내일.getDate();

        ReserveCommand command = new ReserveCommand(
                date,
                theme.getId(),
                reservationTime.getId(),
                reserver.getId()
        );

        // when
        ReservationResponse response = reservationService.reserve(command);

        assertThat(response.id()).isNotNull();
        assertThat(response.member().name()).isEqualTo(reserver.getName());
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(reservationTime));
    }

    @Test
    void 예약이_존재하면_예약을_생성할_수_없다() {
        Theme theme = themeDbFixture.공포();
        Member reserver = memberDbFixture.유저1_생성();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Reservation reservation = Reservation.reserve(
                reserver, reservationDateTime, theme
        );
        reservationRepository.save(reservation);

        ReserveCommand command = new ReserveCommand(
                reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getReservationTime().getId(),
                reservation.getReserver().getId()
        );

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("이미 예약이 존재하는 시간입니다.");
    }

    @Test
    void 예약을_삭제한다() {
        Member reserver = memberDbFixture.유저1_생성();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Theme theme = themeDbFixture.공포();

        Reservation reservation = reservationRepository.save(Reservation.reserve(reserver, reservationDateTime, theme));

        reservationService.deleteById(reservation.getId());

        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    void 존재하지_않는_예약을_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationService.deleteById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
    }


    @Test
    void 존재하지_않는_회원으로_예약할_수_없다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();
        Theme theme = themeDbFixture.공포();
        LocalDate date = ReservationDateFixture.예약날짜_내일.getDate();

        ReserveCommand command = new ReserveCommand(
                date,
                theme.getId(),
                reservationTime.getId(),
                999L
        );

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("존재하지 않는 멤버입니다.");
    }

    @Test
    void 존재하지_않는_테마로_예약할_수_없다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();
        Member reserver = memberDbFixture.유저1_생성();
        LocalDate date = ReservationDateFixture.예약날짜_내일.getDate();

        ReserveCommand command = new ReserveCommand(
                date,
                999L,
                reservationTime.getId(),
                reserver.getId()
        );

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 테마가 존재하지 않습니다.");
    }

    @Test
    void 존재하지_않는_시간으로_예약할_수_없다() {
        Theme theme = themeDbFixture.공포();
        Member reserver = memberDbFixture.유저1_생성();
        LocalDate date = ReservationDateFixture.예약날짜_내일.getDate();

        ReserveCommand command = new ReserveCommand(
                date,
                theme.getId(),
                999L,
                reserver.getId()
        );

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("예약 시간을 찾을 수 없습니다.");
    }
}
