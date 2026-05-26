package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static roomescape.exception.ErrorCode.FUTURE_RANKING_PERIOD;
import static roomescape.exception.ErrorCode.INVALID_RANKING_PERIOD;
import static roomescape.exception.ErrorCode.LONG_RANKING_PERIOD;
import static roomescape.exception.ErrorCode.REFERENCED_THEME;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.repository.FakeDatabase;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeReservationTimeRepository;
import roomescape.repository.FakeThemeRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ServiceThemeCreateRequest;
import roomescape.service.dto.response.ServiceThemeResponse;

public class ThemeServiceTest {

    private ThemeService themeService;

    private ThemeRepository themeRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void beforeEach() {
        FakeDatabase fakeDatabase = new FakeDatabase();

        reservationRepository = new FakeReservationRepository(fakeDatabase);
        reservationTimeRepository = new FakeReservationTimeRepository(fakeDatabase);
        themeRepository = new FakeThemeRepository(fakeDatabase);

        themeService = new ThemeService(themeRepository, reservationRepository);
    }

    @Test
    void readFutureRankingPeriodExceptionTest() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(2);

        assertThatThrownBy(() -> themeService.readRanking(startDate, endDate))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(FUTURE_RANKING_PERIOD.getMessage());
    }

    @Test
    void readInvalidRankingPeriodExceptionTest() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> themeService.readRanking(startDate, endDate))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(INVALID_RANKING_PERIOD.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {367, 1000})
    void readLongRankingPeriodExceptionTest(int period) {
        LocalDate startDate = LocalDate.now().minusDays(period);
        LocalDate endDate = LocalDate.now();

        assertThatThrownBy(() -> themeService.readRanking(startDate, endDate))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(LONG_RANKING_PERIOD.getMessage());
    }

    @Test
    void deleteReferencedThemeExceptionTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.create(new Theme("방탈출1", "방탈출1 설명", "url.jpg"));

        reservationRepository.create(new Reservation("fizz", LocalDate.now().plusDays(1), reservationTime, theme));

        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(REFERENCED_THEME.getMessage());
    }

    @Test
    void createTest() {
        String name = "피즈의 모험";
        String description = "모험 이야기";
        String thumbnailUrl = "url.jpg";
        ServiceThemeCreateRequest request = new ServiceThemeCreateRequest(name, description, thumbnailUrl);

        ServiceThemeResponse response = themeService.create(request);

        assertThat(response).isEqualTo(
                new ServiceThemeResponse(
                        1L,
                        name,
                        description,
                        thumbnailUrl
                )
        );
    }

    @Test
    void readAllTest() {
        themeService.create(new ServiceThemeCreateRequest(
                "피즈의 모험",
                "모험 이야기",
                "url.jpg"
        ));
        themeService.create(new ServiceThemeCreateRequest(
                "나무의 일대기",
                "모험 이야기",
                "url.jpg"
        ));

        List<ServiceThemeResponse> responses = themeService.readAll();

        assertThat(responses.size()).isEqualTo(2);
        assertThat(responses.get(0)).isEqualTo(new ServiceThemeResponse(
                1L,
                "피즈의 모험",
                "모험 이야기",
                "url.jpg"
        ));
        assertThat(responses.get(1)).isEqualTo(new ServiceThemeResponse(
                2L,
                "나무의 일대기",
                "모험 이야기",
                "url.jpg"
        ));
    }

    @Test
    void deleteTest() {
        themeService.create(new ServiceThemeCreateRequest(
                "피즈의 모험",
                "모험 이야기",
                "url.jpg"
        ));

        themeService.delete(1L);

        List<ServiceThemeResponse> responses = themeService.readAll();
        assertThat(responses.size()).isEqualTo(0);
    }

    @Test
    void readRankingTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme1 = themeRepository.create(new Theme("피즈의 모험", "모험 이야기", "url.jpg"));
        Theme theme2 = themeRepository.create(new Theme("피즈의 모험2", "모험 이야기", "url.jpg"));

        reservationRepository.create(new Reservation("fizz", LocalDate.of(2026, 5, 2), reservationTime, theme1));
        reservationRepository.create(new Reservation("fizz", LocalDate.of(2026, 5, 4), reservationTime, theme1));
        reservationRepository.create(new Reservation("fizz2", LocalDate.of(2026, 5, 4), reservationTime, theme2));

        List<ServiceThemeResponse> responses = themeService.readRanking(LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 7));

        assertThat(responses.get(0).name()).isEqualTo("피즈의 모험");
        assertThat(responses.get(1).name()).isEqualTo("피즈의 모험2");
    }
}
