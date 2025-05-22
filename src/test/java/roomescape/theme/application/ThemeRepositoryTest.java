package roomescape.theme.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@ActiveProfiles("test")
@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("최근 7일간 예약이 많은 순서대로 테마를 조회한다")
    void findPopularThemes() {
        // given
        TestEntities testEntities = createTestEntities();
        createReservationData(testEntities);
        flushAndClear();

        // when
        List<Theme> popularThemes = themeRepository.findPopularThemes();

        // then
        assertAll(
                () -> assertThat(popularThemes).hasSize(3),
                () -> assertThat(popularThemes.get(0).getId()).isEqualTo(testEntities.theme1().getId()),
                () -> assertThat(popularThemes.get(1).getId()).isEqualTo(testEntities.theme2().getId()),
                () -> assertThat(popularThemes.get(2).getId()).isEqualTo(testEntities.theme3().getId())
        );
    }

    private TestEntities createTestEntities() {
        Theme theme1 = persistTheme("방탈출1", "설명1", "thumbnail1");
        Theme theme2 = persistTheme("방탈출2", "설명2", "thumbnail2");
        Theme theme3 = persistTheme("방탈출3", "설명3", "thumbnail3");

        Member member = persistMember();
        ReservationTime time = persistReservationTime();

        return new TestEntities(theme1, theme2, theme3, member, time);
    }

    private void createReservationData(TestEntities entities) {
        // 최근 7일 이내 예약 데이터 생성 (theme1: 5건, theme2: 3건, theme3: 1건)
        createReservationsForDate(entities.theme1(), entities.member(), entities.time(),
                LocalDate.now().minusDays(1), 2);
        createReservationsForDate(entities.theme1(), entities.member(), entities.time(),
                LocalDate.now().minusDays(2), 3);
        createReservationsForDate(entities.theme2(), entities.member(), entities.time(),
                LocalDate.now().minusDays(3), 3);
        createReservationsForDate(entities.theme3(), entities.member(), entities.time(),
                LocalDate.now().minusDays(4), 1);

        // 집계에서 제외될 8일 이전 데이터
        createReservationsForDate(entities.theme3(), entities.member(), entities.time(),
                LocalDate.now().minusDays(8), 5);
    }

    private Theme persistTheme(String name, String description, String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);
        entityManager.persist(theme);
        return theme;
    }

    private Member persistMember() {
        Member member = new Member("test@test.com", "password", "테스터", Role.USER);
        entityManager.persist(member);
        return member;
    }

    private ReservationTime persistReservationTime() {
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);
        return time;
    }

    private void createReservationsForDate(Theme theme, Member member, ReservationTime time,
                                           LocalDate date, int count) {
        for (int i = 0; i < count; i++) {
            Reservation reservation = Reservation.createReserved(member, theme, date, time);
            entityManager.persist(reservation);
        }
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private record TestEntities(
            Theme theme1,
            Theme theme2,
            Theme theme3,
            Member member,
            ReservationTime time
    ) {
    }
}
