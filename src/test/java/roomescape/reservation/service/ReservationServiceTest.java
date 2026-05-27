package roomescape.reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.dto.response.ReservationsAndWaitingsResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservation.domain.exception.ReservationAlreadyExistsException;
import roomescape.reservation.domain.exception.ReservationCancellationException;
import roomescape.reservation.domain.exception.ReservationModificationException;
import roomescape.reservation.domain.exception.ReservationOptionChangedException;
import roomescape.reservation.service.dto.request.ReservationCreateRequest;
import roomescape.reservation.service.dto.request.ReservationUpdateRequest;
import roomescape.reservation.service.dto.response.ReservationOptionResponse;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.reservation.service.support.FakeReservationRepository;
import roomescape.reservationtime.service.support.FakeReservationTimeRepository;
import roomescape.theme.service.support.FakeThemeRepository;
import roomescape.wating.domain.Waiting;
import roomescape.wating.service.support.FakeWaitingRepository;

import java.sql.Date;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDate.of(2026, 5, 8)
                    .atTime(10, 30)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant(),
            ZoneId.of("Asia/Seoul")
    );
    private static final LocalDateTime NOW = LocalDateTime.now(FIXED_CLOCK);

    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private FakeWaitingRepository waitingRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        waitingRepository = new FakeWaitingRepository();
        reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                waitingRepository,
                FIXED_CLOCK
        );
    }

    @Test
    void 예약을_생성한다() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(11, 0)));
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));

        // when
        ReservationResponse response = reservationService.create(
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 5, 8), 1L, 1L)
        );

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("브라운");
        assertThat(reservationRepository.savedReservation().getCustomerName()).isEqualTo("브라운");
    }

    @Test
    void 예약자_이름으로_현재_시간_이후의_예약_및_대기_목록을_조회한다() {
        // given
        final LocalDateTime oneHourBefore = NOW.minusHours(1);
        final LocalDateTime oneHourAfter = NOW.plusHours(1);

        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                oneHourBefore.toLocalDate(),
                ReservationTime.of(1L, oneHourBefore.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationRepository.add(Reservation.of(
                2L,
                "브라운",
                oneHourAfter.toLocalDate(),
                ReservationTime.of(2L, oneHourAfter.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        waitingRepository.add(Waiting.of(
                1L,
                "브라운",
                Date.valueOf(oneHourBefore.toLocalDate()),
                NOW,
                ReservationTime.of(1L, oneHourBefore.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        waitingRepository.add(Waiting.of(
                1L,
                "브라운",
                Date.valueOf(oneHourAfter.toLocalDate()),
                NOW,
                ReservationTime.of(1L, oneHourAfter.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when
        ReservationsAndWaitingsResponse responses = reservationService.getReservationsByCustomerName("브라운");

        // then
        assertThat(responses.reservations()).hasSize(1);
        assertThat(responses.reservations().getFirst().name()).isEqualTo("브라운");

        assertThat(responses.waitings()).hasSize(1);
        assertThat(responses.waitings().getFirst().customerName()).isEqualTo("브라운");
        assertThat(responses.waitings().getFirst().rank()).isEqualTo(1);
    }

    @Test
    void 현재_이전_시간으로_예약하면_예외가_발생한다() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(10, 0)));
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 5, 8), 1L, 1L)
        ))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_예약_시간으로_예약하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 8, 5), 1L, 1L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 존재하지_않는_테마로_예약하면_예외가_발생한다() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(10, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 8, 5), 1L, 1L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 이미_예약된_시간으로_예약하면_예외가_발생한다() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(11, 0)));
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));
        reservationRepository.failToSaveByDuplicatedReservation();

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 5, 8), 1L, 1L)
        ))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @Test
    void 예약_옵션이_변경된_상태로_예약하면_예외가_발생한다() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(11, 0)));
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));
        reservationRepository.failToSaveByChangedOption();

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 5, 8), 1L, 1L)
        ))
                .isInstanceOf(ReservationOptionChangedException.class);
    }

    @Test
    void 예약_일정을_수정한다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));

        // when
        ReservationResponse response = reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 8, 6), 2L)
        );

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("브라운");
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 8, 6));
        assertThat(response.time().id()).isEqualTo(2L);
        assertThat(response.theme().id()).isEqualTo(1L);
        assertThat(reservationRepository.findById(1L).get().getTime().getId()).isEqualTo(2L);
    }

    @Test
    void 존재하지_않는_예약을_수정하면_예외가_발생한다() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(11, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 8, 5), 1L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 존재하지_않는_예약_시간으로_수정하면_예외가_발생한다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 8, 5), 999L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 현재_이전_시간으로_예약_일정을_수정하면_예외가_발생한다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(2L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(10, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 5, 8), 1L)
        ))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약일_당일에는_예약_시작_전이어도_사용자가_예약_일정을_수정할_수_없다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(12, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 5, 9), 2L)
        ))
                .isInstanceOf(ReservationModificationException.class);
    }

    @Test
    void 관리자는_예약일_당일에도_예약_일정을_수정할_수_있다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(12, 0)));

        // when
        ReservationResponse response = reservationService.updateByAdmin(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 5, 9), 2L)
        );

        // then
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 9));
        assertThat(response.time().id()).isEqualTo(2L);
    }

    @Test
    void 이미_예약된_시간으로_예약_일정을_수정하면_예외가_발생한다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));
        reservationRepository.failToUpdateByDuplicatedReservation();

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 8, 6), 2L)
        ))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @Test
    void 예약_옵션이_변경된_상태로_예약_일정을_수정하면_예외가_발생한다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));
        reservationRepository.failToUpdateByChangedOption();

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 8, 6), 2L)
        ))
                .isInstanceOf(ReservationOptionChangedException.class);
    }

    @Test
    void 예약_가능_날짜와_테마를_조회한다() {
        // given
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));

        // when
        ReservationOptionResponse response = reservationService.getReservationOptions();

        // then
        assertThat(response.dates()).hasSize(14);
        assertThat(response.dates().getFirst()).isEqualTo(LocalDate.of(2026, 5, 8));
        assertThat(response.dates().getLast()).isEqualTo(LocalDate.of(2026, 5, 21));
        assertThat(response.themes())
                .extracting(theme -> theme.name())
                .containsExactly("링");
    }

    @Test
    void 존재하지_않는_예약을_취소하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> reservationService.cancel(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 예약일_당일에는_예약_시작_전이어도_사용자가_예약을_취소할_수_없다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when & then
        assertThatThrownBy(() -> reservationService.cancel(1L))
                .isInstanceOf(ReservationCancellationException.class);
    }

    @Test
    void 관리자는_예약일_당일에도_예약을_삭제할_수_있다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when
        reservationService.delete(1L);

        // then
        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

    @Test
    void 존재하지_않는_예약을_관리자가_삭제하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> reservationService.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 대기_순위는_같은_슬롯_내_createdAt_순서로_계산된다() {
        // given
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.of(2026, 5, 9);

        waitingRepository.add(Waiting.of(1L, "코로구", Date.valueOf(futureDate), NOW.minusMinutes(2), time, theme));
        waitingRepository.add(Waiting.of(2L, "재키", Date.valueOf(futureDate), NOW.minusMinutes(1), time, theme));
        waitingRepository.add(Waiting.of(3L, "브라운", Date.valueOf(futureDate), NOW, time, theme));

        // when
        ReservationsAndWaitingsResponse response = reservationService.getReservationsByCustomerName("브라운");

        // then
        assertThat(response.waitings()).hasSize(1);
        assertThat(response.waitings().getFirst().rank()).isEqualTo(3);
    }

    @Test
    void 예약_취소_시_해당_슬롯의_가장_빠른_대기가_예약으로_전환된다() {
        // given
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.of(2026, 5, 9);

        reservationRepository.add(Reservation.of(1L, "브라운", futureDate, time, theme));

        waitingRepository.add(Waiting.of(1L, "코로구", Date.valueOf(futureDate), NOW.minusMinutes(2), time, theme));
        waitingRepository.add(Waiting.of(2L, "재키", Date.valueOf(futureDate), NOW.minusMinutes(1), time, theme));

        // when
        reservationService.cancel(1L);

        // then
        assertThat(reservationRepository.savedReservation().getCustomerName()).isEqualTo("코로구");
    }

    @Test
    void 예약_취소_시_대기가_없으면_예약만_삭제된다() {
        // given
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.of(2026, 5, 9);

        reservationRepository.add(Reservation.of(1L, "브라운", futureDate, time, theme));

        // when
        reservationService.cancel(1L);

        // then
        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

}
