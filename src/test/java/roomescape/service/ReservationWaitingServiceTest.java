package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.theme.Theme;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.reservationwaiting.CannotCreateWaitingForOwnedReservationException;
import roomescape.exception.reservationwaiting.DuplicatedReservationWaitingException;
import roomescape.exception.reservationwaiting.InvalidDateTimeWaitingException;
import roomescape.exception.reservationwaiting.NotFoundReservationWaitingException;
import roomescape.service.reservationwaiting.ReservationWaitingService;
import roomescape.service.reservationwaiting.dto.ReservationWaitingListResponse;
import roomescape.service.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.service.reservationwaiting.dto.ReservationWaitingResponse;

public class ReservationWaitingServiceTest extends ServiceTest {
    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Nested
    @DisplayName("예약 대기 목록 조회 API")
    class FindAllReservationWaiting {
        @Test
        void 예약_대기_목록을_조회할_수_있다() {
            ReservationTime time = timeFixture.createFutureTime();
            Theme theme = themeFixture.createFirstTheme();
            Member member = memberFixture.createUserMember();
            Reservation reservation = reservationFixture.createFutureReservation(time, theme, member);
            waitingFixture.createWaiting(reservation, member);

            ReservationWaitingListResponse response = reservationWaitingService.findAllReservationWaiting();

            assertThat(response.getWaitings().size())
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("예약 대기 추가")
    class SaveReservationWaiting {
        ReservationTime time;
        Theme theme;
        Member user;
        Member admin;
        Reservation reservation;
        String date;
        String timeId;
        String themeId;

        @BeforeEach
        void setUp() {
            time = timeFixture.createFutureTime();
            theme = themeFixture.createFirstTheme();
            admin = memberFixture.createAdminMember();
            reservation = reservationFixture.createFutureReservation(time, theme, admin);
            date = reservation.getDate().toString();
            timeId = reservation.getTime().getId().toString();
            themeId = reservation.getTheme().getId().toString();
            user = memberFixture.createUserMember();
        }

        @Test
        void 예약_대기를_추가할_수_있다() {
            ReservationWaitingRequest request = new ReservationWaitingRequest(date, timeId, themeId);

            ReservationWaitingResponse response = reservationWaitingService.saveReservationWaiting(request, user);

            assertThat(response.getId())
                    .isEqualTo(1L);
        }

        @Test
        void 같은_사용자가_같은_예약에_대해선_예약_대기를_두_번_이상_추가_시_예외가_발생한다() {
            waitingFixture.createWaiting(reservation, user);
            ReservationWaitingRequest request = new ReservationWaitingRequest(date, timeId, themeId);

            assertThatThrownBy(() -> reservationWaitingService.saveReservationWaiting(request, user))
                    .isInstanceOf(DuplicatedReservationWaitingException.class);
        }

        @Test
        void 본인이_예약한_건에_대해서_예약_대기_추가_시_예외가_발생한다() {
            ReservationWaitingRequest request = new ReservationWaitingRequest(date, timeId, themeId);

            assertThatThrownBy(() -> reservationWaitingService.saveReservationWaiting(request, admin))
                    .isInstanceOf(CannotCreateWaitingForOwnedReservationException.class);
        }


        @Test
        void 예약이_존재하지_않는데_예약_대기_추가_시_예외가_발생한다() {
            String nonExistDate = "2000-04-09";
            ReservationWaitingRequest request = new ReservationWaitingRequest(nonExistDate, timeId, themeId);

            assertThatThrownBy(() -> reservationWaitingService.saveReservationWaiting(request, user))
                    .isInstanceOf(NotFoundReservationException.class);
        }

        @Test
        void 지난_예약에_대해_예약_대기_추가_시_예외가_발생한다() {
            Reservation pastReservation = reservationFixture.createPastReservation(time, theme, admin);
            String pastDate = pastReservation.getDate().toString();
            ReservationWaitingRequest request = new ReservationWaitingRequest(pastDate, timeId, themeId);

            assertThatThrownBy(() -> reservationWaitingService.saveReservationWaiting(request, user))
                    .isInstanceOf(InvalidDateTimeWaitingException.class);
        }
    }

    @Nested
    @DisplayName("사용자 예약 대기 삭제")
    class DeleteReservationWaiting {
        Member user;
        Reservation reservation;

        @BeforeEach
        void setUp() {
            ReservationTime time = timeFixture.createFutureTime();
            Theme theme = themeFixture.createFirstTheme();
            user = memberFixture.createUserMember();
            reservation = reservationFixture.createFutureReservation(time, theme, user);
            waitingFixture.createWaiting(reservation, user);
        }

        @Test
        void 예약_id와_회원을_지정해_예약_대기를_삭제할_수_있다() {
            reservationWaitingService.deleteReservationWaiting(reservation.getId(), user);

            List<ReservationWaiting> waitings = waitingFixture.findAllWaiting();
            assertThat(waitings.size())
                    .isEqualTo(0);
        }

        @Test
        void 예약_id가_존재하지_않는_예약_대기_삭제_시_예외가_발생한다() {
            assertThatThrownBy(() -> reservationWaitingService.deleteReservationWaiting(10L, user))
                    .isInstanceOf(NotFoundReservationWaitingException.class);
        }

        @Test
        void 본인이_예약자로_존재하지_않는_예약_대기_삭제_시_예외가_발생한다() {
            Member admin = memberFixture.createAdminMember();

            assertThatThrownBy(() -> reservationWaitingService.deleteReservationWaiting(reservation.getId(), admin))
                    .isInstanceOf(NotFoundReservationWaitingException.class);
        }
    }

    @Nested
    @DisplayName("관리자 예약 대기 삭제")
    class DeleteAdminReservationWaiting {
        ReservationWaiting waiting;

        @BeforeEach
        void setUp() {
            ReservationTime time = timeFixture.createFutureTime();
            Theme theme = themeFixture.createFirstTheme();
            Member member = memberFixture.createUserMember();
            Reservation reservation = reservationFixture.createFutureReservation(time, theme, member);
            waiting = waitingFixture.createWaiting(reservation, member);
        }

        @Test
        void 예약_대기_id로_예약_대기를_삭제할_수_있다() {
            reservationWaitingService.deleteAdminReservationWaiting(waiting.getId());

            List<ReservationWaiting> waitings = waitingFixture.findAllWaiting();
            assertThat(waitings.size())
                    .isEqualTo(0);
        }

        @Test
        void 예약_대기_id가_존재하지_않는_예약_대기_삭제_시_예외가_발생한다() {
            assertThatThrownBy(() -> reservationWaitingService.deleteAdminReservationWaiting(10L))
                    .isInstanceOf(NotFoundReservationWaitingException.class);
        }
    }
}
