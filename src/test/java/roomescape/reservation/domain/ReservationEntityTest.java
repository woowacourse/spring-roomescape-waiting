package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatNoException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@ActiveProfiles("test")
@DataJpaTest
public class ReservationEntityTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("테이블 생성 테스트")
    void createReservationTest() {
        // given
        final Member member = new Member("admin@email.com", "admin", "어드민", Role.ADMIN);
        final Theme theme = new Theme("theme1", "description", "thumbnail");
        final ReservationTime reservationTime = new ReservationTime(LocalTime.MIDNIGHT);

        entityManager.persist(member);
        entityManager.persist(theme);
        entityManager.persist(reservationTime);

        // when - then
        assertThatNoException()
                .isThrownBy(
                        () -> entityManager.persist(
                                Reservation.createReserved(member, theme, LocalDate.now(), reservationTime)));
    }
}
