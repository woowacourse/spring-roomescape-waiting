package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;

@JdbcTest
@Import(value = {SlotRepository.class, ReservationTimeRepository.class, ThemeRepository.class})
class SlotRepositoryTest {
    private static final LocalDate FUTURE = LocalDate.of(2099, 1, 1);

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private ReservationTime giveTime(int hour) {
        return timeRepository.save(ReservationTime.of(LocalTime.of(hour, 0)));
    }

    private Theme giveTheme(String name) {
        return themeRepository.save(
                Theme.create(new ThemeName(name), name + " 설명", new ThumbnailUrl("https://test-theme.com")));
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        void 슬롯을_저장하면_ID가_부여된_슬롯이_반환된다() {
            ReservationTime time = giveTime(10);
            Theme theme = giveTheme("테마1");

            Slot saved = slotRepository.save(Slot.create(new ReservationDate(FUTURE), time, theme));

            assertSoftly(soft -> {
                soft.assertThat(saved.getId()).isPositive();
                soft.assertThat(saved.getDate().getValue()).isEqualTo(FUTURE);
                soft.assertThat(saved.getTime().getId()).isEqualTo(time.getId());
                soft.assertThat(saved.getTheme().getId()).isEqualTo(theme.getId());
            });
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        void ID로_조회하면_슬롯이_반환된다() {
            ReservationTime time = giveTime(10);
            Theme theme = giveTheme("테마1");

            Slot saved = slotRepository.save(Slot.create(new ReservationDate(FUTURE), time, theme));

            assertThat(slotRepository.findById(saved.getId())).isPresent();
        }

        @Test
        void 존재하지_않는_ID는_빈_Optional을_반환한다() {
            assertThat(slotRepository.findById(Long.MAX_VALUE)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByDateAndTimeAndTheme")
    class FindByDateAndTimeAndTheme {

        @Test
        void 날짜_시간_테마가_일치하는_슬롯을_반환한다() {
            ReservationTime time = giveTime(10);
            Theme theme = giveTheme("테마1");

            Slot saved = slotRepository.save(Slot.create(new ReservationDate(FUTURE), time, theme));

            assertThat(slotRepository.findByDateAndTimeAndTheme(FUTURE, time, theme))
                    .isPresent()
                    .get()
                    .extracting(Slot::getId)
                    .isEqualTo(saved.getId());
        }

        @Test
        void 조건이_하나라도_다르면_빈_Optional을_반환한다() {
            ReservationTime time = giveTime(10);
            ReservationTime otherTime = giveTime(11);
            Theme theme = giveTheme("테마1");
            Theme otherTheme = giveTheme("테마2");
            slotRepository.save(Slot.create(new ReservationDate(FUTURE), time, theme));

            assertSoftly(soft -> {
                soft.assertThat(slotRepository.findByDateAndTimeAndTheme(FUTURE.plusDays(1), time, theme)).isEmpty();
                soft.assertThat(slotRepository.findByDateAndTimeAndTheme(FUTURE, otherTime, theme)).isEmpty();
                soft.assertThat(slotRepository.findByDateAndTimeAndTheme(FUTURE, time, otherTheme)).isEmpty();
            });
        }
    }
}
