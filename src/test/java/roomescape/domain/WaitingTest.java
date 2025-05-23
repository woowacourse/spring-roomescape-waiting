package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class WaitingTest {

    @PersistenceContext
    private EntityManager em;

    @DisplayName("예약 대기 승인 시 예약 도메인으로 변환된다.(생성된다)")
    @Test
    void confirm() {
        // given
        Waiting waiting = Waiting.create(
                createDefaultMember(),
                new BookingSlot(DEFAULT_DATE, createDefaultReservationTime(), createDefaultTheme())
        );

        // when
        Reservation reservation = waiting.confirm();

        // then
        assertAll(
                () -> assertThat(reservation).isNotNull(),
                () -> assertThat(reservation.getMember()).isEqualTo(waiting.getMember()),
                () -> assertThat(reservation.getDate()).isEqualTo(waiting.getDate()),
                () -> assertThat(reservation.getTime()).isEqualTo(waiting.getTime()),
                () -> assertThat(reservation.getTheme()).isEqualTo(waiting.getTheme())
        );
    }

    @Test
    void sameWaiterWith() {
        // given
        Member member = createDefaultMember();
        ReservationTime time = createDefaultReservationTime();
        Theme theme = createDefaultTheme();
        Waiting waiting = Waiting.create(
                member,
                new BookingSlot(DEFAULT_DATE, time, theme)
        );
        em.persist(member);
        em.persist(time);
        em.persist(theme);
        em.persist(waiting);

        // when
        Long memberId = member.getId();
        boolean owned = waiting.isOwnedBy(memberId);

        assertThat(owned).isTrue();
    }
}
