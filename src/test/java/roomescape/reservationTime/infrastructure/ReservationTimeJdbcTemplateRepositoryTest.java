package roomescape.reservationTime.infrastructure;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.reservationTime.domain.ReservationTime;

@JdbcTest
@Import(ReservationTimeJdbcTemplateRepository.class)
class ReservationTimeJdbcTemplateRepositoryTest {

    private final ReservationTimeJdbcTemplateRepository timeRepository;

    @Autowired
    ReservationTimeJdbcTemplateRepositoryTest(ReservationTimeJdbcTemplateRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    @Test
    @DisplayName("예약 시간을 저장한다")
    void save_success() {
        // given
        ReservationTime testReservationTime = ReservationTime.create(
                LocalTime.now()
        );

        // when
        ReservationTime result = timeRepository.save(testReservationTime);

        // then
        Assertions.assertNotNull(result.getId());
    }

    @Test
    @DisplayName("아이디에 맞는 예약 시간이 존재하면 조회한다")
    void findById_success() {
        // given
        ReservationTime time1 = ReservationTime.create(LocalTime.of(12, 0));
        ReservationTime time2 = ReservationTime.create(LocalTime.of(13, 0));
        timeRepository.save(time1);
        ReservationTime saved2 = timeRepository.save(time2);

        // when
        Optional<ReservationTime> result = timeRepository.findById(saved2.getId());

        // then
        Assertions.assertTrue(result.isPresent());

        LocalTime foundTime = result.get().getStartAt();
        Assertions.assertEquals(foundTime, saved2.getStartAt());
    }

    @Test
    @DisplayName("존재하지 않는 아이디로 예약 시간을 조회하면 빈 Optional을 반환한다")
    void findById_success_when_not_found() {
        // when
        long notExistTimeId = 999L;
        Optional<ReservationTime> result = timeRepository.findById(notExistTimeId);

        // then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("전체 예약 시간 목록을 조회한다")
    void findAll_success() {
        // given
        ReservationTime time1 = ReservationTime.create(LocalTime.of(11, 0));
        ReservationTime time2 = ReservationTime.create(LocalTime.of(12, 0));
        ReservationTime time3 = ReservationTime.create(LocalTime.of(13, 0));

        timeRepository.save(time1);
        timeRepository.save(time2);
        timeRepository.save(time3);

        // when
        List<ReservationTime> result = timeRepository.findAll();

        // then
        Assertions.assertEquals(3, result.size());
    }

    @Test
    @DisplayName("아이디를 기반으로 예약 시간을 삭제한다")
    void deleteById_success() {
        // given
        ReservationTime time = ReservationTime.create(LocalTime.of(11, 0));
        ReservationTime saved = timeRepository.save(time);

        // when
        Long deleteTargetId = saved.getId();
        timeRepository.deleteById(deleteTargetId);

        // then
        Optional<ReservationTime> deleteTargetFindResult = timeRepository.findAll()
                .stream()
                .filter(reservationTime -> reservationTime.getId().equals(deleteTargetId))
                .findAny();
        Assertions.assertTrue(deleteTargetFindResult.isEmpty());
    }
}
