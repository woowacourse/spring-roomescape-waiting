package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({ReservationTimeRepository.class, ReservationRepository.class, ThemeRepository.class})
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void ID로_시간_조회() {
        ReservationTime saved = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
        Optional<ReservationTime> time = reservationTimeRepository.findTimeById(saved.getId());

        assertThat(time)
                .map(ReservationTime::getId)
                .hasValue(saved.getId());

        assertThat(time)
                .map(ReservationTime::getStartAt)
                .hasValue(LocalTime.of(9, 0));
    }

    @Test
    void 시간_저장() {
        ReservationTime newTime = new ReservationTime(LocalTime.of(19, 0));

        ReservationTime saved = reservationTimeRepository.save(newTime);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    void 예약에_사용중인_시간_존재하는_경우() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "url"));
        reservationRepository.save(new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));

        boolean exists = reservationTimeRepository.existsByTimeId(time.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void 예약에_사용중인_시간_존재하지_않는_경우() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
        
        boolean exists = reservationTimeRepository.existsByTimeId(time.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void 시간_삭제() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
        
        reservationTimeRepository.delete(time.getId());

        assertThat(reservationTimeRepository.findTimeById(time.getId())).isEmpty();
    }
}
