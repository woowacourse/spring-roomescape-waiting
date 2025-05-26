package roomescape.integration.service;

import static org.assertj.core.api.Assertions.*;
import static roomescape.integration.fixture.ReservationDateFixture.예약날짜_25_4_22;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.ClockConfig;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWait;
import roomescape.domain.reservation.schedule.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationScheduleDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ReservationWaitDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitRepository;
import roomescape.service.ReservationWaitService;
import roomescape.service.request.CreateReservationWaitRequest;
import roomescape.service.response.MyReservationWaitResponse;
import roomescape.service.response.ReservationResponse;
import roomescape.service.response.ReservationWaitResponse;

@Transactional
@SpringBootTest
@Import(ClockConfig.class)
class ReservationWaitServiceTest {

    @Autowired
    private ReservationWaitService service;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Autowired
    private ReservationWaitDbFixture waitDbFixture;

    @Autowired
    private ReservationWaitRepository waitRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Member member;
    private ReservationTime time;
    private Theme theme;
    private ReservationSchedule schedule;

    @BeforeEach
    void setUp(
            @Autowired ReservationScheduleDbFixture reservationScheduleDbFixture,
            @Autowired ReservationTimeDbFixture reservationTimeDbFixture,
            @Autowired ThemeDbFixture themeDbFixture
    ) {
        member = memberDbFixture.leehyeonsu4888_지메일_gustn111느낌표두개();
        time = reservationTimeDbFixture.예약시간_10시();
        theme = themeDbFixture.공포();
        schedule = reservationScheduleDbFixture.createSchedule(예약날짜_25_4_22, time, theme);
    }

    @Test
    void 예약대기를_생성할_수_있다() {
        // given
        reservationDbFixture.예약_생성(schedule, member); // 예약 있어야 대기 가능

        CreateReservationWaitRequest request = new CreateReservationWaitRequest(
                schedule.getDate(), time.getId(), theme.getId());

        // when
        ReservationWaitResponse response = service.createReservationWait(request, member.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.name()).isEqualTo(member.getName().name());
            softly.assertThat(response.rank()).isEqualTo(1L);
        });
    }

    @Test
    void 예약이_없는_스케줄에는_예약대기를_생성할_수_없다() {
        // given
        CreateReservationWaitRequest request = new CreateReservationWaitRequest(
                예약날짜_25_4_22.date(), time.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> service.createReservationWait(request, member.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 예약대기를_승인하면_예약이_생성되고_대기는_삭제된다() {
        // given
        ReservationWait wait = waitDbFixture.createReservationWait(schedule, member);

        // when
        ReservationResponse response = service.approveReservationWait(wait.getId());

        // then
        Optional<Reservation> optionalReservation = reservationRepository.findById(response.id());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(optionalReservation).isPresent();
            softly.assertThat(waitRepository.findById(wait.getId())).isNotPresent();
        });
    }

    @Test
    void 예약대기_승인시_이미_예약이_있으면_예외() {
        // given
        reservationDbFixture.예약_생성(schedule, member);
        ReservationWait wait = waitDbFixture.createReservationWait(schedule, member);

        // when & then
        assertThatThrownBy(() -> service.approveReservationWait(wait.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 본인_예약대기를_삭제할_수_있다() {
        // given
        ReservationWait wait = waitDbFixture.createReservationWait(schedule, member);

        // when
        service.deleteReservationWait(wait.getId(), member.getId());

        // then
        assertThat(waitRepository.findById(wait.getId())).isNotPresent();
    }

    @Test
    void 다른_사람_예약대기를_삭제하면_예외() {
        // given
        Member anotherMember = memberDbFixture.leehyeonsu48888_지메일_gustn111느낌표두개_어드민();
        ReservationWait wait = waitDbFixture.createReservationWait(schedule, member);

        // when & then
        assertThatThrownBy(() -> service.deleteReservationWait(wait.getId(), anotherMember.getId()))
                .isInstanceOf(roomescape.global.exception.AccessDeniedException.class);
    }

    @Test
    void 내_모든_예약대기를_조회할_수_있다() {
        // given
        waitDbFixture.createReservationWait(schedule, member);

        // when
        List<MyReservationWaitResponse> responses = service.findAllMyWaitReservation(member.getId());

        // then
        assertThat(responses).hasSize(1);
    }

    @Test
    void 모든_예약대기를_조회할_수_있다() {
        // given
        waitDbFixture.createReservationWait(schedule, member);

        // when
        List<ReservationWaitResponse> responses = service.getAllWaitReservation();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(1);
            softly.assertThat(responses.get(0).name()).isEqualTo(member.getName().name());
        });
    }
}
