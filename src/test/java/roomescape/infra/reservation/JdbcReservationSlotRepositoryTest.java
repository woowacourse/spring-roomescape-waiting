package roomescape.infra.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.application.exception.DuplicateResourceException;
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

    @DisplayName("중복 예약 슬롯은 유니크 제약 위반을 발생시킨다")
    @Test
    void saveWhenDuplicate() {
        // given
        Theme theme = themeRepository.save(Theme.create("스릴러", "긴장감 넘치는 추격 테마", "/themes/thriller-night"));
        ReservationTime time = timeRepository.save(ReservationTime.create(LocalTime.of(11, 0)));
        ReservationSlot slot = ReservationSlot.create(LocalDate.of(2030, 2, 1), time, theme);

        slotRepository.save(slot);

        // when & then
        assertThatThrownBy(() -> slotRepository.save(slot))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @DisplayName("특정 날짜, 테마 ID, 시간 ID를 갖는 슬롯의 존재 여부를 확인할 수 있다")
    @Test
    void existsByDateAndThemeIdAndTimeId() {
        // given
        Theme theme = themeRepository.save(Theme.create("스릴러", "긴장감 넘치는 추격 테마", "/themes/thriller-night"));
        ReservationTime time = timeRepository.save(ReservationTime.create(LocalTime.of(11, 0)));
        LocalDate date = LocalDate.of(2030, 2, 1);

        slotRepository.save(ReservationSlot.create(date, time, theme));

        // when
        boolean exists = slotRepository.existsByDateAndThemeIdAndTimeId(date, theme.getId(), time.getId());
        boolean notExists = slotRepository.existsByDateAndThemeIdAndTimeId(date.plusDays(1), theme.getId(),
                time.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
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
