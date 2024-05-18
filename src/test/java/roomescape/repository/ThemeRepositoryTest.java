package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemePopularFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.*;


class ThemeRepositoryTest extends RepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("테마를 저장한다.")
    void save() {
        // given
        final Theme theme = THEME_HORROR();

        // when
        final Theme actual = themeRepository.save(theme);

        // then
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    @DisplayName("Id에 해당하는 테마를 조회한다.")
    void findById() {
        // given
        final Theme theme = THEME_DETECTIVE();
        testEntityManager.persist(theme);

        // when
        final Optional<Theme> actual = themeRepository.findById(theme.getId());

        // then
        assertThat(actual).isNotEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 Id로 테마를 조회하면 빈 옵셔널을 반환한다.")
    void returnEmptyOptionalWhenFindByNotExistingId() {
        // given
        final Long notExistingId = 0L;

        // when
        final Optional<Theme> actual = themeRepository.findById(notExistingId);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Id에 해당하는 테마를 삭제한다.")
    void deleteById() {
        // when
        final Theme theme = THEME_DETECTIVE();
        testEntityManager.persist(theme);
        themeRepository.deleteById(theme.getId());

        // then
        final List<Theme> actual = themeRepository.findAll();
        assertThat(actual).doesNotContain(theme);
    }

    @Test
    @DisplayName("인기 테마 목록을 조회한다.")
    void findPopularThemes() {
        // given
        loadPopularThemeData();
        final ThemePopularFilter themePopularFilter
                = ThemePopularFilter.getThemePopularFilter(LocalDate.parse("2034-05-12"));

        // when
        final int limit = themePopularFilter.getLimit();
        final List<Theme> actual = themeRepository.findPopularThemesBy(themePopularFilter);

        // then
        assertThat(actual).hasSize(limit);
    }

    private void loadPopularThemeData() {
        final Member member = MEMBER_MIA();
        testEntityManager.persist(member);

        final ReservationTime reservationTimeSix = RESERVATION_TIME_SIX();
        final ReservationTime reservationTimeSeven = RESERVATION_TIME_SEVEN();
        testEntityManager.persist(reservationTimeSix);
        testEntityManager.persist(reservationTimeSeven);

        final Theme horror1 = new Theme("호러1", THEME_HORROR_DESCRIPTION, THEME_HORROR_THUMBNAIL);
        final Theme horror2 = new Theme("호러2", THEME_HORROR_DESCRIPTION, THEME_HORROR_THUMBNAIL);
        final Theme horror3 = new Theme("호러3", THEME_HORROR_DESCRIPTION, THEME_HORROR_THUMBNAIL);
        final Theme horror4 = new Theme("호러4", THEME_HORROR_DESCRIPTION, THEME_HORROR_THUMBNAIL);
        final Theme horror5 = new Theme("호러5", THEME_HORROR_DESCRIPTION, THEME_HORROR_THUMBNAIL);
        final Theme detective1 = new Theme("추리1", THEME_DETECTIVE_DESCRIPTION, THEME_DETECTIVE_DESCRIPTION);
        final Theme detective2 = new Theme("추리2", THEME_DETECTIVE_DESCRIPTION, THEME_DETECTIVE_DESCRIPTION);
        final Theme detective3 = new Theme("추리3", THEME_DETECTIVE_DESCRIPTION, THEME_DETECTIVE_DESCRIPTION);
        final Theme detective4 = new Theme("추리4", THEME_DETECTIVE_DESCRIPTION, THEME_DETECTIVE_DESCRIPTION);
        final Theme detective5 = new Theme("추리5", THEME_DETECTIVE_DESCRIPTION, THEME_DETECTIVE_DESCRIPTION);
        testEntityManager.persist(horror1);
        testEntityManager.persist(horror2);
        testEntityManager.persist(horror3);
        testEntityManager.persist(horror4);
        testEntityManager.persist(horror5);
        testEntityManager.persist(detective1);
        testEntityManager.persist(detective2);
        testEntityManager.persist(detective3);
        testEntityManager.persist(detective4);
        testEntityManager.persist(detective5);
        testEntityManager.flush();


        testEntityManager.persist(new Reservation(member, "2034-05-08", reservationTimeSix, horror2));
        testEntityManager.persist(new Reservation(member, "2034-05-09", reservationTimeSix, horror2));
        testEntityManager.persist(new Reservation(member, "2034-05-10", reservationTimeSix, horror2));
        testEntityManager.persist(new Reservation(member, "2034-05-11", reservationTimeSix, horror2));
        testEntityManager.persist(new Reservation(member, "2034-05-12", reservationTimeSix, horror2));
        testEntityManager.persist(new Reservation(member, "2034-05-08", reservationTimeSix, horror1));
        testEntityManager.persist(new Reservation(member, "2034-05-09", reservationTimeSix, horror1));
        testEntityManager.persist(new Reservation(member, "2034-05-10", reservationTimeSix, horror1));
        testEntityManager.persist(new Reservation(member, "2034-05-11", reservationTimeSix, horror1));
        testEntityManager.persist(new Reservation(member, "2034-05-08", reservationTimeSix, horror3));
        testEntityManager.persist(new Reservation(member, "2034-05-08", reservationTimeSeven, horror3));
        testEntityManager.persist(new Reservation(member, "2034-05-09", reservationTimeSeven, horror3));
        testEntityManager.persist(new Reservation(member, "2034-05-08", reservationTimeSeven, horror4));
        testEntityManager.persist(new Reservation(member, "2034-05-08", reservationTimeSix, horror4));
        testEntityManager.persist(new Reservation(member, "2034-05-09", reservationTimeSix, horror4));
        testEntityManager.persist(new Reservation(member, "2034-05-09", reservationTimeSix, horror5));
        testEntityManager.persist(new Reservation(member, "2034-05-09", reservationTimeSeven, horror5));
        testEntityManager.persist(new Reservation(member, "2034-05-09", reservationTimeSix, detective1));
        testEntityManager.persist(new Reservation(member, "2034-05-09", reservationTimeSeven, detective1));
        testEntityManager.persist(new Reservation(member, "2034-05-10", reservationTimeSix, detective2));
        testEntityManager.persist(new Reservation(member, "2034-05-10", reservationTimeSeven, detective2));
        testEntityManager.persist(new Reservation(member, "2034-05-10", reservationTimeSeven, detective3));
        testEntityManager.persist(new Reservation(member, "2034-05-10", reservationTimeSeven, detective3));
        testEntityManager.persist(new Reservation(member, "2034-05-11", reservationTimeSix, detective4));
        testEntityManager.persist(new Reservation(member, "2034-05-11", reservationTimeSeven, detective4));
        testEntityManager.flush();
    }
}
