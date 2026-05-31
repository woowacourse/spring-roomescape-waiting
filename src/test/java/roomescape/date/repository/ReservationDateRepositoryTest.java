package roomescape.date.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JdbcTest
class ReservationDateRepositoryTest {

    private JdbcReservationDateRepository reservationDateRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        reservationDateRepository = new JdbcReservationDateRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("등록된 예약날짜가 여러개이면 조회 시 등록된 개수만큼 반환한다.")
    void findAll() {
        // given
        List<ReservationDate> reservationDates = List.of(
                ReservationDateFixture.oneWeekLater(),
                ReservationDateFixture.twoWeeksLater()
        );
        saveAll(reservationDates);

        // when
        List<ReservationDate> actual = reservationDateRepository.findAll();

        // then
        Assertions.assertThat(actual)
                .hasSize(reservationDates.size());
    }

    @Test
    @DisplayName("등록된 예약날짜와 조회된 예약날짜의 모든 필드는 일치한다")
    void findById() {
        // given
        ReservationDate saved = save(ReservationDateFixture.oneWeekLater());

        // when
        ReservationDate actual = reservationDateRepository.findById(saved.getId()).get();

        // then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    @DisplayName("등록되지 않은 날짜를 조회하면 빈 값을 반환한다.")
    void findById_wrongId() {
        // given
        Long wrongId = Long.MIN_VALUE;

        // when
        Optional<ReservationDate> actual = reservationDateRepository.findById(wrongId);

        // then
        Assertions.assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("예약날짜를 1개 등록하면 예약날짜 데이터 수가 1 증가한다.")
    void save() {
        // given
        List<ReservationDate> reservationDates = List.of();

        // when
        reservationDateRepository.save(ReservationDateFixture.oneWeekLater());

        // then
        Assertions.assertThat(reservationDateRepository.findAll())
                .hasSize(reservationDates.size() + 1);
    }

    @Test
    @DisplayName("등록된 날짜를 활성화할 수 있다.")
    void updateStatus_active() {
        // given
        ReservationDate saved = save(ReservationDateFixture.oneWeekLater());
        saved.updateStatus(true);

        // when
        reservationDateRepository.updateStatus(saved);

        // then
        Assertions.assertThat(reservationDateRepository.findById(saved.getId()).get().isActive())
                .isTrue();
    }

    @Test
    @DisplayName("등록된 날짜를 비활성화할 수 있다.")
    void updateStatus_inactive() {
        // given
        ReservationDate saved = save(ReservationDateFixture.activeOneWeekLater());
        saved.updateStatus(false);

        // when
        reservationDateRepository.updateStatus(saved);

        // then
        Assertions.assertThat(reservationDateRepository.findById(saved.getId()).get().isActive())
                .isFalse();
    }

    @Test
    @DisplayName("이미 등록된 날짜인 지 확인할 수 있다.")
    void existsByDate() {
        // given
        ReservationDate saved = save(ReservationDateFixture.activeOneWeekLater());

        // when
        boolean result = reservationDateRepository.existsByDate(saved.getDate());

        // then
        Assertions.assertThat(result)
                .isTrue();
    }

    private List<ReservationDate> saveAll(List<ReservationDate> reservationDates) {
        List<ReservationDate> savedReservationDates = new ArrayList<>();
        for (ReservationDate reservationDate : reservationDates) {
            savedReservationDates.add(save(reservationDate));
        }
        return savedReservationDates;
    }

    private ReservationDate save(ReservationDate reservationDate) {
        return reservationDateRepository.save(reservationDate);
    }

}
