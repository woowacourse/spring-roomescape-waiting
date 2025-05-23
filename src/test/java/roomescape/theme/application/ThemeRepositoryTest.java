package roomescape.theme.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberFixtures;
import roomescape.reservation.domain.ReservationFixtures;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.reservation.time.domain.ReservationTimeFixtures;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFixtures;

@ActiveProfiles("test")
@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("최근 7일간 예약이 많은 순서대로 10개 테마를 조회한다")
    void findPopularThemes() {
        // given
        TestEntities testEntities = createTestEntities();
        createTestReservations(testEntities);
        flushAndClear();

        // when
        List<Theme> popularThemes = themeRepository.findPopularThemes();

        // then
        assertAll(
                () -> assertThat(popularThemes).hasSize(10),
                // theme 11번
                () -> assertThat(popularThemes.get(0).getId()).isEqualTo(testEntities.themes.get(10).getId()),
                () -> assertThat(popularThemes.get(1).getId()).isEqualTo(testEntities.themes.get(9).getId()),
                () -> assertThat(popularThemes.get(2).getId()).isEqualTo(testEntities.themes.get(8).getId()),

                // theme 2번
                () -> assertThat(popularThemes.get(9).getId()).isEqualTo(testEntities.themes.get(1).getId())
        );
    }

    private TestEntities createTestEntities() {
        Member member = MemberFixtures.persistUserMember(entityManager);
        ReservationTime time = ReservationTimeFixtures.persistReservationTime(entityManager);
        List<Theme> themes = createTestThemes();
        return new TestEntities(themes, member, time);
    }

    private List<Theme> createTestThemes() {
        return IntStream.range(0, 11)
                .mapToObj(i -> ThemeFixtures.persistTheme(entityManager))
                .toList();
    }

    private void createTestReservations(TestEntities entities) {
        // 최근 7일 이내 예약 데이터 생성 (theme1: 1건, theme2: 2건, theme3: 3건 ~ theme10: 10건, theme11: 11건)
        for (int i = 0; i < entities.themes().size(); i++) {
            createMultipleReservations(
                    entities.themes().get(i),
                    entities.member(),
                    entities.time(),
                    LocalDate.now().minusDays(1),
                    i + 1
            );
        }

        // 집계에서 제외될 8일 이전 데이터
        createMultipleReservations(entities.themes.getFirst(), entities.member(), entities.time(),
                LocalDate.now().minusDays(8), 5);
    }

    private void createMultipleReservations(
            Theme theme,
            Member member,
            ReservationTime time,
            LocalDate date,
            int count
    ) {
        for (int i = 0; i < count; i++) {
            ReservationFixtures.persistReservedReservation(entityManager, theme, member, time, date);
        }
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private record TestEntities(
            List<Theme> themes,
            Member member,
            ReservationTime time
    ) {
    }
}
