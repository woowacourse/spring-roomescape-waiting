package roomescape.reservation.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.theme.domain.Theme;

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    void 전체_대기예약을_time_theme_member와_함께_조회한다() {
        List<Waiting> result = waitingRepository.findAllWithAssociations();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTime()).isNotNull();
        assertThat(result.get(0).getTheme()).isNotNull();
        assertThat(result.get(0).getMember()).isNotNull();
    }

    @Test
    void id로_대기예약을_theme_time과_함께_조회한다() {
        Optional<Waiting> result = waitingRepository.findByIdWithAssociations(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTheme()).isNotNull();
        assertThat(result.get().getTime()).isNotNull();
    }

    @Test
    void 특정_멤버의_대기예약을_theme_time과_함께_조회한다() {
        Long memberId = 1L; // 엠제이

        List<Waiting> result = waitingRepository.findByMemberIdWithAssociations(memberId);

        assertThat(result).hasSize(2); // 엠제이: 대기 2건
        assertThat(result).allSatisfy(w -> {
            assertThat(w.getTheme()).isNotNull();
            assertThat(w.getTime()).isNotNull();
        });
    }

    @Test
    void 대기예약_순번을_계산한다() {
        Waiting target = waitingRepository.findById(3L).get(); // 엠제이 2번째 대기
        Long count = waitingRepository.countByThemeAndDateAndTimeAndIdLessThan(
                target.getTheme(), target.getDate(), target.getTime(), target.getId());

        assertThat(count).isEqualTo(1L); // 리사가 먼저 대기함
    }

    @Test
    void 특정_날짜_시간_테마_멤버로_대기_존재여부를_확인한다() {
        boolean exists = waitingRepository.existsByDateAndTimeAndThemeAndMember(
                LocalDate.now().plusDays(3),
                targetTime(),
                targetTheme(),
                targetMember()
        );

        assertThat(exists).isTrue();
    }

    @Test
    void 특정_id와_멤버id를_기반으로_대기_존재여부를_확인한다() {
        boolean exists = waitingRepository.existsByIdAndMember_Id(1L, 1L); // 엠제이의 첫 번째 대기
        assertThat(exists).isTrue();
    }


    private ReservationTime targetTime() {
        return waitingRepository.findById(1L).get().getTime();
    }

    private Theme targetTheme() {
        return waitingRepository.findById(1L).get().getTheme();
    }

    private Member targetMember() {
        return waitingRepository.findById(1L).get().getMember();
    }
}
