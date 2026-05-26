package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.reservationtime.CreateReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponses;
import roomescape.exception.ReservationTimeInUseException;
import roomescape.fixture.Fixtures;
import roomescape.repository.fake.FakeReservationRepository;
import roomescape.repository.fake.FakeReservationTimeRepository;

class ReservationTimeServiceTest {

    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeReservationRepository reservationRepository;
    private ReservationTimeService service;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationRepository = new FakeReservationRepository();
        service = new ReservationTimeService(reservationTimeRepository, reservationRepository);
    }

    @Test
    void createReservationTime_저장된_시간을_id와_함께_반환한다() {
        CreateReservationTimeRequest request = new CreateReservationTimeRequest(LocalTime.of(10, 0));

        ReservationTime created = service.createReservationTime(request);

        assertThat(created.getId()).isPositive();
        assertThat(created.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void getReservationTime_id로_단건을_조회한다() {
        Long id = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 30)));

        ReservationTime found = service.getReservationTime(id);

        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getStartAt()).isEqualTo(LocalTime.of(10, 30));
    }

    @Test
    void getReservationTime_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.getReservationTime(9999L))
                .isInstanceOf(roomescape.exception.ResourceNotFoundException.class)
                .hasMessageContaining("예약 시간")
                .hasMessageContaining("9999");
    }

    @Test
    void getReservationTimes_다음_페이지가_있으면_hasNext가_true() {
        reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(11, 0)));
        reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(12, 0)));

        ReservationTimeResponses responses = service.getReservationTimes(0, 2);

        assertThat(responses.times()).hasSize(2);
        assertThat(responses.hasNext()).isTrue();
    }

    @Test
    void getReservationTimes_다음_페이지가_없으면_hasNext가_false() {
        reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(11, 0)));

        ReservationTimeResponses responses = service.getReservationTimes(0, 2);

        assertThat(responses.times()).hasSize(2);
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void deleteReservationTime_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.deleteReservationTime(9999L))
                .isInstanceOf(roomescape.exception.ResourceNotFoundException.class)
                .hasMessageContaining("예약 시간")
                .hasMessageContaining("9999");
    }

    @Test
    void deleteReservationTime_참조하는_예약이_없으면_정상_삭제() {
        Long id = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        service.deleteReservationTime(id);

        ReservationTimeResponses responses = service.getReservationTimes(0, 10);
        assertThat(responses.times()).extracting("id").doesNotContain(id);
    }

    @Test
    void deleteReservationTime_해당_시간을_참조하는_예약이_존재하면_예외() {
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(timeId, LocalTime.of(10, 0)));

        assertThatThrownBy(() -> service.deleteReservationTime(timeId))
                .isInstanceOf(ReservationTimeInUseException.class)
                .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");
    }

    @Test
    void deleteReservationTime_다른_시간을_참조하는_예약만_있으면_정상_삭제() {
        Long usedTimeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long targetTimeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(11, 0)));
        reservationRepository.save(buildReservation(usedTimeId, LocalTime.of(10, 0)));

        service.deleteReservationTime(targetTimeId);

        ReservationTimeResponses responses = service.getReservationTimes(0, 10);
        assertThat(responses.times()).extracting("id").doesNotContain(targetTimeId);
    }

    private Reservation buildReservation(Long timeId, LocalTime startAt) {
        User user = Fixtures.memberWithId(1L, "브라운");
        Theme theme = new Theme(1L, "공포", "무서움", "https://thumbnail.url");
        ReservationTime time = new ReservationTime(timeId, startAt);
        return Fixtures.reservation(user, theme, LocalDate.of(2026, 5, 8), time);
    }
}
