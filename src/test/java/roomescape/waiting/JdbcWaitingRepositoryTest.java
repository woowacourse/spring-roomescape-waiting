package roomescape.waiting.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.waiting.adapter.out.persistence.JdbcWaitingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import(JdbcWaitingRepository.class)
class JdbcWaitingRepositoryTest {

    private static final long MEMBER_ID = 1L;
    private static final long SLOT_ID = 1L;

    @Autowired
    private JdbcWaitingRepository waitingRepository;

    @Test
    @DisplayName("대기 저장에 성공한다.")
    void save_테스트() {
        Waiting waiting = Waiting.create(MEMBER_ID, SLOT_ID);

        Waiting savedWaiting = waitingRepository.save(waiting);

        assertThat(savedWaiting.getId()).isNotNull();
        assertThat(savedWaiting.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(savedWaiting.getSlotId()).isEqualTo(SLOT_ID);
    }

    @Test
    @DisplayName("회원과 슬롯로 대기 존재 여부를 확인할 수 있다.")
    void existsBySlotIdAndMemberId_true_테스트() {
        waitingRepository.save(Waiting.create(MEMBER_ID, SLOT_ID));

        boolean result = waitingRepository.existsBySlotIdAndMemberId(MEMBER_ID, SLOT_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("회원과 슬롯로 대기가 없으면 false를 반환한다.")
    void existsBySlotIdAndMemberId_false_테스트() {
        boolean result = waitingRepository.existsBySlotIdAndMemberId(999L, 999L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("대기 id로 대기를 조회할 수 있다.")
    void findById_테스트() {
        Waiting savedWaiting = waitingRepository.save(Waiting.create(MEMBER_ID, SLOT_ID));

        Waiting result = waitingRepository.findById(savedWaiting.getId())
                .orElseThrow();

        assertThat(result.getId()).isEqualTo(savedWaiting.getId());
        assertThat(result.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(result.getSlotId()).isEqualTo(SLOT_ID);
    }

    @Test
    @DisplayName("대기 id로 대기를 삭제할 수 있다.")
    void deleteById_테스트() {
        Waiting savedWaiting = waitingRepository.save(Waiting.create(MEMBER_ID, SLOT_ID));

        waitingRepository.deleteById(savedWaiting.getId());

        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("특정 슬롯의 대기 목록을 신청 순서대로 조회할 수 있다.")
    void findAllBySlotIdOrderById_테스트() {
        Waiting first = waitingRepository.save(Waiting.create(3L, SLOT_ID));
        Waiting second = waitingRepository.save(Waiting.create(2L, SLOT_ID));
        Waiting otherSlotWaiting = waitingRepository.save(Waiting.create(4L, 2L));
        Waiting third = waitingRepository.save(Waiting.create(MEMBER_ID, SLOT_ID));

        List<Waiting> result = waitingRepository.findAllBySlotIdOrderById(SLOT_ID);

        assertThat(result).extracting(Waiting::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(result).extracting(Waiting::getId)
                .doesNotContain(otherSlotWaiting.getId());
    }

    @Test
    @DisplayName("여러 슬롯의 대기 목록을 한 번에 조회할 수 있다.")
    void findAllBySlotIds_테스트() {
        Waiting firstSlotFirst = waitingRepository.save(Waiting.create(3L, SLOT_ID));
        Waiting firstSlotSecond = waitingRepository.save(Waiting.create(2L, SLOT_ID));
        Waiting secondSlotFirst = waitingRepository.save(Waiting.create(4L, 2L));
        Waiting firstSlotThird = waitingRepository.save(Waiting.create(MEMBER_ID, SLOT_ID));

        List<Waiting> result = waitingRepository.findAllBySlotIds(List.of(2L, SLOT_ID));

        assertThat(result).extracting(Waiting::getId)
                .containsExactlyInAnyOrder(
                        firstSlotFirst.getId(),
                        firstSlotSecond.getId(),
                        firstSlotThird.getId(),
                        secondSlotFirst.getId()
                );
    }

    @Test
    @DisplayName("날짜와 테마로 대기가 있는 시간 id를 조회할 수 있다.")
    void findTimeIdByDateAndThemeId_테스트() {
        waitingRepository.save(Waiting.create(MEMBER_ID, SLOT_ID));

        Set<Long> result = waitingRepository.findTimeIdByDateAndThemeId(LocalDate.parse("2026-05-05"), 1L);

        assertThat(result).containsExactly(1L);
    }
}
