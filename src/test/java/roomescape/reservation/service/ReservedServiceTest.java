package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
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
class ReservedServiceTest {

    @Autowired
    private ReservationDateTimeDbFixture reservationDateTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;
    @Autowired
    private MemberDbFixture memberDbFixture;
    @Autowired
    private ReservedService reservedService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 사용자가_예약한_예약을_삭제한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Reservation reservation = reservationRepository.save(Reservation.reserve(유저1, 내일_열시, 공포));

        // when
        reservedService.cancel(reservation.getId(), 유저1.getId());

        // then
        assertThat(reservationRepository.findById(reservation.getId()).get().getStatus()).isEqualTo(
                ReservationStatus.CANCELED_RESERVATION);
    }

    @Test
    void 사용자가_예약한_예약이_없으면_예외가_발생한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();

        // when & then
        assertThatThrownBy(() -> reservedService.cancel(1L, 유저1.getId())).isInstanceOf(
                NotFoundException.class);
    }

    @Test
    void 해당_예약자가_아닌_유저가_삭제하면_예외를_발생한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Reservation reservation = reservationRepository.save(Reservation.reserve(유저1, 내일_열시, 공포));

        // when & then
        assertThatThrownBy(() -> reservedService.cancel(reservation.getId(), 유저2.getId())).isInstanceOf(
                NotAuthorizationException.class);
    }

    @Test
    void 예약자가_취소하면_대기_예약자가_예약이_된다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        // 유저1이 예약
        Long reservationId = reservationRepository.save(Reservation.reserve(유저1, 내일_열시, 공포)).getId();

        // 유저2 대기
        reservationRepository.save(Reservation.waiting(유저2, 내일_열시, 공포));

        // when
        reservedService.cancel(reservationId, 유저1.getId());

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).anyMatch(
                r -> r.getReserver().getId().equals(유저2.getId())
                        && r.getTimeId().equals(내일_열시.getTimeId())
                        && r.getDate().equals(내일_열시.getDate())
                        && r.getTheme().getId().equals(공포.getId()));
    }
}
