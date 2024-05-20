package roomescape.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.domain.member.domain.Role.ADMIN;
import static roomescape.fixture.LocalDateFixture.AFTER_ONE_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.AFTER_TWO_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.BEFORE_ONE_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.BEFORE_THREE_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.TODAY;
import static roomescape.fixture.LocalTimeFixture.BEFORE_ONE_HOUR;
import static roomescape.fixture.LocalTimeFixture.TEN_HOUR;
import static roomescape.fixture.MemberFixture.ADMIN_MEMBER;
import static roomescape.fixture.MemberFixture.MEMBER_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.TEN_RESERVATION_TIME;
import static roomescape.fixture.ThemeFixture.DUMMY_THEME;

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
import roomescape.domain.reservation.dto.ReservationMineResponse;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.service.FakeThemeRepository;
import roomescape.global.exception.EscapeApplicationException;
import roomescape.global.exception.NoMatchingDataException;

class ReservationServiceTest {

    private ReservationService reservationService;
    private FakeReservationRepository fakeReservationRepository;
    private FakeReservationTimeRepository fakeReservationTimeRepository;
    private FakeThemeRepository fakeThemeRepository;
    private FakeMemberRepository fakeMemberRepository;

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

    @DisplayName("예약이 가능합니다.")
    @Test
    void should_reserve() {
        fakeReservationTimeRepository.save(TEN_RESERVATION_TIME);
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(MEMBER_MEMBER);
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);

        Reservation reservation = reservationService.addReservation(reservationAddRequest);

        assertThat(reservation).isNotNull();
    }

    @DisplayName("존재 하지 않는 멤버로 예약 시 예외를 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_with_non_exist_member() {
        fakeReservationTimeRepository.save(TEN_RESERVATION_TIME);
        fakeThemeRepository.save(DUMMY_THEME);
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);//

        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(NoMatchingDataException.class)
                .hasMessage("존재 하지 않는 멤버로 예약할 수 없습니다.");
    }

    @DisplayName("존재 하지 않는 테마로 예약 시 예외를 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_with_non_exist_theme() {
        fakeReservationTimeRepository.save(TEN_RESERVATION_TIME);
        fakeMemberRepository.save(ADMIN_MEMBER);
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);
        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(NoMatchingDataException.class)
                .hasMessage("존재 하지 않는 테마로 예약할 수 없습니다");
    }


    @DisplayName("존재하지 않는 예약시각으로 예약 시 예외가 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_reserve_non_exist_time() {
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(ADMIN_MEMBER);
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(AFTER_TWO_DAYS_DATE, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(NoMatchingDataException.class)
                .hasMessage("존재 하지 않는 예약시각으로 예약할 수 없습니다.");
    }

    @DisplayName("예약 날짜와 예약시각 그리고 테마 아이디가 같은 경우 예외를 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_reserve_date_and_time_duplicated() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "dummy", "description", "url");
        Member member = new Member(1L, "dummy", "dummy", "dummy", ADMIN);
        Reservation reservation = new Reservation(null, AFTER_ONE_DAYS_DATE, reservationTime, theme, member);
        fakeReservationRepository.save(reservation);

        ReservationAddRequest conflictRequest = new ReservationAddRequest(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservation(conflictRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("예약 날짜와 예약시간 그리고 테마가 겹치는 예약은 할 수 없습니다.");
    }

    @DisplayName("date가 현재날짜 보다 이전이면 예약시 예외가 발생한다")
    @Test
    void should_throw_exception_when_date_is_past() {
        fakeReservationTimeRepository.save(TEN_RESERVATION_TIME);
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(MEMBER_MEMBER);
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(BEFORE_ONE_DAYS_DATE, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage(BEFORE_ONE_DAYS_DATE.atTime(TEN_RESERVATION_TIME.getStartAt()) + ": 예약은 현재 보다 이전일 수 없습니다");
    }

    @DisplayName("date가 오늘이고 time이 현재시간 보다 이전이면 예약시 예외가 발생한다")
    @Test
    void should_throw_exception_when_time_is_past_and_date_is_today() {
        fakeReservationTimeRepository.save(new ReservationTime(null, BEFORE_ONE_HOUR));
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(MEMBER_MEMBER);
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(TODAY, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage(TODAY.atTime(BEFORE_ONE_HOUR) + ": 예약은 현재 보다 이전일 수 없습니다");
    }

    @DisplayName("예약 가능 시각을 알 수 있습니다.")
    @Test
    void should_know_bookable_times() {
        ReservationTime reservationTime = new ReservationTime(null, TEN_HOUR);
        Theme theme = new Theme(null, "테마1", "설명", "썸네일");
        fakeReservationTimeRepository.save(reservationTime);
        fakeReservationRepository.save(
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
        fakeReservationTimeRepository.save(reservationTime);
        fakeReservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(11, 0)));
        fakeReservationRepository.save(
                new Reservation(null, AFTER_ONE_DAYS_DATE, reservationTime, theme, ADMIN_MEMBER));

        List<BookableTimeResponse> bookableTimes = reservationService.findBookableTimes(
                new BookableTimesRequest(AFTER_ONE_DAYS_DATE, 1L));

        assertThat(bookableTimes.get(1).alreadyBooked()).isFalse();
    }

    @DisplayName("없는 id의 예약을 삭제하면 예외를 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_remove_reservation_with_non_exist_id() {
        assertThatThrownBy(() -> reservationService.removeReservation(1L))
                .isInstanceOf(NoMatchingDataException.class)
                .hasMessage("해당 id를 가진 예약이 존재하지 않습니다.");
    }

    @DisplayName("필터링된 예약 목록을 불러올 수 있습니다.")
    @Test
    void should_get_filtered_reservation_list() {
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(MEMBER_MEMBER);
        fakeReservationRepository.save(
                new Reservation(null, BEFORE_ONE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, MEMBER_MEMBER)
        );
        fakeReservationRepository.save(
                new Reservation(null, BEFORE_THREE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, MEMBER_MEMBER)
        );

        List<Reservation> filteredReservationList = reservationService
                .findFilteredReservationList(1L, 1L, BEFORE_ONE_DAYS_DATE, TODAY);

        assertAll(
                () -> assertThat(filteredReservationList).hasSize(1),
                () -> assertThat(filteredReservationList.get(0).getDate()).isEqualTo(BEFORE_ONE_DAYS_DATE)
        );

    }

    @DisplayName("멤버의 ID로 예약목록을 불러 올 수 있다.")
    @Test
    void should_find_reservations_by_member_id() {
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(MEMBER_MEMBER);
        fakeReservationRepository.save(
                new Reservation(null, BEFORE_ONE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, MEMBER_MEMBER)
        );

        List<ReservationMineResponse> memberReservations = reservationService.findReservationByMemberId(
                MEMBER_MEMBER.getId());

        assertThat(memberReservations).hasSize(1);
    }
}
