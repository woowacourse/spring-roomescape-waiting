package roomescape.reservationtime.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservationtime.domain.repository.AvailableReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.reservationtime.infra.JdbcReservationTimeRepository;
import roomescape.support.RepositoryTestHelper;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.infra.JdbcThemeRepository;

@JdbcTest
class JdbcReservationTimeRepositoryTest {

    ReservationTimeRepository timeRepository;
    ThemeRepository themeRepository;
    RepositoryTestHelper testHelper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        timeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        themeRepository = new JdbcThemeRepository(jdbcTemplate);
        testHelper = new RepositoryTestHelper(jdbcTemplate);
        testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservationTime(LocalTime.of(11, 0));
    }

    @DisplayName("날짜/테마 선택 시 예약 가능한 시간 조회하는 테스트입니다.")
    @Test
    void check_available_time() {
        Theme savedTheme = themeRepository.save(Theme.builder()
                .name("theme name")
                .description("theme description")
                .thumbnailImgUrl("theme img url")
                .price(30000L)
                .build());

        List<AvailableReservationTime> times = timeRepository
                .findAvailableByThemeAndDate(savedTheme.getId(), LocalDate.of(2026, 5, 4));

        List<LocalTime> expectedTimes = List.of(
                LocalTime.parse("09:00"),
                LocalTime.parse("10:00"),
                LocalTime.parse("11:00")
        );

        assertThat(times.size()).isEqualTo(3);
        for (int i = 0; i < 3; i++) {
            assertThat(times.get(i).startAt()).isEqualTo(expectedTimes.get(i));
            assertThat(times.get(i).available()).isTrue();
        }
    }

    @DisplayName("db에 특정 시간이 존재하는 것을 테스트 합니다.")
    @Test
    void check_exists_successfully() {
        Boolean alreadyExists = timeRepository.existsByStartAt(LocalTime.of(9, 0));
        assertThat(alreadyExists).isTrue();
    }
}
