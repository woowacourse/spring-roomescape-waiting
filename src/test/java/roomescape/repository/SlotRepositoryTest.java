package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.SlotRepository;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.theme.ThumbnailUrl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DataJpaTest
class SlotRepositoryTest {
    private final static Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-10T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private final static LocalDate TODAY = LocalDate.of(2026, 5, 10);

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private ReservationTime givenTime(int hour) {
        return reservationTimeRepository.save(ReservationTime.create(LocalTime.of(hour, 0)));
    }

    private Theme givenTheme(String name) {
        return themeRepository.save(Theme.create(new ThemeName(name), "테스트 테마 입니다.", new ThumbnailUrl("https://test.com")));
    }

    private Slot givenSlot(ReservationDate date, ReservationTime time, Theme theme) {
        return slotRepository.save(Slot.create(date, time, theme, LocalDateTime.now(FIXED_CLOCK)));
    }

    @Test
    @DisplayName("ID 부여하며 저장")
    void save() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");

        Slot saved = givenSlot(new ReservationDate(TODAY), time, theme);

        assertSoftly(softly -> {
            softly.assertThat(saved.getId()).isNotNull();
            softly.assertThat(saved.getDate().getDate()).isEqualTo(TODAY);
            softly.assertThat(saved.getTime().getStartAt()).isEqualTo(LocalTime.of(14, 0));
        });
    }

    @Test
    @DisplayName("전체 조회")
    void findAll() {
        ReservationTime time1 = givenTime(14);
        ReservationTime time2 = givenTime(15);
        Theme theme = givenTheme("테스트 테마");
        givenSlot(new ReservationDate(TODAY), time1, theme);
        givenSlot(new ReservationDate(TODAY), time2, theme);

        List<Slot> all = slotRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("저장 후 ID 조회")
    void findById() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot saved = givenSlot(new ReservationDate(TODAY), time, theme);

        Optional<Slot> found = slotRepository.findById(saved.getId());

        assertSoftly(softly -> {
            softly.assertThat(found).isPresent();
            softly.assertThat(found.get().getId()).isEqualTo(saved.getId());
            softly.assertThat(found.get().getDate().getDate()).isEqualTo(TODAY);
        });
    }

    @Test
    @DisplayName("날짜/시간/테마로 슬롯 조회")
    void findByDateAndTimeAndTheme() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        ReservationDate date = new ReservationDate(TODAY);
        Slot saved = givenSlot(date, time, theme);

        Optional<Slot> found = slotRepository.findByDateAndTimeAndTheme(date, time, theme);

        assertSoftly(softly -> {
            softly.assertThat(found).isPresent();
            softly.assertThat(found.get().getId()).isEqualTo(saved.getId());
        });
    }

    @Test
    @DisplayName("존재하지 않는 날짜/시간/테마 조회 시 빈 Optional")
    void findByDateAndTimeAndTheme_notFound() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        ReservationDate otherDate = new ReservationDate(TODAY.plusDays(1));

        Optional<Slot> found = slotRepository.findByDateAndTimeAndTheme(otherDate, time, theme);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("삭제 후 조회 시 빈 Optional")
    void deleteById() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot saved = givenSlot(new ReservationDate(TODAY), time, theme);

        slotRepository.deleteById(saved.getId());

        assertThat(slotRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("해당 timeId를 사용하는 슬롯 존재 확인")
    void existsByTimeId_true() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        givenSlot(new ReservationDate(TODAY), time, theme);

        assertThat(slotRepository.existsByTimeId(time.getId())).isTrue();
    }

    @Test
    @DisplayName("사용되지 않는 timeId는 false")
    void existsByTimeId_false() {
        ReservationTime time = givenTime(14);

        assertThat(slotRepository.existsByTimeId(time.getId())).isFalse();
    }

    @Test
    @DisplayName("해당 themeId를 사용하는 슬롯 존재 확인")
    void existsByThemeId_true() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        givenSlot(new ReservationDate(TODAY), time, theme);

        assertThat(slotRepository.existsByThemeId(theme.getId())).isTrue();
    }

    @Test
    @DisplayName("사용되지 않는 themeId는 false")
    void existsByThemeId_false() {
        Theme theme = givenTheme("테스트 테마");

        assertThat(slotRepository.existsByThemeId(theme.getId())).isFalse();
    }

    @Test
    @DisplayName("이름으로 슬롯 조회")
    void findAllByName() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        reservationRepository.save(Reservation.create("유저1", slot).withStatus(Status.APPROVED));
        reservationRepository.save(Reservation.create("유저2", slot).withStatus(Status.WAITING));

        List<Slot> slots = slotRepository.findAllByName("유저1");

        assertThat(slots).hasSize(1);
        assertThat(slots.get(0).getId()).isEqualTo(slot.getId());
    }
}
