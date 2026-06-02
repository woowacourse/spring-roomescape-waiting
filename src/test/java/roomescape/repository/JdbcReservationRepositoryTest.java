package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class
})
class JdbcReservationRepositoryTest {

    @Autowired
    JdbcReservationRepository reservationRepository;
    @Autowired
    JdbcReservationTimeRepository reservationTimeRepository;
    @Autowired
    JdbcThemeRepository themeRepository;

    @Test
    void 예약을_저장하면_id를_부여한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Reservation reservation = Reservation.create("루드비코", LocalDate.parse("2026-05-06"),
                reservationTime,
                theme);

        // when
        Reservation saved = reservationRepository.save(reservation);

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 예약을_id로_조회한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Reservation reservation = Reservation.create("루드비코", LocalDate.parse("2026-05-06"),
                reservationTime,
                theme);

        // when
        Reservation saved = reservationRepository.save(reservation);
        Optional<Reservation> result = reservationRepository.findById(saved.getId());

        // then
        assertThat(result)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    void 저장된_모든_예약을_조회한다() {
        // given
        ReservationTime reservationTime1 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Reservation rudevicoReservation = Reservation.create("루드비코", LocalDate.parse("2026-05-06"),
                reservationTime1,
                theme);

        ReservationTime reservationTime2 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("11:00")));
        Reservation cocoReservation = Reservation.create("코코", LocalDate.parse("2026-05-06"),
                reservationTime2,
                theme);

        // when
        Reservation savedRudevicoReservation = reservationRepository.save(rudevicoReservation);
        Reservation savedCocoReservation = reservationRepository.save(cocoReservation);

        List<Reservation> foundReservations = reservationRepository.findAll();

        // then
        assertThat(foundReservations)
                .hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        savedRudevicoReservation, savedCocoReservation
                );
    }

    @Test
    void 예약을_삭제한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Reservation reservation = reservationRepository.save(
                Reservation.create("루드비코", LocalDate.parse("2026-05-06"),
                        reservationTime,
                        theme));

        // when
        reservationRepository.delete(reservation.getId());
        Optional<Reservation> result = reservationRepository.findById(reservation.getId());

        // then
        assertThat(result).isNotPresent();
    }
}
