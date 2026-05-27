package roomescape.waiting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.waiting.infrastructure.JdbcWaitingRepository;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Transactional
@ActiveProfiles("test")
@Import(JdbcWaitingRepository.class)
class JdbcWaitingRepositoryTest {

    private static final long MEMBER_ID = 1L;
    private static final long SCHEDULE_ID = 1L;

    @Autowired
    private JdbcWaitingRepository waitingRepository;

    @Test
    @DisplayName("대기 저장에 성공한다.")
    void save_테스트() {
        Waiting waiting = new Waiting(null, MEMBER_ID, SCHEDULE_ID);

        Waiting savedWaiting = waitingRepository.save(waiting);

        assertThat(savedWaiting.getId()).isNotNull();
        assertThat(savedWaiting.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(savedWaiting.getScheduleId()).isEqualTo(SCHEDULE_ID);
    }

    @Test
    @DisplayName("회원과 스케줄로 대기 존재 여부를 확인할 수 있다.")
    void existsByScheduleIdAndMemberId_true_테스트() {
        waitingRepository.save(new Waiting(null, MEMBER_ID, SCHEDULE_ID));

        boolean result = waitingRepository.existsByScheduleIdAndMemberId(MEMBER_ID, SCHEDULE_ID);

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
        Waiting savedWaiting = waitingRepository.save(new Waiting(null, MEMBER_ID, SCHEDULE_ID));

        Waiting result = waitingRepository.findById(savedWaiting.getId())
                .orElseThrow();

        assertThat(result.getId()).isEqualTo(savedWaiting.getId());
        assertThat(result.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(result.getScheduleId()).isEqualTo(SCHEDULE_ID);
    }

    @Test
    @DisplayName("대기 id로 대기를 삭제할 수 있다.")
    void deleteById_테스트() {
        Waiting savedWaiting = waitingRepository.save(new Waiting(null, MEMBER_ID, SCHEDULE_ID));

        waitingRepository.deleteById(savedWaiting.getId());

        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("특정 대기 id까지의 순번을 조회할 수 있다.")
    void countByScheduleIdAndIdLessThanEqual_테스트() {
        waitingRepository.save(new Waiting(null, 3L, SCHEDULE_ID));
        waitingRepository.save(new Waiting(null, 2L, SCHEDULE_ID));
        Waiting myWaiting = waitingRepository.save(new Waiting(null, MEMBER_ID, SCHEDULE_ID));
        waitingRepository.save(new Waiting(null, 4L, SCHEDULE_ID));

        long count = waitingRepository.countByScheduleIdAndIdLessThanEqual(SCHEDULE_ID, myWaiting.getId());

        assertThat(count).isEqualTo(3L);
    }

}
