package roomescape.reservation.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.ReservationTestFixture.createConfirmedReservation;
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
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.exception.ReservationException.ReservationTimeInUseException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.support.RepositoryTestSupport;

@Import({ReservationTimeOperation.class, ReservationTimeValidator.class})
class ReservationTimeOperationTest extends RepositoryTestSupport {

    @Autowired
    private ReservationTimeOperation reservationTimeOperation;

    @Autowired
    private ReservationThemeRepository reservationThemeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTheme savedTheme;
    private Member savedMember;
    private ReservationTime savedTime;

    @BeforeEach
    void setUp() {
        savedTheme = reservationThemeRepository.save(getReservationThemeFixture());
        savedMember = memberRepository.save(getUserFixture());
        savedTime = reservationTimeRepository.save(getReservationTimeFixture());
    }

    @Test
    @DisplayName("사용하지 않는 시간을 성공적으로 삭제한다")
    void removeTime_success() {
        // given
        ReservationTime unusedTime = savedTime;

        // when
        reservationTimeOperation.removeTime(unusedTime);

        // then
        assertThat(reservationTimeRepository.findById(unusedTime.getId())).isEmpty();
    }

    @Test
    @DisplayName("현재 예약에서 사용 중인 시간 삭제 시 예외가 발생한다")
    void removeTime_throws_exception() {
        // given
        Reservation reservation = createConfirmedReservation(
                LocalDate.now().plusDays(2),
                savedTime,
                savedTheme,
                savedMember
        );
        reservationRepository.save(reservation);

        // when & then
        assertThatThrownBy(() -> reservationTimeOperation.removeTime(savedTime))
                .isInstanceOf(ReservationTimeInUseException.class);
    }
}
