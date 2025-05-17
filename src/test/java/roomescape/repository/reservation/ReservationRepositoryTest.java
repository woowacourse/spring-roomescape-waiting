package roomescape.repository.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
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

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member(null, "name", "email@email.com", "password", Role.USER);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("특정 기간 동안 가장 많이 예약된 상위 테마들을 조회할 수 있다")
    void findTopThemesByReservationCountBetweenTest() {
        // given
        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);
        LocalDate outOfRangeDate = LocalDate.of(2025, 6, 1);

        Theme[] themes = new Theme[12];
        for (int i = 0; i < 12; i++) {
            themes[i] = new Theme("theme" + i, "description", "thumbnail");
            entityManager.persist(themes[i]);
        }

        for (int i = 0; i < 12; i++) {
            createReservationsInRange(themes[i], 15 - i, startDate);
        }

        createReservationsInRange(themes[10], 20, outOfRangeDate);
        createReservationsInRange(themes[11], 30, outOfRangeDate);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Long> topThemeIds = reservationRepository.findTopThemesByReservationCountBetween(startDate, endDate);

        // then
        Assertions.assertAll(
                () -> {
                    assertThat(topThemeIds).hasSize(10);
                    for (int i = 0; i < 10; i++) {
                        assertThat(topThemeIds.get(i)).isEqualTo(themes[i].getId());
                    }
                }
        );
    }

    private void createReservationsInRange(Theme theme, int count, LocalDate startDate) {
        for (int i = 0; i < count; i++) {
            ReservationTime time = new ReservationTime(LocalTime.of(10, 0).plusHours(i % 8));
            entityManager.persist(time);

            Reservation reservation = new Reservation(startDate.plusDays(i % 7), time, theme, member);
            entityManager.persist(reservation);
        }
    }
}
