package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationWithWaitingOrder;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final ReservationTime VALID_TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme VALID_THEME = new Theme(
            1L,
            "무인도 탈출",
            "갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!",
            "https://picsum.photos/seed/roomescape1/800/600.jpg"
    );
    private static final ReservationCreateCommand VALID_COMMAND_MOA = new ReservationCreateCommand(
            "모아", LocalDate.of(2026, 5, 9), 1L, 1L
    );

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @InjectMocks
    private AdminReservationService reservationService;

    @Test
    @DisplayName("같은 이름+날짜+시간+테마에 이미 예약이 있으면 ReservationConflictException이 발생한다")
    void 같은_날짜_시간_테마에_이미_예약이_있으면_예외가_발생한다() {
        given(reservationTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_TIME));
        given(themeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_THEME));
        given(reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(
                VALID_COMMAND_MOA.name(), VALID_COMMAND_MOA.date(), 1L, 1L))
                .willReturn(true);

        assertThrows(
                ReservationConflictException.class,
                () -> reservationService.create(VALID_COMMAND_MOA)
        );
    }

    @Test
    @DisplayName("충돌이 없으면 정상적으로 예약을 생성한다")
    void 충돌이_없으면_정상적으로_예약을_생성한다() {
        ReservationWithWaitingOrder saved = new ReservationWithWaitingOrder(
                1L, VALID_COMMAND_MOA.name(), VALID_COMMAND_MOA.date(), VALID_TIME, VALID_THEME, 0L);
        given(reservationTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_TIME));
        given(themeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_THEME));
        given(reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(
                VALID_COMMAND_MOA.name(), VALID_COMMAND_MOA.date(), 1L, 1L))
                .willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willReturn(saved);

        ReservationResult created = reservationService.create(VALID_COMMAND_MOA);

        assertThat(created).isEqualTo(ReservationResult.from(saved));
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 예약을 생성하면 ReservationTimeNotFoundException이 발생한다")
    void 존재하지_않는_timeId로_예약시_예외가_발생한다() {
        given(reservationTimeRepository.findByIdWithLock(1L)).willReturn(Optional.empty());

        assertThrows(
                ReservationTimeNotFoundException.class,
                () -> reservationService.create(VALID_COMMAND_MOA)
        );
    }

    @Test
    @DisplayName("존재하지 않는 themeId로 예약을 생성하면 ThemeNotFoundException이 발생한다")
    void 존재하지_않는_themeId로_예약시_예외가_발생한다() {
        given(reservationTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_TIME));
        given(themeRepository.findByIdWithLock(1L)).willReturn(Optional.empty());

        assertThrows(
                ThemeNotFoundException.class,
                () -> reservationService.create(VALID_COMMAND_MOA)
        );
    }

    @Test
    @DisplayName("존재하는 예약은 정상적으로 삭제된다")
    void 존재하는_예약은_정상적으로_삭제된다() {
        given(reservationRepository.existsById(1L)).willReturn(true);

        assertDoesNotThrow(() -> reservationService.delete(1L));
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하면 ReservationNotFoundException이 발생한다")
    void 존재하지_않는_예약_삭제시_예외가_발생한다() {
        given(reservationRepository.existsById(1L)).willReturn(false);

        assertThrows(
                ReservationNotFoundException.class,
                () -> reservationService.delete(1L)
        );
    }

    @Nested
    @DisplayName("테마, 날짜, 시간대가 같을 때")
    class 테마_날짜_시간대가_같을_때 {

        @Test
        @DisplayName("해당 타임 슬롯에 예약이 없다면 얘약을 허용한다")
        void 해당_타임_슬롯에_예약이_없다면_예약을_허용한다() {
            given(reservationTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_TIME));
            given(themeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_THEME));
            given(reservationRepository.existsByNameAndDateAndTimeIdAndThemeId("모아", VALID_COMMAND_MOA.date(), 1L,
                    1L))
                    .willReturn(false);
            given(reservationRepository.save(any(Reservation.class))).willReturn(new ReservationWithWaitingOrder(
                    1L, "모아", VALID_COMMAND_MOA.date(), VALID_TIME, VALID_THEME, 0L));

            ReservationResult created = reservationService.create(VALID_COMMAND_MOA);

            assertThat(created.waitingOrder()).isZero();
        }

        @Nested
        @DisplayName("해당 타임 슬롯에 이미 예약이 있을 때")
        class 해당_타임_슬롯에_이미_예약이_있을_때 {

            @Test
            @DisplayName("기존 예약자와 동일한 사용자의 예약 요청이라면 거부한다")
            void 사용자_이름이_같으면_ReservationConflictException을_던진다() {
                given(reservationTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_TIME));
                given(themeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_THEME));
                given(reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(
                        VALID_COMMAND_MOA.name(), VALID_COMMAND_MOA.date(), 1L, 1L))
                        .willReturn(true);

                assertThrows(
                        ReservationConflictException.class,
                        () -> reservationService.create(VALID_COMMAND_MOA)
                );
            }

            @Test
            @DisplayName("기존 예약자와 다른 사용자의 예약 요청이라면 허용한다")
            void 사용자_이름이_다르면_예약_대기_순번을_부여한다() {
                given(reservationTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_TIME));
                given(themeRepository.findByIdWithLock(1L)).willReturn(Optional.of(VALID_THEME));
                given(reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(
                        "모아",
                        VALID_COMMAND_MOA.date(),
                        1L,
                        1L))
                        .willReturn(false);
                given(reservationRepository.save(any(Reservation.class))).willReturn(new ReservationWithWaitingOrder(
                        1L, "모아", VALID_COMMAND_MOA.date(), VALID_TIME, VALID_THEME, 1L));

                ReservationResult created = reservationService.create(
                        new ReservationCreateCommand("모아", VALID_COMMAND_MOA.date(), 1L, 1L));

                assertThat(created.waitingOrder()).isEqualTo(1L);
            }
        }
    }
}
