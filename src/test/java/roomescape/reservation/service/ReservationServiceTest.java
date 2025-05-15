package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.CurrentDateTime;
import roomescape.fake.FakeMemberRepository;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.fake.FakeThemeRepository;
import roomescape.fake.TestCurrentDateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.dto.ReservationCreateCommand;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.reservation.service.dto.ReservationSearchCondition;

class ReservationServiceTest {

    private final ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
    private final FakeReservationRepository reservationDao = new FakeReservationRepository();
    private final FakeThemeRepository themeDao = new FakeThemeRepository();
    private final FakeMemberRepository memberDao = new FakeMemberRepository();
    private final CurrentDateTime currentDateTime = new TestCurrentDateTime(LocalDateTime.of(2025, 4, 2, 11, 0));
    private final ReservationService reservationService = new ReservationService(reservationDao,
            reservationTimeRepository, themeDao,
            memberDao, currentDateTime);
    private final LocalDate tomorrow = currentDateTime.getDate().plusDays(1);

    private static Stream<Arguments> getConditionAndResultSize() {
        return Stream.of(
                Arguments.arguments(new ReservationSearchCondition(null, null, null, null), 5),
                Arguments.arguments(new ReservationSearchCondition(1L, null, null, null), 4),
                Arguments.arguments(new ReservationSearchCondition(null, 1L, null, null), 4),
                Arguments.arguments(new ReservationSearchCondition(1L, 1L, null, null), 3),
                Arguments.arguments(
                        new ReservationSearchCondition(1L, 1L, LocalDate.of(2025, 4, 4), LocalDate.of(2025, 4, 6)), 1)
        );
    }

    @DisplayName("날짜와 시간과 테마가 중복되는 예약을 할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateReservation() {
        // given
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        final Theme savedTheme = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        final Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, savedMember.getId(),
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
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        final Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final Theme savedTheme1 = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        final ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(),
                savedTheme1.getId());
        reservationService.createReservation(request1);
        final Theme savedTheme2 = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        final ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow, savedMember.getId(),
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
        final Theme savedTheme = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        final Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, savedMember.getId(), 1L,
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
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        final Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, savedMember.getId(),
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
        final Theme savedTheme = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        final Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final LocalDate yesterday = currentDateTime.getDate().minusDays(1);
        final ReservationCreateCommand request = new ReservationCreateCommand(yesterday, savedMember.getId(), 1L,
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
        final LocalTime time = LocalTime.of(11, 0);
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme savedTheme = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        final Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(),
                savedTheme.getId());
        // when
        final ReservationInfo result = reservationService.createReservation(request);
        // then
        final Reservation savedReservation = reservationDao.findById(1L);
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
        final LocalTime time = LocalTime.of(11, 0);
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa");
        final Theme savedTheme = themeDao.save(theme);
        final Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(),
                savedTheme.getId());
        final ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow.plusDays(1),
                savedMember.getId(),
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
        final LocalTime time = LocalTime.of(11, 0);
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa");
        final Theme savedTheme = themeDao.save(theme);
        final Member savedMember = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, savedMember.getId(),
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
        final LocalTime time1 = LocalTime.of(11, 0);
        final LocalTime time2 = LocalTime.of(12, 0);
        final ReservationTime savedTime1 = reservationTimeRepository.save(new ReservationTime(time1));
        final ReservationTime savedTime2 = reservationTimeRepository.save(new ReservationTime(time2));
        final Theme theme = new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa");
        final Theme savedTheme = themeDao.save(theme);
        final Member savedMember1 = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final Member savedMember2 = memberDao.save(
                new Member(null, "리버", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, savedMember1.getId(),
                savedTime1.getId(), savedTheme.getId());
        final ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow, savedMember2.getId(),
                savedTime2.getId(), savedTheme.getId());

        final ReservationInfo createdReservation1 = reservationService.createReservation(request1);
        final ReservationInfo createdReservation2 = reservationService.createReservation(request2);

        // when
        final List<ReservationInfo> result = reservationService.findReservationsByMemberId(savedMember1.getId());

        // then
        assertThat(result).containsExactlyElementsOf(List.of(createdReservation1));
    }

    @DisplayName("기간을 지정하지 않은 예약 목록을 조회하다")
    @MethodSource(value = "getConditionAndResultSize")
    @ParameterizedTest()
    void getReservationsOfNullDate(ReservationSearchCondition condition, int expectedSize) {
        // given
        final LocalTime time = LocalTime.of(11, 0);
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme1 = new Theme(null, "우테코탈출1", "탈출탈출탈출, ", "aaaa");
        final Theme theme2 = new Theme(null, "우테코탈출2", "탈출탈출탈출, ", "aaaa");
        final Theme savedTheme1 = themeDao.save(theme1);
        final Theme savedTheme2 = themeDao.save(theme2);
        final Member savedMember1 = memberDao.save(
                new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final Member savedMember2 = memberDao.save(
                new Member(null, "리버", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        final ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, savedMember1.getId(),
                savedTime.getId(), savedTheme1.getId());
        final ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow, savedMember1.getId(),
                savedTime.getId(), savedTheme2.getId());
        final ReservationCreateCommand request3 = new ReservationCreateCommand(tomorrow.plusDays(1),
                savedMember2.getId(), savedTime.getId(), savedTheme1.getId());
        final ReservationCreateCommand request4 = new ReservationCreateCommand(tomorrow.plusDays(2),
                savedMember1.getId(), savedTime.getId(), savedTheme1.getId());
        final ReservationCreateCommand request5 = new ReservationCreateCommand(tomorrow.plusDays(4),
                savedMember1.getId(), savedTime.getId(), savedTheme1.getId());

        final ReservationInfo createdReservation1 = reservationService.createReservation(request1);
        final ReservationInfo createdReservation2 = reservationService.createReservation(request2);
        final ReservationInfo createdReservation3 = reservationService.createReservation(request3);
        final ReservationInfo createdReservation4 = reservationService.createReservation(request4);
        final ReservationInfo createdReservation5 = reservationService.createReservation(request5);

        // when & then
        assertThat(reservationService.getReservations(condition)).hasSize(expectedSize);
    }
}

