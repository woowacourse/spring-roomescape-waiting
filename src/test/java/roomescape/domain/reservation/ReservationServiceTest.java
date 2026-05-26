package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.admin.dto.ReservationResponse;
import roomescape.domain.reservation.dto.CreateReservationRequest;
import roomescape.domain.reservation.dto.CreateReservationResponse;
import roomescape.domain.reservation.dto.UpdateReservationRequest;
import roomescape.domain.reservation.dto.UserReservationResponse;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;
import roomescape.domain.userreservation.UserReservation;
import roomescape.domain.userreservation.WaitingStatus;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.fake.FakeReservationDateRepository;
import roomescape.support.fake.FakeReservationRepository;
import roomescape.support.fake.FakeReservationTimeRepository;
import roomescape.support.fake.FakeThemeRepository;
import roomescape.support.fake.FakeUserRepository;
import roomescape.support.fake.FakeUserReservationRepository;

class ReservationServiceTest {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeReservationDateRepository reservationDateRepository;
    private FakeThemeRepository themeRepository;
    private FakeUserRepository userRepository;
    private FakeUserReservationRepository userReservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationDateRepository = new FakeReservationDateRepository();
        themeRepository = new FakeThemeRepository();
        userRepository = new FakeUserRepository();
        userReservationRepository = new FakeUserReservationRepository();
    }

    @Test
    @DisplayName("존재하는 예약 시간으로 예약을 생성한다.")
    void createReservationWithExistingReservationTime() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        ReservationDate reservationDate = ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13));
        ReservationDate savedReservationDate = reservationDateRepository.save(reservationDate);
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        CreateReservationRequest request = new CreateReservationRequest(
            "보예",
            savedReservationDate.getId(),
            savedReservationTime.getId(),
            theme.getId()
        );

        // when
        CreateReservationResponse response = reservationService.createReservation(request);

        // then
        assertSoftly(softly -> {
            assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 13));
            assertThat(response.time()).isEqualTo(LocalTime.of(10, 0));
            assertThat(response.theme().name()).isEqualTo("공포");
        });
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약을 생성하면 예외가 발생한다.")
    void throwExceptionWhenCreatingReservationWithNonExistentReservationTime() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        CreateReservationRequest request = new CreateReservationRequest("보예", 1L, 1L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("존재하지 않는 예약 시간대 입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약을 생성하면 예외가 발생한다.")
    void throwExceptionWhenCreatingReservationWithNonExistentTheme() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13)));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        CreateReservationRequest request = new CreateReservationRequest(
            "보예",
            reservationDate.getId(),
            reservationTime.getId(),
            3L
        );

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("존재하지 않는 테마 입니다.");
    }

    @Test
    @DisplayName("예약 목록을 전체 조회한다.")
    void getAllReservations() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationDate savedReservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운테마", "theme-url"));
        reservationRepository.save(
            Reservation.createWithoutId(savedReservationDate,
                reservationTime,
                theme
            )
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when
        List<ReservationResponse> responses = reservationService.getAllReservations();

        // then
        assertSoftly(softly -> {
            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().date()).isEqualTo(LocalDate.of(2026, 5, 13));
            assertThat(responses.getFirst().time().id()).isEqualTo(reservationTime.getId());
            assertThat(responses.getFirst().time().startAt()).isEqualTo(LocalTime.of(10, 0));
            assertThat(responses.getFirst().theme().id()).isEqualTo(theme.getId());
            assertThat(responses.getFirst().theme().name()).isEqualTo("공포");
        });
    }

    @Test
    @DisplayName("사용자가 이름으로 예약을 조회한다.")
    void getUserReservationsByName() {
        // given
        String name = "보예짱";
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationDate firstReservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        ReservationDate secondReservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 14))
        );
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운테마", "theme-url"));
        User user = userRepository.save(User.createWithoutId(name));
        Reservation secondReservation = reservationRepository.save(
            Reservation.createWithoutId(secondReservationDate,
                reservationTime,
                theme
            )
        );
        Reservation firstReservation = reservationRepository.save(
            Reservation.createWithoutId(firstReservationDate,
                reservationTime,
                theme
            )
        );
        userReservationRepository.save(UserReservation.createWithoutId(
            secondReservation,
            user,
            null,
            WaitingStatus.CONFIRMED,
            LocalDateTime.of(2026, 5, 12, 13, 0),
            LocalDateTime.of(2026, 5, 12, 13, 0)
        ));
        userReservationRepository.save(UserReservation.createWithoutId(
            firstReservation,
            user,
            null,
            WaitingStatus.CONFIRMED,
            LocalDateTime.of(2026, 5, 12, 13, 0),
            LocalDateTime.of(2026, 5, 12, 13, 0)
        ));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when
        UserReservationResponse userReservations = reservationService.getUserReservations(name);

        // then
        assertSoftly(softly -> {
            assertThat(userReservations.reservation().size()).isEqualTo(2);
            assertThat(userReservations.name()).isEqualTo("보예짱");
            assertThat(userReservations.reservation())
                .extracting(
                    reservationPayload -> reservationPayload.date().startWhen(),
                    reservationPayload -> reservationPayload.time().startAt(),
                    reservationPayload -> reservationPayload.theme().name()
                )
                .containsExactly(
                    tuple(LocalDate.of(2026, 5, 14), LocalTime.of(10, 0), "공포"),
                    tuple(LocalDate.of(2026, 5, 13), LocalTime.of(10, 0), "공포")
                );
        });
    }

    @Test
    @DisplayName("오늘보다 이전 날짜는 예약할 수 없다.")
    void throwExceptionWhenCreatingReservationBeforeToday() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(14, 0))
        );
        ReservationDate beforeToday = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 10))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        CreateReservationRequest request = new CreateReservationRequest(
            "보예",
            beforeToday.getId(),
            reservationTime.getId(),
            theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("예약 날짜는 오늘 이후여야 합니다. 오늘 날짜:" + LocalDate.of(2026, 5, 12));
    }

    @Test
    @DisplayName("오늘 예약일 경우 현재 시간 이전은 예약할 수 없다.")
    void throwExceptionWhenCreatingReservationBeforeCurrentTimeOnToday() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime beforeNow = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(12, 59))
        );
        ReservationDate today = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 12))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        CreateReservationRequest request = new CreateReservationRequest(
            "보예",
            today.getId(),
            beforeNow.getId(),
            theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("예약 시간은 현재 이후여야 합니다. 현재 시각:" + LocalTime.of(13, 0));
    }

    @Test
    @DisplayName("오늘 예약이지만 현재 시간은 예약할 수 있다.")
    void createReservationAtCurrentTimeOnToday() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime nowTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(13, 0))
        );
        ReservationDate today = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 12))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        CreateReservationRequest request = new CreateReservationRequest(
            "보예",
            today.getId(),
            nowTime.getId(),
            theme.getId()
        );

        // when
        CreateReservationResponse response = reservationService.createReservation(request);
        Reservation reservation = reservationRepository.findById(response.id()).orElseThrow();

        // then
        assertSoftly(softly -> {
                assertThat(response.id()).isEqualTo(reservation.getId());
                assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 12));
                assertThat(response.time()).isEqualTo(LocalTime.of(13, 0));
                assertThat(response.theme().name()).isEqualTo("공포");
                assertThat(response.theme().content()).isEqualTo("무서운 테마");
                assertThat(response.theme().url()).isEqualTo("theme-url");
            }
        );
    }

    @Test
    @DisplayName("날짜가 오늘 이후이고 현재 시간보다 이전이면 정상 예약 된다.")
    void createReservationAfterTodayEvenIfTimeIsBeforeNow() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        ReservationDate today = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        CreateReservationRequest request = new CreateReservationRequest(
            "보예",
            today.getId(),
            reservationTime.getId(),
            theme.getId()
        );

        // when
        CreateReservationResponse response = reservationService.createReservation(request);
        Reservation reservation = reservationRepository.findById(response.id()).orElseThrow();

        // then
        assertSoftly(softly -> {
                assertThat(response.id()).isEqualTo(reservation.getId());
                assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 13));
                assertThat(response.time()).isEqualTo(LocalTime.of(10, 0));
                assertThat(response.theme().name()).isEqualTo("공포");
                assertThat(response.theme().content()).isEqualTo("무서운 테마");
                assertThat(response.theme().url()).isEqualTo("theme-url");
            }
        );
    }

    @Test
    @DisplayName("중복된 예약은 예외가 발생한다.")
    void throwExceptionWhenCreatingDuplicatedReservation() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        ReservationDate reservationDate = ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13));
        ReservationDate savedReservationDate = reservationDateRepository.save(reservationDate);
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        CreateReservationRequest request = new CreateReservationRequest(
            "보예",
            savedReservationDate.getId(),
            savedReservationTime.getId(),
            theme.getId()
        );
        reservationService.createReservation(request);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("중복 예약입니다. 예약 정보를 다시 확인해주세요.");
    }

    @Test
    @DisplayName("사용자는 미래 예약을 삭제할 수 있다.")
    void deleteFutureReservationForUser() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        Reservation savedReservation = reservationRepository.save(
            Reservation.createWithoutId(reservationDate, reservationTime, theme)
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when
        reservationService.cancelUserReservation(savedReservation.getId());

        // then
        assertThat(reservationRepository.findById(savedReservation.getId())).isEmpty();
    }

    @Test
    @DisplayName("사용자는 이미 시간이 지난 예약을 삭제할 수 없다.")
    void throwExceptionWhenUserDeletesPastTimeReservation() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(12, 59))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 12))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        Reservation savedReservation = reservationRepository.save(
            Reservation.createWithoutId(reservationDate, reservationTime, theme)
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when & then
        assertThatThrownBy(() -> reservationService.cancelUserReservation(savedReservation.getId()))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("현재보다 이전 시간 예약을 삭제할 수 없습니다. 현재 시각:" + LocalTime.of(13, 0));
    }

    @Test
    @DisplayName("사용자는 이미 날짜가 지난 예약을 삭제할 수 없다.")
    void throwExceptionWhenUserDeletesPastDateReservation() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(12, 59))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 11))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        Reservation savedReservation = reservationRepository.save(
            Reservation.createWithoutId(reservationDate, reservationTime, theme)
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when & then
        assertThatThrownBy(() -> reservationService.cancelUserReservation(savedReservation.getId()))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("예전 예약은 삭제할 수 없습니다. 오늘 날짜:" + LocalDate.of(2026, 5, 12));
    }

    @Test
    @DisplayName("사용자가 존재하지 않는 예약을 삭제하면 예외가 발생한다.")
    void throwExceptionWhenUserDeletesNonExistentReservation() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when & then
        assertThatThrownBy(() -> reservationService.cancelUserReservation(1L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("존재하지 않는 예약건 입니다");
    }

    @Test
    @DisplayName("예약 날짜와 시간을 수정한다.")
    void updateReservationDateAndTime() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime beforeReservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        ReservationTime afterReservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(15, 0))
        );
        ReservationDate beforeReservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        ReservationDate afterReservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 14))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        Reservation savedReservation = reservationRepository.save(
            Reservation.createWithoutId(beforeReservationDate, beforeReservationTime, theme)
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(
            afterReservationDate.getDate(),
            afterReservationTime.getStartAt()
        );

        // when
        reservationService.updateReservation(savedReservation.getId(), request);

        // then
        Reservation updatedReservation = reservationRepository.findById(savedReservation.getId()).orElseThrow();
        assertSoftly(softly -> {
            assertThat(updatedReservation.getDate().getDate()).isEqualTo(LocalDate.of(2026, 5, 14));
            assertThat(updatedReservation.getTime().getStartAt()).isEqualTo(LocalTime.of(15, 0));
            assertThat(updatedReservation.getTheme().getName()).isEqualTo("공포");
        });
    }

    @Test
    @DisplayName("예약 시간만 수정한다.")
    void updateReservationTimeOnly() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime beforeReservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        ReservationTime afterReservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(15, 0))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        Reservation savedReservation = reservationRepository.save(
            Reservation.createWithoutId(reservationDate, beforeReservationTime, theme)
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(null, afterReservationTime.getStartAt());

        // when
        reservationService.updateReservation(savedReservation.getId(), request);
        Reservation updatedReservation = reservationRepository.findById(savedReservation.getId()).orElseThrow();

        // then
        assertSoftly(softly -> {
            assertThat(updatedReservation.getDate().getDate()).isEqualTo(LocalDate.of(2026, 5, 13));
            assertThat(updatedReservation.getTime().getStartAt()).isEqualTo(LocalTime.of(15, 0));
        });
    }

    @Test
    @DisplayName("존재하지 않는 예약을 수정하면 예외가 발생한다.")
    void throwExceptionWhenUpdatingNonExistentReservation() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(LocalDate.of(2026, 5, 13), LocalTime.of(10, 0));

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(1L, request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("존재하지 않는 예약건 입니다");
    }

    @Test
    @DisplayName("존재하지 않는 예약 날짜로 수정하면 예외가 발생한다.")
    void throwExceptionWhenUpdatingReservationWithNonExistentDate() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        Reservation savedReservation = reservationRepository.save(
            Reservation.createWithoutId(reservationDate, reservationTime, theme)
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(LocalDate.of(2026, 5, 14), null);

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("존재하지 않는 날짜 입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 수정하면 예외가 발생한다.")
    void throwExceptionWhenUpdatingReservationWithNonExistentTime() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        Reservation savedReservation = reservationRepository.save(
            Reservation.createWithoutId(reservationDate, reservationTime, theme)
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(null, LocalTime.of(15, 0));

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("존재하지 않는 예약 시간대 입니다.");
    }

    @Test
    @DisplayName("오늘보다 이전 날짜로 예약을 수정할 수 없다.")
    void throwExceptionWhenUpdatingReservationToDateBeforeToday() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(15, 0))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        ReservationDate beforeToday = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 11))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        Reservation savedReservation = reservationRepository.save(
            Reservation.createWithoutId(reservationDate, reservationTime, theme)
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(beforeToday.getDate(), null);

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("예약 날짜는 오늘 이후여야 합니다. 오늘 날짜:" + LocalDate.of(2026, 5, 12));
    }

    @Test
    @DisplayName("오늘 예약을 현재 시간보다 이전으로 수정할 수 없다.")
    void throwExceptionWhenUpdatingReservationToTimeBeforeNowOnToday() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(15, 0))
        );
        ReservationTime beforeNow = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(12, 59))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 12))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        Reservation savedReservation = reservationRepository.save(
            Reservation.createWithoutId(reservationDate, reservationTime, theme)
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(null, beforeNow.getStartAt());

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("예약 시간은 현재 이후여야 합니다. 현재 시각:" + LocalTime.of(13, 0));
    }

    @Test
    @DisplayName("중복된 예약으로 수정하면 예외가 발생한다.")
    void throwExceptionWhenUpdatingReservationToDuplicatedSchedule() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        ReservationTime otherReservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(15, 0))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        Reservation savedReservation = reservationRepository.save(
            Reservation.createWithoutId(reservationDate, reservationTime, theme)
        );
        reservationRepository.save(
            Reservation.createWithoutId(reservationDate, otherReservationTime, theme)
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationDateRepository,
            userReservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(null, otherReservationTime.getStartAt());

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("중복 예약입니다. 예약 정보를 다시 확인해주세요.");
    }

    private Clock fixedClockAt(LocalDateTime dateTime) {
        return Clock.fixed(dateTime.atZone(ZONE_ID).toInstant(), ZONE_ID);
    }
}
