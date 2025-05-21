package roomescape.waiting.repository;

import java.time.LocalDate;
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
import roomescape.theme.domain.Theme;
import roomescape.waiting.controller.response.WaitingInfoResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.WaitingWithRank;

@CustomJpaTest
class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

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

        Waiting waitingByUser2 = Waiting.builder().
                reserver(유저2)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        // 유저2가 먼저 예약
        waitingRepository.save(waitingByUser2);

        // 유저1 예약
        Waiting waitingByUser1 = Waiting.builder().
                reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();

        Waiting savedWaitingByUser1 = waitingRepository.save(waitingByUser1);

        // when
        List<WaitingWithRank> result = waitingRepository.findWithRankByMemberId(유저1.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.getFirst().waiting().getId()).isEqualTo(savedWaitingByUser1.getId());
            softly.assertThat(result.getFirst().rank()).isEqualTo(2);
        });
    }

    @Test
    void 대기_정보를_모두_반환한다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Waiting waiting1 = Waiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        Waiting waiting2 = Waiting.builder()
                .reserver(유저2)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();

        waitingRepository.save(waiting1);
        waitingRepository.save(waiting2);

        List<WaitingInfoResponse> result = waitingRepository.getAll();

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
    void 같은_날짜_시간에_대기_예약이_있는지_확인할_수_있다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Waiting waiting = Waiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        waitingRepository.save(waiting);

        // when
        boolean 존재함 = waitingRepository.existsByMemberIdAndDateAndTimeId(
                유저1.getId(),
                내일_열시.getDate(),
                내일_열시.getReservationTime().getId()
        );
        boolean 존재하지않음 = waitingRepository.existsByMemberIdAndDateAndTimeId(
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

        Waiting waiting = Waiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        boolean 소유자_확인 = waitingRepository.existsByIdAndReserverId(savedWaiting.getId(), 유저1.getId());
        boolean 다른_회원_확인 = waitingRepository.existsByIdAndReserverId(savedWaiting.getId(), 유저2.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(소유자_확인).isTrue();
            softly.assertThat(다른_회원_확인).isFalse();
        });
    }

    @Test
    void 날짜와_시간으로_대기_예약_존재_여부를_확인할_수_있다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        Waiting waiting = Waiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();
        waitingRepository.save(waiting);

        // when
        boolean 존재함 = waitingRepository.existsByDateAndTimeId(
                내일_열시.getDate(),
                내일_열시.getReservationTime().getId()
        );
        boolean 존재하지않음 = waitingRepository.existsByDateAndTimeId(
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
    void 예약_대기_중_ID가_제일_작은_엔티티를_가져온다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();
        ReservationDateTime 내일_열한시 = reservationDateTimeDbFixture.내일_열한시();

        LocalDate date = 내일_열시.getDate();
        Long timeId = 내일_열시.getTimeId();

        Waiting waiting1 = Waiting.builder()
                .reserver(유저2)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();

        waitingRepository.save(waiting1);

        Waiting waiting2 = Waiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열시)
                .theme(공포)
                .build();

        waitingRepository.save(waiting2);

        Waiting waiting3 = Waiting.builder()
                .reserver(유저1)
                .reservationDatetime(내일_열한시)
                .theme(공포)
                .build();

        waitingRepository.save(waiting3);

        // when
        Waiting firstWaiting = waitingRepository.findByDateAndTimeId(date, timeId).getFirst();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(firstWaiting).isNotNull();
            softly.assertThat(firstWaiting.getReservationDatetime().getDate()).isEqualTo(date);
            softly.assertThat(firstWaiting.getReservationDatetime().getTimeId()).isEqualTo(timeId);
            softly.assertThat(firstWaiting.getId()).isEqualTo(waiting1.getId());
        });
    }
}
