package roomescape.domain.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.repository.JdbcReservationRepository;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.dto.request.ThemeCreateRequestDto;
import roomescape.domain.theme.dto.response.ThemeResponseDto;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.mapper.ThemeMapper;
import roomescape.domain.theme.repository.JdbcThemeRepository;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.JdbcTimeRepository;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.error.exception.GeneralException;

class ThemeServiceTest {

    private ThemeService themeService;
    private ThemeRepository themeRepository;
    private TimeRepository timeRepository;
    private ReservationRepository reservationRepository;
    private int timeSequence;
    private final Clock fixedClock = Clock.fixed(
        Instant.parse("2026-05-08T00:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);

        themeRepository = new JdbcThemeRepository(dataSource);
        timeRepository = new JdbcTimeRepository(dataSource);
        reservationRepository = new JdbcReservationRepository(dataSource);
        themeService = new ThemeService(themeRepository, new ThemeMapper(), fixedClock);
        timeSequence = 0;
    }

    @Nested
    class GetThemesTest {

        @Test
        void 등록된_테마_목록을_조회한다() {
            // given
            Theme theme1 = themeRepository.save(
                Theme.create("테마1", "설명1", "https://roomescape.com/images/themes/theme1.png"));
            Theme theme2 = themeRepository.save(
                Theme.create("테마2", "설명2", "https://roomescape.com/images/themes/theme2.png"));

            // when
            List<ThemeResponseDto> actual = themeService.getThemes();

            // then
            assertThat(actual).containsExactly(
                new ThemeResponseDto(theme1.getId(), "테마1", "설명1", "https://roomescape.com/images/themes/theme1.png"),
                new ThemeResponseDto(theme2.getId(), "테마2", "설명2", "https://roomescape.com/images/themes/theme2.png")
            );
        }

        @Test
        void 삭제된_테마는_조회하지_않는다() {
            // given
            Theme deletedTheme = themeRepository.save(
                Theme.create("삭제된 테마", "설명1", "https://roomescape.com/images/themes/deleted.png"));
            Theme activeTheme = themeRepository.save(
                Theme.create("조회할 테마", "설명2", "https://roomescape.com/images/themes/active.png"));
            themeRepository.deleteThemeById(deletedTheme.getId());

            // when
            List<ThemeResponseDto> actual = themeService.getThemes();

            // then
            assertThat(actual).containsExactly(
                new ThemeResponseDto(activeTheme.getId(), "조회할 테마", "설명2",
                    "https://roomescape.com/images/themes/active.png")
            );
        }

        @Test
        void 조회할_테마가_없으면_빈_목록을_반환한다() {
            // when
            List<ThemeResponseDto> actual = themeService.getThemes();

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class GetPopularThemesTest {

        @Test
        void 오늘을_제외하고_직전_7일의_예약_개수로_인기_테마를_조회한다() {
            // given
            LocalDate today = LocalDate.now(fixedClock);

            Theme includedTheme = themeRepository.save(
                Theme.create("직전 7일 포함 테마", "설명", "https://image.com/included.png"));
            Theme todayTheme = themeRepository.save(
                Theme.create("오늘 예약 테마", "설명", "https://image.com/today.png"));
            Theme beforeRangeTheme = themeRepository.save(
                Theme.create("범위 밖 테마", "설명", "https://image.com/before-range.png"));
            Theme lessPopularTheme = themeRepository.save(
                Theme.create("예약 적은 테마", "설명", "https://image.com/less-popular.png"));

            saveReservations(includedTheme, today.minusDays(1), 3);
            saveReservations(includedTheme, today.minusDays(7), 2);
            saveReservations(todayTheme, today, 10);
            saveReservations(beforeRangeTheme, today.minusDays(8), 10);
            saveReservations(lessPopularTheme, today.minusDays(1), 1);

            // when
            List<ThemeResponseDto> actual = themeService.getPopularThemes();

            // then
            assertThat(actual)
                .extracting(ThemeResponseDto::name)
                .containsExactly("직전 7일 포함 테마", "예약 적은 테마");
        }

        @Test
        void 삭제된_테마와_예약과_시간은_인기_테마_조회에_반영하지_않는다() {
            // given
            LocalDate targetDate = LocalDate.now(fixedClock).minusDays(1);

            Theme activeTheme = themeRepository.save(
                Theme.create("조회할 테마", "설명", "https://image.com/active.png"));
            Theme deletedTheme = themeRepository.save(
                Theme.create("삭제된 테마", "설명", "https://image.com/deleted-theme.png"));
            Theme reservationDeletedTheme = themeRepository.save(
                Theme.create("예약 삭제 테마", "설명", "https://image.com/deleted-reservation.png"));
            Theme timeDeletedTheme = themeRepository.save(
                Theme.create("시간 삭제 테마", "설명", "https://image.com/deleted-time.png"));

            saveReservations(activeTheme, targetDate, 2);
            saveReservations(deletedTheme, targetDate, 5);

            List<Long> reservationIds = saveReservationIds(reservationDeletedTheme, targetDate, 4);
            reservationIds.forEach(reservationRepository::deleteReservationById);

            List<Long> timeIds = saveTimeIdsOfReservations(timeDeletedTheme, targetDate, 3);
            timeIds.forEach(timeRepository::deleteTimeById);

            themeRepository.deleteThemeById(deletedTheme.getId());

            // when
            List<ThemeResponseDto> actual = themeService.getPopularThemes();

            // then
            assertThat(actual)
                .extracting(ThemeResponseDto::name)
                .containsExactly("조회할 테마");
        }
    }

    @Nested
    class SaveThemeTest {

        @Nested
        class Success {

            @Test
            void 성공() {
                // given
                ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                    "피온",
                    "테마 설명",
                    "https://roomescape.com/images/themes/prison-room.png"
                );

                // when
                ThemeResponseDto actual = themeService.saveTheme(request);

                // then
                assertThat(actual).isEqualTo(new ThemeResponseDto(
                    1L,
                    "피온",
                    "테마 설명",
                    "https://roomescape.com/images/themes/prison-room.png"
                ));
            }

            @Test
            void 삭제된_테마와_같은_name으로_생성할_수_있다() {
                // given
                Theme deletedTheme = themeRepository.save(Theme.create("피온", "기존 테마 설명",
                    "https://roomescape.com/images/themes/existing.png"));
                themeRepository.deleteThemeById(deletedTheme.getId());
                ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                    "피온",
                    "테마 설명",
                    "https://roomescape.com/images/themes/prison-room.png"
                );

                // when
                ThemeResponseDto actual = themeService.saveTheme(request);

                // then
                assertThat(actual).isEqualTo(new ThemeResponseDto(
                    2L,
                    "피온",
                    "테마 설명",
                    "https://roomescape.com/images/themes/prison-room.png"
                ));
            }
        }

        @Nested
        class Failed {

            @Test
            void 같은_name의_테마가_이미_존재하면_예외가_발생한다() {
                // given
                themeRepository.save(Theme.create("피온", "기존 테마 설명",
                    "https://roomescape.com/images/themes/existing.png"));
                ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                    "피온",
                    "테마 설명",
                    "https://roomescape.com/images/themes/prison-room.png"
                );

                // when & then
                assertThatThrownBy(() -> themeService.saveTheme(request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("이미 등록된 테마입니다.");
            }
        }
    }

    @Nested
    class DeleteThemeTest {

        @Nested
        class Success {

            @Test
            void 성공() {
                // given
                themeRepository.save(
                    Theme.create("브라운", "테마 설명", "https://roomescape.com/images/themes/prison-room.png"));

                // when
                themeService.deleteThemeById(1L);
                List<ThemeResponseDto> actual = themeService.getThemes();

                // then
                assertThat(actual).isEmpty();
            }
        }

        @Nested
        class Failed {

            @Test
            void 테마_ID가_존재하지_않으면_예외가_발생한다() {
                // when & then
                assertThatThrownBy(() -> themeService.deleteThemeById(999L))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("테마를 찾을 수 없습니다.");
            }

            @Test
            void 이미_삭제된_테마_ID이면_예외가_발생한다() {
                // given
                Theme deletedTheme = themeRepository.save(
                    Theme.create("브라운", "테마 설명", "https://roomescape.com/images/themes/prison-room.png"));
                themeRepository.deleteThemeById(deletedTheme.getId());

                // when & then
                assertThatThrownBy(() -> themeService.deleteThemeById(deletedTheme.getId()))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("테마를 찾을 수 없습니다.");
            }
        }
    }

    private void saveReservations(Theme theme, LocalDate date, int count) {
        for (int i = 0; i < count; i++) {
            saveReservation(theme, date, i);
        }
    }

    private List<Long> saveReservationIds(Theme theme, LocalDate date, int count) {
        List<Long> reservationIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Reservation reservation = saveReservation(theme, date, i);
            reservationIds.add(reservation.getId());
        }
        return reservationIds;
    }

    private List<Long> saveTimeIdsOfReservations(Theme theme, LocalDate date, int count) {
        List<Long> timeIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Reservation reservation = saveReservation(theme, date, i);
            timeIds.add(reservation.getTime().getId());
        }
        return timeIds;
    }

    private Reservation saveReservation(Theme theme, LocalDate date, int index) {
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0).plusMinutes(timeSequence++)));
        return reservationRepository.save(Reservation.create("예약자" + theme.getId() + "-" + index, date, time, theme));
    }
}
