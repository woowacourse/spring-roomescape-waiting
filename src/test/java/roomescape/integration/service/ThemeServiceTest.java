package roomescape.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static roomescape.exception.ExceptionType.DELETE_USED_THEME;
import static roomescape.exception.ExceptionType.DUPLICATE_THEME;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationFixture.ReservationOfDateAndTheme;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_RESERVATION_TIME;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import roomescape.domain.ReservationStatus;
import roomescape.domain.Themes;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.RoomescapeException;
import roomescape.fixture.ThemeFixture;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.ThemeService;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeService themeService;
    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("테마가 여러개 있으면 ")
    @Nested
    class MultipleTheme {
        private Theme theme1 = ThemeFixture.themeOfName("name1");
        private Theme theme2 = ThemeFixture.themeOfName("name2");
        private Theme theme3 = ThemeFixture.themeOfName("name3");
        private Theme theme4 = ThemeFixture.themeOfName("name4");

        @BeforeEach
        void init() {
            theme1 = themeRepository.save(theme1);
            theme2 = themeRepository.save(theme2);
            theme3 = themeRepository.save(theme3);
            theme4 = themeRepository.save(theme4);
            memberRepository.save(DEFAULT_MEMBER);
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
            reservationTimeRepository.save(DEFAULT_RESERVATION_TIME);

            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(1), theme1));
            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(2), theme1));
            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(3), theme1));
            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(4), theme1));
            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(5), theme1));

            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(1), theme3));
            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(2), theme3));
            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(3), theme3));

            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(1), theme2));
            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(3), theme2));

            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now().minusDays(3), theme4));

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
            memberRepository.save(DEFAULT_MEMBER);
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

            assertThat(new Themes(themeRepository.findAll()).getThemes())
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
                    new Reservation(LocalDate.now()
                            .plusDays(1), defaultTime, defaultTheme, DEFAULT_MEMBER, ReservationStatus.BOOKED));

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
