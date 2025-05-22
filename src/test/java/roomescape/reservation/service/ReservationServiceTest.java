package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
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
import roomescape.fixture.entity.ReservationDateFixture;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.exception.InAlreadyReservationException;
import roomescape.reservation.exception.PastReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.exception.InAlreadyWaitingException;

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

        ReserveCommand command = new ReserveCommand(date, theme.getId(), reservationTime.getId(), reserver.getId());

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
        Reservation reservation = Reservation.reserve(reserver, reservationDateTime, theme);
        reservationRepository.save(reservation);

        ReserveCommand command = new ReserveCommand(reservation.getDate(), reservation.getTheme().getId(),
                reservation.getReservationTime().getId(), reservation.getReserver().getId());

        assertThatThrownBy(() -> reservationService.reserve(command)).isInstanceOf(InvalidArgumentException.class)
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
        assertThatThrownBy(() -> reservationService.delete(1L)).isInstanceOf(NotFoundException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
    }


    @Test
    void 존재하지_않는_회원으로_예약할_수_없다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();
        Theme theme = themeDbFixture.공포();
        LocalDate date = ReservationDateFixture.예약날짜_내일.getDate();

        ReserveCommand command = new ReserveCommand(date, theme.getId(), reservationTime.getId(), 999L);

        assertThatThrownBy(() -> reservationService.reserve(command)).isInstanceOf(InvalidArgumentException.class)
                .hasMessage("존재하지 않는 멤버입니다.");
    }

    @Test
    void 존재하지_않는_테마로_예약할_수_없다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();
        Member reserver = memberDbFixture.유저1_생성();
        LocalDate date = ReservationDateFixture.예약날짜_내일.getDate();

        ReserveCommand command = new ReserveCommand(date, 999L, reservationTime.getId(), reserver.getId());

        assertThatThrownBy(() -> reservationService.reserve(command)).isInstanceOf(NotFoundException.class)
                .hasMessage("해당 테마가 존재하지 않습니다.");
    }

    @Test
    void 존재하지_않는_시간으로_예약할_수_없다() {
        Theme theme = themeDbFixture.공포();
        Member reserver = memberDbFixture.유저1_생성();
        LocalDate date = ReservationDateFixture.예약날짜_내일.getDate();

        ReserveCommand command = new ReserveCommand(date, theme.getId(), 999L, reserver.getId());

        assertThatThrownBy(() -> reservationService.reserve(command)).isInstanceOf(NotFoundException.class)
                .hasMessage("예약 시간을 찾을 수 없습니다.");
    }

    @Test
    void 예약_대기와_함께_조회할_수_있다() {
        // given
        Member member = memberDbFixture.유저1_생성();
        Theme theme = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        // 예약 등록
        reservationRepository.save(Reservation.reserve(member, 내일_열시, theme));
        // 대기 등록
        reservationRepository.save(Reservation.waiting(member, 내일_열시, theme));

        // when
        List<MyReservationResponse> responses = reservationService.getAllReservations(member.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(r -> r.status().equals("예약"));
            assertThat(responses).anyMatch(r -> !r.status().equals("예약"));
        });
    }

    @Test
    void 내_예약_목록을_조회한다() {
        Member member1 = memberDbFixture.유저1_생성();
        Member member2 = memberDbFixture.유저2_생성();

        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Theme theme = themeDbFixture.공포();

        Reservation reservation1 = reservationRepository.save(Reservation.reserve(member1, reservationDateTime, theme));
        reservationRepository.save(Reservation.reserve(member2, reservationDateTime, theme));

        List<MyReservationResponse> myReservations = reservationService.getAllReservations(member1.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(myReservations).hasSize(1);
            softly.assertThat(myReservations.get(0).theme()).isEqualTo(reservation1.getTheme().getName());
            softly.assertThat(myReservations.get(0).date()).isEqualTo(reservation1.getDate());
            softly.assertThat(myReservations.get(0).time()).isEqualTo(reservation1.getStartAt());
        });
    }

    @Test
    void 존재하지_않는_회원의_예약목록을_조회하면_빈_리스트를_반환한다() {
        List<MyReservationResponse> myReservations = reservationService.getAllReservations(999L);

        assertThat(myReservations).isEmpty();
    }

    @Test
    void 대기_예약을_생성한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        ReserveCommand command = new ReserveCommand(
                내일_열시.getDate(),
                공포.getId(),
                내일_열시.getReservationTime().getId(),
                유저1.getId()
        );

        reservationRepository.save(Reservation.reserve(유저2, 내일_열시, 공포));

        // when
        ReservationResponse response = reservationService.waiting(command);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.id()).isNotNull();
            softly.assertThat(response.member().id()).isEqualTo(유저1.getId());
            softly.assertThat(response.member().name()).isEqualTo(유저1.getName());
            softly.assertThat(response.member().email()).isEqualTo(유저1.getEmail());
            softly.assertThat(response.theme().id()).isEqualTo(공포.getId());
            softly.assertThat(response.theme().name()).isEqualTo(공포.getName());
            softly.assertThat(response.date()).isEqualTo(내일_열시.getDate());
            softly.assertThat(response.time().id()).isEqualTo(내일_열시.getReservationTime().getId());
            softly.assertThat(response.time().startAt()).isEqualTo(내일_열시.getStartAt());
        });
    }

    @Test
    void 예약이_존재하지_않는다면_대기를_할_수_없다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        ReserveCommand command = new ReserveCommand(
                내일_열시.getDate(),
                공포.getId(),
                내일_열시.getReservationTime().getId(),
                유저1.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.waiting(command))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void 예약자는_예약_대기를_할_수_없다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        ReserveCommand command = new ReserveCommand(
                내일_열시.getDate(),
                공포.getId(),
                내일_열시.getReservationTime().getId(),
                유저1.getId()
        );

        reservationRepository.save(Reservation.reserve(유저1, 내일_열시, 공포));

        // when & then
        assertThatThrownBy(() -> reservationService.waiting(command))
                .isInstanceOf(InAlreadyReservationException.class);
    }

    @Test
    void 존재하지_않는_테마로_대기_예약을_생성할_수_없다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        ReserveCommand command = new ReserveCommand(
                내일_열시.getDate(),
                999L,
                내일_열시.getReservationTime().getId(),
                유저1.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.waiting(command))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void 과거_날짜로_대기_예약을_생성할_수_없다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 과거_시간 = reservationDateTimeDbFixture._7일전_열시();

        ReserveCommand command = new ReserveCommand(
                과거_시간.getDate(),
                공포.getId(),
                과거_시간.getReservationTime().getId(),
                유저1.getId()
        );

        Member 유저2 = memberDbFixture.유저2_생성();

        reservationRepository.save(Reservation.reserve(유저2, 과거_시간, 공포));

        // when & then
        assertThatThrownBy(() -> reservationService.waiting(command))
                .isInstanceOf(PastReservationException.class);
    }

    @Test
    void 같은_사용자가_동일한_예약_대기를_할_수_없다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        ReserveCommand command = new ReserveCommand(
                내일_열시.getDate(),
                공포.getId(),
                내일_열시.getReservationTime().getId(),
                유저1.getId()
        );

        Member 유저2 = memberDbFixture.유저2_생성();
        reservationRepository.save(Reservation.reserve(유저2, 내일_열시, 공포));

        reservationService.waiting(command);

        // when & then
        assertThatThrownBy(() -> reservationService.waiting(command))
                .isInstanceOf(InAlreadyWaitingException.class);
    }

    @Test
    void 존재하지_않는_회원으로_대기_예약을_생성할_수_없다() {
        // given
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        ReserveCommand command = new ReserveCommand(
                내일_열시.getDate(),
                공포.getId(),
                내일_열시.getReservationTime().getId(),
                999L
        );

        Member 유저2 = memberDbFixture.유저2_생성();

        reservationRepository.save(Reservation.reserve(유저2, 내일_열시, 공포));

        // when & then
        assertThatThrownBy(() -> reservationService.waiting(command))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("존재하지 않는 멤버입니다.");
    }
}
