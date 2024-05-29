package roomescape.domain.reservationwaiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.BaseRepositoryTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ReservationWaitingFixture;
import roomescape.support.fixture.ThemeFixture;

class ReservationWaitingRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        Member member = save(MemberFixture.user());
        ReservationTime reservationTime = save(ReservationTimeFixture.ten());
        Theme theme = save(ThemeFixture.theme());
        reservation = save(ReservationFixture.create(member, reservationTime, theme));
    }

    @Test
    @DisplayName("예약의 전체 예약 대기를 조회한다.")
    void findAllByReservation() {
        Member jamie = save(MemberFixture.jamie());
        Member prin = save(MemberFixture.prin());
        save(ReservationWaitingFixture.create(reservation, jamie));
        save(ReservationWaitingFixture.create(reservation, prin));

        List<ReservationWaiting> waitings = reservationWaitingRepository.findAllByReservation(reservation);

        assertThat(waitings).hasSize(2);
    }

    @Test
    @DisplayName("중복으로 예약 대기를 생성하면 예외가 발생한다")
    void createDuplicate() {
        Member jamie = save(MemberFixture.jamie());
        ReservationWaiting reservationWaiting = ReservationWaitingFixture.create(reservation, jamie);
        save(reservationWaiting);

        ReservationWaiting duplicatedWaiting = ReservationWaitingFixture.create(reservation, jamie);
        assertThatThrownBy(() -> reservationWaitingRepository.save(duplicatedWaiting))
                .isExactlyInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("예약 대기의 랭크를 조회한다")
    void findRankByReservationWaiting() {
        for (int cnt = 0; cnt < 5; cnt++) {
            Member waitedMember = save(MemberFixture.create("waited" + cnt + "@email.com"));
            save(ReservationWaitingFixture.create(reservation, waitedMember));
        }
        Member prin = save(MemberFixture.prin());
        save(ReservationWaitingFixture.create(reservation, prin));

        List<WaitingWithRank> waitingWithRanks = reservationWaitingRepository.findAllWithRankByMember(prin);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(waitingWithRanks).hasSize(1);
            softly.assertThat(waitingWithRanks.get(0).rank()).isEqualTo(6);
        });
    }
}
