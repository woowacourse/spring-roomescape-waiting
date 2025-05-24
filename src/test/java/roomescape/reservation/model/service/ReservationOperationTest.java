package roomescape.reservation.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.ReservationTestFixture.createConfirmedReservation;
import static roomescape.ReservationTestFixture.createPendingWaiting;
import static roomescape.ReservationTestFixture.createUser;
import static roomescape.ReservationTestFixture.getReservationThemeFixture;
import static roomescape.ReservationTestFixture.getReservationTimeFixture;
import static roomescape.ReservationTestFixture.getUserFixture;
import static roomescape.reservation.model.entity.vo.ReservationStatus.CONFIRMED;
import static roomescape.reservation.model.entity.vo.ReservationWaitingStatus.ACCEPTED;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.entity.vo.ReservationStatus;
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.vo.Schedule;
import roomescape.support.RepositoryTestSupport;

@Import({ReservationOperation.class, ReservationValidator.class})
class ReservationOperationTest extends RepositoryTestSupport {

    @Autowired
    private ReservationOperation reservationOperation;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationThemeRepository reservationThemeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    private Member savedMember;
    private ReservationTime savedTime;
    private ReservationTheme savedTheme;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        savedMember = memberRepository.save(getUserFixture());
        savedTime = reservationTimeRepository.save(getReservationTimeFixture());
        savedTheme = reservationThemeRepository.save(getReservationThemeFixture());
        testDate = LocalDate.now().plusDays(5);
    }

    @Test
    @DisplayName("예약을 성공적으로 생성한다")
    void reserve_success() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        // when
        Reservation result = reservationOperation.reserve(schedule, savedMember.getId());

        // then
        SoftAssertions.assertSoftly(softly ->{
            softly.assertThat(result.getDate()).isEqualTo(testDate);
            softly.assertThat(result.getTime()).isEqualTo(savedTime);
            softly.assertThat(result.getTheme()).isEqualTo(savedTheme);
            softly.assertThat(result.getMember()).isEqualTo(savedMember);
            softly.assertThat(result.getStatus()).isEqualTo(CONFIRMED);
        });
    }

    @Test
    @DisplayName("중복된 스케줄로 예약 시 예외가 발생한다")
    void reserve_duplicated_schedule_throws_exception() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        Reservation existingReservation = createConfirmedReservation(testDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(existingReservation);

        // when & then
        assertThatThrownBy(() -> reservationOperation.reserve(schedule, savedMember.getId()))
                .isInstanceOf(InvalidReservationTimeException.class);
    }

    @Test
    @DisplayName("예약을 취소하면 상태가 CANCELED로 변경된다")
    void cancel_changes_status_to_cancelled() {
        // given
        Reservation reservation = createConfirmedReservation(testDate, savedTime, savedTheme, savedMember);
        Reservation savedReservation = reservationRepository.save(reservation);

        // when
        reservationOperation.cancel(savedReservation);

        // then
        Reservation updatedReservation = reservationRepository.findById(savedReservation.getId()).get();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("예약 취소 시 첫 번째 예약대기 있을 경우, 해당 예약대기의 정보로 예약확정이 된다.")
    void cancel_automatically_accepts_waiting_reservation() {
        // given
        Reservation reservation = createConfirmedReservation(testDate, savedTime, savedTheme, savedMember);
        Reservation savedReservation = reservationRepository.save(reservation);

        Member waitingMember = memberRepository.save(createUser("대기자", "waiting@test.com", "1234"));
        ReservationWaiting waiting = createPendingWaiting(testDate, savedTime, savedTheme, waitingMember);
        reservationWaitingRepository.save(waiting);

        // when
        reservationOperation.cancel(savedReservation);

        // then
        Optional<Reservation> newReservation = reservationRepository.getAllByStatuses(List.of(CONFIRMED)).stream()
                .filter(r -> r.getMember().getId().equals(waitingMember.getId()))
                .findFirst();
        assertThat(newReservation.get().getMember().getId()).isEqualTo(waitingMember.getId());
    }

    @Test
    @DisplayName("예약 취소 시 첫 번째 예약대기 있을 경우, 예약대기가 승인 상태로 변경된다.")
    void cancel_change_to_accepts_waiting_reservation_status() {
        // given
        Reservation reservation = createConfirmedReservation(testDate, savedTime, savedTheme, savedMember);
        Reservation savedReservation = reservationRepository.save(reservation);

        Member waitingMember = memberRepository.save(createUser("대기자", "waiting@test.com", "1234"));
        ReservationWaiting waiting = createPendingWaiting(testDate, savedTime, savedTheme, waitingMember);
        ReservationWaiting firstWaiting = reservationWaitingRepository.save(waiting);

        // when
        reservationOperation.cancel(savedReservation);

        // then
        ReservationWaiting reservationWaiting = reservationWaitingRepository.getById(firstWaiting.getId());
        assertThat(reservationWaiting.getStatus()).isEqualTo(ACCEPTED);
    }

    @Test
    @DisplayName("예약 취소 시 대기 중인 예약이 없으면 새로운 예약이 생성되지 않는다")
    void cancel_without_waiting_reservation() {
        // given
        Reservation reservation = createConfirmedReservation(testDate, savedTime, savedTheme, savedMember);
        Reservation savedReservation = reservationRepository.save(reservation);

        List<Reservation> beforeReservations = reservationRepository.getAllByStatuses(List.of(CONFIRMED));

        // when
        reservationOperation.cancel(savedReservation);

        // then
        List<Reservation> afterReservations = reservationRepository.getAllByStatuses(List.of(CONFIRMED));
        assertThat(beforeReservations).hasSize(1);
        assertThat(afterReservations).hasSize(0);
    }
}
