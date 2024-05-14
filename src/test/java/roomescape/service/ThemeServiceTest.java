package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.Fixture.defaultLoginuser;
import static roomescape.exception.ExceptionType.DELETE_USED_THEME;
import static roomescape.exception.ExceptionType.DUPLICATE_THEME;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.LoginMember;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.CollectionReservationRepository;
import roomescape.repository.CollectionReservationTimeRepository;
import roomescape.repository.CollectionThemeRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

class ThemeServiceTest {

    private static final LoginMember DEFAULT_LOGIN_MEMBER = defaultLoginuser;
    private ThemeRepository themeRepository;
    private CollectionReservationTimeRepository reservationTimeRepository;
    private ReservationRepository reservationRepository;
    private ThemeService themeService;

    @BeforeEach
    void initService() {
        themeRepository = new CollectionThemeRepository();
        reservationTimeRepository = new CollectionReservationTimeRepository();
        reservationRepository = new CollectionReservationRepository();
        themeService = new ThemeService(themeRepository, reservationRepository);
    }

    @DisplayName("테마가 여러개 있으면 ")
    @Nested
    class MultipleTheme {
        private Theme theme1 = new Theme("name1", "description1", "thumbnail1");
        private Theme theme2 = new Theme("name2", "description2", "thumbnail2");
        private Theme theme3 = new Theme("name3", "description3", "thumbnail3");
        private Theme theme4 = new Theme("name4", "description4", "thumbnail4");

        @BeforeEach
        void init() {
            theme1 = themeRepository.save(theme1);
            theme2 = themeRepository.save(theme2);
            theme3 = themeRepository.save(theme3);
            theme4 = themeRepository.save(theme4);
        }

        @DisplayName("테마를 모두 조회할 수 있다.")
        @Test
        void findAllTest() {
            //when
            List<ThemeResponse> themeResponses = themeService.findAll();

            //then
            assertThat(themeResponses).hasSize(4);
        }

        @DisplayName("예약 개수에 따라 인기 테마를 조회할 수 있다.")
        @Test
        void findPopularTest() {
            //when
            ReservationTime reservationTime = new ReservationTime(LocalTime.of(11, 30));
            reservationTime = reservationTimeRepository.save(reservationTime);

            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(1), reservationTime, theme1, DEFAULT_LOGIN_MEMBER));
            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(2), reservationTime, theme1, DEFAULT_LOGIN_MEMBER));
            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(3), reservationTime, theme1, DEFAULT_LOGIN_MEMBER));
            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(4), reservationTime, theme1, DEFAULT_LOGIN_MEMBER));
            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(5), reservationTime, theme1, DEFAULT_LOGIN_MEMBER));

            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(1), reservationTime, theme3, DEFAULT_LOGIN_MEMBER));
            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(2), reservationTime, theme3, DEFAULT_LOGIN_MEMBER));
            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(3), reservationTime, theme3, DEFAULT_LOGIN_MEMBER));

            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(1), reservationTime, theme2, DEFAULT_LOGIN_MEMBER));
            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(3), reservationTime, theme2, DEFAULT_LOGIN_MEMBER));

            reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(3), reservationTime, theme4, DEFAULT_LOGIN_MEMBER));

            //when
            List<ThemeResponse> popularThemes = themeService.findAndOrderByPopularity(5);

            assertThat(popularThemes).contains(
                    ThemeResponse.from(theme1),
                    ThemeResponse.from(theme3),
                    ThemeResponse.from(theme2),
                    ThemeResponse.from(theme4)
            );
        }
    }

    @DisplayName("테마, 시간이 하나 존재할 때")
    @Nested
    class OneThemeTest {
        private ReservationTime defaultTime = new ReservationTime(LocalTime.now().plusMinutes(5));
        private Theme defaultTheme = new Theme("name", "description", "thumbnail");

        @BeforeEach
        void addDefaultData() {
            defaultTime = reservationTimeRepository.save(defaultTime);
            defaultTheme = themeRepository.save(defaultTheme);
        }

        @DisplayName("동일한 이름의 테마를 예약할 수 없다.")
        @Test
        void duplicatedThemeSaveFailTest() {
            assertThatThrownBy(() -> themeService.save(new ThemeRequest(
                    defaultTheme.getName(), "description", "thumbnail"
            ))).isInstanceOf(RoomescapeException.class)
                    .hasMessage(DUPLICATE_THEME.getMessage());
        }

        @DisplayName("다른 이름의 테마를 예약할 수 있다.")
        @Test
        void notDuplicatedThemeNameSaveTest() {
            themeService.save(new ThemeRequest("otherName", "description", "thumbnail"));

            assertThat(themeRepository.findAll().getThemes())
                    .hasSize(2);
        }

        @DisplayName("테마에 예약이 없다면 테마를 삭제할 수 있다.")
        @Test
        void removeSuccessTest() {

            themeService.delete(1L);
            assertThat(themeRepository.findById(1L)).isEmpty();
        }

        @DisplayName("테마에 예약이 있다면 테마를 삭제할 수 없다.")
        @Test
        void removeFailTest() {
            //given
            reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(1), defaultTime, defaultTheme, defaultLoginuser));

            //when & then
            assertThatThrownBy(() -> themeService.delete(1L))
                    .isInstanceOf(RoomescapeException.class)
                    .hasMessage(DELETE_USED_THEME.getMessage());
        }

        @DisplayName("존재하지 않는 테마 id로 삭제하더라도 오류로 간주하지 않는다.")
        @Test
        void notExistThemeDeleteTest() {
            assertThatCode(() -> themeService.delete(2L))
                    .doesNotThrowAnyException();
        }
    }
}
