package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;

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
import roomescape.member.domain.Member;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.domain.Theme;
import roomescape.waiting.controller.response.WaitingInfoResponse;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class WaitingQueryServiceTest {

    @Autowired
    private WaitingQueryService waitingQueryService;

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
    void 대기_예약_목록을_모두_조회한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        reservationRepository.save(Reservation.waiting(유저1, 내일_열시, 공포));
        reservationRepository.save(Reservation.waiting(유저2, 내일_열시, 공포));

        // when
        List<WaitingInfoResponse> result = waitingQueryService.getAllInfo();

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
    void 나의_예약_순위를_알_수_있다() {
        // given
        Member 유저2 = memberDbFixture.유저2_생성();
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        reservationRepository.save(Reservation.waiting(유저2, 내일_열시, 공포));
        reservationRepository.save(Reservation.waiting(유저1, 내일_열시, 공포));

        // when
        List<MyReservationResponse> result = waitingQueryService.getWaitingReservations(유저1.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.getFirst().status()).isEqualTo("2");
        });
    }

    @Test
    void 예약_대기를_확인할_수_있다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();
        LocalDate date = 내일_열시.getDate();
        Long timeId = 내일_열시.getTimeId();

        reservationRepository.save(Reservation.waiting(유저1, 내일_열시, 공포));

        // when
        boolean result = waitingQueryService.existWaiting(유저1.getId(), date, timeId);

        assertThat(result).isTrue();
    }
}
