package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {
    @Autowired
    private ReservationRepository reservationRepository;

    private long getItemSize() {
        return reservationRepository.findAll().size();
    }

    @DisplayName("Db에 등록된 모든 예약 목록을 조회한다.")
    @Test
    void given_when_findAll_then_returnReservations() {
        //given, when, then
        assertThat(reservationRepository.findAll()).hasSize(8);
    }

    @DisplayName("Db에 예약 정보를 저장한다.")
    @Test
    void given_reservation_when_create_then_returnCreatedReservationId() {
        //given
        Reservation expected = new Reservation(
                new Member("poke@test.com", new Password("password", "salt"), "poke", Role.USER),
                LocalDate.parse("2099-01-11"),
                new ReservationTime(1L, LocalTime.parse("10:00")),
                new Theme(1L, "name", "description", "thumbnail"),
                ReservationStatus.RESERVED);
        //when
        final Reservation savedReservation = reservationRepository.save(expected);
        //then
        assertThat(savedReservation).isEqualTo(expected);
    }

    @DisplayName("예약 id로 Db에서 예약 정보를 삭제한다.")
    @Test
    void given_when_delete_then_deletedFromDb() {
        //given
        long initialSize = getItemSize();
        //when
        reservationRepository.deleteById(1L);
        long afterSize = getItemSize();
        //then
        assertThat(afterSize).isEqualTo(initialSize - 1);
    }

    @DisplayName("예약 날짜, 시간Id, 테마Id, 사용자Id 를 통해 중복 예약이 있는지 확인할 수 있다.")
    @Test
    void given_dateAndTimeIdAndThemeIdAndMemberId_when_isExist_then_getExistResult() {
        //given, when, then
        assertThat(reservationRepository
                .existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate.parse("2024-05-01"), 3L, 2L, 1L)).isTrue();
    }

    @DisplayName("예약 날짜, 시간Id, 테마Id를 통해 예약여부를 확인할 수 있다.")
    @Test
    void given_dateAndTimeIdAndThemeId_when_isExist_then_getExistResult() {
        //given, when, then
        assertThat(reservationRepository
                .existsByDateAndTimeIdAndThemeId(LocalDate.parse("2024-05-01"), 3L, 2L)).isTrue();
    }

    @DisplayName("시간 Id로 등록한 예약이 존재하는지 확인할 수 있다.")
    @Test
    void given_when_isExistTimeId_then_getExistResult() {
        //given, when, then
        assertThat(reservationRepository.existsById(1L)).isTrue();
    }

    @DisplayName("타임 Id로 등록한 예약이 존재하는지 확인할 수 있다.")
    @Test
    void given_when_isExistByTimeId_then_getExistResult() {
        //given, when, then
        assertThat(reservationRepository.existsByTimeId(2L)).isTrue();
    }

    @DisplayName("테마 Id로 등록한 예약이 존재하는지 확인할 수 있다.")
    @Test
    void given_when_isExistThemeId_then_getExistResult() {
        //given, when, then
        assertThat(reservationRepository.existsByThemeId(2L)).isTrue();
    }

    @DisplayName("memberId, themeId, 기간을 이용하여 예약을 조회할 수 있다.")
    @Test
    void given_memberIdThemeIdAndPeriod_when_find_then_Reservations() {
        //given
        Long themeId = 2L;
        Long memberId = 1L;
        LocalDate dateFrom = LocalDate.parse("2024-04-30");
        LocalDate dateTo = LocalDate.parse("2024-05-01");
        //when, then
        final List<Reservation> reservations = reservationRepository
                .findAllByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo);
        assertThat(reservations.size()).isEqualTo(3);
    }

    @DisplayName("특정 member의 예약을 조회할 수 있다.")
    @Test
    void given_member_when_findByMember_then_Reservations() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(1L, "user@test.com", password, "poke", Role.USER);
        //when, then
        assertThat(reservationRepository.findByMember(member)).hasSize(7);
    }

    @DisplayName("예약 날짜와 테마Id에 대한 예약을 조회할 수 있다.")
    @Test
    void given_dateAndThemeId_When_findByDateAndThemeId_then_Reservations() {
        //given, when, then
        assertThat(reservationRepository.findByDateAndThemeId(LocalDate.parse("2024-05-01"), 2L))
                .hasSize(2);
    }

    @DisplayName("예약 날짜, 시간Id, 테마Id에 대한 예약을 조회할 수 있다.")
    @Test
    void given_dateAndTimeIdAndThemeId_When_findByDateAndTimeIdAndThemeId_then_Reservations() {
        //given, when, then
        assertThat(reservationRepository.findByDateAndTimeIdAndThemeId(LocalDate.parse("2999-04-30"), 1L, 1L))
                .hasSize(2);
    }
}
