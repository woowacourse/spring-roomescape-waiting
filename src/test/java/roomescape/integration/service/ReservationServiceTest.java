package roomescape.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.common.Constant.FIXED_CLOCK;
import static roomescape.integration.fixture.ReservationDateFixture.예약날짜_오늘;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.ServiceTestBase;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.service.ReservationService;
import roomescape.service.request.ReservationCreateRequest;

class ReservationServiceTest extends ServiceTestBase {

    @Autowired
    private ReservationService service;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    private final LocalDate today = LocalDate.now();
    private final LocalTime time = LocalTime.of(10, 0);

    @Test
    void 모든_예약을_조회한다() {
        // given
        var reservationTime = reservationTimeDbFixture.예약시간(time);
        var theme = themeDbFixture.공포();
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationDateTime = new ReservationDateTime(예약날짜_오늘, reservationTime, FIXED_CLOCK);
        reservationDbFixture.예약_생성(reservationDateTime.getReservationDate(), reservationTime, theme, member);

        // when
        var all = service.findAllReservations();

        // then
        assertSoftly(softly -> {
            softly.assertThat(all).hasSize(1);
            var response = all.get(0);
            softly.assertThat(response.name()).isEqualTo("한스");
            softly.assertThat(response.date()).isEqualTo(예약날짜_오늘.date());
            softly.assertThat(response.time().startAt()).isEqualTo(time);
            softly.assertThat(response.theme().id()).isEqualTo(theme.getId());
            softly.assertThat(response.theme().name()).isEqualTo(theme.getName().name());
            softly.assertThat(response.theme().description()).isEqualTo(theme.getDescription().description());
            softly.assertThat(response.theme().thumbnail()).isEqualTo(theme.getThumbnail().thumbnail());
        });
    }

    @Test
    void 예약을_생성할_수_있다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간(time);
        var theme = themeDbFixture.공포();

        var request = new ReservationCreateRequest(today, reservationTime.getId(), theme.getId());

        // when
        var response = service.createReservation(request, member.getId());

        // then
        assertThat(response.name()).isEqualTo("한스");
    }

    @Test
    void 예약시간이_없으면_예외가_발생한다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var request = new ReservationCreateRequest(today, 999L, 1L);

        // when // then
        assertThatThrownBy(() -> service.createReservation(request, member.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 이미_예약된_시간과_테마면_예외가_발생한다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간(time);
        var theme = themeDbFixture.공포();
        var reservationDateTime = new ReservationDateTime(예약날짜_오늘, reservationTime, FIXED_CLOCK);
        reservationDbFixture.예약_생성(reservationDateTime.getReservationDate(), reservationTime, theme, member);
        var request = new ReservationCreateRequest(
                예약날짜_오늘.date(),
                reservationTime.getId(),
                theme.getId()
        );

        // when // then
        assertThatThrownBy(() -> service.createReservation(request, member.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 테마가_없으면_예외가_발생한다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간(time);
        var request = new ReservationCreateRequest(today, reservationTime.getId(), 999L);

        // when // then
        assertThatThrownBy(() -> service.createReservation(request, member.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 예약을_삭제할_수_있다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간(time);
        var theme = themeDbFixture.공포();
        var reservationDateTime = new ReservationDateTime(new ReservationDate(today), reservationTime,
                FIXED_CLOCK);
        var reservation = reservationDbFixture.예약_생성(reservationDateTime.getReservationDate(), reservationTime, theme,
                member);

        // when
        service.deleteReservationById(reservation.getId());

        // then
        assertThat(service.findAllReservations()).isEmpty();
    }

    @Test
    void 삭제할_예약이_없으면_예외() {
        // when // then
        assertThatThrownBy(() -> service.deleteReservationById(999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 내_예약_조회() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var reservation = reservationDbFixture.예약_25_4_22(reservationTime, theme, member);

        // when
        var all = service.findAllMyReservation(member.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(all).hasSize(1);
            var response = all.get(0);
            softly.assertThat(response.reservationId()).isEqualTo(reservation.getId());
            softly.assertThat(response.theme()).isEqualTo(reservation.getTheme().getName().name());
            softly.assertThat(response.date()).isEqualTo(reservation.getDate());
            softly.assertThat(response.time()).isEqualTo(reservation.getStartAt());
            softly.assertThat(response.status()).isEqualTo("예약");
        });
    }

    @Test
    void 멤버로_예약을_조회할_수_있다() {
        // given
        var member1 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var member2 = memberDbFixture.leehyeonsu4888_지메일_gustn111느낌표두개();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        reservationDbFixture.예약_생성(예약날짜_오늘, reservationTime, theme, member1);
        reservationDbFixture.예약_생성(예약날짜_오늘, reservationTime, theme, member2);

        // when
        var result = service.findAllReservationsWithFilter(
                member1.getId(), null, 예약날짜_오늘.date(), 예약날짜_오늘.date());

        // then
        assertThat(result).allMatch(r -> r.name().equals("한스"));
    }

    @Test
    void 테마로_예약을_조회할_수_있다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme1 = themeDbFixture.공포();
        var theme2 = themeDbFixture.로맨스();
        reservationDbFixture.예약_생성(예약날짜_오늘, reservationTime, theme1, member);
        reservationDbFixture.예약_생성(예약날짜_오늘, reservationTime, theme2, member);

        // when
        var result = service.findAllReservationsWithFilter(
                null, theme1.getId(), 예약날짜_오늘.date(), 예약날짜_오늘.date());

        // then
        assertThat(result).allMatch(r -> r.theme().id().equals(theme1.getId()));
    }

    @Test
    void 날짜_범위로_예약을_조회할_수_있다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var date1 = 예약날짜_오늘;
        var date2 = roomescape.integration.fixture.ReservationDateFixture.예약날짜_25_4_22;
        reservationDbFixture.예약_생성(date1, reservationTime, theme, member);
        reservationDbFixture.예약_생성(date2, reservationTime, theme, member);

        // when
        var result = service.findAllReservationsWithFilter(
                null, null, date1.date(), date2.date());

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 조건에_맞는_예약이_없으면_빈_리스트를_반환한다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationTime = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        reservationDbFixture.예약_생성(예약날짜_오늘, reservationTime, theme, member);

        // when
        var result = service.findAllReservationsWithFilter(
                999L, 999L, java.time.LocalDate.of(2099, 1, 1), java.time.LocalDate.of(2099, 1, 2));

        // then
        assertThat(result).isEmpty();
    }
}
