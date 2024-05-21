package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationStatusRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@ServiceTest
class ReservationWaitingServiceTest {

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Autowired
    private ReservationStatusRepository reservationStatusRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약을 대기한다.")
    void queueWaitList() {
        Theme theme = themeRepository.save(new Theme("테마 1", "desc", "url"));
        LocalDate date = LocalDate.parse("2023-01-01");
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationRequest request = new ReservationRequest(member.getId(), date, time.getId(), theme.getId());

        reservationWaitingService.enqueueWaitingList(request);

        Optional<ReservationStatus> firstWaiting = reservationStatusRepository.findFirstWaiting(
                new Reservation(member, date, time, theme, LocalDateTime.of(2022, 1, 1, 12, 0))
        );
        assertThat(firstWaiting).isPresent()
                .get()
                .extracting(ReservationStatus::getReservation)
                .extracting(Reservation::getMember)
                .isEqualTo(member);
    }

    @Test
    @DisplayName("예약 대기를 취소하면, 다음 대기자가 예약이 확정된다.")
    void cancelWaitList() {
        Theme theme = themeRepository.save(new Theme("테마 1", "desc", "url"));
        LocalDate date = LocalDate.parse("2023-01-01");
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Member member1 = memberRepository.save(MemberFixture.createMember("아루"));
        Member member2 = memberRepository.save(MemberFixture.createMember("아루2"));
        Reservation firstWaiting = new Reservation(member1, date, time, theme,
                LocalDateTime.parse("1999-01-01T00:00:00"));
        Reservation nextWaiting = new Reservation(member2, date, time, theme,
                LocalDateTime.parse("1999-01-03T00:00:00"));
        long firstId = reservationStatusRepository.save(new ReservationStatus(firstWaiting, BookStatus.WAITING))
                .getId();
        long secondId = reservationStatusRepository.save(new ReservationStatus(nextWaiting, BookStatus.WAITING))
                .getId();

        reservationWaitingService.cancelWaitingList(member1.getId(), firstId);

        ReservationStatus status = reservationStatusRepository.getById(secondId);
        assertThat(status.isBooked()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = BookStatus.class, names = {"BOOKED", "WAITING"})
    @DisplayName("이미 예약된 항목의 경우, 예약 대기를 시도할 경우 예외가 발생한다.")
    void alreadyBookedOnQueueing(BookStatus status) {
        Theme theme = themeRepository.save(new Theme("테마 1", "desc", "url"));
        LocalDate date = LocalDate.parse("2023-01-01");
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationRequest request = new ReservationRequest(member.getId(), date, time.getId(), theme.getId());
        Reservation reservation = new Reservation(member, date, time, theme,
                LocalDateTime.parse("1999-01-01T00:00:00"));
        reservationStatusRepository.save(new ReservationStatus(reservation, status));

        assertThatCode(() -> reservationWaitingService.enqueueWaitingList(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 예약했거나 대기한 항목입니다.");
    }
}
