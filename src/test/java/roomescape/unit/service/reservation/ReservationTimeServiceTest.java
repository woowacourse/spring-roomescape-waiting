package roomescape.unit.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.dto.reservation.AddReservationTimeDto;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.service.reservation.ReservationTimeService;
import roomescape.unit.config.ServiceFixture;


@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTimeServiceTest {

    static ReservationTimeService reservationTimeService;

    static ReservationRepository reservationRepository;
    static ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setup() {
        reservationRepository = ServiceFixture.fakeReservationRepository();
        reservationTimeRepository = ServiceFixture.fakeReservationTimeRepository();
        reservationTimeService = new ReservationTimeService(reservationRepository, reservationTimeRepository);
    }

    @Test
    void 예약시간을_추가하고_조회할_수_있다() {
        reservationTimeService.addReservationTime(new AddReservationTimeDto(LocalTime.now().plusMinutes(30L)));
        assertThat(reservationTimeService.allReservationTimes().size()).isEqualTo(1);
    }

    @Test
    void 예약시간을_삭제하고_조회할_수_있다() {
        long id = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(LocalTime.now().plusMinutes(30L)));

        int before = reservationTimeService.allReservationTimes().size();
        reservationTimeService.deleteReservationTime(id);
        int after = reservationTimeService.allReservationTimes().size();

        assertAll(() -> {
            assertThat(before).isEqualTo(1);
            assertThat(after).isEqualTo(0);
        });
    }

    @Test
    void 특정_시간에_대한_예약이_존재할때_시간을_삭제하려고하면_예외가_발생한다() {
        LocalTime startAt = LocalTime.now().plusMinutes(30L);
        Long id = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(startAt));
        ReservationTime reservationTime = new ReservationTime(id, startAt);
        Theme theme = new Theme(0L, "공포", "공포테마입니다.", "ㅁㄴㅇㄹ");
        Reservation reservation = new Reservation(null, "praisebak", LocalDate.now().plusDays(1), reservationTime,
                theme);
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(id))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 중복_시간을_설정할_수_없다() {
        LocalTime startAt = LocalTime.now().plusMinutes(30L);
        AddReservationTimeDto initialReservationTime = new AddReservationTimeDto(startAt);
        reservationTimeService.addReservationTime(initialReservationTime);

        AddReservationTimeDto duplicateAddReservationTime = new AddReservationTimeDto(startAt);

        assertThatThrownBy(() -> reservationTimeService.addReservationTime(duplicateAddReservationTime))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_시간을_조회시_예외가_발생한다() {
        assertThatThrownBy(() -> reservationTimeService.getReservationTimeById(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
