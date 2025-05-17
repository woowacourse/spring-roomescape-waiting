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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import roomescape.reservation.service.dto.ReservationSearchCondition;

class ReservationServiceTest {

    private ReservationTimeRepository reservationTimeRepository;
    private FakeReservationRepository reservationRepository;
    private FakeThemeRepository themeDao;
    private FakeMemberRepository memberDao;
    private CurrentDateTime currentDateTime;
    private ReservationService reservationService;
    private ReservationTime savedTime;
    private LocalDate tomorrow;
    private Theme savedTheme;
    private Member savedMember;
    private ReservationCreateCommand createCommand;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationRepository = new FakeReservationRepository();
        themeDao = new FakeThemeRepository();
        memberDao = new FakeMemberRepository();
        currentDateTime = new TestCurrentDateTime(LocalDateTime.of(2025, 4, 2, 11, 0));
        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeDao,
                memberDao, currentDateTime);
        savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        savedTheme = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출탈출", "탈출해라"));
        savedMember = memberDao.save(new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        tomorrow = currentDateTime.getDate().plusDays(1);
        createCommand = new ReservationCreateCommand(tomorrow, savedMember.getId(), savedTime.getId(),
                savedTheme.getId());
    }

    @DisplayName("날짜와 시간과 테마가 중복되는 예약을 할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateReservation() {
        // given
        reservationService.createReservation(createCommand);

        // when
        // then
        assertThatThrownBy(() -> reservationService.createReservation(createCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간에 이미 예약이 존재합니다.");
    }

    @DisplayName("날짜와 시간이 같아도 테마가 다르면 예외가 발생하지 않는다")
    @Test
    void shouldNot_ThrowException_WhenThemeIsDifferent() {
        // given
        reservationService.createReservation(createCommand);
        Theme savedTheme2 = themeDao.save(new Theme(null, "우테코탈출", "탈출탈출탈출, ", "aaaa"));
        ReservationCreateCommand command2 = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(), savedTheme2.getId());

        // when
        // then
        assertThatCode(() -> reservationService.createReservation(command2))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약 시간이 존재하지 않을 경우 예외가 발생한다")
    @Test
    void validateTime() {
        // given
        ReservationCreateCommand command = new ReservationCreateCommand(tomorrow, savedMember.getId(), 100L,
                savedTheme.getId());

        // when
        // then
        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시간이 존재하지 않습니다.");
    }

    @DisplayName("테마가 존재하지 않을 경우 예외가 발생한다")
    @Test
    void validateTheme() {
        // given
        ReservationCreateCommand command = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(), 100L);

        // when
        // then
        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마가 존재하지 않습니다.");
    }

    @DisplayName("과거 시간에 예약할 경우 예외가 발생한다")
    @Test
    void validatePastTime() {
        // given
        ReservationTime savedTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        ReservationCreateCommand command = new ReservationCreateCommand(tomorrow.minusDays(2), savedMember.getId(),
                savedTime2.getId(), savedTheme.getId());

        // when
        // then
        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지나간 날짜와 시간은 예약 불가합니다.");
    }

    @DisplayName("예약을 생성할 수 있다")
    @Test
    void create() {
        // given
        ReservationCreateCommand command = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(), savedTheme.getId());

        // when
        ReservationInfo result = reservationService.createReservation(command);

        // then
        Reservation savedReservation = reservationRepository.findById(1L);
        assertAll(
                () -> assertThat(result.member().name()).isEqualTo(savedMember.getName()),
                () -> assertThat(result.date()).isEqualTo(tomorrow),
                () -> assertThat(result.time().startAt()).isEqualTo(savedTime.getStartAt()),
                () -> assertThat(result.theme().name()).isEqualTo(savedTheme.getName()),
                () -> assertThat(result.theme().description()).isEqualTo(savedTheme.getDescription()),
                () -> assertThat(result.theme().thumbnail()).isEqualTo(savedTheme.getThumbnail()),
                () -> assertThat(savedReservation.getName()).isEqualTo(savedMember.getName()),
                () -> assertThat(savedReservation.getDate()).isEqualTo(command.date()),
                () -> assertThat(savedReservation.getTime().getStartAt()).isEqualTo(savedTime.getStartAt()),
                () -> assertThat(savedReservation.getTheme().getName()).isEqualTo(savedTheme.getName()),
                () -> assertThat(savedReservation.getTheme().getDescription()).isEqualTo(savedTheme.getDescription()),
                () -> assertThat(savedReservation.getTheme().getThumbnail()).isEqualTo(savedTheme.getThumbnail())
        );
    }

    @DisplayName("예약을 삭제할 수 있다")
    @Test
    void cancelById() {
        // given
        reservationService.createReservation(createCommand);

        // when
        reservationService.cancelReservationById(1L);

        // then
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @DisplayName("멤버 예약 목록을 조회할 수 있다")
    @Test
    void findReservationsByMember() {
        // given
        ReservationInfo createdReservation = reservationService.createReservation(createCommand);
        ReservationTime savedTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Member savedMember2 = memberDao.save(new Member(null, "리버", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        ReservationCreateCommand command2 = new ReservationCreateCommand(tomorrow, savedMember2.getId(),
                savedTime2.getId(), savedTheme.getId());
        reservationService.createReservation(command2);

        // when
        List<ReservationInfo> result = reservationService.findReservationsByMemberId(savedMember.getId());

        // then
        assertThat(result).containsExactlyElementsOf(List.of(createdReservation));
    }

    @DisplayName("멤버/테마/기간 조건에 따른 예약 목록을 조회할 수 있다")
    @MethodSource(value = "getConditionAndResultSize")
    @ParameterizedTest()
    void gerReservations(ReservationSearchCondition condition, int expectedSize) {
        // given
        createReservations();

        // when
        // then
        assertThat(reservationService.getReservations(condition)).hasSize(expectedSize);
    }

    private void createReservations() {
        Theme savedTheme2 = themeDao.save(new Theme(null, "우테코탈출2", "탈출탈출탈출, ", "aaaa"));
        Member savedMember2 = memberDao.save(new Member(null, "리버", "admin@gmail.com", "qwer!", MemberRole.ADMIN));
        ReservationCreateCommand command1 = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(), savedTheme.getId());
        ReservationCreateCommand command2 = new ReservationCreateCommand(tomorrow, savedMember.getId(),
                savedTime.getId(), savedTheme2.getId());
        ReservationCreateCommand command3 = new ReservationCreateCommand(tomorrow.plusDays(1), savedMember2.getId(),
                savedTime.getId(), savedTheme.getId());
        ReservationCreateCommand command4 = new ReservationCreateCommand(tomorrow.plusDays(2), savedMember.getId(),
                savedTime.getId(), savedTheme.getId());
        ReservationCreateCommand command5 = new ReservationCreateCommand(tomorrow.plusDays(4), savedMember.getId(),
                savedTime.getId(), savedTheme.getId());
        reservationService.createReservation(command1);
        reservationService.createReservation(command2);
        reservationService.createReservation(command3);
        reservationService.createReservation(command4);
        reservationService.createReservation(command5);
    }

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
}

