package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.auth.web.exception.NotAuthorizationException;
import roomescape.common.CleanUp;
import roomescape.fixture.db.MemberDbFixture;
import roomescape.fixture.db.ReservationDateTimeDbFixture;
import roomescape.fixture.db.ReservationTimeDbFixture;
import roomescape.fixture.db.ThemeDbFixture;
import roomescape.fixture.entity.ReservationDateFixture;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

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
    @Autowired
    private WaitingRepository waitingRepository;

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

        reservationService.delete(reservation.getId());

        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    void 존재하지_않는_예약을_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationService.delete(1L))
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
                .isInstanceOf(NotFoundException.class)
                .hasMessage("예약 시간을 찾을 수 없습니다.");
    }

    @Test
    void 예약자가_취소하면_대기_예약자가_예약이_된다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        // 유저1이 예약
        reservationRepository.save(
                Reservation.builder()
                        .reserver(유저1)
                        .reservationDateTime(내일_열시)
                        .theme(공포)
                        .build()
        );

        waitingRepository.save(Waiting.builder()
                .reservationDateTime(내일_열시)
                .reserver(유저2)
                .theme(공포)
                .build());

        // when
        reservationService.delete(유저1.getId());

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations)
                .anyMatch(r -> r.getReserver().getId().equals(유저2.getId())
                        && r.getReservationDatetime().getTimeId().equals(내일_열시.getTimeId())
                        && r.getReservationDatetime().getDate().equals(내일_열시.getDate())
                        && r.getTheme().getId().equals(공포.getId()));
    }

    @Test
    void 사용자가_예약한_예약을_삭제한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .reserver(유저1)
                        .reservationDateTime(내일_열시)
                        .theme(공포)
                        .build()
        );

        // when
        reservationService.deleteByUser(reservation.getId(), 유저1.getId());

        // then
        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    void 사용자가_예약한_예약이_없으면_예외가_발생한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();

        // when & then
        assertThatThrownBy(() -> reservationService.deleteByUser(1L, 유저1.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 해당_예약자가_아닌_유저가_삭제하면_예외를_발생한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .reserver(유저1)
                        .reservationDateTime(내일_열시)
                        .theme(공포)
                        .build()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.deleteByUser(reservation.getId(), 유저2.getId()))
                .isInstanceOf(NotAuthorizationException.class);
    }
}
