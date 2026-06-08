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
import roomescape.domain.reservation.dto.ReservationWithWaitingNumber;
import roomescape.domain.reservation.dto.UpdateReservationRequest;
import roomescape.domain.reservation.dto.UserReservationsResponse;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateService;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationslot.ReservationSlotService;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeService;
import roomescape.domain.user.User;
import roomescape.domain.user.UserService;
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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);
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
                ReservationStatus.CONFIRMED,
                now
            )
        );

        ReservationService reservationService = createReservationService(now);

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
    @DisplayName("관리자는 취소된 예약을 포함한 예약 목록을 전체 조회한다.")
    void getAllReservationsIncludingCanceledReservation() {
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
        User confirmedUser = userRepository.save(User.createWithoutId("보예"));
        User canceledUser = userRepository.save(User.createWithoutId("수민"));
        reservationRepository.save(Reservation.createWithoutId(
            reservationSlot,
            confirmedUser,
            ReservationStatus.CONFIRMED,
            now
        ));
        reservationRepository.save(Reservation.createWithoutId(
            reservationSlot,
            canceledUser,
            ReservationStatus.CANCELED,
            now
        ));
        ReservationService reservationService = createReservationService(now);

        // when
        List<ReservationResponse> responses = reservationService.getAllReservations();

        // then
        assertThat(responses)
            .extracting(ReservationResponse::userName, ReservationResponse::reservationStatus)
            .containsExactly(
                tuple("보예", ReservationStatus.CONFIRMED),
                tuple("수민", ReservationStatus.CANCELED)
            );
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
            ReservationStatus.CONFIRMED,
            now
        ));
        reservationRepository.save(Reservation.createWithoutId(
            firstReservation,
            user,
            ReservationStatus.CONFIRMED,
            now
        ));
        ReservationService reservationService = createReservationService(now);

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
                    "status"
                )
                .containsExactly(
                    tuple(LocalDate.of(2026, 5, 13), LocalTime.of(10, 0), "공포", "CONFIRMED"),
                    tuple(LocalDate.of(2026, 5, 14), LocalTime.of(10, 0), "공포", "CONFIRMED")
                );
        });
    }

    @Test
    @DisplayName("사용자는 취소된 예약을 포함한 본인의 예약 목록을 조회한다.")
    void getUserReservationsIncludingCanceledReservation() {
        // given
        String name = "보예짱";
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운테마", "theme-url"));
        User user = userRepository.save(User.createWithoutId(name));
        ReservationSlot reservationSlot = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        reservationRepository.save(Reservation.createWithoutId(
            reservationSlot,
            user,
            ReservationStatus.CONFIRMED,
            now
        ));
        reservationRepository.save(Reservation.createWithoutId(
            reservationSlot,
            user,
            ReservationStatus.WAITING,
            now
        ));
        reservationRepository.save(Reservation.createWithoutId(
            reservationSlot,
            user,
            ReservationStatus.CANCELED,
            now
        ));
        ReservationService reservationService = createReservationService(now);

        // when
        UserReservationsResponse userReservations = reservationService.getUserReservations(name);

        // then
        assertThat(userReservations.reservations())
            .extracting("status", "waitingNumber")
            .containsExactlyInAnyOrder(
                tuple("CANCELED", null),
                tuple("WAITING", 1L),
                tuple("CONFIRMED", null)
            );
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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);
        CreateReservationRequest request = new CreateReservationRequest(
            "보예",
            today.getId(),
            reservationTime.getId(),
            theme.getId()
        );

        // when
        CreateReservationResponse response = reservationService.createReservation(request);
        Reservation reservation = reservationRepository.findActiveReservation(response.id()).orElseThrow();

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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);

        // when
        reservationService.cancelUserReservation(savedUserReservation.getId());

        // then
        assertThat(reservationRepository.findActiveReservation(savedUserReservation.getId())).isEmpty();
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
        ReservationService reservationService = createReservationService(now);

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
        ReservationService reservationService = createReservationService(now);

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
        ReservationService reservationService = createReservationService(now);

        // when & then
        assertThatThrownBy(() -> reservationService.cancelUserReservation(1L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("사용자 예약 신청이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("확정 예약을 취소하면 슬롯의 활성 예약은 0건이다.")
    void cancelConfirmedReservation() {
        // given
        Clock now = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));

        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        Theme theme = themeRepository.save(
            Theme.createWithoutId("공포", "무서운 테마", "theme-url")
        );
        ReservationSlot reservationSlot = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        User user = userRepository.save(User.createWithoutId("boye"));
        Reservation confirmedReservation = saveConfirmedReservation(reservationSlot, user, now);
        ReservationService reservationService = createReservationService(now);

        // when
        reservationService.cancelUserReservation(confirmedReservation.getId());

        // then
        assertSoftly(softly -> {
                assertThat(reservationSlotRepository.findBySchedule(
                    reservationTime.getId(),
                    reservationDate.getId(),
                    theme.getId())
                ).isNotEmpty();
                assertThat(reservationRepository.countByReservationSlotId(reservationSlot.getId())).isZero();
                assertThat(reservationRepository.findActiveReservation(confirmedReservation.getId())).isEmpty();
            }
        );
    }

    @Test
    @DisplayName("확정 예약자가 예약을 취소하면 대기 1번 예약자만 확정으로 변경된다.")
    void promoteFirstWaitingReservation() {
        // given
        Clock confirmedClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        Clock firstWaitingClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 1));
        Clock secondWaitingClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 2));
        Clock cancelClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 3));
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        Theme theme = themeRepository.save(
            Theme.createWithoutId("공포", "무섭다", "theme-url")
        );
        ReservationSlot reservationSlot = reservationSlotRepository.save(ReservationSlot.createWithoutId(
            reservationDate, reservationTime, theme
        ));
        User confirmedUser = userRepository.save(User.createWithoutId("보예"));
        User firstWaitingUser = userRepository.save(User.createWithoutId("수민"));
        User secondWaitingUser = userRepository.save(User.createWithoutId("말랑"));
        saveConfirmedReservation(reservationSlot, confirmedUser, confirmedClock);
        saveWaitingReservation(reservationSlot, firstWaitingUser, firstWaitingClock);
        saveWaitingReservation(reservationSlot, secondWaitingUser, secondWaitingClock);

        // when
        ReservationService reservationService = createReservationService(cancelClock);
        reservationService.cancelUserReservation(confirmedUser.getId());

        // then
        ReservationWithWaitingNumber updatedReservation = reservationRepository.findReservations("수민").getFirst();
        ReservationWithWaitingNumber waitingReservation = reservationRepository.findReservations("말랑").getFirst();
        assertSoftly(softly -> {
            assertThat(updatedReservation.reservation().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(waitingReservation.reservation().getStatus()).isEqualTo(ReservationStatus.WAITING);
            assertThat(waitingReservation.waitingNumber()).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("대기 예약자가 대기를 취소한다면 다른 예약이 확정으로 승격되지 않는다.")
    void notPromoteFirstWaitingReservation() {
        // given
        Clock confirmedClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 0));
        Clock firstWaitingClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 1));
        Clock secondWaitingClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 2));
        Clock cancelClock = fixedClockAt(LocalDateTime.of(2026, 5, 12, 13, 3));
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 13))
        );
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        Theme theme = themeRepository.save(
            Theme.createWithoutId("공포", "무섭다", "theme-url")
        );
        ReservationSlot reservationSlot = reservationSlotRepository.save(ReservationSlot.createWithoutId(
            reservationDate, reservationTime, theme
        ));
        User confirmedUser = userRepository.save(User.createWithoutId("보예"));
        User firstWaitingUser = userRepository.save(User.createWithoutId("수민"));
        User secondWaitingUser = userRepository.save(User.createWithoutId("말랑"));
        reservationRepository.save(Reservation.createWithoutId(
            reservationSlot, confirmedUser, ReservationStatus.CONFIRMED, confirmedClock
        ));
        reservationRepository.save(Reservation.createWithoutId(
            reservationSlot, firstWaitingUser, ReservationStatus.WAITING, firstWaitingClock
        ));
        reservationRepository.save(Reservation.createWithoutId(
            reservationSlot, secondWaitingUser, ReservationStatus.WAITING, secondWaitingClock
        ));
        ReservationService reservationService = createReservationService(cancelClock);

        // when
        reservationService.cancelUserReservation(firstWaitingUser.getId());
        ReservationWithWaitingNumber confirmedReservation = reservationRepository.findReservations("보예").getFirst();
        ReservationWithWaitingNumber cancelReservation = reservationRepository.findReservations("수민").getFirst();
        ReservationWithWaitingNumber waitingReservation = reservationRepository.findReservations("말랑").getFirst();

        // then
        assertSoftly(softly -> {
                assertThat(confirmedReservation.reservation().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
                assertThat(cancelReservation.reservation().getStatus()).isEqualTo(ReservationStatus.CANCELED);
                assertThat(waitingReservation.reservation().getStatus()).isEqualTo(ReservationStatus.WAITING);
                assertThat(waitingReservation.waitingNumber()).isEqualTo(1);
            }
        );
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
        ReservationService reservationService = createReservationService(now);
        UpdateReservationRequest request = new UpdateReservationRequest(
            afterReservationDate.getId(),
            afterReservationTime.getId()
        );

        // when
        reservationService.updateReservation(savedUserReservation.getId(), request);

        // then
        ReservationSlot updatedReservation = reservationRepository.findActiveReservation(savedUserReservation.getId())
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
            firstWaitingClock
        );
        Reservation secondWaitingReservation = saveWaitingReservation(
            savedReservation,
            secondWaitingUser,
            secondWaitingClock
        );
        ReservationService reservationService = createReservationService(updateClock);
        UpdateReservationRequest request = new UpdateReservationRequest(
            reservationDate.getId(),
            reservationTime.getId()
        );

        // when
        reservationService.updateReservation(confirmedReservation.getId(), request);

        // then
        Reservation updatedConfirmedReservation = reservationRepository.findActiveReservation(
                confirmedReservation.getId())
            .orElseThrow();
        Reservation updatedFirstWaitingReservation = reservationRepository.findActiveReservation(
                firstWaitingReservation.getId())
            .orElseThrow();
        Reservation updatedSecondWaitingReservation = reservationRepository.findActiveReservation(
                secondWaitingReservation.getId())
            .orElseThrow();
        assertSoftly(softly -> {
            assertThat(updatedFirstWaitingReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(updatedSecondWaitingReservation.getStatus()).isEqualTo(ReservationStatus.WAITING);
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
        ReservationService reservationService = createReservationService(now);
        UpdateReservationRequest request = new UpdateReservationRequest(null, afterReservationTime.getId());

        // when
        reservationService.updateReservation(savedUserReservation.getId(), request);
        ReservationSlot updatedReservation = reservationRepository.findActiveReservation(savedUserReservation.getId())
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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);
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
        ReservationService reservationService = createReservationService(now);
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

    private ReservationService createReservationService(Clock clock) {
        return new ReservationService(
            reservationRepository,
            new UserService(userRepository),
            new ReservationSlotService(
                reservationSlotRepository,
                themeRepository,
                reservationDateRepository,
                reservationRepository
            ),
            new ThemeService(themeRepository, reservationSlotRepository, clock),
            new ReservationDateService(reservationSlotRepository, reservationDateRepository),
            new ReservationTimeService(reservationTimeRepository, reservationSlotRepository),
            clock
        );
    }

    private Reservation saveConfirmedReservation(ReservationSlot reservationSlot, User user, Clock clock) {
        return reservationRepository.save(Reservation.createWithoutId(
            reservationSlot,
            user,
            ReservationStatus.CONFIRMED,
            clock
        ));
    }

    private Reservation saveWaitingReservation(
        ReservationSlot reservationSlot,
        User user,
        Clock clock
    ) {
        return reservationRepository.save(Reservation.createWithoutId(
            reservationSlot,
            user,
            ReservationStatus.WAITING,
            clock
        ));
    }

    private Clock fixedClockAt(LocalDateTime dateTime) {
        return Clock.fixed(dateTime.atZone(ZONE_ID).toInstant(), ZONE_ID);
    }
}
