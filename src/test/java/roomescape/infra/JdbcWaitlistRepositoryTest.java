package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitlistRepository;

@JdbcTest
@Import({
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class,
        JdbcWaitlistRepository.class
})
class JdbcWaitlistRepositoryTest {

    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalDate FUTURE_THIRD_DATE = LocalDate.now().plusDays(3);
    private static final LocalTime TEN = LocalTime.of(10, 0);
    private static final LocalTime TWELVE = LocalTime.of(12, 0);

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Test
    void 예약대기를_저장하고_id로_조회한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        Long saveId = waitlistRepository.save(new Reservation(
                "브라운", FUTURE_THIRD_DATE, reservationTime, theme));

        Optional<Waitlist> waitlist = waitlistRepository.findById(saveId);

        assertThat(waitlist)
                .isPresent()
                .get()
                .satisfies(result -> {
                    assertThat(result.getId()).isEqualTo(saveId);
                    assertThat(result.getName()).isEqualTo("브라운");
                    assertThat(result.getDate()).isEqualTo(FUTURE_THIRD_DATE);
                    assertThat(result.getCreatedAt()).isNotNull();
                    assertThat(result.getTime().getId()).isEqualTo(reservationTime.getId());
                    assertThat(result.getTheme().getId()).isEqualTo(theme.getId());
                });
    }

    @Test
    void id에_해당하는_예약대기가_없으면_빈_Optional을_반환한다() {
        Optional<Waitlist> waitlist = waitlistRepository.findById(1L);

        assertThat(waitlist).isEmpty();
    }

    @Test
    void 같은_슬롯에서_현재_대기보다_앞선_대기_수를_반환한다() {
        ReservationTime ten = createReservationTime(TEN);
        ReservationTime twelve = createReservationTime(TWELVE);
        Theme theme = createTheme();

        waitlistRepository.save(new Reservation(
                "브라운", FUTURE_THIRD_DATE, ten, theme));
        waitlistRepository.save(new Reservation(
                "브리", FUTURE_THIRD_DATE, twelve, theme));
        Long saveId = waitlistRepository.save(new Reservation(
                "워니", FUTURE_THIRD_DATE, ten, theme));

        Waitlist waitlist = waitlistRepository.findById(saveId).orElseThrow();

        assertThat(waitlistRepository.countBefore(waitlist)).isEqualTo(1);
    }

    @Test
    void 같은_사용자의_같은_슬롯_대기가_존재하면_true를_반환한다() {
        ReservationTime reservationTime = createReservationTime(LocalTime.of(10, 0));
        Theme theme = createTheme();
        Reservation reservation = new Reservation("브라운", FUTURE_THIRD_DATE, reservationTime, theme);

        waitlistRepository.save(reservation);

        assertThat(waitlistRepository.existsBySameUser(reservation)).isTrue();
    }

    @Test
    void 예약대기를_삭제한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        Long saveId = waitlistRepository.save(new Reservation(
                "브라운", FUTURE_SECOND_DATE, reservationTime, theme));
        waitlistRepository.deleteById(saveId);

        assertThat(waitlistRepository.findById(saveId)).isEmpty();
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime);
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme);
        return new Theme(
                id,
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImageUrl()
        );
    }
}
