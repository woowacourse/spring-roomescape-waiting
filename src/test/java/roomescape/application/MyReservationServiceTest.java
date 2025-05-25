package roomescape.application;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.BaseTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.fixture.MemberDbFixture;
import roomescape.fixture.ReservationDbFixture;
import roomescape.fixture.ReservationTimeDbFixture;
import roomescape.fixture.ThemeDbFixture;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.response.MyReservationResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MyReservationServiceTest extends BaseTest {

    @Autowired
    private MyReservationService myReservationService;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Test
    void 나의_예약_기록을_조회한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();
        Member member = memberDbFixture.한스_사용자();

        Reservation reservation = reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), Role.USER, member.getEmail());

        List<MyReservationResponse> myReservations = myReservationService.getMyReservations(loginMember);

        assertAll(
                () -> assertThat(myReservations).hasSize(1),
                () -> assertThat(myReservations.getFirst().id()).isEqualTo(reservation.getId())
        );
    }

}
