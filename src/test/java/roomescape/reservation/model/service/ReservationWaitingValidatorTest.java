package roomescape.reservation.model.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.exception.ReservationException.AlreadyDoneWaitingException;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.vo.Schedule;
import roomescape.support.RepositoryTestSupport;

@Import(ReservationWaitingValidator.class)
class ReservationWaitingValidatorTest extends RepositoryTestSupport {

    @Autowired
    private ReservationWaitingValidator reservationWaitingValidator;

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

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
    @DisplayName("대기하지 않은 사용자는 중복 대기 검증을 통과한다")
    void validateAlreadyWaiting_success_not_waiting() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        // when & then
        assertThatCode(() -> reservationWaitingValidator.validateAlreadyWaiting(schedule, savedMember.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 대기 중인 사용자는 중복 대기 검증에서 예외가 발생한다")
    void validateAlreadyWaiting_throws_exception_already_waiting() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        ReservationWaiting existingWaiting = createPendingWaiting(testDate, savedTime, savedTheme, savedMember);
        reservationWaitingRepository.save(existingWaiting);

        // when & then
        assertThatThrownBy(() -> reservationWaitingValidator.validateAlreadyWaiting(schedule, savedMember.getId()))
                .isInstanceOf(AlreadyDoneWaitingException.class);
    }
}
