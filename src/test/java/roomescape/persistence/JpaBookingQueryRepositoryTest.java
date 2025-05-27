package roomescape.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createMemberByName;
import static roomescape.TestFixture.createNewReservation;
import static roomescape.TestFixture.createWaiting;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.persistence.dto.MemberBookingProjection;

@DataJpaTest
class JpaBookingQueryRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private JpaBookingQueryRepository bookingQueryRepository;

    @DisplayName("예약 + 대기 통합 조회")
    @Test
    void query_allBookings() {
        // given
        Member member = createMemberByName("member1");
        Member member2 = createMemberByName("member2");
        Member member3 = createMemberByName("member3");
        ReservationTime time = createDefaultReservationTime();
        Theme theme = createDefaultTheme();
        em.persist(member);
        em.persist(member2);
        em.persist(member3);
        em.persist(time);
        em.persist(theme);

        em.persist(createNewReservation(member2, DEFAULT_DATE, time, theme));
        em.persist(createWaiting(member, DEFAULT_DATE, time, theme));
        em.persist(createNewReservation(member, DEFAULT_DATE.plusDays(1), time, theme));
        em.persist(createNewReservation(member, DEFAULT_DATE.plusDays(2), time, theme));

        em.persist(createWaiting(member2, DEFAULT_DATE.plusDays(3), time, theme));
        em.persist(createWaiting(member3, DEFAULT_DATE.plusDays(3), time, theme));
        em.persist(createWaiting(member, DEFAULT_DATE.plusDays(3), time, theme));

        // when
        List<MemberBookingProjection> bookings = bookingQueryRepository.findAllBookingsByMemberId(
                member.getId());

        // then
        assertAll(
                () -> assertThat(bookings).hasSize(4),
                () -> assertThat(bookings).extracting(MemberBookingProjection::getType)
                        .containsExactly("WAITED", "RESERVED", "RESERVED", "WAITED"),
                () -> assertThat(bookings).extracting(MemberBookingProjection::getRank)
                        .containsExactly(1, 0, 0, 3)
        );
    }

}
