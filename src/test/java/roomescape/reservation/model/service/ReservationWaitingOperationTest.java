package roomescape.reservation.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.ReservationTestFixture.createConfirmedReservation;
import static roomescape.ReservationTestFixture.createPendingWaiting;
import static roomescape.ReservationTestFixture.getReservationThemeFixture;
import static roomescape.ReservationTestFixture.getReservationTimeFixture;
import static roomescape.ReservationTestFixture.getUserFixture;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.member.model.Role;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.exception.ReservationException.AlreadyDoneWaitingException;
import roomescape.reservation.model.exception.ReservationException.ReservationNotFoundException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.vo.Schedule;
import roomescape.support.RepositoryTestSupport;

@Import({ReservationWaitingOperation.class, ReservationValidator.class, ReservationWaitingValidator.class})
class ReservationWaitingOperationTest extends RepositoryTestSupport {

    @Autowired
    private ReservationWaitingOperation reservationWaitingOperation;

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationThemeRepository reservationThemeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTheme savedTheme;
    private Member savedMember;
    private ReservationTime savedTime;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        savedTheme = reservationThemeRepository.save(getReservationThemeFixture());
        savedMember = memberRepository.save(getUserFixture());
        savedTime = reservationTimeRepository.save(getReservationTimeFixture());
        testDate = LocalDate.now().plusDays(5);
    }

    @Test
    @DisplayName("예약이 존재하고 대기하지 않은 사용자는 대기 등록에 성공한다")
    void waiting_success() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        Reservation existingReservation = createConfirmedReservation(testDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(existingReservation);

        Member waitingMember = savedMember;

        // when
        reservationWaitingOperation.waiting(schedule, waitingMember.getId());

        // then
        assertThat(reservationWaitingRepository.getAll()).hasSize(1);
        assertThat(reservationWaitingRepository.getAll().get(0).getMember().getId()).isEqualTo(waitingMember.getId());

    }

    @Test
    @DisplayName("예약이 존재하지 않으면 대기 등록에서 예외가 발생한다")
    void waiting_throws_exception_when_reservation_not_exists() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        // when & then
        assertThatThrownBy(() -> reservationWaitingOperation.waiting(schedule, savedMember.getId()))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("이미 대기 중인 사용자는 대기 등록에서 예외가 발생한다")
    void waiting_throws_exception_when_already_waiting() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        Reservation existingReservation = createConfirmedReservation(testDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(existingReservation);

        Member waitingMember = Member.builder()
                .name("대기자")
                .email("이메일")
                .password("패스워드")
                .role(Role.USER)
                .build();
        memberRepository.save(waitingMember);
        ReservationWaiting existingWaiting = createPendingWaiting(testDate, savedTime, savedTheme, waitingMember);
        reservationWaitingRepository.save(existingWaiting);

        // when & then
        assertThatThrownBy(() -> reservationWaitingOperation.waiting(schedule, waitingMember.getId()))
                .isInstanceOf(AlreadyDoneWaitingException.class);
    }
}
