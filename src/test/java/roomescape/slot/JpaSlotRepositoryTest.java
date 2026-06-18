package roomescape.slot;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.adapter.out.persistence.JpaSlotRepository;
import roomescape.slot.domain.Slot;
import roomescape.theme.domain.Theme;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaSlotRepository.class)
class JpaSlotRepositoryTest {

    @Autowired
    private JpaSlotRepository repository;

    @Test
    @DisplayName("슬롯을 저장할 수 있다.")
    void saves_slot_successfully() {
        Slot slot = slot(LocalDate.of(2026, 5, 7), 1L, 2L);

        Slot savedSlot = repository.save(slot);

        assertThat(savedSlot.getId()).isNotNull();
        assertThat(savedSlot.getDate()).isEqualTo(LocalDate.of(2026, 5, 7));
        assertThat(savedSlot.getTimeId()).isEqualTo(1L);
        assertThat(savedSlot.getThemeId()).isEqualTo(2L);
    }

    private Slot slot(LocalDate date, long timeId, long themeId) {
        return Slot.create(
                date,
                new ReservationTime(timeId, LocalTime.of(10, 0)),
                new Theme(themeId, "theme", "description", "thumbnail")
        );
    }

    @Test
    @DisplayName("슬롯을 조회할 수 있다.")
    void finds_slot_successfully() {
        Optional<Slot> slot = repository.findById(1L);

        assertThat(slot).isPresent();
        assertThat(slot.get().getDate()).isEqualTo(LocalDate.of(2026, 5, 5));
        assertThat(slot.get().getTimeId()).isEqualTo(1L);
        assertThat(slot.get().getThemeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("전체 슬롯을 조회할 수 있다.")
    void finds_all_slots_successfully() {
        List<Slot> slots = repository.findAll();

        assertThat(slots).hasSize(5);
    }

    @Test
    @DisplayName("슬롯을 삭제할 수 있다.")
    void deletes_slot_successfully() {
        Slot savedSlot = repository.save(slot(LocalDate.of(2026, 5, 8), 3L, 4L));

        repository.deleteById(savedSlot.getId());

        assertThat(repository.findById(savedSlot.getId())).isEmpty();
    }

    @Test
    @DisplayName("날짜, 시간id, 테마id을 가진 슬롯을 찾을 수 있다.")
    void finds_slot_by_date_time_id_and_theme_id() {
        Slot slot = repository.findByDateAndTimeIdAndThemeId(LocalDate.of(2026, 5, 5), 1L, 1L)
                .orElseThrow();

        assertThat(slot.getId()).isEqualTo(1L);
        assertThat(slot.getDate()).isEqualTo(LocalDate.of(2026, 5, 5));
        assertThat(slot.getTimeId()).isEqualTo(1L);
        assertThat(slot.getThemeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이미 존재하는 슬롯이면 true를 반환한다.")
    void existing_slot_returns_true() {
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
    void missing_slot_returns_false() {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        long themeId = 1L;
        long timeId = 99L;

        // when
        boolean result = repository.existsByDateAndThemeIdAndTimeId(date, themeId, timeId);

        // then
        assertThat(result).isFalse();
    }

}
