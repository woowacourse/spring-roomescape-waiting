package roomescape.waiting.repository;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.config.CustomJpaTest;
import roomescape.fixture.db.MemberDbFixture;
import roomescape.fixture.db.ReservationDateTimeDbFixture;
import roomescape.fixture.db.ThemeDbFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.dto.WaitingWithRank;
import roomescape.theme.domain.Theme;

@CustomJpaTest
class ReservationWaitingRepositoryTest {

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private ReservationDateTimeDbFixture reservationDateTimeDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Test
    void 나의_예약_순위를_알_수_있다() {
        // given
        Member 유저2 = memberDbFixture.유저2_생성();
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        ReservationWaiting waitingByUser2 = ReservationWaiting.builder().
                reserver(유저2)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        // 유저2가 먼저 예약
        reservationWaitingRepository.save(waitingByUser2);

        // 유저1 예약
        ReservationWaiting waitingByUser1 = ReservationWaiting.builder().
                reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();

        ReservationWaiting savedWaitingByUser1 = reservationWaitingRepository.save(waitingByUser1);

        // when
        List<WaitingWithRank> result = reservationWaitingRepository.findWithRankByMemberId(유저1.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.getFirst().reservationWaiting().getId()).isEqualTo(savedWaitingByUser1.getId());
            softly.assertThat(result.getFirst().rank()).isEqualTo(2);
        });
    }

    @Test
    void 같은_날짜_시간에_대기_예약이_있는지_확인할_수_있다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        ReservationWaiting waiting = ReservationWaiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        reservationWaitingRepository.save(waiting);

        // when
        boolean 존재함 = reservationWaitingRepository.existsByMemberIdAndDateAndTimeId(
                유저1.getId(),
                내일_열시.getDate(),
                내일_열시.getReservationTime().getId()
        );
        boolean 존재하지않음 = reservationWaitingRepository.existsByMemberIdAndDateAndTimeId(
                유저1.getId(),
                내일_열시.getDate().plusDays(1),
                내일_열시.getReservationTime().getId()
        );

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(존재함).isTrue();
            softly.assertThat(존재하지않음).isFalse();
        });
    }

    @Test
    void 대기_예약의_소유자를_확인할_수_있다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        ReservationWaiting waiting = ReservationWaiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        ReservationWaiting savedWaiting = reservationWaitingRepository.save(waiting);

        // when
        boolean 소유자_확인 = reservationWaitingRepository.existsByIdAndReserverId(savedWaiting.getId(), 유저1.getId());
        boolean 다른_회원_확인 = reservationWaitingRepository.existsByIdAndReserverId(savedWaiting.getId(), 유저2.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(소유자_확인).isTrue();
            softly.assertThat(다른_회원_확인).isFalse();
        });
    }
}
