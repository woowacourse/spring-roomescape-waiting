package roomescape.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.domain.member.domain.Role.ADMIN;
import static roomescape.domain.reservation.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.domain.reservation.domain.reservation.ReservationStatus.WAITING;
import static roomescape.domain.reservation.service.ReservationService.DUPLICATED_RESERVATION_ERROR_MESSAGE;
import static roomescape.domain.reservation.service.ReservationService.DUPLICATED_RESERVATION_WAITING_ERROR_MESSAGE;
import static roomescape.domain.reservation.service.ReservationService.NON_EXIST_MEMBER_ERROR_MESSAGE;
import static roomescape.domain.reservation.service.ReservationService.NON_EXIST_RESERVATION_ID_ERROR_MESSAGE;
import static roomescape.domain.reservation.service.ReservationService.NON_EXIST_RESERVATION_TIME_ERROR_MESSAGE;
import static roomescape.domain.reservation.service.ReservationService.NON_EXIST_THEME_ERROR_MESSAGE;
import static roomescape.domain.reservation.service.ReservationService.PAST_RESERVATION_ERROR_MESSAGE;
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
import static roomescape.fixture.TimestampFixture.TIMESTAMP_BEFORE_ONE_YEAR;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.service.FakeMemberRepository;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.reservation.dto.command.ReservationAddCommand;
import roomescape.domain.reservation.dto.request.BookableTimesRequest;
import roomescape.domain.reservation.dto.response.BookableTimeResponse;
import roomescape.domain.reservation.dto.response.ReservationMineResponse;
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
        ReservationAddCommand reservationAddRequest = new ReservationAddCommand(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);

        Reservation reservation = reservationService.addReservedReservation(reservationAddRequest);

        assertThat(reservation.getStatus()).isEqualTo(RESERVED);
    }

    @DisplayName("예약대기가 가능합니다.")
    @Test
    void should_reserve_waiting() {
        fakeReservationTimeRepository.save(TEN_RESERVATION_TIME);
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(MEMBER_MEMBER);
        ReservationAddCommand reservationAddRequest = new ReservationAddCommand(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);

        Reservation reservation = reservationService.addWaitingReservation(reservationAddRequest);

        assertThat(reservation.getStatus()).isEqualTo(WAITING);
    }

    @DisplayName("존재 하지 않는 멤버로 예약 시 예외를 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_with_non_exist_member() {
        fakeReservationTimeRepository.save(TEN_RESERVATION_TIME);
        fakeThemeRepository.save(DUMMY_THEME);
        ReservationAddCommand reservationAddRequest = new ReservationAddCommand(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);//

        assertThatThrownBy(() -> reservationService.addReservedReservation(reservationAddRequest))
                .isInstanceOf(NoMatchingDataException.class)
                .hasMessage(NON_EXIST_MEMBER_ERROR_MESSAGE);
    }

    @DisplayName("존재 하지 않는 테마로 예약 시 예외를 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_with_non_exist_theme() {
        fakeReservationTimeRepository.save(TEN_RESERVATION_TIME);
        fakeMemberRepository.save(ADMIN_MEMBER);
        ReservationAddCommand reservationAddRequest = new ReservationAddCommand(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);
        assertThatThrownBy(() -> reservationService.addReservedReservation(reservationAddRequest))
                .isInstanceOf(NoMatchingDataException.class)
                .hasMessage(NON_EXIST_THEME_ERROR_MESSAGE);
    }


    @DisplayName("존재하지 않는 예약시각으로 예약 시 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_non_exist_time() {
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(ADMIN_MEMBER);
        ReservationAddCommand reservationAddRequest = new ReservationAddCommand(AFTER_TWO_DAYS_DATE, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservedReservation(reservationAddRequest))
                .isInstanceOf(NoMatchingDataException.class)
                .hasMessage(NON_EXIST_RESERVATION_TIME_ERROR_MESSAGE);
    }

    @DisplayName("예약 날짜와 예약시각 그리고 테마 아이디가 같은 경우 예외를 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_date_and_time_duplicated() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "dummy", "description", "url");
        Member member = new Member(1L, "dummy", "dummy", "dummy", ADMIN);
        Reservation reservation = new Reservation(null, AFTER_ONE_DAYS_DATE, reservationTime, theme, member, RESERVED,
                TIMESTAMP_BEFORE_ONE_YEAR);
        fakeReservationRepository.save(reservation);

        ReservationAddCommand conflictRequest = new ReservationAddCommand(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservedReservation(conflictRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage(DUPLICATED_RESERVATION_ERROR_MESSAGE);
    }

    @DisplayName("예약 대기가 멤버 id와 예약 날짜와 예약시각 그리고 테마 아이디가 같은 경우 예외를 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_waiting_with_member_id_and_date_and_time_duplicated() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "dummy", "description", "url");
        Member member = new Member(1L, "dummy", "dummy", "dummy", ADMIN);
        Reservation reservation = new Reservation(null, AFTER_ONE_DAYS_DATE, reservationTime, theme, member, RESERVED,
                TIMESTAMP_BEFORE_ONE_YEAR);
        fakeReservationRepository.save(reservation);

        ReservationAddCommand conflictRequest = new ReservationAddCommand(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addWaitingReservation(conflictRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage(DUPLICATED_RESERVATION_WAITING_ERROR_MESSAGE);
    }

    @DisplayName("date가 현재날짜 보다 이전이면 예약시 예외가 발생한다")
    @Test
    void should_throw_exception_when_date_is_past() {
        fakeReservationTimeRepository.save(TEN_RESERVATION_TIME);
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(MEMBER_MEMBER);
        ReservationAddCommand reservationAddRequest = new ReservationAddCommand(BEFORE_ONE_DAYS_DATE, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservedReservation(reservationAddRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage(BEFORE_ONE_DAYS_DATE.atTime(TEN_RESERVATION_TIME.getStartAt()) + ": 예약은 현재 보다 이전일 수 없습니다");
    }

    @DisplayName("date가 오늘이고 time이 현재시간 보다 이전이면 예약시 예외가 발생한다")
    @Test
    void should_throw_exception_when_time_is_past_and_date_is_today() {
        fakeReservationTimeRepository.save(new ReservationTime(null, BEFORE_ONE_HOUR));
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(MEMBER_MEMBER);
        ReservationAddCommand reservationAddRequest = new ReservationAddCommand(TODAY, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservedReservation(reservationAddRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage(TODAY.atTime(BEFORE_ONE_HOUR) + PAST_RESERVATION_ERROR_MESSAGE);
    }

    @DisplayName("예약 가능 시각을 알 수 있습니다.")
    @Test
    void should_know_bookable_times() {
        ReservationTime reservationTime = new ReservationTime(null, TEN_HOUR);
        Theme theme = new Theme(null, "테마1", "설명", "썸네일");
        ReservationTime savedTime = fakeReservationTimeRepository.save(reservationTime);

        List<BookableTimeResponse> bookableTimes = reservationService.findBookableTimes(
                new BookableTimesRequest(AFTER_ONE_DAYS_DATE, savedTime.getId()));

        assertThat(bookableTimes.get(0).alreadyBooked()).isFalse();
    }

    @DisplayName("예약 불가능 시각을 알 수 있습니다.")
    @Test
    void should_know_not_bookable_times() {
        fakeReservationTimeRepository.save(TEN_RESERVATION_TIME);
        fakeThemeRepository.save(DUMMY_THEME);
        fakeReservationRepository.save(
                new Reservation(null, AFTER_ONE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, ADMIN_MEMBER, RESERVED,
                        TIMESTAMP_BEFORE_ONE_YEAR));

        List<BookableTimeResponse> bookableTimes = reservationService.findBookableTimes(
                new BookableTimesRequest(AFTER_ONE_DAYS_DATE, DUMMY_THEME.getId()));

        assertThat(bookableTimes.get(0).alreadyBooked()).isTrue();
    }

    @DisplayName("없는 id의 예약을 삭제하면 예외를 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_remove_reservation_with_non_exist_id() {
        assertThatThrownBy(() -> reservationService.removeReservation(1L))
                .isInstanceOf(NoMatchingDataException.class)
                .hasMessage(NON_EXIST_RESERVATION_ID_ERROR_MESSAGE);
    }

    @DisplayName("필터링된 예약 목록을 불러올 수 있습니다.")
    @Test
    void should_get_filtered_reservation_list() {
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(MEMBER_MEMBER);
        fakeReservationRepository.save(
                new Reservation(null, BEFORE_ONE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, MEMBER_MEMBER, RESERVED,
                        TIMESTAMP_BEFORE_ONE_YEAR)
        );
        fakeReservationRepository.save(
                new Reservation(null, BEFORE_THREE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, MEMBER_MEMBER,
                        RESERVED, TIMESTAMP_BEFORE_ONE_YEAR)
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
                new Reservation(null, BEFORE_ONE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, MEMBER_MEMBER, RESERVED,
                        TIMESTAMP_BEFORE_ONE_YEAR)
        );

        List<ReservationMineResponse> memberReservations = reservationService.findReservationByMemberId(
                MEMBER_MEMBER.getId());

        assertThat(memberReservations).hasSize(1);
    }

    @DisplayName("예약 대기 목록만을 불러올 수 있다.")
    @Test
    void should_find_waiting_list() {
        fakeThemeRepository.save(DUMMY_THEME);
        fakeMemberRepository.save(MEMBER_MEMBER);
        fakeReservationRepository.save(
                new Reservation(null, BEFORE_ONE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, MEMBER_MEMBER, RESERVED,
                        TIMESTAMP_BEFORE_ONE_YEAR)
        );
        fakeReservationRepository.save(
                new Reservation(null, BEFORE_ONE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, ADMIN_MEMBER, WAITING,
                        TIMESTAMP_BEFORE_ONE_YEAR));

        List<Reservation> waitingReservations = reservationService.findWaitingReservations();

        assertThat(waitingReservations).hasSize(1);
    }

    @DisplayName("예약 삭제 시 해당 예약과 관련된 예약대기가 예약으로 바뀐다.")
    @Test
    void should_change_waiting_reservation_to_reserved_reservation_when_delete_related_reservation() {
        Reservation reservedReservation = new Reservation(null, BEFORE_ONE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME,
                MEMBER_MEMBER, RESERVED, TIMESTAMP_BEFORE_ONE_YEAR);
        Reservation waitingReservation = new Reservation(null, BEFORE_ONE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME,
                ADMIN_MEMBER, WAITING, TIMESTAMP_BEFORE_ONE_YEAR);
        Reservation savedReservedReservation = fakeReservationRepository.save(reservedReservation);
        Reservation savedWaitingReservation = fakeReservationRepository.save(waitingReservation);

        reservationService.removeReservation(savedReservedReservation.getId());
        Reservation changedWaitingReservation = fakeReservationRepository.findById(savedWaitingReservation.getId())
                .get();

        assertThat(changedWaitingReservation.getStatus()).isEqualTo(RESERVED);
    }

}
