package roomescape.application;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.BaseTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationInfo;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.fixture.MemberDbFixture;
import roomescape.fixture.ReservationDateFixture;
import roomescape.fixture.ReservationDbFixture;
import roomescape.fixture.ReservationTimeDbFixture;
import roomescape.fixture.ThemeDbFixture;
import roomescape.fixture.WaitingDbFixture;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.ReservationCreateRequest;
import roomescape.presentation.dto.response.MemberResponse;
import roomescape.presentation.dto.response.ReservationTimeResponse;
import roomescape.presentation.dto.response.ThemeResponse;
import roomescape.presentation.dto.response.WaitingResponse;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingServiceTest extends BaseTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Autowired
    private WaitingDbFixture waitingDbFixture;

    @Test
    void 예약대기를_생성한다() {
        Member member = memberDbFixture.한스_사용자();
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);

        ReservationCreateRequest request = new ReservationCreateRequest(
                ReservationDateFixture.예약날짜_25_4_22.getDate(),
                reservationTime.getId(),
                theme.getId()
        );
        Member waitingMember = memberDbFixture.듀이_사용자();
        LoginMember loginMember = new LoginMember(waitingMember.getId(), waitingMember.getName(), Role.USER, waitingMember.getEmail());

        WaitingResponse response = waitingService.createWaiting(request, loginMember);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.date()).isEqualTo(ReservationDateFixture.예약날짜_25_4_22.getDate()),
                () -> assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(reservationTime)),
                () -> assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme)),
                () -> assertThat(response.member()).isEqualTo(MemberResponse.from(waitingMember))
        );
    }

    @Test
    void 과거_시간으로_예약대기하면_예외가_발생한다() {
        Member member = memberDbFixture.한스_사용자();
        ReservationDate reservationDate = ReservationDateFixture.예약날짜_7일전;
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        reservationDbFixture.예약_생성(member, reservationDate, reservationTime, theme);

        ReservationCreateRequest request = new ReservationCreateRequest(
                reservationDate.getDate(),
                reservationTime.getId(),
                theme.getId()
        );
        Member waitingMember = memberDbFixture.듀이_사용자();
        LoginMember loginMember = new LoginMember(waitingMember.getId(), waitingMember.getName(), Role.USER, waitingMember.getEmail());

        assertThatThrownBy(() -> waitingService.createWaiting(request, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약한_날짜_테마_시간에_예약대기하면_예외가_발생한다() {
        Member member = memberDbFixture.한스_사용자();
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);

        ReservationCreateRequest request = new ReservationCreateRequest(
                ReservationDateFixture.예약날짜_25_4_22.getDate(),
                reservationTime.getId(),
                theme.getId()
        );
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), Role.USER, member.getEmail());

        assertThatThrownBy(() -> waitingService.createWaiting(request, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미_예약대기한_날짜_테마_시간에_예약대기하면_예외가_발생한다() {
        Member member = memberDbFixture.한스_사용자();
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);

        ReservationCreateRequest request = new ReservationCreateRequest(
                ReservationDateFixture.예약날짜_25_4_22.getDate(),
                reservationTime.getId(),
                theme.getId()
        );
        Member waitingMember = memberDbFixture.듀이_사용자();
        LoginMember loginMember = new LoginMember(waitingMember.getId(), waitingMember.getName(), Role.USER, waitingMember.getEmail());
        waitingService.createWaiting(request, loginMember);

        assertThatThrownBy(() -> waitingService.createWaiting(request, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 사용자의_모든_예약을_조회한다() {
        Member member = memberDbFixture.한스_사용자();
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        Member alreadyReservedMember = memberDbFixture.듀이_사용자();
        Reservation alreadyReservedReservation = reservationDbFixture.예약_생성(alreadyReservedMember, ReservationDateFixture.예약날짜_25_4_23, reservationTime, theme);
        ReservationInfo reservationInfo = ReservationInfo.create(alreadyReservedReservation);

        waitingDbFixture.대기_25_4_23_10시_공포(reservationInfo, member);

        List<Waiting> waitings = waitingService.findWaitingsByMember(member);

        assertThat(waitings).hasSize(1);
    }

    @Test
    void 예약대기를_취소한다() {
        Member member = memberDbFixture.한스_사용자();
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        Member alreadyReservedMember = memberDbFixture.듀이_사용자();
        Reservation alreadyReservedReservation = reservationDbFixture.예약_생성(alreadyReservedMember, ReservationDateFixture.예약날짜_25_4_23, reservationTime, theme);
        ReservationInfo reservationInfo = ReservationInfo.create(alreadyReservedReservation);

        Waiting waiting = waitingDbFixture.대기_25_4_23_10시_공포(reservationInfo, member);

        waitingService.deleteWaitingById(waiting.getId());

        List<Waiting> waitings = waitingService.findWaitingsByMember(member);

        assertThat(waitings).isEmpty();
    }

    @Test
    void 존재하지_않는_예약대기를_취소하면_예외가_발생한다() {
        assertThatThrownBy(() -> waitingService.deleteWaitingById(3L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 예약대기를_모두_조회한다() {
        Member member = memberDbFixture.한스_사용자();
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        Member alreadyReservedMember = memberDbFixture.듀이_사용자();
        Reservation alreadyReservedReservation = reservationDbFixture.예약_생성(alreadyReservedMember, ReservationDateFixture.예약날짜_25_4_23, reservationTime, theme);
        ReservationInfo reservationInfo = ReservationInfo.create(alreadyReservedReservation);

        waitingDbFixture.대기_25_4_23_10시_공포(reservationInfo, member);

        List<WaitingResponse> waitings = waitingService.getWaitings();
        WaitingResponse response = waitings.getFirst();

        assertAll(
                () -> assertThat(waitings).hasSize(1),
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.date()).isEqualTo(ReservationDateFixture.예약날짜_25_4_23.getDate()),
                () -> assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(reservationTime)),
                () -> assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme)),
                () -> assertThat(response.member()).isEqualTo(MemberResponse.from(member))
        );
    }

    @Test
    void 예약대기가_존재하면_true를_반환한다() {
        Member member = memberDbFixture.한스_사용자();
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        Member alreadyReservedMember = memberDbFixture.듀이_사용자();
        Reservation alreadyReservedReservation = reservationDbFixture.예약_생성(alreadyReservedMember, ReservationDateFixture.예약날짜_25_4_23, reservationTime, theme);
        ReservationInfo reservationInfo = ReservationInfo.create(alreadyReservedReservation);

        waitingDbFixture.대기_25_4_23_10시_공포(reservationInfo, member);

        assertThat(waitingService.existsWaitings(reservationInfo)).isTrue();
    }

    @Test
    void 예약대기가_존재하지_않으면_false를_반환한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        Member alreadyReservedMember = memberDbFixture.듀이_사용자();
        Reservation alreadyReservedReservation = reservationDbFixture.예약_생성(alreadyReservedMember, ReservationDateFixture.예약날짜_25_4_23, reservationTime, theme);
        ReservationInfo reservationInfo = ReservationInfo.create(alreadyReservedReservation);

        assertThat(waitingService.existsWaitings(reservationInfo)).isFalse();
    }

    @Test
    void 예약대기의_첫번째_순위를_조회한다() {
        Member member = memberDbFixture.한스_사용자();
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        Member alreadyReservedMember = memberDbFixture.듀이_사용자();
        Reservation alreadyReservedReservation = reservationDbFixture.예약_생성(alreadyReservedMember, ReservationDateFixture.예약날짜_25_4_23, reservationTime, theme);
        ReservationInfo reservationInfo = ReservationInfo.create(alreadyReservedReservation);

        Waiting waiting = waitingDbFixture.대기_25_4_23_10시_공포(reservationInfo, member);

        assertThat(waitingService.findFirstRankWaitingByReservationInfo(reservationInfo)).isEqualTo(waiting);
    }

    @Test
    @Transactional
    void 예약대기의_순번을_업데이트한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        Member alreadyReservedMember = memberDbFixture.듀이_사용자();
        Reservation alreadyReservedReservation = reservationDbFixture.예약_생성(alreadyReservedMember, ReservationDateFixture.예약날짜_25_4_23, reservationTime, theme);
        ReservationInfo reservationInfo = ReservationInfo.create(alreadyReservedReservation);

        Member firstMember = memberDbFixture.한스_사용자();
        Waiting waiting = waitingDbFixture.대기_25_4_23_10시_공포(reservationInfo, firstMember);
        Reservation newReservation = reservationDbFixture.예약_생성(firstMember, ReservationDateFixture.예약날짜_25_4_23, reservationTime, theme);
        ReservationInfo newReservationInfo = ReservationInfo.create(newReservation);

        waitingService.updateWaitings(reservationInfo, newReservationInfo);

        assertThat(waiting.getRank()).isEqualTo(0L);
    }
}
