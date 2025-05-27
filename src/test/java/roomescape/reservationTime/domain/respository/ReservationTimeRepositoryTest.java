package roomescape.reservationTime.domain.respository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.infrastructure.ReservationTimeRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Import(ReservationTimeRepositoryAdapter.class)
class ReservationTimeRepositoryTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @DisplayName("예약 시간을 저장한다")
    @Test
    void save() {
        // given
        // 오전 10시로 시작 시간 설정
        LocalTime startAt = LocalTime.of(10, 0);
        // 예약 시간 객체 생성
        ReservationTime time = new ReservationTime(startAt);

        // when
        ReservationTime savedTime = timeRepository.save(time);

        // then
        assertThat(savedTime.getId()).isNotNull();
        assertThat(savedTime.getStartAt()).isEqualTo(startAt);
    }

    @DisplayName("시작 시간으로 예약 시간 존재 여부를 확인한다")
    @Test
    void existsByStartAt() {
        // given
        // 오전 10시로 시작 시간 설정
        LocalTime startAt = LocalTime.of(10, 0);
        // 예약 시간 객체 생성
        ReservationTime time = new ReservationTime(startAt);
        // 예약 시간 저장
        timeRepository.save(time);

        // when
        boolean exists = timeRepository.existsByStartAt(startAt);

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("모든 예약 시간을 조회한다")
    @Test
    void findAll() {
        // given
        // 오전 10시 예약 시간 객체 생성 및 저장
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time1);

        // 오후 12시 예약 시간 객체 생성 및 저장
        ReservationTime time2 = new ReservationTime(LocalTime.of(12, 0));
        timeRepository.save(time2);

        // when
        List<ReservationTime> times = timeRepository.findAll();

        // then
        assertThat(times).hasSize(2);
        assertThat(times).extracting("startAt")
                .containsExactlyInAnyOrder(LocalTime.of(10, 0), LocalTime.of(12, 0));
    }

    @DisplayName("ID로 예약 시간을 조회한다")
    @Test
    void findById() {
        // given
        // 오전 10시로 시작 시간 설정
        LocalTime startAt = LocalTime.of(10, 0);
        // 예약 시간 객체 생성
        ReservationTime time = new ReservationTime(startAt);
        // 예약 시간 저장 및 저장된 객체 참조
        ReservationTime savedTime = timeRepository.save(time);

        // when
        Optional<ReservationTime> foundTime = timeRepository.findById(savedTime.getId());

        // then
        assertThat(foundTime).isPresent();
        assertThat(foundTime.get().getId()).isEqualTo(savedTime.getId());
        assertThat(foundTime.get().getStartAt()).isEqualTo(startAt);
    }

    @DisplayName("ID로 예약 시간을 삭제한다")
    @Test
    void deleteById() {
        // given
        // 오전 10시로 시작 시간 설정
        LocalTime startAt = LocalTime.of(10, 0);
        // 예약 시간 객체 생성
        ReservationTime time = new ReservationTime(startAt);
        // 예약 시간 저장 및 저장된 객체 참조
        ReservationTime savedTime = timeRepository.save(time);
        // 저장된 예약 시간의 ID 추출
        Long timeId = savedTime.getId();

        // when
        timeRepository.deleteById(timeId);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<ReservationTime> foundTime = timeRepository.findById(timeId);
        assertThat(foundTime).isEmpty();
    }
}
