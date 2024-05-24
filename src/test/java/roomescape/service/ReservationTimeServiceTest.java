package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.MemberRole;
import roomescape.exception.time.DuplicatedTimeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.exception.time.ReservationReferencedTimeException;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.reservationtime.dto.ReservationTimeAvailableListResponse;
import roomescape.service.reservationtime.dto.ReservationTimeListResponse;
import roomescape.service.reservationtime.dto.ReservationTimeRequest;
import roomescape.service.reservationtime.dto.ReservationTimeResponse;
import roomescape.service.reservationwaiting.ReservationWaitingService;

class ReservationTimeServiceTest extends ServiceTest {
    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Nested
    @DisplayName("시간 목록 조회")
    class FindAllReservation {
        @Test
        void 시간_목록을_조회할_수_있다() {
            ReservationTimeListResponse response = reservationTimeService.findAllReservationTime();

            assertThat(response.getTimes().size())
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("예약 가능 시간 목록 조회")
    class FindAllAvailableReservationTime {
        @Test
        void 예약이_가능한_시간을_필터링해_조회할_수_있다() {
            LocalDate date = LocalDate.of(2024, 10, 5);
            long themeId = 1L;
            ReservationTimeAvailableListResponse response = reservationTimeService.findAllAvailableReservationTime(date,
                    themeId);

            assertThat(response.getTimes().get(0).getAlreadyBooked())
                    .isFalse();
        }

        @Test
        void 예약이_불가한_시간을_필터링해_조회할_수_있다() {
            LocalDate date = LocalDate.of(2000, 4, 1);
            long themeId = 1L;
            ReservationTimeAvailableListResponse response = reservationTimeService.findAllAvailableReservationTime(date,
                    themeId);

            assertThat(response.getTimes().get(0).getAlreadyBooked())
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("시간 추가")
    class SaveReservationTime {
        @Test
        void 시간을_추가할_수_있다() {
            LocalTime time = LocalTime.of(11, 0);
            ReservationTimeRequest request = new ReservationTimeRequest(time);

            ReservationTimeResponse response = reservationTimeService.saveReservationTime(request);

            assertThat(response.getStartAt())
                    .isEqualTo(time);
        }

        @Test
        void 중복된_시간_추가시_예외가_발생한다() {
            LocalTime time = LocalTime.of(2, 30);
            ReservationTimeRequest request = new ReservationTimeRequest(time);

            assertThatThrownBy(() -> reservationTimeService.saveReservationTime(request))
                    .isInstanceOf(DuplicatedTimeException.class);
        }
    }

    @Nested
    @DisplayName("시간 삭제")
    class DeleteReservationTime {
        @Test
        void 시간을_삭제할_수_있다() {
            Member member = new Member(
                    1L,
                    new MemberName("사용자"),
                    new MemberEmail("user@gmail.com"),
                    new MemberPassword("1234567890"),
                    MemberRole.USER
            );
            reservationWaitingService.deleteReservationWaiting(1L, member);
            reservationService.deleteReservation(1L);

            reservationTimeService.deleteReservationTime(1L);

            ReservationTimeListResponse response = reservationTimeService.findAllReservationTime();
            assertThat(response.getTimes().size())
                    .isEqualTo(0);
        }

        @Test
        void 존재하지_않는_시간_삭제_시_예외가_발생한다() {
            assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(13L))
                    .isInstanceOf(NotFoundTimeException.class);
        }

        @Test
        void 예약이_존재하는_시간_삭제_시_예외가_발생한다() {
            assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                    .isInstanceOf(ReservationReferencedTimeException.class);
        }
    }
}
