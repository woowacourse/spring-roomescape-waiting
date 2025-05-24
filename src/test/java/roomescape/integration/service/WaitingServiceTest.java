package roomescape.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.integration.fixture.ReservationDateFixture.예약날짜_오늘;

import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.ClockConfig;
import roomescape.global.dto.SessionMember;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationDateFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.integration.fixture.WaitingDbFixture;
import roomescape.service.WaitingService;
import roomescape.service.request.WaitingCreateRequest;

@Transactional
@SpringBootTest
@Import(ClockConfig.class)
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private WaitingDbFixture waitingDbFixture;

    @Autowired
    private Clock clock;

    @Test
    void 대기를_생성할_수_있다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var request = new WaitingCreateRequest(예약날짜_오늘.date(), reservationTime.getId(), theme.getId());

        // when
        var response = waitingService.createWaiting(request, member.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.id()).isNotNull();
            softly.assertThat(response.name()).isEqualTo(member.getName().name());
            softly.assertThat(response.date()).isEqualTo(예약날짜_오늘.date());
            softly.assertThat(response.time().id()).isEqualTo(reservationTime.getId());
            softly.assertThat(response.theme().id()).isEqualTo(theme.getId());
        });
    }

    @Test
    void 중복된_예약이나_대기는_생성할_수_없다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var request = new WaitingCreateRequest(예약날짜_오늘.date(), reservationTime.getId(), theme.getId());
        waitingService.createWaiting(request, member.getId());

        // when // then
        assertThatThrownBy(() -> waitingService.createWaiting(request, member.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 모든_대기를_조회할_수_있다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        waitingDbFixture.대기_생성(
                ReservationDateFixture.예약날짜_오늘,
                reservationTime,
                theme,
                member,
                LocalDateTime.now(clock)
        );

        // when
        var all = waitingService.findAll();

        // then
        assertThat(all).hasSize(1);
    }

    @Test
    void 대기를_삭제할_수_있다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var waiting = waitingDbFixture.대기_생성(
                ReservationDateFixture.예약날짜_오늘,
                reservationTime,
                theme,
                member,
                LocalDateTime.now(clock)
        );
        var sessionMember = new SessionMember(member.getId(), member.getName(), member.getRole());

        // when
        waitingService.deleteWaitingById(waiting.getId(), sessionMember);

        // then
        assertThat(waitingService.findAll()).isEmpty();
    }

    @Test
    void 대기_삭제_권한이_없으면_대기를_취소할_수_없다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var request = new WaitingCreateRequest(예약날짜_오늘.date(), reservationTime.getId(), theme.getId());
        var response = waitingService.createWaiting(request, member.getId());
        var 다른_유저 = memberDbFixture.leehyeonsu4888_지메일_gustn111느낌표두개();
        var sessionMember = new SessionMember(다른_유저.getId(), 다른_유저.getName(), 다른_유저.getRole());

        // when // then
        assertThatThrownBy(() -> waitingService.deleteWaitingById(response.id(), sessionMember))
                .isInstanceOf(IllegalStateException.class);
    }
} 
