package roomescape.infra.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.infra.theme.JdbcThemeRepository;

@DisplayName("예약 슬롯 JDBC 저장소")
@JdbcTest(properties = "spring.sql.init.mode=always")
@Import({
        JdbcThemeRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcReservationSlotRepository.class
})
class JdbcReservationSlotRepositoryTest {

    @Autowired
    private JdbcThemeRepository themeRepository;

    @Autowired
    private JdbcReservationTimeRepository timeRepository;

    @Autowired
    private JdbcReservationSlotRepository slotRepository;

    @DisplayName("예약 슬롯을 저장할 수 있다")
    @Test
    void save() {
        // when
        ReservationSlot saved = saveSlot(
                "스릴러",
                "긴장감 넘치는 추격 테마",
                "/themes/thriller-night",
                LocalDate.of(2030, 2, 1),
                LocalTime.of(11, 0)
        );

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2030, 2, 1));
    }

    @DisplayName("예약 슬롯을 일정으로 조회할 수 있다")
    @Test
    void findBySchedule() {
        // given
        ReservationSlot saved = saveSlot(
                "미스터리",
                "단서를 따라가는 추리 테마",
                "/themes/mystery-night",
                LocalDate.of(2030, 2, 2),
                LocalTime.of(13, 0)
        );

        // when & then
        assertThat(slotRepository.findBySchedule(saved.getTime().getId(), saved.getDate(), saved.getTheme().getId()))
                .hasValueSatisfying(slot -> {
                    assertThat(slot.getId()).isEqualTo(saved.getId());
                    assertThat(slot.getDate()).isEqualTo(saved.getDate());
                    assertThat(slot.getTime().getId()).isEqualTo(saved.getTime().getId());
                    assertThat(slot.getTheme().getId()).isEqualTo(saved.getTheme().getId());
                });
    }

    @DisplayName("시간과 테마로 예약 슬롯 존재 여부를 확인할 수 있다")
    @Test
    void existsByTimeIdAndThemeId() {
        // given
        ReservationSlot saved = saveSlot(
                "판타지",
                "마법과 모험이 있는 테마",
                "/themes/fantasy-adventure",
                LocalDate.of(2030, 2, 3),
                LocalTime.of(15, 0)
        );

        // then
        assertThat(slotRepository.existsByTimeId(saved.getTime().getId())).isTrue();
        assertThat(slotRepository.existsByThemeId(saved.getTheme().getId())).isTrue();
    }

    private ReservationSlot saveSlot(
            String themeName,
            String description,
            String thumbnailUrl,
            LocalDate date,
            LocalTime startAt
    ) {
        Theme theme = themeRepository.save(Theme.create(themeName, description, thumbnailUrl));
        ReservationTime time = timeRepository.save(ReservationTime.create(startAt));
        return slotRepository.save(ReservationSlot.create(date, time, theme));
    }
}
