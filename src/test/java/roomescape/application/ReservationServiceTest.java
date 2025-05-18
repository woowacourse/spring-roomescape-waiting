package roomescape.application;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.BaseTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.fixture.MemberDbFixture;
import roomescape.fixture.ReservationDateFixture;
import roomescape.fixture.ReservationDbFixture;
import roomescape.fixture.ReservationTimeDbFixture;
import roomescape.fixture.ThemeDbFixture;
import roomescape.presentation.dto.request.AdminReservationCreateRequest;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.ReservationCreateRequest;
import roomescape.presentation.dto.response.MemberResponse;
import roomescape.presentation.dto.response.MyReservationResponse;
import roomescape.presentation.dto.response.ReservationResponse;
import roomescape.presentation.dto.response.ReservationTimeResponse;
import roomescape.presentation.dto.response.ThemeResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationServiceTest extends BaseTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Test
    void 예약을_모두_조회한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.한스_사용자();
        reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);

        List<ReservationResponse> responses = reservationService.getReservations();
        ReservationResponse response = responses.getFirst();

        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.date()).isEqualTo(ReservationDateFixture.예약날짜_25_4_22.getDate()),
                () -> assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(reservationTime)),
                () -> assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme)),
                () -> assertThat(response.member()).isEqualTo(MemberResponse.from(member))
        );
    }

    @Test
    void 사용자가_예약을_생성한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.한스_사용자();

        ReservationCreateRequest request = new ReservationCreateRequest(
                ReservationDateFixture.예약날짜_25_4_22.getDate(),
                reservationTime.getId(),
                theme.getId()
        );
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), Role.USER, member.getEmail());

        ReservationResponse response = reservationService.createReservation(request, loginMember);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.date()).isEqualTo(ReservationDateFixture.예약날짜_25_4_22.getDate()),
                () -> assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(reservationTime)),
                () -> assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme)),
                () -> assertThat(response.member()).isEqualTo(MemberResponse.from(member))
        );
    }

    @Test
    void 관리자가_예약을_생성한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.한스_사용자();

        AdminReservationCreateRequest request = new AdminReservationCreateRequest(
                ReservationDateFixture.예약날짜_25_4_22.getDate(),
                reservationTime.getId(),
                theme.getId(),
                member.getId()
        );

        ReservationResponse response = reservationService.createAdminReservation(request);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.date()).isEqualTo(ReservationDateFixture.예약날짜_25_4_22.getDate()),
                () -> assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(reservationTime)),
                () -> assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme)),
                () -> assertThat(response.member()).isEqualTo(MemberResponse.from(member))
        );
    }

    @Test
    void 같은일시_다른테마_예약은_생성할_수_있다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme horror = themeDbFixture.공포();
        Theme mystery = themeDbFixture.커스텀_테마("미스테리");
        Member member = memberDbFixture.한스_사용자();
        reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, horror);

        ReservationCreateRequest request = new ReservationCreateRequest(
                ReservationDateFixture.예약날짜_25_4_22.getDate(),
                reservationTime.getId(),
                mystery.getId()
        );
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), Role.USER, member.getEmail());

        assertThatCode(() -> reservationService.createReservation(request, loginMember))
                .doesNotThrowAnyException();
    }

    @Test
    void 같은일시_같은테마_예약이_존재할때_예약을_생성하면_예외가_발생한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.한스_사용자();
        reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);

        ReservationCreateRequest request = new ReservationCreateRequest(
                ReservationDateFixture.예약날짜_25_4_22.getDate(),
                reservationTime.getId(),
                theme.getId()
        );
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), Role.USER, member.getEmail());

        assertThatThrownBy(() -> reservationService.createReservation(request, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약을_삭제한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.한스_사용자();
        Reservation reservation = reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);

        reservationService.deleteReservationById(reservation.getId());

        List<ReservationResponse> responses = reservationService.getReservations();
        assertThat(responses).hasSize(0);
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_예외가_발생한다() {
        assertThatThrownBy(() -> reservationService.deleteReservationById(3L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 조건에따라_예약을_조회한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.한스_사용자();
        Reservation reservation = reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);

        Long themeId = theme.getId();
        Long memberId = member.getId();
        LocalDate dateFrom = LocalDate.of(2025, 4, 22);
        LocalDate dateTo = LocalDate.of(2025, 4, 22);

        List<ReservationResponse> responses = reservationService.getReservationsByFilter(themeId, memberId, dateFrom, dateTo);
        ReservationResponse response = responses.getFirst();

        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(response.id()).isEqualTo(reservation.getId()),
                () -> assertThat(response.date()).isEqualTo(reservation.getDate())
        );
    }

    @Test
    void 나의_예약_기록을_조회한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.한스_사용자();

        Reservation reservation = reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), Role.USER, member.getEmail());

        List<MyReservationResponse> responses = reservationService.getMyReservations(loginMember);
        MyReservationResponse response = responses.getFirst();

        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(response.reservationId()).isEqualTo(reservation.getId()),
                () -> assertThat(response.date()).isEqualTo(reservation.getDate()),
                () -> assertThat(response.time()).isEqualTo(reservationTime.getStartAt()),
                () -> assertThat(response.theme()).isEqualTo(theme.getName()),
                () -> assertThat(response.status()).isEqualTo(ReservationStatus.RESERVED.getName())
        );
    }
}
