package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.dto.ReservationTimeAvailabilityResponse;
import roomescape.domain.reservationtime.dto.TimeCreationRequest;
import roomescape.domain.reservationtime.dto.TimeCreationResponse;
import roomescape.support.exception.RoomescapeException;

class ReservationTimeServiceTest {

    private ReservationTimeService reservationTimeService;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationRepository = new FakeReservationRepository();
        reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);
    }

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void createReservationTime() {
        TimeCreationRequest request = new TimeCreationRequest(LocalTime.of(10, 0));

        TimeCreationResponse response = reservationTimeService.createReservationTime(request);

        assertThat(response.startAt()).isEqualTo(request.startAt());
        assertThat(reservationTimeRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("중복된 시간 생성 시 예외가 발생한다.")
    void createDuplicateTime() {
        LocalTime startAt = LocalTime.of(10, 0);
        reservationTimeService.createReservationTime(new TimeCreationRequest(startAt));

        assertThatThrownBy(() -> reservationTimeService.createReservationTime(new TimeCreationRequest(startAt)))
            .isInstanceOf(RoomescapeException.class);
    }

    @Test
    @DisplayName("특정 테마와 날짜의 예약 가능 시간을 조회한다.")
    void getReservationTimeAvailability() {
        // given
        ReservationTime time1 = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        ReservationTime time2 = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(11, 0)));
        reservationRepository.addReservedTime(time1.getId());

        // when
        List<ReservationTimeAvailabilityResponse> responses = reservationTimeService.getReservationTimeAvailability(1L,
            1L);

        // then
        assertThat(responses).hasSize(2);
        assertThat(
            responses.stream().filter(r -> r.timeId().equals(time1.getId())).findFirst().get().available()).isFalse();
        assertThat(
            responses.stream().filter(r -> r.timeId().equals(time2.getId())).findFirst().get().available()).isTrue();
    }

    @Test
    @DisplayName("사용 중인 시간을 삭제하려 하면 예외가 발생한다.")
    void deleteInUseTime() {
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        reservationRepository.setCount(1);

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(time.getId()))
            .isInstanceOf(RoomescapeException.class);
    }

    private static class FakeReservationTimeRepository implements ReservationTimeRepository {

        private final List<ReservationTime> times = new ArrayList<>();
        private Long idCounter = 1L;

        @Override
        public Optional<ReservationTime> findById(Long id) {
            return times.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public List<ReservationTime> findAll() {
            return times;
        }

        @Override
        public ReservationTime save(ReservationTime reservationTime) {
            ReservationTime saved = ReservationTime.of(idCounter++, reservationTime.getStartAt());
            times.add(saved);
            return saved;
        }

        @Override
        public int deleteById(Long id) {
            return times.removeIf(t -> t.getId().equals(id)) ? 1 : 0;
        }

        @Override
        public boolean existsByStartAt(LocalTime startAt) {
            return times.stream().anyMatch(t -> t.getStartAt().equals(startAt));
        }
    }

    private static class FakeReservationRepository implements ReservationRepository {

        private final List<Long> reservedTimeIds = new ArrayList<>();
        private int count = 0;

        public void setCount(int count) {
            this.count = count;
        }

        public void addReservedTime(Long timeId) {
            reservedTimeIds.add(timeId);
        }

        @Override
        public int countByTimeId(Long timeId) {
            return count;
        }

        @Override
        public List<Long> findReservedTimes(Long themeId, Long dateId) {
            return reservedTimeIds;
        }

        // 나머지 미사용 메서드
        @Override
        public Reservation save(Reservation r) {
            return null;
        }

        @Override
        public List<Reservation> findAll() {
            return null;
        }

        @Override
        public int deleteById(Long id) {
            return 0;
        }

        @Override
        public int countByReservationDateId(Long id) {
            return 0;
        }

        @Override
        public int countByThemeId(Long id) {
            return 0;
        }

        @Override
        public List<Reservation> findByName(String name) {
            return null;
        }

        @Override
        public Optional<Reservation> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public int updateReservation(Long id, Long d, Long t) {
            return 0;
        }

        @Override
        public boolean existsByDateIdAndTimeIdAndThemeId(Long d, Long t, Long th) {
            return false;
        }
    }
}
