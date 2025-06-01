package roomescape.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.integration.fixture.ReservationDateFixture.예약날짜_오늘;

import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.ServiceTestBase;
import roomescape.global.dto.SessionMember;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationDateFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.integration.fixture.WaitingDbFixture;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.WaitingService;
import roomescape.service.request.WaitingCreateRequest;

class WaitingServiceTest extends ServiceTestBase {

    @Autowired
    private WaitingService sut;

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

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 대기를_생성할_수_있다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var request = new WaitingCreateRequest(예약날짜_오늘.date(), reservationTime.getId(), theme.getId());

        // when
        var response = sut.createWaiting(request, member.getId());

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
    void 이미_예약이_있다면_대기를_생성할_수_없다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        reservationDbFixture.예약_생성(예약날짜_오늘, reservationTime, theme, member);
        var request = new WaitingCreateRequest(예약날짜_오늘.date(), reservationTime.getId(), theme.getId());

        // when // then
        assertThatThrownBy(() -> sut.createWaiting(request, member.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 예약이 존재합니다.");
    }

    @Test
    void 이미_대기가_있다면_대기를_생성할_수_없다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var request = new WaitingCreateRequest(예약날짜_오늘.date(), reservationTime.getId(), theme.getId());
        sut.createWaiting(request, member.getId());

        // when // then
        assertThatThrownBy(() -> sut.createWaiting(request, member.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 대기가 존재합니다.");
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
        var all = sut.findAll();

        // then
        assertThat(all).hasSize(1);
    }

    @Test
    void 대기를_승인하면_예약으로_전환되고_대기는_삭제된다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var waiting = waitingDbFixture.대기_25_4_22(reservationTime, theme, member);

        // whenR
        sut.approveWaitingById(waiting.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
            softly.assertThat(reservationRepository.findAll()).hasSize(1);
        });
    }

    @Test
    void 대기를_승인할_때_이미_예약이_존재하면_예외가_발생한다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var schedule = ReservationDateFixture.예약날짜_오늘;

        reservationDbFixture.예약_생성(schedule, reservationTime, theme, member);
        var waiting = waitingDbFixture.대기_생성(schedule, reservationTime, theme, member, LocalDateTime.now(clock));

        // when // then
        assertThatThrownBy(() -> sut.approveWaitingById(waiting.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 예약이 존재합니다.");
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
        sut.deleteWaitingById(waiting.getId(), sessionMember);

        // then
        assertThat(sut.findAll()).isEmpty();
    }

    @Test
    void 대기_삭제_권한이_없으면_대기를_취소할_수_없다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu48888_지메일_어드민();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var request = new WaitingCreateRequest(예약날짜_오늘.date(), reservationTime.getId(), theme.getId());
        var response = sut.createWaiting(request, member.getId());
        var 다른_유저 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var sessionMember = new SessionMember(다른_유저.getId(), 다른_유저.getName(), 다른_유저.getRole());

        // when // then
        assertThatThrownBy(() -> sut.deleteWaitingById(response.id(), sessionMember))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 대기를 삭제할 권한이 없습니다.");
    }

    @Test
    void 내_대기_조회() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var waiting = waitingDbFixture.대기_25_4_22(reservationTime, theme, member);

        // when
        var all = sut.findAllMyWaiting(member.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(all).hasSize(1);
            var response = all.get(0);
            softly.assertThat(response.reservationId()).isEqualTo(waiting.getId());
            softly.assertThat(response.theme()).isEqualTo(waiting.getTheme().getName().name());
            softly.assertThat(response.date()).isEqualTo(waiting.getDate());
            softly.assertThat(response.time()).isEqualTo(waiting.getStartAt());
            softly.assertThat(response.status()).isEqualTo("1번째 예약 대기");
        });
    }
} 
