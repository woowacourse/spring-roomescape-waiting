package roomescape.schedule.repository.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.repository.jpa.JpaMemberRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.jpa.JpaReservationTimeRepository;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.respository.jpa.JpaScheduleRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.jpa.JpaThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
public class JpaScheduleRepositoryTest {

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Autowired
    private JpaScheduleRepository jpaScheduleRepository;

    private Theme theme;
    private ReservationTime time;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        theme = jpaThemeRepository.save(new Theme(null, "test theme", "test description", "test thumbnail"));
        time = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        schedule = jpaScheduleRepository.save(new Schedule(null, LocalDate.now().plusDays(1), time, theme));
    }

    @Test
    void 날짜_시간ID_테마ID_와_일치하는_스케줄_찾기() {
        Schedule findSchedule = jpaScheduleRepository.findByDateAndTimeIdAndThemeId(
                LocalDate.now().plusDays(1), time.getId(), theme.getId()).orElse(null
        );
        assertThat(findSchedule).isEqualTo(schedule);
    }

    @Test
    void 날짜_시간ID_테마ID_와_일치하는_스케줄_존재하는_경우_True() {
        boolean isExists = jpaScheduleRepository.existsByDateAndTimeIdAndThemeId(
                LocalDate.now().plusDays(1), time.getId(), theme.getId());
        assertThat(isExists).isTrue();
    }

    @Test
    void 날짜_시간ID_테마ID_와_일치하는_스케줄_존재하는_경우_False() {
        jpaScheduleRepository.delete(schedule);
        boolean isExists = jpaScheduleRepository.existsByDateAndTimeIdAndThemeId(
                LocalDate.now().plusDays(1), time.getId(), theme.getId());
        assertThat(isExists).isFalse();
    }
}
