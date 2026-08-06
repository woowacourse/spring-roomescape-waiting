package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.dto.ReservationCreationRequest;
import roomescape.domain.reservation.dto.ReservationCreationResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservation.dto.ReservationUpdateRequest;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waitingreservation.WaitingReservationRepository;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.RoomescapeException;

class ReservationServiceTest {

    private static final Clock CLOCK = Clock.systemDefaultZone();

    private ReservationService reservationService;
    private ReservationRepository reservationRepository;
    private ReservationDateRepository reservationDateRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private WaitingReservationRepository waitingReservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        reservationDateRepository = mock(ReservationDateRepository.class);
        reservationTimeRepository = mock(ReservationTimeRepository.class);
        themeRepository = mock(ThemeRepository.class);
        configureReservationRepository(reservationRepository);
        configureReservationDateRepository(reservationDateRepository);
        configureReservationTimeRepository(reservationTimeRepository);
        configureThemeRepository(themeRepository);
        waitingReservationRepository = mock(WaitingReservationRepository.class);

        reservationService = new ReservationService(
                reservationRepository,
                new ReservationSlotResolver(reservationDateRepository, reservationTimeRepository, themeRepository),
                waitingReservationRepository,
                CLOCK
        );
    }

    private void configureReservationRepository(ReservationRepository repository) {
        List<Reservation> reservations = new ArrayList<>();
        long[] idCounter = {1L};
        when(repository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            Reservation saved = Reservation.of(
                    idCounter[0]++,
                    reservation.getName(),
                    reservation.getDate(),
                    reservation.getTime(),
                    reservation.getTheme()
            );
            reservations.add(saved);
            return saved;
        });
        when(repository.findAll()).thenAnswer(invocation -> reservations);
        doAnswer(invocation -> {
            Reservation target = invocation.getArgument(0);
            reservations.removeIf(reservation -> reservation.getId().equals(target.getId()));
            return null;
        }).when(repository).delete(any(Reservation.class));
        when(repository.countByTimeId(any(Long.class))).thenAnswer(invocation -> {
            Long timeId = invocation.getArgument(0);
            return (int) reservations.stream()
                    .filter(reservation -> reservation.getTime().getId().equals(timeId))
                    .count();
        });
        when(repository.countByDateId(any(Long.class))).thenAnswer(invocation -> {
            Long dateId = invocation.getArgument(0);
            return (int) reservations.stream()
                    .filter(reservation -> reservation.getDate().getId().equals(dateId))
                    .count();
        });
        when(repository.findReservedTimes(any(Long.class), any(Long.class))).thenAnswer(invocation -> {
            Long themeId = invocation.getArgument(0);
            Long dateId = invocation.getArgument(1);
            return reservations.stream()
                    .filter(reservation -> reservation.getTheme().getId().equals(themeId)
                            && reservation.getDate().getId().equals(dateId))
                    .map(reservation -> reservation.getTime().getId())
                    .toList();
        });
        when(repository.countByThemeId(any(Long.class))).thenAnswer(invocation -> {
            Long themeId = invocation.getArgument(0);
            return (int) reservations.stream()
                    .filter(reservation -> reservation.getTheme().getId().equals(themeId))
                    .count();
        });
        when(repository.findByName(any(String.class))).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            return reservations.stream()
                    .filter(reservation -> reservation.getName().equals(name))
                    .toList();
        });
        when(repository.findUpcomingByName(any(String.class), any(LocalDate.class), any(LocalTime.class)))
                .thenAnswer(invocation -> {
                    String name = invocation.getArgument(0);
                    LocalDate currentDate = invocation.getArgument(1);
                    LocalTime currentTime = invocation.getArgument(2);
                    return reservations.stream()
                            .filter(reservation -> reservation.getName().equals(name))
                            .filter(reservation -> reservation.getDate().getPlayDay().isAfter(currentDate)
                                    || (reservation.getDate().getPlayDay().isEqual(currentDate)
                                    && reservation.getTime().getStartAt().isAfter(currentTime)))
                            .toList();
                });
        when(repository.findById(any(Long.class))).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return reservations.stream()
                    .filter(reservation -> reservation.getId().equals(id))
                    .findFirst();
        });
        when(repository.existsByDateIdAndTimeIdAndThemeId(any(Long.class), any(Long.class), any(Long.class)))
                .thenAnswer(invocation -> {
                    Long dateId = invocation.getArgument(0);
                    Long timeId = invocation.getArgument(1);
                    Long themeId = invocation.getArgument(2);
                    return reservations.stream()
                            .anyMatch(reservation -> reservation.getDate().getId().equals(dateId)
                                    && reservation.getTime().getId().equals(timeId)
                                    && reservation.getTheme().getId().equals(themeId));
                });
    }

    private void configureReservationDateRepository(ReservationDateRepository repository) {
        List<ReservationDate> dates = new ArrayList<>();
        long[] idCounter = {1L};
        when(repository.save(any(ReservationDate.class))).thenAnswer(invocation -> {
            ReservationDate reservationDate = invocation.getArgument(0);
            ReservationDate saved = ReservationDate.of(idCounter[0]++, reservationDate.getPlayDay());
            dates.add(saved);
            return saved;
        });
        when(repository.findById(any(Long.class))).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return dates.stream().filter(date -> date.getId().equals(id)).findFirst();
        });
        when(repository.findAll()).thenAnswer(invocation -> dates);
        when(repository.existsByPlayDay(any(LocalDate.class))).thenAnswer(invocation -> {
            LocalDate playDay = invocation.getArgument(0);
            return dates.stream().anyMatch(date -> date.getPlayDay().equals(playDay));
        });
    }

    private void configureReservationTimeRepository(ReservationTimeRepository repository) {
        List<ReservationTime> times = new ArrayList<>();
        long[] idCounter = {1L};
        when(repository.save(any(ReservationTime.class))).thenAnswer(invocation -> {
            ReservationTime reservationTime = invocation.getArgument(0);
            ReservationTime saved = ReservationTime.of(idCounter[0]++, reservationTime.getStartAt());
            times.add(saved);
            return saved;
        });
        when(repository.findById(any(Long.class))).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return times.stream().filter(time -> time.getId().equals(id)).findFirst();
        });
        when(repository.findAll()).thenAnswer(invocation -> times);
        when(repository.existsByStartAt(any(LocalTime.class))).thenAnswer(invocation -> {
            LocalTime startAt = invocation.getArgument(0);
            return times.stream().anyMatch(time -> time.getStartAt().equals(startAt));
        });
    }

    private void configureThemeRepository(ThemeRepository repository) {
        List<Theme> themes = new ArrayList<>();
        long[] idCounter = {1L};
        when(repository.save(any(Theme.class))).thenAnswer(invocation -> {
            Theme theme = invocation.getArgument(0);
            Theme saved = Theme.of(idCounter[0]++, theme.getName(), theme.getContent(), theme.getUrl());
            themes.add(saved);
            return saved;
        });
        when(repository.findById(any(Long.class))).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return themes.stream().filter(theme -> theme.getId().equals(id)).findFirst();
        });
        when(repository.findAll()).thenAnswer(invocation -> themes);
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationCreationRequest request = new ReservationCreationRequest("테스터", date.getId(), time.getId(),
                theme.getId());

        ReservationCreationResponse response = reservationService.createReservation(request);

        assertThat(response.name()).isEqualTo("테스터");
        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("마감된 일시로 예약 생성 시 예외가 발생한다.")
    void createReservationWithPastTime() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().minusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationCreationRequest request = new ReservationCreationRequest("테스터", date.getId(), time.getId(),
                theme.getId());

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("중복된 예약 생성 시 예외가 발생한다.")
    void createDuplicateReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        reservationService.createReservation(
                new ReservationCreationRequest("테스터1", date.getId(), time.getId(), theme.getId()));

        ReservationCreationRequest duplicateRequest = new ReservationCreationRequest("테스터2", date.getId(), time.getId(),
                theme.getId());

        assertThatThrownBy(() -> reservationService.createReservation(duplicateRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationErrorCode.RESERVATION_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("이름으로 예약을 조회한다.")
    void getReservationsByName() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));
        reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        List<ReservationResponse> responses = reservationService.getReservationsByName("테스터");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void getAllReservations() {
        ReservationDate date1 = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time1 = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationDate date2 = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(2)));
        ReservationTime time2 = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(11, 0)));

        reservationService.createReservation(
                new ReservationCreationRequest("테스터1", date1.getId(), time1.getId(), theme.getId()));
        reservationService.createReservation(
                new ReservationCreationRequest("테스터2", date2.getId(), time2.getId(), theme.getId()));

        List<ReservationResponse> responses = reservationService.getAllReservations();

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void cancelReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationCreationResponse response = reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        reservationService.cancelReservation(response.id());

        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("관리자가 예약을 삭제한다.")
    void deleteReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationCreationResponse response = reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        reservationService.deleteReservation(response.id());

        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 삭제 시 예외가 발생한다.")
    void deleteNotFoundReservation() {
        assertThatThrownBy(() -> reservationService.deleteReservation(999L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("마감된 예약 삭제 시 예외가 발생한다.")
    void cancelClosedReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now()));
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.now().plusMinutes(9)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId("마감예약테스터", date, time, theme));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservation.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("예약을 수정한다.")
    void updateReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationCreationResponse creationResponse = reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        ReservationDate newDate = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(2)));
        ReservationTime newTime = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(14, 0)));

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(newDate.getId(), newTime.getId());

        ReservationResponse updateResponse = reservationService.updateReservation(creationResponse.id(), updateRequest);

        assertThat(updateResponse.date()).isEqualTo(newDate.getPlayDay());
        assertThat(updateResponse.time().id()).isEqualTo(newTime.getId());
    }

    @Test
    @DisplayName("기존 예약과 같은 날짜와 시간으로 예약 수정 시 예외가 발생한다.")
    void updateReservationWithoutChange() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationCreationResponse creationResponse = reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(date.getId(), time.getId());

        assertThatThrownBy(() -> reservationService.updateReservation(creationResponse.id(), updateRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationErrorCode.RESERVATION_NOT_CHANGED.getMessage());
    }

    @Test
    @DisplayName("마감된 일시로 예약 수정 시 예외가 발생한다.")
    void updateReservationWithPastTime() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationCreationResponse creationResponse = reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        ReservationDate pastDate = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().minusDays(1)));

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(pastDate.getId(), time.getId());

        assertThatThrownBy(() -> reservationService.updateReservation(creationResponse.id(), updateRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("이미 존재하는 시간으로 예약 수정 시 예외가 발생한다.")
    void updateReservationToDuplicatedTime() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        ReservationTime anotherTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(14, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationCreationResponse myReservation = reservationService.createReservation(
                new ReservationCreationRequest("내예약", date.getId(), time.getId(), theme.getId()));

        reservationService.createReservation(
                new ReservationCreationRequest("다른사람예약", date.getId(), anotherTime.getId(), theme.getId()));

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(date.getId(), anotherTime.getId());

        assertThatThrownBy(() -> reservationService.updateReservation(myReservation.id(), updateRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationErrorCode.RESERVATION_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("마감된 예약 수정 시 예외가 발생한다.")
    void updateClosedReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now()));
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.now().plusMinutes(9)));
        ReservationTime newTime = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(14, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId("마감예약테스터", date, time, theme));

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(date.getId(), newTime.getId());

        assertThatThrownBy(() -> reservationService.updateReservation(reservation.getId(), updateRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED.getMessage());
    }

}
