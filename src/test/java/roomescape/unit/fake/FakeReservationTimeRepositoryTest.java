package roomescape.unit.fake;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.response.AvailableTimeResponse;

class FakeReservationTimeRepositoryTest {

    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeReservationRepository reservationRepository;

    public FakeReservationTimeRepositoryTest() {
        this.reservationRepository = new FakeReservationRepository();
        this.reservationTimeRepository = new FakeReservationTimeRepository(reservationRepository);
    }

    @Test
    void 예약_시간_생성() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        // when
        ReservationTime savedTime = reservationTimeRepository.save(time);
        // then
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        assertThat(allTimes).hasSize(1);
        assertThat(allTimes.getFirst().getId()).isEqualTo(savedTime.getId());
    }

    @Test
    void 예약_시간_삭제() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        ReservationTime savedTime = reservationTimeRepository.save(time);
        // when
        reservationTimeRepository.deleteById(savedTime.getId());
        // then
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        assertThat(allTimes).hasSize(0);
    }

    @Test
    void id로_예약_시간_조회() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        ReservationTime savedTime = reservationTimeRepository.save(time);
        // when
        Optional<ReservationTime> optionalTime = reservationTimeRepository.findById(savedTime.getId());
        // then
        assertThat(optionalTime).isPresent();
        assertThat(optionalTime.get().getId()).isEqualTo(savedTime.getId());
    }

    @Test
    void 특정날짜와_테마의_예약시간들을_예약여부와_함께_조회한다() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(9, 0)));
        Theme theme = new Theme(1L, "name", "desc", "thumb");
        Member member = new Member(1L, "name1", "email@domain.com", "pass1", Role.MEMBER);
        reservationRepository.save(
                Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), savedTime, theme)
        );
        // when
        List<AvailableTimeResponse> filteredTimes = reservationTimeRepository.findByDateAndThemeIdWithBooked(
                LocalDate.of(2025, 1, 1), 1L);
        // then
        assertThat(filteredTimes).hasSize(1);
        assertThat(filteredTimes.getFirst().alreadyBooked()).isTrue();
    }
}