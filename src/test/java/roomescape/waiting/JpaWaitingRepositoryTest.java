package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.waiting.adapter.out.persistence.JpaWaitingRepository;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;
import roomescape.waiting.domain.Waiting;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaWaitingRepository.class)
class JpaWaitingRepositoryTest {

    private static final long MEMBER_ID = 1L;
    private static final long SLOT_ID = 1L;

    @Autowired
    private JpaWaitingRepository waitingRepository;

    @Test
    @DisplayName("대기 저장에 성공한다.")
    void saves_waiting_successfully() {
        Waiting waiting = Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(SLOT_ID));

        Waiting savedWaiting = waitingRepository.save(waiting);

        assertThat(savedWaiting.getId()).isNotNull();
        assertThat(savedWaiting.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(savedWaiting.getSlotId()).isEqualTo(SLOT_ID);
    }

    @Test
    @DisplayName("회원과 슬롯로 대기 존재 여부를 확인할 수 있다.")
    void checks_waiting_existence_by_member_and_slot() {
        waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(SLOT_ID)));

        boolean result = waitingRepository.existsBySlotIdAndMemberId(MEMBER_ID, SLOT_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("회원과 슬롯로 대기가 없으면 false를 반환한다.")
    void missing_waiting_by_member_and_slot_returns_false() {
        boolean result = waitingRepository.existsBySlotIdAndMemberId(999L, 999L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("회원 id와 슬롯 id 조합으로 대기 존재 여부를 정확히 확인한다.")
    void checks_waiting_existence_by_distinct_member_and_slot_ids() {
        waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(3L), roomescape.TestFixtures.slot(SLOT_ID)));

        assertSoftly(softly -> {
            softly.assertThat(waitingRepository.existsBySlotIdAndMemberId(3L, SLOT_ID)).isTrue();
            softly.assertThat(waitingRepository.existsBySlotIdAndMemberId(MEMBER_ID, 3L)).isFalse();
        });
    }

    @Test
    @DisplayName("대기 id로 대기를 조회할 수 있다.")
    void finds_waiting_by_id_successfully() {
        Waiting savedWaiting = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(SLOT_ID)));

        Waiting result = waitingRepository.findById(savedWaiting.getId())
                .orElseThrow();

        assertThat(result.getId()).isEqualTo(savedWaiting.getId());
        assertThat(result.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(result.getSlotId()).isEqualTo(SLOT_ID);
    }

    @Test
    @DisplayName("대기 id로 락을 걸고 대기를 조회할 수 있다.")
    void finds_waiting_by_id_with_lock_successfully() {
        Waiting savedWaiting = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(SLOT_ID)));

        Waiting result = waitingRepository.findByIdForUpdate(savedWaiting.getId())
                .orElseThrow();

        assertThat(result.getId()).isEqualTo(savedWaiting.getId());
        assertThat(result.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(result.getSlotId()).isEqualTo(SLOT_ID);
    }

    @Test
    @DisplayName("대기 id로 대기를 삭제할 수 있다.")
    void deletes_waiting_by_id_successfully() {
        Waiting savedWaiting = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(SLOT_ID)));

        waitingRepository.deleteById(savedWaiting.getId());

        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("특정 슬롯의 대기 목록을 신청 순서대로 조회할 수 있다.")
    void finds_waitings_by_slot_id_in_request_order() {
        Waiting first = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(3L), roomescape.TestFixtures.slot(SLOT_ID)));
        Waiting second = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(2L), roomescape.TestFixtures.slot(SLOT_ID)));
        Waiting otherSlotWaiting = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(4L), roomescape.TestFixtures.slot(2L)));
        Waiting third = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(SLOT_ID)));

        List<Waiting> result = waitingRepository.findAllBySlotIdOrderById(SLOT_ID);

        assertThat(result).extracting(Waiting::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(result).extracting(Waiting::getId)
                .doesNotContain(otherSlotWaiting.getId());
    }

    @Test
    @DisplayName("특정 슬롯의 대기 목록을 락을 걸고 신청 순서대로 조회할 수 있다.")
    void finds_waitings_by_slot_id_with_lock_in_request_order() {
        Waiting first = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(3L), roomescape.TestFixtures.slot(SLOT_ID)));
        Waiting second = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(2L), roomescape.TestFixtures.slot(SLOT_ID)));
        Waiting otherSlotWaiting = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(4L), roomescape.TestFixtures.slot(2L)));
        Waiting third = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(SLOT_ID)));

        List<Waiting> result = waitingRepository.findAllBySlotIdOrderByIdForUpdate(SLOT_ID);

        assertThat(result).extracting(Waiting::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(result).extracting(Waiting::getId)
                .doesNotContain(otherSlotWaiting.getId());
    }

    @Test
    @DisplayName("여러 슬롯의 대기 목록을 한 번에 조회할 수 있다.")
    void finds_waitings_for_multiple_slots_at_once() {
        Waiting firstSlotFirst = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(3L), roomescape.TestFixtures.slot(SLOT_ID)));
        Waiting firstSlotSecond = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(2L), roomescape.TestFixtures.slot(SLOT_ID)));
        Waiting secondSlotFirst = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(4L), roomescape.TestFixtures.slot(2L)));
        Waiting firstSlotThird = waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(SLOT_ID)));

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
    @DisplayName("빈 슬롯 id 목록으로 대기를 조회하면 빈 목록을 반환한다.")
    void empty_slot_ids_return_empty_waitings() {
        waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(3L), roomescape.TestFixtures.slot(SLOT_ID)));

        List<Waiting> result = waitingRepository.findAllBySlotIds(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("특정 회원의 대기 상세 목록을 대기 신청 순서대로 조회할 수 있다.")
    void finds_all_waiting_details_by_member_id_in_request_order() {
        Waiting otherMemberWaiting = waitingRepository.save(
                Waiting.create(roomescape.TestFixtures.member(2L), roomescape.TestFixtures.slot(SLOT_ID)));
        Waiting first = waitingRepository.save(
                Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(SLOT_ID)));
        Waiting second = waitingRepository.save(
                Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(2L)));

        List<WaitingDetailProjection> result = waitingRepository.findAllWaitingDetailsByMemberId(MEMBER_ID);

        assertThat(result).extracting(WaitingDetailProjection::id)
                .containsExactly(first.getId(), second.getId());
        assertThat(result).extracting(WaitingDetailProjection::id)
                .doesNotContain(otherMemberWaiting.getId());
        assertSoftly(softly -> {
            softly.assertThat(result.getFirst().slotId()).isEqualTo(SLOT_ID);
            softly.assertThat(result.getFirst().memberName()).isEqualTo("a");
            softly.assertThat(result.getFirst().date()).isEqualTo(LocalDate.parse("2026-05-05"));
            softly.assertThat(result.getFirst().themeId()).isEqualTo(1L);
            softly.assertThat(result.getFirst().themeName()).isEqualTo("세기의 도둑");
            softly.assertThat(result.getFirst().timeId()).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("날짜와 테마로 대기가 있는 시간 id를 조회할 수 있다.")
    void finds_waiting_time_ids_by_date_and_theme() {
        waitingRepository.save(Waiting.create(roomescape.TestFixtures.member(MEMBER_ID), roomescape.TestFixtures.slot(SLOT_ID)));

        Set<Long> result = waitingRepository.findTimeIdByDateAndThemeId(LocalDate.parse("2026-05-05"), 1L);

        assertThat(result).containsExactly(1L);
    }
}
