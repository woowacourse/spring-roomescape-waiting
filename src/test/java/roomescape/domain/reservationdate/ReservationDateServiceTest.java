package roomescape.domain.reservationdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.dto.ReservationDateCreationRequest;
import roomescape.domain.reservationdate.dto.ReservationDateCreationResponse;
import roomescape.domain.reservationdate.dto.ReservationDateResponse;
import roomescape.support.exception.RoomescapeException;

class ReservationDateServiceTest {

    private ReservationDateService reservationDateService;
    private FakeReservationDateRepository reservationDateRepository;
    private FakeReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationDateRepository = new FakeReservationDateRepository();
        reservationRepository = new FakeReservationRepository();
        reservationDateService = new ReservationDateService(reservationRepository, reservationDateRepository);
    }

    @Test
    @DisplayName("예약 날짜를 생성한다.")
    void createReservationDate() {
        ReservationDateCreationRequest request = new ReservationDateCreationRequest(LocalDate.now().plusDays(1));

        ReservationDateCreationResponse response = reservationDateService.createReservationDate(request);

        assertThat(response.playDay()).isEqualTo(request.playDay());
        assertThat(reservationDateRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("중복된 날짜 생성 시 예외가 발생한다.")
    void createDuplicateDate() {
        LocalDate playDay = LocalDate.now().plusDays(1);
        reservationDateService.createReservationDate(new ReservationDateCreationRequest(playDay));

        assertThatThrownBy(
            () -> reservationDateService.createReservationDate(new ReservationDateCreationRequest(playDay)))
            .isInstanceOf(RoomescapeException.class);
    }

    @Test
    @DisplayName("오늘 이후의 날짜만 조회한다.")
    void getAllAvailableReservationDate() {
        reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().minusDays(1)));
        reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));

        List<ReservationDateResponse> responses = reservationDateService.getAllAvailableReservationDate();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).playDay()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    @DisplayName("사용 중인 날짜를 삭제하려 하면 예외가 발생한다.")
    void deleteInUseDate() {
        ReservationDate date = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        reservationRepository.setCount(1);

        assertThatThrownBy(() -> reservationDateService.deleteReservationDate(date.getId()))
            .isInstanceOf(RoomescapeException.class);
    }

    private static class FakeReservationDateRepository implements ReservationDateRepository {

        private final List<ReservationDate> dates = new ArrayList<>();
        private Long idCounter = 1L;

        @Override
        public Optional<ReservationDate> findById(Long id) {
            return dates.stream().filter(d -> d.getId().equals(id)).findFirst();
        }

        @Override
        public List<ReservationDate> findAll() {
            return dates;
        }

        @Override
        public ReservationDate save(ReservationDate reservationDate) {
            ReservationDate saved = ReservationDate.of(idCounter++, reservationDate.getPlayDay());
            dates.add(saved);
            return saved;
        }

        @Override
        public int deleteById(Long id) {
            return dates.removeIf(d -> d.getId().equals(id)) ? 1 : 0;
        }

        @Override
        public boolean existsByPlayDay(LocalDate playDay) {
            return dates.stream().anyMatch(d -> d.getPlayDay().equals(playDay));
        }
    }

    private static class FakeReservationRepository implements ReservationRepository {

        private int count = 0;

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public int countByReservationDateId(Long dateId) {
            return count;
        }

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
        public int countByTimeId(Long id) {
            return 0;
        }

        @Override
        public List<Long> findReservedTimes(Long themeId, Long dateId) {
            return null;
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
