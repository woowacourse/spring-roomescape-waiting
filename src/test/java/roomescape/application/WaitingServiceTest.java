package roomescape.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.exception.DuplicateWaitingException;
import roomescape.common.BaseTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
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
import roomescape.presentation.dto.request.WaitingRequest;
import roomescape.presentation.dto.response.MemberResponse;
import roomescape.presentation.dto.response.ReservationTimeResponse;
import roomescape.presentation.dto.response.ThemeResponse;
import roomescape.presentation.dto.response.WaitingResponse;
import roomescape.presentation.dto.response.WaitingWithRank;

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

    @BeforeEach
    void setUp() {
        createReservation();
    }

    @Test
    void 예약_대기를_추가한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.제임스_사용자();

        WaitingRequest request = new WaitingRequest(
                ReservationDateFixture.예약날짜_25_4_22.getDate(),
                theme.getId(),
                reservationTime.getId()
        );
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), Role.USER, member.getEmail());

        WaitingResponse response = waitingService.createWaiting(request, loginMember);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.date()).isEqualTo(ReservationDateFixture.예약날짜_25_4_22.getDate()),
                () -> assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(reservationTime)),
                () -> assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme)),
                () -> assertThat(response.member()).isEqualTo(MemberResponse.from(member))
        );
    }

    @Test
    void 나의_예약_대기_목록을_조회한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.제임스_사용자();
        waitingDbFixture.예약_대기_제임스_25_4_22_10시_공포(member, reservationTime, theme);

        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), Role.USER, member.getEmail());

        List<WaitingWithRank> myWaitings = waitingService.getMyWaitingsWithRank(loginMember);
        WaitingWithRank waitingWithRank = myWaitings.getFirst();

        assertAll(
                () -> assertThat(myWaitings).hasSize(1),
                () -> assertThat(waitingWithRank.id()).isEqualTo(1L),
                () -> assertThat(waitingWithRank.date()).isEqualTo(ReservationDateFixture.예약날짜_25_4_22.getDate()),
                () -> assertThat(waitingWithRank.time()).isEqualTo(reservationTime.getStartAt()),
                () -> assertThat(waitingWithRank.themeName()).isEqualTo("공포")
        );
    }

    @Test
    void 예약_대기를_취소한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.제임스_사용자();
        Waiting waiting = waitingDbFixture.예약_대기_제임스_25_4_22_10시_공포(member, reservationTime, theme);

        waitingService.deleteWaiting(waiting.getId());

        List<WaitingResponse> allWaitings = waitingService.getAllWaitings();
        assertThat(allWaitings).hasSize(0);
    }

    @Test
    void 존재하지_않는_예약_대기를_취소할_경우_예외가_발생한다() {
        assertThatThrownBy(() -> waitingService.deleteWaiting(3L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("[ERROR] 예약 대기를 찾을 수 없습니다. : 3");
    }

    @Test
    void 예약_대기를_요청한_멤버가_다시_예약_대기를_시도할_경우_예외가_발생한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.제임스_사용자();
        WaitingRequest request = new WaitingRequest(
                ReservationDateFixture.예약날짜_25_4_22.getDate(),
                theme.getId(),
                reservationTime.getId()
        );
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), Role.USER, member.getEmail());

        waitingService.createWaiting(request, loginMember);

        assertThatThrownBy(() -> waitingService.createWaiting(request, loginMember))
                .isInstanceOf(DuplicateWaitingException.class)
                .hasMessage("[ERROR] 이미 예약 대기를 요청한 상태입니다.");
    }

    private Reservation createReservation() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.한스_사용자();

        return reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);
    }
}
