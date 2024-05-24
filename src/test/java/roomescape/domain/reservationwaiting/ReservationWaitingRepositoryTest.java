package roomescape.domain.reservationwaiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
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
        Member member = save(MemberFixture.create("abc@email.com"));
        ReservationTime reservationTime = save(ReservationTimeFixture.create());
        Theme theme = save(ThemeFixture.create());
        reservation = save(ReservationFixture.create(member, reservationTime, theme));
    }

    @Test
    @DisplayName("예약의 전체 예약 대기를 조회한다.")
    void findAllByReservation() {
        Member jamie = save(MemberFixture.create("jamie@email.com"));
        Member prin = save(MemberFixture.create("prin@email.com"));
        save(ReservationWaitingFixture.create(reservation, jamie));
        save(ReservationWaitingFixture.create(reservation, prin));

        List<ReservationWaiting> waitings = reservationWaitingRepository.findAllByReservation(reservation);

        assertThat(waitings).hasSize(2);
    }

    @Test
    @DisplayName("중복으로 예약 대기를 생성하면 예외가 발생한다")
    void createDuplicate() {
        Member waitingMember = save(MemberFixture.create("jamie@email.com"));
        ReservationWaiting reservationWaiting = ReservationWaitingFixture.create(reservation, waitingMember);
        save(reservationWaiting);

        ReservationWaiting duplicatedWaiting = ReservationWaitingFixture.create(reservation, waitingMember);
        assertThatThrownBy(() -> reservationWaitingRepository.save(duplicatedWaiting))
                .isExactlyInstanceOf(DataIntegrityViolationException.class);
    }
}
