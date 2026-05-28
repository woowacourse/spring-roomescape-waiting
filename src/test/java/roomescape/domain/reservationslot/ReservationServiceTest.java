package roomescape.domain.reservationslot;

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
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationService;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.admin.dto.ReservationResponse;
import roomescape.domain.reservation.dto.CreateReservationRequest;
import roomescape.domain.reservation.dto.CreateReservationResponse;
import roomescape.domain.reservation.dto.UpdateReservationRequest;
import roomescape.domain.reservation.dto.UserReservationsResponse;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.fake.FakeReservationDateRepository;
import roomescape.support.fake.FakeReservationRepository;
import roomescape.support.fake.FakeReservationSlotRepository;
import roomescape.support.fake.FakeReservationTimeRepository;
import roomescape.support.fake.FakeThemeRepository;
import roomescape.support.fake.FakeUserRepository;

class ReservationServiceTest {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private FakeReservationSlotRepository reservationSlotRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeReservationDateRepository reservationDateRepository;
    private FakeThemeRepository themeRepository;
    private FakeUserRepository userRepository;
    private FakeReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationSlotRepository = new FakeReservationSlotRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationDateRepository = new FakeReservationDateRepository();
        themeRepository = new FakeThemeRepository();
        userRepository = new FakeUserRepository();
        reservationRepository = new FakeReservationRepository();
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
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
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
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
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
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
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
        ReservationSlot reservationSlot = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(savedReservationDate, reservationTime, theme)
        );
        User user = userRepository.save(User.createWithoutId("보예"));
        reservationRepository.save(
            Reservation.createWithoutId(
                reservationSlot,
                user,
                null,
                ReservationStatus.CONFIRMED,
                now
            )
        );

        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
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
        ReservationSlot secondReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(secondReservationDate,
                reservationTime,
                theme
            )
        );
        ReservationSlot firstReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(firstReservationDate,
                reservationTime,
                theme
            )
        );
        reservationRepository.save(Reservation.createWithoutId(
            secondReservation,
            user,
            null,
            ReservationStatus.CONFIRMED,
            now
        ));
        reservationRepository.save(Reservation.createWithoutId(
            firstReservation,
            user,
            null,
            ReservationStatus.CONFIRMED,
            now
        ));
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when
        UserReservationsResponse userReservations = reservationService.getUserReservations(name);

        // then
        assertSoftly(softly -> {
            assertThat(userReservations.reservations()).hasSize(2);
            assertThat(userReservations.username()).isEqualTo("보예짱");
            assertThat(userReservations.reservations())
                .extracting(
                    "reservationSlot.date.startWhen",
                    "reservationSlot.time.startAt",
                    "reservationSlot.theme.name",
                    "status",
                    "waitingNumber"
                )
                .containsExactly(
                    tuple(LocalDate.of(2026, 5, 13), LocalTime.of(10, 0), "공포", "CONFIRMED", null),
                    tuple(LocalDate.of(2026, 5, 14), LocalTime.of(10, 0), "공포", "CONFIRMED", null)
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
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
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
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
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
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
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
        ReservationSlot reservation = reservationSlotRepository.findById(response.id()).orElseThrow();

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
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
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
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
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
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        Reservation savedUserReservation = saveConfirmedReservation(savedReservation, now);
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when
        reservationService.cancelUserReservation(savedUserReservation.getId());

        // then
        assertThat(reservationRepository.findById(savedUserReservation.getId())).isEmpty();
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
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        Reservation savedUserReservation = saveConfirmedReservation(savedReservation, now);
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when & then
        assertThatThrownBy(() -> reservationService.cancelUserReservation(savedUserReservation.getId()))
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
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        Reservation savedUserReservation = saveConfirmedReservation(savedReservation, now);
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when & then
        assertThatThrownBy(() -> reservationService.cancelUserReservation(savedUserReservation.getId()))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("예전 예약은 삭제할 수 없습니다. 오늘 날짜:" + LocalDate.of(2026, 5, 12));
    }

    @Test
    @DisplayName("사용자가 존재하지 않는 예약을 삭제하면 예외가 발생한다.")
    void throwExceptionWhenUserDeletesNonExistentReservation() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );

        // when & then
        assertThatThrownBy(() -> reservationService.cancelUserReservation(1L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("사용자 예약 신청이 존재하지 않습니다.");
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
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(beforeReservationDate, beforeReservationTime, theme)
        );
        Reservation savedUserReservation = saveConfirmedReservation(savedReservation, now);
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(
            afterReservationDate.getId(),
            afterReservationTime.getId()
        );

        // when
        reservationService.updateReservation(savedUserReservation.getId(), request);

        // then
        ReservationSlot updatedReservation = reservationRepository.findById(savedUserReservation.getId())
            .orElseThrow()
            .getReservationSlot();
        assertSoftly(softly -> {
            assertThat(updatedReservation.getDate().getDate()).isEqualTo(LocalDate.of(2026, 5, 14));
            assertThat(updatedReservation.getTime().getStartAt()).isEqualTo(LocalTime.of(15, 0));
            assertThat(updatedReservation.getTheme().getName()).isEqualTo("공포");
        });
    }

    @Test
    @DisplayName("확정 예약을 같은 날짜와 시간으로 수정하면 수정 시각 기준으로 대기 순서를 재정렬한다.")
    void updateReservationToSameSchedule() {
        // given
        Clock confirmedClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        Clock firstWaitingClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 1));
        Clock secondWaitingClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 2));
        Clock updateClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 3));
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        User confirmedUser = userRepository.save(User.createWithoutId("보예"));
        User firstWaitingUser = userRepository.save(User.createWithoutId("로치"));
        User secondWaitingUser = userRepository.save(User.createWithoutId("수민"));
        Reservation confirmedReservation = saveConfirmedReservation(savedReservation, confirmedUser, confirmedClock);
        Reservation firstWaitingReservation = saveWaitingReservation(
            savedReservation,
            firstWaitingUser,
            1L,
            firstWaitingClock
        );
        Reservation secondWaitingReservation = saveWaitingReservation(
            savedReservation,
            secondWaitingUser,
            2L,
            secondWaitingClock
        );
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            updateClock
        );
        UpdateReservationRequest request = new UpdateReservationRequest(
            reservationDate.getId(),
            reservationTime.getId()
        );

        // when
        reservationService.updateReservation(confirmedReservation.getId(), request);

        // then
        Reservation updatedConfirmedReservation = reservationRepository.findById(confirmedReservation.getId())
            .orElseThrow();
        Reservation updatedFirstWaitingReservation = reservationRepository.findById(firstWaitingReservation.getId())
            .orElseThrow();
        Reservation updatedSecondWaitingReservation = reservationRepository.findById(secondWaitingReservation.getId())
            .orElseThrow();
        assertSoftly(softly -> {
            assertThat(updatedFirstWaitingReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(updatedFirstWaitingReservation.getWaitingNumber()).isEqualTo(0L);
            assertThat(updatedSecondWaitingReservation.getWaitingNumber()).isEqualTo(1L);
            assertThat(updatedSecondWaitingReservation.getStatus()).isEqualTo(ReservationStatus.WAITING);
            assertThat(updatedConfirmedReservation.getWaitingNumber()).isEqualTo(2L);
            assertThat(updatedConfirmedReservation.getStatus()).isEqualTo(ReservationStatus.WAITING);
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
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, beforeReservationTime, theme)
        );
        Reservation savedUserReservation = saveConfirmedReservation(savedReservation, now);
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(null, afterReservationTime.getId());

        // when
        reservationService.updateReservation(savedUserReservation.getId(), request);
        ReservationSlot updatedReservation = reservationRepository.findById(savedUserReservation.getId())
            .orElseThrow()
            .getReservationSlot();

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
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(1L, 2L);

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(1L, request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("사용자 예약 신청이 존재하지 않습니다.");
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
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        Reservation savedUserReservation = saveConfirmedReservation(savedReservation, now);
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(999L, null);

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(savedUserReservation.getId(), request))
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
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        Reservation savedUserReservation = saveConfirmedReservation(savedReservation, now);
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(null, 999L);

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(savedUserReservation.getId(), request))
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
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        Reservation savedUserReservation = saveConfirmedReservation(savedReservation, now);
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(beforeToday.getId(), null);

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(savedUserReservation.getId(), request))
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
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        Reservation savedUserReservation = saveConfirmedReservation(savedReservation, now);
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(null, beforeNow.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(savedUserReservation.getId(), request))
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
        ReservationSlot savedReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        ReservationSlot otherReservation = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, otherReservationTime, theme)
        );
        User user = userRepository.save(User.createWithoutId("보예"));
        Reservation savedUserReservation = saveConfirmedReservation(savedReservation, user, now);
        saveConfirmedReservation(otherReservation, user, now);
        ReservationService reservationService = new ReservationService(
            reservationSlotRepository,
            reservationTimeRepository,
            reservationDateRepository,
            reservationRepository,
            themeRepository,
            userRepository,
            now
        );
        UpdateReservationRequest request = new UpdateReservationRequest(null, otherReservationTime.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(savedUserReservation.getId(), request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("중복 예약입니다. 예약 정보를 다시 확인해주세요.");
    }

    private Reservation saveConfirmedReservation(ReservationSlot reservationSlot, Clock clock) {
        User user = userRepository.save(User.createWithoutId("보예"));
        return saveConfirmedReservation(reservationSlot, user, clock);
    }

    private Reservation saveConfirmedReservation(ReservationSlot reservationSlot, User user, Clock clock) {
        return reservationRepository.save(Reservation.createWithoutId(
            reservationSlot,
            user,
            null,
            ReservationStatus.CONFIRMED,
            clock
        ));
    }

    private Reservation saveWaitingReservation(
        ReservationSlot reservationSlot,
        User user,
        Long waitingNumber,
        Clock clock
    ) {
        return reservationRepository.save(Reservation.createWithoutId(
            reservationSlot,
            user,
            waitingNumber,
            ReservationStatus.WAITING,
            clock
        ));
    }

    private Clock fixedClockAt(LocalDateTime dateTime) {
        return Clock.fixed(dateTime.atZone(ZONE_ID).toInstant(), ZONE_ID);
    }
}
