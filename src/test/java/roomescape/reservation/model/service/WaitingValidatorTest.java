package roomescape.reservation.model.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.ReservationTestFixture;
import roomescape.member.model.Member;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.WaitingRepository;

@SpringBootTest
@Transactional
class WaitingValidatorTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingValidator waitingValidator;

    @PersistenceContext
    EntityManager em;

    private ReservationTime time;
    private ReservationTheme theme;
    private Member member;
    private LocalDate tomorrow = LocalDate.now();

    @BeforeEach
    void setUp() {
        time = ReservationTestFixture.getReservationTimeFixture();
        em.persist(time);

        theme = ReservationTestFixture.getReservationThemeFixture();
        em.persist(theme);

        member = ReservationTestFixture.getUserFixture();
        em.persist(member);
    }

    @DisplayName("내 예약 중 예약 중복이 없다면 예외가 발생하지 않는다")
    @Test
    void noExceptionWhenNoDuplication() {
        assertDoesNotThrow(() ->
            waitingValidator.validateNoDuplication(LocalDate.now(), 1L, 1L, 1L)
        );
    }

    @DisplayName("내 대기 중 예약 중복이 있다면 예외가 발생한다")
    @Test
    void exceptionWhenDuplicationInWaiting() {
        Waiting waiting = new Waiting(theme, tomorrow, time, member);
        waitingRepository.save(waiting);

        assertThatThrownBy(
            () -> waitingValidator.validateNoDuplication(waiting.getDate(),
                waiting.getTime().getId(), waiting.getTheme().getId(), waiting.getMember().getId()))
            .isInstanceOf(
                InvalidReservationTimeException.class);
    }

    @DisplayName("내 예약 중 예약 중복이 있다면 예외가 발생한다")
    @Test
    void exceptionWhenDuplicationInReservation() {
        Reservation reservation = new Reservation(LocalDate.now(), time, theme, member);
        reservationRepository.save(reservation);

        assertThatThrownBy(
            () -> waitingValidator.validateNoDuplication(reservation.getDate(),
                reservation.getTime().getId(), reservation.getTheme().getId(),
                reservation.getMember().getId())).isInstanceOf(
            InvalidReservationTimeException.class);
    }
}
