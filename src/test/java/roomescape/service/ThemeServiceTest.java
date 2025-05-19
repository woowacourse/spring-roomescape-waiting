package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;
import static roomescape.test.fixture.DateFixture.TODAY;
import static roomescape.test.fixture.DateFixture.YESTERDAY;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.business.ThemeCreationContent;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.local.AlreadyReservedThemeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@DataJpaTest
class ThemeServiceTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    private ThemeService themeService;

    @BeforeEach
    void setup() {
        themeService = new ThemeService(themeRepository, reservationRepository);
    }

    @DisplayName("모든 테마를 조회할 수 있다.")
    @Test
    void testMethodNameHere() {
        // given
        entityManager.persist(
                Theme.createWithoutId("테마1", "테마 설명", "thumbnail.jpg"));
        entityManager.persist(
                Theme.createWithoutId("테마2", "테마 설명", "thumbnail.jpg"));
        entityManager.persist(
                Theme.createWithoutId("테마2", "테마 설명", "thumbnail.jpg"));

        entityManager.flush();

        // when
        List<ThemeResponse> allThemes = themeService.findAllThemes();

        // then
        assertThat(allThemes).hasSize(3);
    }

    @Nested
    @DisplayName("인기 테마를 조회할 수 있다.")
    public class findTopThemes {

        @DisplayName("예약 횟수 순으로 테마를 조회할 수 있다.")
        @Test
        void canFindThemeOrderByReservationCount() {
            // given
            ReservationTime reservationTime = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));

            User member = entityManager.persist(
                    User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "password123"));

            Theme firstTheme = entityManager.persist(
                    Theme.createWithoutId("테마1", "테마 설명", "thumbnail.jpg"));
            Theme secondTheme = entityManager.persist(
                    Theme.createWithoutId("테마2", "테마 설명", "thumbnail.jpg"));
            Theme thirdTheme = entityManager.persist(
                    Theme.createWithoutId("테마2", "테마 설명", "thumbnail.jpg"));

            entityManager.persist(Reservation.createWithoutId(
                    YESTERDAY, ReservationStatus.BOOKED, reservationTime, firstTheme, member));
            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, firstTheme, member));
            entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, ReservationStatus.BOOKED, reservationTime, firstTheme, member));

            entityManager.persist(Reservation.createWithoutId(
                    YESTERDAY, ReservationStatus.BOOKED, reservationTime, secondTheme, member));
            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, secondTheme, member));

            entityManager.persist(Reservation.createWithoutId(
                    YESTERDAY, ReservationStatus.BOOKED, reservationTime, thirdTheme, member));

            entityManager.flush();

            // when
            List<Theme> themes = themeRepository.findThemesOrderByReservationCount(YESTERDAY, NEXT_DAY, 3);

            // then
            assertThat(themes).containsExactly(firstTheme, secondTheme, thirdTheme);
        }

        @DisplayName("기간을 벗어난 예약은 조회에 포함되지 않는다.")
        @Test
        void cannotIncludeOutOfDate() {
            // given
            ReservationTime reservationTime = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));

            User member = entityManager.persist(
                    User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "password123"));

            Theme firstTheme = entityManager.persist(
                    Theme.createWithoutId("테마1", "테마 설명", "thumbnail.jpg"));
            Theme secondTheme = entityManager.persist(
                    Theme.createWithoutId("테마2", "테마 설명", "thumbnail.jpg"));

            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, firstTheme, member));
            entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, ReservationStatus.BOOKED, reservationTime, secondTheme, member));

            entityManager.flush();

            // when
            List<Theme> themes = themeRepository.findThemesOrderByReservationCount(YESTERDAY, TODAY, 2);

            // then
            assertThat(themes).containsExactly(firstTheme, secondTheme);
        }

        @DisplayName("기간 동안의 예약이 존재하는 테마가 조회하고자 하는 테마 개수보다 적다면 아무 테마나 조회 개수에 맞춰 출력된다.")
        @Test
        void canFindDefaultTheme() {
            // given
            entityManager.persist(
                    Theme.createWithoutId("테마1", "테마 설명", "thumbnail.jpg"));
            entityManager.persist(
                    Theme.createWithoutId("테마2", "테마 설명", "thumbnail.jpg"));
            entityManager.persist(
                    Theme.createWithoutId("테마2", "테마 설명", "thumbnail.jpg"));

            entityManager.flush();

            // when
            List<Theme> themes = themeRepository.findThemesOrderByReservationCount(YESTERDAY, NEXT_DAY, 3);

            // then
            assertThat(themes).hasSize(3);
        }
    }

    @DisplayName("테마를 추가할 수 있다.")
    @Test
    void canAddTheme() {
        // given
        ThemeCreationContent creationContent = new ThemeCreationContent("테마", "설명", "섬네일");

        // when
        ThemeResponse response = themeService.addTheme(creationContent);

        // then
        Theme expectedTheme = entityManager.find(Theme.class, response.id());
        assertAll(
                () -> assertThat(response.id()).isEqualTo(expectedTheme.getId()),
                () -> assertThat(response.name()).isEqualTo(creationContent.name()),
                () -> assertThat(response.thumbnail()).isEqualTo(creationContent.thumbnail()),
                () -> assertThat(response.description()).isEqualTo(creationContent.description())
        );
    }

    @Nested
    @DisplayName("테마를 삭제할 수 있다.")
    public class deleteThemeById {

        @DisplayName("테마를 성공적으로 삭제할 수 있다.")
        @Test
        void canDeleteTheme() {
            // given
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마1", "테마 설명", "thumbnail.jpg"));

            entityManager.flush();

            // when
            themeService.deleteThemeById(theme.getId());

            // then
            assertThat(entityManager.find(Theme.class, theme.getId())).isNull();
        }

        @DisplayName("이미 예약이 존재하는 경우 테마를 삭제할 수 없다.")
        @Test
        void cannotDeleteThemeByReservation() {
            // given
            ReservationTime reservationTime = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));

            User member = entityManager.persist(
                    User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "password123"));

            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마1", "테마 설명", "thumbnail.jpg"));

            Reservation reservation = entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));

            entityManager.flush();

            // when & then
            assertThatThrownBy(() -> themeService.deleteThemeById(theme.getId()))
                    .isInstanceOf(AlreadyReservedThemeException.class)
                    .hasMessage("예약에서 사용 중인 테마입니다.");
        }
    }
}
