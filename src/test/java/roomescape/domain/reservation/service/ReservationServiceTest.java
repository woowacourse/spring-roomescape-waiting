package roomescape.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.member.domain.Role.ADMIN;
import static roomescape.fixture.LocalDateFixture.AFTER_ONE_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.AFTER_TWO_DAYS_DATE;
import static roomescape.fixture.LocalTimeFixture.TEN_HOUR;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.service.FakeMemberRepository;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.reservation.dto.BookableTimeResponse;
import roomescape.domain.reservation.dto.BookableTimesRequest;
import roomescape.domain.reservation.dto.ReservationAddRequest;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.service.FakeThemeRepository;
import roomescape.global.exception.EscapeApplicationException;

class ReservationServiceTest {

    private static final Member ADMIN_MEMBER = new Member(1L, "어드민", "admin@gmail.com", "123456", ADMIN);
    private static final ReservationTime TEN_RESERVATION_TIME = new ReservationTime(1L, TEN_HOUR);
    private static final Theme DUMMY_THEME = new Theme(1L, "dummy", "dummy", "dummy");

    private ReservationService reservationService;
    private FakeReservationRepository fakeReservationRepository;
    private FakeReservationTimeRepository fakeReservationTimeRepository;
    private FakeThemeRepository fakeThemeRepository;
    private FakeMemberRepository fakeMemberRepository;

    ReservationServiceTest() {
    }

    @BeforeEach
    void setUp() {
        fakeReservationRepository = new FakeReservationRepository();
        fakeReservationTimeRepository = new FakeReservationTimeRepository();
        fakeThemeRepository = new FakeThemeRepository();
        fakeMemberRepository = new FakeMemberRepository();
        reservationService = new ReservationService(fakeReservationRepository, fakeReservationTimeRepository,
                fakeThemeRepository,
                fakeMemberRepository);
    }


    @DisplayName("존재 하지 않는 멤버로 예약 시 예외를 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_with_non_exist_member() {
        fakeReservationTimeRepository.insert(TEN_RESERVATION_TIME);
        fakeThemeRepository.insert(DUMMY_THEME);
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);//

        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("존재 하지 않는 멤버로 예약할 수 없습니다.");
    }

    @DisplayName("존재 하지 않는 테마로 예약 시 예외를 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_with_non_exist_theme() {
        fakeReservationTimeRepository.insert(TEN_RESERVATION_TIME);
        fakeMemberRepository.insert(ADMIN_MEMBER);
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);
        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("존재 하지 않는 테마로 예약할 수 없습니다");
    }


    @DisplayName("예약 가능 시각을 알 수 있습니다.")
    @Test
    void should_know_bookable_times() {
        ReservationTime reservationTime = new ReservationTime(null, TEN_HOUR);
        Theme theme = new Theme(null, "테마1", "설명", "썸네일");
        fakeReservationTimeRepository.insert(reservationTime);
        fakeReservationRepository.insert(
                new Reservation(null, AFTER_ONE_DAYS_DATE, reservationTime, theme, ADMIN_MEMBER));

        List<BookableTimeResponse> bookableTimes = reservationService.findBookableTimes(
                new BookableTimesRequest(AFTER_ONE_DAYS_DATE, 1L));

        assertThat(bookableTimes.get(0).alreadyBooked()).isTrue();
    }

    @DisplayName("예약 불가능 시각을 알 수 있습니다.")
    @Test
    void should_know_not_bookable_times() {
        ReservationTime reservationTime = new ReservationTime(null, TEN_HOUR);
        Theme theme = new Theme(null, "테마1", "설명", "썸네일");
        fakeReservationTimeRepository.insert(reservationTime);
        fakeReservationTimeRepository.insert(new ReservationTime(1L, LocalTime.of(11, 0)));
        fakeReservationRepository.insert(
                new Reservation(null, AFTER_ONE_DAYS_DATE, reservationTime, theme, ADMIN_MEMBER));

        List<BookableTimeResponse> bookableTimes = reservationService.findBookableTimes(
                new BookableTimesRequest(AFTER_ONE_DAYS_DATE, 1L));

        assertThat(bookableTimes.get(1).alreadyBooked()).isFalse();
    }

    @DisplayName("존재하지 않는 예약시각으로 예약 시 예외가 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_reserve_non_exist_time() {
        fakeThemeRepository.insert(DUMMY_THEME);
        fakeMemberRepository.insert(ADMIN_MEMBER);
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(AFTER_TWO_DAYS_DATE, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("존재 하지 않는 예약시각으로 예약할 수 없습니다.");
    }

    @DisplayName("예약 날짜와 예약시각 그리고 테마 아이디가 같은 경우 예외를 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_reserve_date_and_time_duplicated() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "dummy", "description", "url");
        Member member = new Member(1L, "dummy", "dummy", "dummy", ADMIN);
        Reservation reservation = new Reservation(null, AFTER_ONE_DAYS_DATE, reservationTime, theme, member);
        fakeReservationRepository.insert(reservation);

        ReservationAddRequest conflictRequest = new ReservationAddRequest(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservation(conflictRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("예약 날짜와 예약시간 그리고 테마가 겹치는 예약은 할 수 없습니다.");
    }

    @DisplayName("없는 id의 예약을 삭제하면 예외를 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_remove_reservation_with_non_exist_id() {
        assertThatThrownBy(() -> reservationService.removeReservation(1L))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("해당 id를 가진 예약이 존재하지 않습니다.");
    }
}
