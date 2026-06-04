package roomescape.slot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservationtime.ReservationTime;
import roomescape.slot.infrastructure.JdbcSlotRepository;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import(JdbcSlotRepository.class)
class JdbcSlotRepositoryTest {

    @Autowired
    private JdbcSlotRepository repository;

    @Test
    void 슬롯_저장_레포지토리_테스트() {
        Slot slot = slot(LocalDate.of(2026, 5, 7), 1L, 2L);

        Slot savedSlot = repository.save(slot);

        assertThat(savedSlot.getId()).isNotNull();
        assertThat(savedSlot.getDate()).isEqualTo(LocalDate.of(2026, 5, 7));
        assertThat(savedSlot.getTimeId()).isEqualTo(1L);
        assertThat(savedSlot.getThemeId()).isEqualTo(2L);
    }

    @Test
    void 슬롯_조회_레포지토리_테스트() {
        Optional<Slot> slot = repository.findById(1L);

        assertThat(slot).isPresent();
        assertThat(slot.get().getDate()).isEqualTo(LocalDate.of(2026, 5, 5));
        assertThat(slot.get().getTimeId()).isEqualTo(1L);
        assertThat(slot.get().getThemeId()).isEqualTo(1L);
    }

    @Test
    void 슬롯_전체_조회_레포지토리_테스트() {
        List<Slot> slots = repository.findAll();

        assertThat(slots).hasSize(5);
    }

    @Test
    void 슬롯_삭제_레포지토리_테스트() {
        Slot savedSlot = repository.save(slot(LocalDate.of(2026, 5, 8), 3L, 4L));

        repository.deleteById(savedSlot.getId());

        assertThat(repository.findById(savedSlot.getId())).isEmpty();
    }

    @Test
    @DisplayName("날짜, 시간id, 테마id을 가진 슬롯을 찾을 수 있다.")
    void findByDateAndTimeIdAndThemeId_레포지토리_테스트() {
        Slot slot = repository.findByDateAndTimeIdAndThemeId(LocalDate.of(2026, 5, 5), 1L, 1L)
                .orElseThrow();

        assertThat(slot.getId()).isEqualTo(1L);
        assertThat(slot.getDate()).isEqualTo(LocalDate.of(2026, 5, 5));
        assertThat(slot.getTimeId()).isEqualTo(1L);
        assertThat(slot.getThemeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이미 존재하는 슬롯이면 true를 반환한다.")
    void existsByDateAndThemeIdAndTimeId_테스트_1() {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        long themeId = 1L;
        long timeId = 1L;

        // when
        boolean result = repository.existsByDateAndThemeIdAndTimeId(date, themeId, timeId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 슬롯이면 false를 반환한다.")
    void existsByDateAndThemeIdAndTimeId_테스트_2() {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        long themeId = 1L;
        long timeId = 99L;

        // when
        boolean result = repository.existsByDateAndThemeIdAndTimeId(date, themeId, timeId);

        // then
        assertThat(result).isFalse();
    }

    private Slot slot(LocalDate date, long timeId, long themeId) {
        return Slot.create(
                date,
                new ReservationTime(timeId, LocalTime.of(10, 0)),
                new Theme(themeId, "theme", "description", "thumbnail")
        );
    }
}
