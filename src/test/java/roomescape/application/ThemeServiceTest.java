package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.DomainFixtures.JUNK_TIME_SLOT;
import static roomescape.DomainFixtures.JUNK_USER;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.infrastructure.fake.ReservationFakeRepository;
import roomescape.infrastructure.fake.ThemeFakeRepository;

class ThemeServiceTest {

    private final ThemeService themeService = new ThemeService(new ReservationFakeRepository(), new ThemeFakeRepository());

    @Test
    @DisplayName("테마를 추가할 수 있다.")
    void register() {
        // given
        var name = "레벨2 탈출";
        var description = "우테코 레벨2를 탈출하는 내용입니다.";
        var thumbnail = "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg";

        // when
        Theme created = themeService.register(name, description, thumbnail);

        // then
        var themes = themeService.findAllThemes();
        assertThat(themes).contains(created);
    }

    @Test
    @DisplayName("테마를 삭제할 수 있다.")
    void removeById() {
        // given
        var name = "레벨2 탈출";
        var description = "우테코 레벨2를 탈출하는 내용입니다.";
        var thumbnail = "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg";
        Theme created = themeService.register(name, description, thumbnail);

        // when
        boolean isRemoved = themeService.removeById(created.id());

        // then
        var themes = themeService.findAllThemes();
        assertAll(
            () -> assertThat(isRemoved).isTrue(),
            () -> assertThat(themes).doesNotContain(created)
        );
    }

    @Test
    @DisplayName("인기 테마를 조회할 수 있다.")
    void findPopularThemes() {
        // given
        var reservationRepository = new ReservationFakeRepository();
        var themeRepository = new ThemeFakeRepository(reservationRepository);
        var themeService = new ThemeService(reservationRepository, themeRepository);

        var theme1 = themeService.register("테마1", "설명1", "썸네일1");
        var theme2 = themeService.register("테마2", "설명2", "썸네일2");

        reservationRepository.save(
            Reservation.ofExisting(1L, JUNK_USER, LocalDate.of(2024, 5, 30), JUNK_TIME_SLOT, theme1));
        reservationRepository.save(
            Reservation.ofExisting(2L, JUNK_USER, LocalDate.of(2024, 5, 30), JUNK_TIME_SLOT, theme2));
        reservationRepository.save(
            Reservation.ofExisting(3L, JUNK_USER, LocalDate.of(2024, 5, 31), JUNK_TIME_SLOT, theme2));


        // when
        var startDate = LocalDate.of(2024, 5, 25);
        var endDate = LocalDate.of(2024, 6, 5);
        var count = 2;
        var popularThemes = themeService.findPopularThemes(startDate, endDate, count);

        // then
        assertAll(
            () -> assertThat(popularThemes).hasSize(count),
            () -> assertThat(popularThemes).containsSequence(theme2, theme1)
        );
    }

}
