package roomescape.waiting.service;

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
import roomescape.auth.web.exception.NotAuthorizationException;
import roomescape.common.CleanUp;
import roomescape.fixture.db.MemberDbFixture;
import roomescape.fixture.db.ReservationDateTimeDbFixture;
import roomescape.fixture.db.ThemeDbFixture;
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
import roomescape.waiting.controller.response.WaitingInfoResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.exception.InAlreadyWaitingException;
import roomescape.waiting.repository.WaitingRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private MemberDbFixture memberDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;
    @Autowired
    private ReservationDateTimeDbFixture reservationDateTimeDbFixture;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private CleanUp cleanUp;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 대기_예약을_생성한다() {
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

        // when
        ReservationResponse response = waitingService.waiting(command);

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

        // 이미 예약이 되어 있다.
        reservationRepository.save(
                Reservation.builder()
                        .reserver(유저1)
                        .reservationDateTime(내일_열시)
                        .theme(공포)
                        .build()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.waiting(command))
                .isInstanceOf(InAlreadyReservationException.class);
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

        // when & then
        assertThatThrownBy(() -> waitingService.waiting(command))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("존재하지 않는 멤버입니다.");
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
        assertThatThrownBy(() -> waitingService.waiting(command))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 존재하지_않는_시간으로_대기_예약을_생성할_수_없다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();

        ReserveCommand command = new ReserveCommand(
                LocalDate.now().plusDays(1),
                공포.getId(),
                999L,
                유저1.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.waiting(command))
                .isInstanceOf(NotFoundException.class);
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

        // when & then
        assertThatThrownBy(() -> waitingService.waiting(command))
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

        waitingService.waiting(command);

        // when & then
        assertThatThrownBy(() -> waitingService.waiting(command))
                .isInstanceOf(InAlreadyWaitingException.class);
    }

    @Test
    void 나의_예약_순위를_알_수_있다() {
        // given
        Member 유저2 = memberDbFixture.유저2_생성();
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Waiting waitingByUser2 = Waiting.builder().
                reserver(유저2)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        // 유저2가 먼저 예약
        waitingRepository.save(waitingByUser2);

        // 유저1 예약
        Waiting waitingByUser1 = Waiting.builder().
                reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();

        waitingRepository.save(waitingByUser1);

        // when
        List<MyReservationResponse> result = waitingService.getWaitingReservations(유저1.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.getFirst().status()).isEqualTo("2");
        });
    }

    @Test
    void 대기_예약_목록을_모두_조회한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Waiting waiting1 = Waiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        Waiting waiting2 = Waiting.builder()
                .reserver(유저2)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();

        waitingRepository.save(waiting1);
        waitingRepository.save(waiting2);

        // when
        List<WaitingInfoResponse> result = waitingService.getAll();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.get(0).theme()).isEqualTo(공포.getName());
            softly.assertThat(result.get(1).theme()).isEqualTo(공포.getName());
            softly.assertThat(result)
                    .extracting(WaitingInfoResponse::name)
                    .containsExactlyInAnyOrder(유저1.getName(), 유저2.getName());
        });
    }

    @Test
    void 대기_예약을_삭제한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Waiting waiting = Waiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        waitingService.delete(savedWaiting.getId());

        // then
        assertThat(waitingRepository.existsById(savedWaiting.getId())).isFalse();
    }

    @Test
    void 존재하지_않는_대기_예약을_삭제할_수_없다() {
        // when & then
        assertThatThrownBy(() -> waitingService.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 예약 대기를 찾을 수 없습니다.");
    }

    @Test
    void 본인의_대기_예약을_삭제한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Waiting waiting = Waiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        waitingService.deleteByUser(savedWaiting.getId(), 유저1.getId());

        // then
        assertThat(waitingRepository.existsById(savedWaiting.getId())).isFalse();
    }

    @Test
    void 다른_사용자의_대기_예약을_삭제할_수_없다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Waiting waiting = Waiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        Waiting savedWaiting = waitingRepository.save(waiting);

        // when & then
        assertThatThrownBy(() -> waitingService.deleteByUser(savedWaiting.getId(), 유저2.getId()))
                .isInstanceOf(NotAuthorizationException.class);
    }
}
