package roomescape.waiting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.waiting.infrastructure.JdbcWaitingRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Transactional
@ActiveProfiles("test")
@Import(JdbcWaitingRepository.class)
class JdbcWaitingRepositoryTest {

    @Autowired
    private JdbcWaitingRepository waitingRepository;

    @Test
    @DisplayName("예약이 있는 스케줄에는 대기 저장에 성공한다.")
    void save_테스트() {
        Waiting waiting = new Waiting(null, 1L, 1L);

        Waiting savedWaiting = waitingRepository.save(waiting);

        assertThat(savedWaiting.getId()).isNotNull();
        assertThat(savedWaiting.getMemberId()).isEqualTo(1L);
        assertThat(savedWaiting.getScheduleId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("예약이 없는 스케줄에는 DB 제약으로 대기 저장이 실패한다.")
    void save_테스트_2() {
        // test-data.sql 기준: schedule_id=4는 reservation이 존재하지 않음
        Waiting waiting = new Waiting(null, 1L, 4L);

        assertThatThrownBy(() -> waitingRepository.save(waiting))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("회원과 스케줄로 대기 존재 여부를 확인할 수 있다.")
    void existsByScheduleIdAndMemberId_true_테스트() {
        waitingRepository.save(new Waiting(null, 1L, 1L));

        boolean result = waitingRepository.existsByScheduleIdAndMemberId(1L, 1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("회원과 스케줄로 대기가 없으면 false를 반환한다.")
    void existsByScheduleIdAndMemberId_false_테스트() {
        boolean result = waitingRepository.existsByScheduleIdAndMemberId(999L, 999L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("대기 id로 대기를 조회할 수 있다.")
    void findById_테스트() {
        Waiting savedWaiting = waitingRepository.save(new Waiting(null, 1L, 1L));

        Waiting result = waitingRepository.findById(savedWaiting.getId())
                .orElseThrow();

        assertThat(result.getId()).isEqualTo(savedWaiting.getId());
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getScheduleId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("대기 id로 대기를 삭제할 수 있다.")
    void deleteById_테스트() {
        Waiting savedWaiting = waitingRepository.save(new Waiting(null, 1L, 1L));

        waitingRepository.deleteById(savedWaiting.getId());

        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("특정 대기 id까지의 순번을 조회할 수 있다.")
    void countByScheduleIdAndIdLessThanEqual_테스트_1() {
        waitingRepository.save(new Waiting(null, 3L, 1L));
        waitingRepository.save(new Waiting(null, 2L, 1L));
        Waiting myWaiting = waitingRepository.save(new Waiting(null, 1L, 1L));
        waitingRepository.save(new Waiting(null, 4L, 1L));

        long count = waitingRepository.countByScheduleIdAndIdLessThanEqual(1L, myWaiting.getId());

        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("앞 쪽 대기 삭제 시 뒤에 있던 대기의 순번이 1 감소한다.")
    void countByScheduleIdAndIdLessThanEqual_테스트_2() {
        Waiting firstWaiting = waitingRepository.save(new Waiting(null, 3L, 1L));
        Waiting myWaiting = waitingRepository.save(new Waiting(null, 1L, 1L));
        waitingRepository.save(new Waiting(null, 2L, 1L));
        waitingRepository.save(new Waiting(null, 4L, 1L));
        waitingRepository.deleteById(firstWaiting.getId());

        long count = waitingRepository.countByScheduleIdAndIdLessThanEqual(1L, myWaiting.getId());

        assertThat(count).isEqualTo(1L);
    }
}
