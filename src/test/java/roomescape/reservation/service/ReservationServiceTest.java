package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.CurrentDateTime;
import roomescape.fake.FakeMemberRepository;
import roomescape.fake.TestCurrentDateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.dto.ReservationCreateCommand;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.fake.FakeThemeRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

class ReservationServiceTest {

    ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
    FakeReservationRepository reservationDao = new FakeReservationRepository();
    FakeThemeRepository themeDao = new FakeThemeRepository();
    FakeMemberRepository memberDao = new FakeMemberRepository();
    CurrentDateTime currentDateTime = new TestCurrentDateTime(LocalDateTime.of(2025, 4, 2, 11, 0));
    ReservationService reservationService = new ReservationService(reservationDao, reservationTimeRepository, themeDao,
            memberDao, currentDateTime);
    LocalDate tomorrow = currentDateTime.getDate().plusDays(1);

    @DisplayName("날짜와 시간과 테마가 중복되는 예약을 할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateReservation() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme savedTheme = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(),
                savedTheme.getId());
        reservationService.createReservation(request);
        // when
        // then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간에 이미 예약이 존재합니다.");
    }

    @DisplayName("날짜와 시간이 같아도 테마가 다르면 예외가 발생하지 않는다")
    @Test
    void shouldNot_ThrowException_WhenThemeIsDifferent() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        Theme savedTheme1 = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(),
                savedTheme1.getId());
        reservationService.createReservation(request1);
        Theme savedTheme2 = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(),
                savedTheme2.getId());
        // when
        // then
        assertThatCode(() -> reservationService.createReservation(request2))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약 시간이 존재하지 않을 경우 예외가 발생한다")
    @Test
    void validateTime() {
        // given
        Theme savedTheme = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, savedMember.getId(), 1L,
                savedTheme.getId());
        // when
        // then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시간이 존재하지 않습니다.");
    }

    @DisplayName("테마가 존재하지 않을 경우 예외가 발생한다")
    @Test
    void validateTheme() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(), 1L);
        // when
        // then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마가 존재하지 않습니다.");
    }

    @DisplayName("과거 시간에 예약할 경우 예외가 발생한다")
    @Test
    void validatePastTime() {
        // given
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme savedTheme = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        LocalDate yesterday = currentDateTime.getDate().minusDays(1);
        ReservationCreateCommand request = new ReservationCreateCommand(yesterday, savedMember.getId(), 1L,
                savedTheme.getId());
        // when
        // then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지나간 날짜와 시간은 예약 불가합니다.");
    }

    @DisplayName("예약을 생성할 수 있다")
    @Test
    void create() {
        // given
        LocalTime time = LocalTime.of(11, 0);
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        Theme savedTheme = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(),
                savedTheme.getId());
        // when
        ReservationInfo result = reservationService.createReservation(request);
        // then
        Reservation savedReservation = reservationDao.findById(1L);
        assertAll(
                () -> assertThat(result.member().name()).isEqualTo(savedMember.getName()),
                () -> assertThat(result.date()).isEqualTo(tomorrow),
                () -> assertThat(result.time().startAt()).isEqualTo(time),

                () -> assertThat(result.theme().name()).isEqualTo(savedTheme.getName()),
                () -> assertThat(result.theme().description()).isEqualTo(savedTheme.getDescription()),
                () -> assertThat(result.theme().thumbnail()).isEqualTo(savedTheme.getThumbnail()),

                () -> assertThat(savedReservation.getName()).isEqualTo(savedMember.getName()),
                () -> assertThat(savedReservation.getDate()).isEqualTo(request.date()),
                () -> assertThat(savedReservation.getTime().getStartAt()).isEqualTo(time),

                () -> assertThat(savedReservation.getTheme().getName()).isEqualTo(savedTheme.getName()),
                () -> assertThat(savedReservation.getTheme().getDescription()).isEqualTo(savedTheme.getDescription()),
                () -> assertThat(savedReservation.getTheme().getThumbnail()).isEqualTo(savedTheme.getThumbnail())
        );
    }

    @DisplayName("예약 목록을 조회할 수 있다")
    @Test
    void findAll() {
        // given
        LocalTime time = LocalTime.of(11, 0);
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        Theme theme = new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa");
        Theme savedTheme = themeDao.save(theme);
        Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(),
                savedTheme.getId());
        ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow.plusDays(1), savedMember.getId(),
                savedTime.getId(), savedTheme.getId());
        reservationService.createReservation(request1);
        reservationService.createReservation(request2);
        // when
        // then
        assertThat(reservationService.getReservations()).hasSize(2);
    }

    @DisplayName("예약을 삭제할 수 있다")
    @Test
    void cancelById() {
        // given
        LocalTime time = LocalTime.of(11, 0);
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        Theme theme = new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa");
        Theme savedTheme = themeDao.save(theme);
        Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(),
                savedTheme.getId());
        reservationService.createReservation(request);
        // when
        reservationService.cancelReservationById(1L);
        // then
        assertThat(reservationService.getReservations()).isEmpty();
    }

    @DisplayName("멤버 예약 목록을 조회할 수 있다")
    @Test
    void findReservationsByMember() {
        // given
        LocalTime time1 = LocalTime.of(11, 0);
        LocalTime time2 = LocalTime.of(12, 0);
        ReservationTime savedTime1 = reservationTimeRepository.save(new ReservationTime(time1));
        ReservationTime savedTime2 = reservationTimeRepository.save(new ReservationTime(time2));
        Theme theme = new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa");
        Theme savedTheme = themeDao.save(theme);
        Member savedMember1 = memberDao.save(new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        Member savedMember2 = memberDao.save(new Member(null, "리버", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, savedMember1.getId(), savedTime1.getId(), savedTheme.getId());
        ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow, savedMember2.getId(), savedTime2.getId(), savedTheme.getId());

        ReservationInfo createdReservation1 = reservationService.createReservation(request1);
        ReservationInfo createdReservation2 = reservationService.createReservation(request2);

        // when
        List<ReservationInfo> result = reservationService.findReservationsByMemberId(savedMember1.getId());

        // then
        assertThat(result).containsExactlyElementsOf(List.of(createdReservation1));
    }
}

