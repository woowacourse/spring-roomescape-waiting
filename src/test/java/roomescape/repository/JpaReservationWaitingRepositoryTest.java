package roomescape.repository;

import static roomescape.fixture.ReservationFixture.DEFAULT_RESERVATION;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ReservationWaitingFixture.DEFAULT_WAITING;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.ReservationWaiting;

@SpringBootTest
class JpaReservationWaitingRepositoryTest extends DatabaseClearBeforeEachTest {
    @Autowired
    private ReservationWaitingRepository waitingRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public void doAfterClear() {
        reservationTimeRepository.save(DEFAULT_TIME);
        themeRepository.save(DEFAULT_THEME);
        reservationRepository.save(DEFAULT_RESERVATION);
    }

    @Test
    @DisplayName("예약 대기를 잘 저장하는지 확인")
    void save() {
        List<ReservationWaiting> beforeSave = waitingRepository.findByReservation(DEFAULT_RESERVATION);
        ReservationWaiting save = waitingRepository.save(DEFAULT_WAITING);
        List<ReservationWaiting> afterSave = waitingRepository.findByReservation(DEFAULT_RESERVATION);

        Assertions.assertThat(afterSave)
                .containsAll(beforeSave)
                .contains(save);
    }

    @Test
    @DisplayName("회원 아이디를 기준으로 예약 대기를 잘 조회하는지 확인")
    void findAllByMemberId() {
        ReservationWaiting save = waitingRepository.save(DEFAULT_WAITING);
        List<ReservationWaiting> byMemberId = waitingRepository.findAllByMemberId(
                DEFAULT_WAITING.getWaitingMember().getId());

        Assertions.assertThat(byMemberId)
                .contains(save);
    }

    @Test
    @DisplayName("예약을 기준으로 예약 대기를 잘 조회하는지 확인")
    void findByReservation() {
        ReservationWaiting save = waitingRepository.save(DEFAULT_WAITING);
        List<ReservationWaiting> byReservation = waitingRepository.findByReservation(DEFAULT_RESERVATION);

        Assertions.assertThat(byReservation)
                .contains(save);
    }

    @Test
    @DisplayName("예약과 대기 회원을 기준으로 예약 대기 존재 여부를 잘 확인하는지 확인")
    void existsByReservationAndWaitingMember() {
        waitingRepository.save(DEFAULT_WAITING);
        boolean exists = waitingRepository.existsByReservationAndWaitingMember(DEFAULT_WAITING.getReservation(),
                DEFAULT_WAITING.getWaitingMember());

        Assertions.assertThat(exists)
                .isTrue();
    }

    @Test
    @DisplayName("예약 대기를 잘 지우는지 확인")
    void delete() {
        waitingRepository.save(DEFAULT_WAITING);
        List<ReservationWaiting> beforeDelete = waitingRepository.findByReservation(DEFAULT_RESERVATION);
        waitingRepository.delete(DEFAULT_WAITING.getId());
        List<ReservationWaiting> afterDelete = waitingRepository.findByReservation(DEFAULT_RESERVATION);

        Assertions.assertThat(beforeDelete)
                .containsAll(afterDelete)
                .contains(DEFAULT_WAITING);
    }
}
