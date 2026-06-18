package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static roomescape.domain.exception.DomainErrorCode.DUPLICATE_RESERVATION;
import static roomescape.domain.exception.DomainErrorCode.PAST_RESERVATION;
import static roomescape.domain.exception.DomainErrorCode.RESERVATION_NOT_FOUND;
import static roomescape.domain.exception.DomainErrorCode.RESERVATION_TIME_NOT_FOUND;
import static roomescape.domain.exception.DomainErrorCode.THEME_NOT_FOUND;
import static roomescape.domain.exception.DomainErrorCode.WAITLIST_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 빈_슬롯이면_예약은_등록된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationWithStatus reservationWithStatus = reservationService.applyReservation(createReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));

        assertThat(reservationWithStatus.getId()).isNotNull();
        assertThat(reservationWithStatus.getName()).isEqualTo("브라운");
        assertThat(reservationWithStatus.getDate()).isEqualTo(FUTURE_SECOND_DATE);
        assertThat(reservationWithStatus.getTime().getId()).isEqualTo(reservationTime.getId());
        assertThat(reservationWithStatus.getTime().getStartAt()).isEqualTo(reservationTime.getStartAt());
        assertThat(reservationWithStatus.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(reservationWithStatus.getTheme().getName()).isEqualTo(theme.getName());
    }

    @Test
    void 이미_예약된_슬롯이면_대기로_등록된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        reservationService.applyReservation(createReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));
        reservationService.applyReservation(createReservationRequest(
                "워니",
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));

        ReservationWithStatus result = reservationService.applyReservation(createReservationRequest(
                "브리",
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.WAITING);
        assertThat(result.getWaitingOrder()).isEqualTo(2);
    }

    @Test
    void 예약을_추가할_때_예약시간이_없는_경우_예외() {
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                1L,
                theme.getId()
        );

        assertThatRoomEscapeExceptionCode(
                () -> reservationService.applyReservation(request),
                RESERVATION_TIME_NOT_FOUND
        );
    }

    @Test
    void 예약을_추가할_때_테마가_없는_경우_예외() {
        ReservationTime reservationTime = createReservationTime(TEN);

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                1L
        );

        assertThatRoomEscapeExceptionCode(
                () -> reservationService.applyReservation(request),
                THEME_NOT_FOUND
        );
    }

    @Test
    void 걑은_사용자가_이미_예약한_슬롯이면_대기_순번으로_넘어가지_않고_예외() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );

        reservationService.applyReservation(request);

        assertThatRoomEscapeExceptionCode(
                () -> reservationService.applyReservation(request),
                DUPLICATE_RESERVATION
        );
    }

    @Test
    void 다른_사용자가_이미_예약한_슬롯에서_사용자가_중복_대기할_수_없다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );

        reservationService.applyReservation(request);

        ReservationRequest waitlistRequest = new ReservationRequest(
                "브리",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );
        reservationService.applyReservation(waitlistRequest);

        assertThatRoomEscapeExceptionCode(
                () -> reservationService.applyReservation(waitlistRequest),
                DUPLICATE_RESERVATION
        );
    }

    @Test
    void 예약을_삭제한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );

        ReservationWithStatus savedReservation = reservationService.applyReservation(request);

        reservationService.deleteReservation(savedReservation.getId());

        assertThatRoomEscapeExceptionCode(
                () -> reservationService.getReservation(savedReservation.getId()),
                RESERVATION_NOT_FOUND
        );
    }

    @Test
    void 없는_예약을_삭제할_수_없다() {
        assertThatRoomEscapeExceptionCode(
                () -> reservationService.deleteReservation(1L),
                RESERVATION_NOT_FOUND
        );
    }

    @Test
    void 사용자_예약을_취소하면_첫번째_대기가_예약으로_승격되고_대기에서_삭제된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String canceledReservationName = "브라운";
        String waitingFirstName = "브리";
        String waitingSecondName = "워니";

        ReservationWithStatus reservation = reservationService.applyReservation(createReservationRequest(
                canceledReservationName,
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));
        ReservationWithStatus waitingFirst = reservationService.applyReservation(createReservationRequest(
                waitingFirstName,
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));
        ReservationWithStatus waitingSecond = reservationService.applyReservation(createReservationRequest(
                waitingSecondName,
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));

        reservationService.cancelMyReservationAndPromoteWaitlist(reservation.getId(), canceledReservationName);

        assertThatRoomEscapeExceptionCode(
                () -> reservationService.getReservation(reservation.getId()),
                RESERVATION_NOT_FOUND
        );
        assertThatRoomEscapeExceptionCode(
                () -> reservationService.getWaitlist(waitingFirst.getId()),
                WAITLIST_NOT_FOUND
        );
        assertThat(reservationService.getWaitlist(waitingSecond.getId()).getName())
                .isEqualTo(waitingSecondName);
        assertThat(reservationService.getReservations())
                .extracting(Reservation::getName)
                .contains(waitingFirstName)
                .doesNotContain(canceledReservationName);
    }

    @Test
    void 사용자_예약을_취소할_때_존재하지_않는_예약이면_예외() {
        assertThatRoomEscapeExceptionCode(
                () -> reservationService.cancelMyReservationAndPromoteWaitlist(1L, "브라운"),
                RESERVATION_NOT_FOUND
        );
    }

    @Test
    @Sql("/data_relative_dates.sql")
    void 예약을_취소할_때_이미_지난_예약이면_예외() {
        String name = "김민수";
        ReservationWithStatus pastReservation = reservationService.getMyReservations(name).getFirst();

        LocalDateTime dateTime = LocalDateTime.of(
                pastReservation.getDate(),
                pastReservation.getTime().getStartAt()
        );

        assertThat(dateTime.isBefore(LocalDateTime.now())).isTrue();
        assertThatRoomEscapeExceptionCode(
                () -> reservationService.cancelMyReservationAndPromoteWaitlist(pastReservation.getId(), name),
                PAST_RESERVATION
        );
    }

    @Test
    void 사용자가_예약의_날짜와_시간을_수정하면_기존_자리의_첫번째_대기가_예약으로_승격된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String originalReservationName = "브라운";
        String waitingFirstName = "브리";
        String waitingSecondName = "워니";

        ReservationWithStatus originalReservation = reservationService.applyReservation(createReservationRequest(
                originalReservationName,
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));
        ReservationWithStatus waitingFirst = reservationService.applyReservation(createReservationRequest(
                waitingFirstName,
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));
        ReservationWithStatus waitingSecond = reservationService.applyReservation(createReservationRequest(
                waitingSecondName,
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));

        ReservationTime updateTime = createReservationTime(LocalTime.of(12, 0));
        LocalDate updateDate = FUTURE_SECOND_DATE.plusDays(1);
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                updateDate,
                updateTime.getId()
        );

        reservationService.updateMyReservationAndPromoteWaitlist(originalReservation.getId(), originalReservationName,
                updateRequest);

        assertThat(reservationService.getReservations())
                .extracting(
                        Reservation::getName,
                        Reservation::getDate,
                        reservation -> reservation.getTime().getId(),
                        reservation -> reservation.getTheme().getId()
                )
                .contains(
                        tuple(originalReservationName, updateDate, updateTime.getId(), theme.getId()),
                        tuple(waitingFirstName, FUTURE_SECOND_DATE, reservationTime.getId(), theme.getId())
                )
                .doesNotContain(
                        tuple(originalReservationName, FUTURE_SECOND_DATE, reservationTime.getId(), theme.getId())
                );
        assertThat(reservationService.getWaitlist(waitingSecond.getId()).getName())
                .isEqualTo(waitingSecondName);
        assertThatRoomEscapeExceptionCode(
                () -> reservationService.getWaitlist(waitingFirst.getId()),
                WAITLIST_NOT_FOUND
        );
    }

    @Test
    void 예약을_수정할_때_존재하지_않는_예약이면_예외() {
        ReservationTime updateTime = createReservationTime(TEN);
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                FUTURE_SECOND_DATE,
                updateTime.getId()
        );

        assertThatRoomEscapeExceptionCode(
                () -> reservationService.updateMyReservationAndPromoteWaitlist(1L, "브라운", updateRequest),
                RESERVATION_NOT_FOUND
        );
    }

    @Test
    void 예약을_수정할_때_존재하지_않는_시간_ID이면_예외() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";
        ReservationRequest request = new ReservationRequest(
                name,
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );
        ReservationWithStatus reservation = reservationService.applyReservation(request);

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                FUTURE_SECOND_DATE.plusDays(1),
                999L
        );

        assertThatRoomEscapeExceptionCode(
                () -> reservationService.updateMyReservationAndPromoteWaitlist(reservation.getId(), name,
                        updateRequest),
                RESERVATION_TIME_NOT_FOUND
        );
    }

    @Test
    void 예약을_수정할_때_변경하려는_예약_시간이_이미_차_있으면_예외() {
        ReservationTime tenClock = createReservationTime(TEN);
        ReservationTime twelveClock = createReservationTime(LocalTime.of(12, 0));
        Theme theme = createTheme();

        String name = "브라운";
        ReservationRequest request = new ReservationRequest(
                name,
                FUTURE_SECOND_DATE,
                tenClock.getId(),
                theme.getId()
        );
        ReservationWithStatus reservation = reservationService.applyReservation(request);

        ReservationRequest anotherRequest = new ReservationRequest(
                "브리",
                FUTURE_SECOND_DATE.plusDays(1),
                twelveClock.getId(),
                theme.getId()
        );
        reservationService.applyReservation(anotherRequest);

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                FUTURE_SECOND_DATE.plusDays(1),
                twelveClock.getId()
        );

        assertThatRoomEscapeExceptionCode(
                () -> reservationService.updateMyReservationAndPromoteWaitlist(reservation.getId(), name,
                        updateRequest),
                DUPLICATE_RESERVATION
        );
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        return timeRepository.save(reservationTime);
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        return themeRepository.save(theme);
    }

    private ReservationRequest createReservationRequest(
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        return new ReservationRequest(
                name,
                date,
                time.getId(),
                theme.getId()
        );
    }

    private void assertThatRoomEscapeExceptionCode(ThrowingCallable callable, DomainErrorCode expectedCode) {
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(RoomEscapeException.class,
                        exception -> assertThat(exception.code()).isEqualTo(expectedCode));
    }
}
