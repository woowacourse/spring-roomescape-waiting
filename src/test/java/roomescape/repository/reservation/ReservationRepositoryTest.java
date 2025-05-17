package roomescape.repository.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.enums.Role;
import roomescape.repository.member.MemberRepository;

@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Theme theme1;
    private Theme theme2;
    private Theme theme3;
    private Member member;
    private ReservationTime time1;
    private ReservationTime time2;

    @BeforeEach
    void setUp() {
        theme1 = new Theme("bestTheme1", "description", "thumbnail1");
        theme2 = new Theme("bestTheme2", "description", "thumbnail2");
        theme3 = new Theme("bestTheme3", "description", "thumbnail3");
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(theme3);

        member = new Member(null, "name", "email@email.com", "password", Role.USER);
        memberRepository.save(member);

        time1 = new ReservationTime(LocalTime.of(10, 0));
        time2 = new ReservationTime(LocalTime.of(12, 0));
        entityManager.persist(time1);
        entityManager.persist(time2);

        createReservations(theme1, 3);
        createReservations(theme2, 2);
        createReservations(theme3, 1);

        entityManager.flush();
        entityManager.clear();
    }
    
    @Test
    @DisplayName("특정 기간 동안 가장 많이 예약된 상위 테마들을 조회할 수 있다")
    void findTopThemesByReservationCountBetweenTest() {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);

        // when
        List<Long> topThemeIds = reservationRepository.findTopThemesByReservationCountBetween(startDate, endDate);

        // then
        assertThat(topThemeIds).hasSize(3);
        assertThat(topThemeIds.get(0)).isEqualTo(theme1.getId());
        assertThat(topThemeIds.get(1)).isEqualTo(theme2.getId());
        assertThat(topThemeIds.get(2)).isEqualTo(theme3.getId());
    }

    private void createReservations(Theme theme, int count) {
        for (int i = 0; i < count; i++) {
            Reservation reservation = new Reservation(LocalDate.now().plusDays(i), time1, theme, member);
            entityManager.persist(reservation);
        }
    }
}
