package roomescape.reservation.repository;

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
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.ReservationWithRank;
import roomescape.theme.domain.Theme;

@CustomJpaTest
class ReservationStatusRepositoryTest {

    @Autowired
    private ReservationStatusRepository reservationStatusRepository;

    @Autowired
    private ReservationRepository reservationRepository;

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

        // 유저2가 먼저 예약 대기
        reservationRepository.save(Reservation.waiting(유저2, 내일_열시, 공포));

        // 유저1 예약 대기
        Reservation savedWaitingByUser1 = reservationRepository.save(Reservation.waiting(유저1, 내일_열시, 공포));

        // when
        List<ReservationWithRank> result = reservationStatusRepository.findWithRankByMemberIdAndStatus(
                유저1.getId(),
                ReservationStatus.WAITING);

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

        Reservation waiting1 = reservationRepository.save(Reservation.waiting(유저1, 내일_열시, 공포));
        Reservation waiting2 = reservationRepository.save(Reservation.waiting(유저2, 내일_열시, 공포));

        List<Reservation> result = reservationStatusRepository.findByStatus(ReservationStatus.WAITING);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.get(0).getTheme().getName()).isEqualTo(공포.getName());
            softly.assertThat(result.get(1).getTheme().getName()).isEqualTo(공포.getName());
            softly.assertThat(result)
                    .extracting(Reservation::getReserverName)
                    .containsExactlyInAnyOrder(유저1.getName(), 유저2.getName());
        });
    }

    @Test
    void 같은_날짜_시간에_대기_예약이_있는지_확인할_수_있다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        reservationRepository.save(Reservation.waiting(유저1, 내일_열시, 공포));

        // when
        boolean 존재함 = reservationStatusRepository.existsByMemberIdAndDateAndTimeIdAndStatus(
                유저1.getId(),
                내일_열시.getDate(),
                내일_열시.getReservationTime().getId(),
                ReservationStatus.WAITING
        );
        boolean 존재하지않음 = reservationStatusRepository.existsByMemberIdAndDateAndTimeIdAndStatus(
                유저1.getId(),
                내일_열시.getDate().plusDays(1),
                내일_열시.getReservationTime().getId(),
                ReservationStatus.WAITING
        );

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(존재함).isTrue();
            softly.assertThat(존재하지않음).isFalse();
        });
    }

    @Test
    void 날짜와_시간으로_대기_예약_존재_여부를_확인할_수_있다() {
        // given
        Member 유저1 = memberDbFixture.유저1_생성();
        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime 내일_열시 = reservationDateTimeDbFixture.내일_열시();

        reservationRepository.save(Reservation.waiting(유저1, 내일_열시, 공포));
        // when
        boolean 존재함 = reservationStatusRepository.existsByDateAndTimeIdAndStatus(
                내일_열시.getDate(),
                내일_열시.getReservationTime().getId(),
                ReservationStatus.WAITING
        );

        boolean 존재하지않음 = reservationStatusRepository.existsByDateAndTimeIdAndStatus(
                내일_열시.getDate().plusDays(1),
                내일_열시.getReservationTime().getId(),
                ReservationStatus.WAITING
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

        Reservation waiting1 = Reservation.waiting(유저2, 내일_열시, 공포);
        reservationRepository.save(waiting1);

        Reservation waiting2 = Reservation.waiting(유저1, 내일_열시, 공포);
        reservationRepository.save(waiting2);

        Reservation waiting3 = Reservation.waiting(유저1, 내일_열한시, 공포);
        reservationRepository.save(waiting3);

        // when
        Reservation firstWaiting = reservationStatusRepository
                .findByDateAndTimeIdAndStatus(date, timeId, ReservationStatus.WAITING)
                .getFirst();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(firstWaiting).isNotNull();
            softly.assertThat(firstWaiting.getDate()).isEqualTo(date);
            softly.assertThat(firstWaiting.getReservationTime().getId()).isEqualTo(timeId);
            softly.assertThat(firstWaiting.getId()).isEqualTo(waiting1.getId());
        });
    }
}
