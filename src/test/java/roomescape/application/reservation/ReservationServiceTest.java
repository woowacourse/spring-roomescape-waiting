package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.MEMBER_ARU;
import static roomescape.fixture.MemberFixture.MEMBER_PK;
import static roomescape.fixture.MemberFixture.MEMBER_SEESAW;
import static roomescape.fixture.ThemeFixture.TEST_THEME;
import static roomescape.fixture.TimeFixture.TWELVE_PM;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@ServiceTest
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("정상적인 예약 요청을 받아서 저장한다.")
    void shouldReturnReservationResponseWhenValidReservationRequestSave() {
        ReservationTime time = reservationTimeRepository.save(TWELVE_PM.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        Member member = memberRepository.save(MEMBER_ARU.create());
        ReservationRequest reservationRequest = new ReservationRequest(
                member.getId(),
                LocalDate.of(2024, 1, 1),
                time.getId(),
                theme.getId()
        );

        reservationService.bookReservation(reservationRequest);

        List<Reservation> reservations = reservationRepository.findAllBookedReservations();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약을 생성시 예외가 발생한다.")
    void shouldReturnIllegalArgumentExceptionWhenNotFoundReservationTime() {
        Theme savedTheme = themeRepository.save(TEST_THEME.create());
        Member member = memberRepository.save(MEMBER_ARU.create());
        ReservationRequest request = new ReservationRequest(
                member.getId(),
                LocalDate.of(2024, 1, 1),
                99L,
                savedTheme.getId());

        assertThatCode(() -> reservationService.bookReservation(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약을 생성시 예외를 반환한다.")
    void shouldThrowIllegalArgumentExceptionWhenNotFoundTheme() {
        ReservationTime time = reservationTimeRepository.save(TWELVE_PM.create());
        Member member = memberRepository.save(MEMBER_ARU.create());
        ReservationRequest request = new ReservationRequest(
                member.getId(),
                LocalDate.of(2024, 1, 1),
                time.getId(),
                99L
        );
        assertThatCode(() -> reservationService.bookReservation(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }

    @Test
    @DisplayName("과거 시간을 예약하는 경우 예외를 반환한다.")
    void shouldThrowsIllegalArgumentExceptionWhenReservationDateIsBeforeCurrentDate() {
        ReservationTime time = reservationTimeRepository.save(TWELVE_PM.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        Member member = memberRepository.save(MEMBER_ARU.create());
        ReservationRequest request = new ReservationRequest(
                member.getId(),
                LocalDate.of(1999, 12, 31),
                time.getId(),
                theme.getId()
        );

        assertThatCode(() -> reservationService.bookReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 시간보다 과거로 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("예약 대기를 취소하더라도, 현재 확정된 예약이 변경되지 않는다.")
    void remainsSameBookStatusOnWaitingCancellation() {
        ReservationTime time = reservationTimeRepository.save(TWELVE_PM.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        Member aru = memberRepository.save(MEMBER_ARU.create());
        Member pk = memberRepository.save(MEMBER_PK.create());
        Member seesaw = memberRepository.save(MEMBER_SEESAW.create());
        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDateTime createdAt = date.minusDays(1).atStartOfDay();

        List<Reservation> reservations = Stream.of(
                        new Reservation(aru, theme, date, time, createdAt, BookStatus.BOOKED),
                        new Reservation(pk, theme, date, time, createdAt.plusHours(1), BookStatus.WAITING),
                        new Reservation(seesaw, theme, date, time, createdAt.plusHours(2), BookStatus.WAITING))
                .map(reservationRepository::save)
                .toList();

        reservationService.cancelWaitingList(pk.getId(), reservations.get(1).getId());

        List<Reservation> waiting = reservationRepository.findAllWaitingReservations();
        List<Reservation> booked = reservationRepository.findAllBookedReservations();
        assertAll(
                () -> assertThat(waiting).hasSize(1),
                () -> assertThat(booked).hasSize(1)
        );
    }
}
