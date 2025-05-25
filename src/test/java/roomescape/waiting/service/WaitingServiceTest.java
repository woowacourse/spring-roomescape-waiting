package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import roomescape.global.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;

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
    private CleanUp cleanUp;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 대기_예약을_삭제한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Reservation savedWaiting = reservationRepository.save(Reservation.waiting(유저1, 내일_열시, 공포));

        // when
        waitingService.delete(savedWaiting.getId());

        // then
        assertThat(reservationRepository.existsById(savedWaiting.getId())).isFalse();
    }

    @Test
    void 존재하지_않는_대기_예약을_삭제할_수_없다() {
        // when & then
        assertThatThrownBy(() -> waitingService.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 본인의_대기_예약을_삭제한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Reservation savedWaiting = reservationRepository.save(Reservation.waiting(유저1, 내일_열시, 공포));

        // when
        waitingService.cancelWaiting(savedWaiting.getId(), 유저1.getId());

        // then
        assertThat(reservationRepository.findById(savedWaiting.getId()).get().getStatus()).isEqualTo(
                ReservationStatus.CANCELED_WAITING);
    }

    @Test
    void 대기_예약이_아닌데_삭제를_하면_예외를_발생한다() {
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Reservation reservation = reservationRepository.save(Reservation.reserve(유저1, 내일_열시, 공포));

        assertThatThrownBy(() -> waitingService.cancelWaiting(reservation.getId(), 유저1.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 다른_사용자의_대기_예약을_삭제할_수_없다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Reservation savedWaiting = reservationRepository.save(Reservation.waiting(유저1, 내일_열시, 공포));

        // when & then
        assertThatThrownBy(() -> waitingService.cancelWaiting(savedWaiting.getId(), 유저2.getId()))
                .isInstanceOf(NotAuthorizationException.class);
    }


}
