package roomescape.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import roomescape.domain.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.domain.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.domain.reservation.dto.response.ReservationByNameResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.dto.response.ReservationStatus;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.repository.JdbcReservationRepository;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.dto.response.ReservationThemeResponseDto;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.mapper.ThemeMapper;
import roomescape.domain.theme.repository.JdbcThemeRepository;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.dto.response.ReservationTimeResponseDto;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.mapper.TimeMapper;
import roomescape.domain.time.repository.JdbcTimeRepository;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralNotFoundException;

class ReservationServiceTest {

    private ReservationService reservationService;
    private ReservationRepository reservationRepository;
    private TimeRepository timeRepository;
    private ThemeRepository themeRepository;
    private final Clock fixedClock = Clock.fixed(
        Instant.parse("2026-05-08T00:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);

        reservationRepository = new JdbcReservationRepository(dataSource);
        timeRepository = new JdbcTimeRepository(dataSource);
        themeRepository = new JdbcThemeRepository(dataSource);
        reservationService = new ReservationService(reservationRepository, timeRepository, themeRepository,
            fixedClock);
    }

    @Nested
    class GetReservationTest {

        @Test
        void 성공() {
            // given
            LocalDate date = LocalDate.of(2026, 4, 30);
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Time time3 = timeRepository.save(Time.create(LocalTime.of(12, 0)));
            Theme theme = themeRepository.save(
                Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));

            reservationRepository.save(Reservation.create("제이콥", date, time1, theme));
            reservationRepository.save(Reservation.create("라이", date.plusDays(1), time2, theme));
            reservationRepository.save(Reservation.create("티모", date.plusDays(2), time3, theme));

            // when
            List<ReservationResponseDto> actual = reservationService.getReservations();

            // then
            assertThat(actual).containsExactly(
                new ReservationResponseDto(1L, "제이콥", date, TimeMapper.toResponseDto(time1),
                    ThemeMapper.toResponseDto(theme), false),
                new ReservationResponseDto(2L, "라이", date.plusDays(1), TimeMapper.toResponseDto(time2),
                    ThemeMapper.toResponseDto(theme), false),
                new ReservationResponseDto(3L, "티모", date.plusDays(2), TimeMapper.toResponseDto(time3),
                    ThemeMapper.toResponseDto(theme), false)
            );
        }

        @Test
        void 취소된_예약은_취소_여부를_함께_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 4, 30);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(
                Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
            Reservation reservation = reservationRepository.save(Reservation.create("제이콥", date, time, theme));
            reservationRepository.update(reservation.cancel());

            // when
            List<ReservationResponseDto> actual = reservationService.getReservations();

            // then
            assertThat(actual).containsExactly(
                new ReservationResponseDto(reservation.getId(), "제이콥", date, TimeMapper.toResponseDto(time),
                    ThemeMapper.toResponseDto(theme), true)
            );
        }

        @Test
        void 예약이_없으면_빈_목록을_반환한다() {
            // when
            List<ReservationResponseDto> actual = reservationService.getReservations();

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class GetReservationsByNameTest {

        @Test
        void 성공() {
            // given
            LocalDate date = LocalDate.now(fixedClock).plusDays(1);
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Time time3 = timeRepository.save(Time.create(LocalTime.of(12, 0)));
            Theme theme = themeRepository.save(
                Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
            Reservation savedReservation1 = reservationRepository.save(
                Reservation.create("브라운", date, time1, theme));
            Reservation savedReservation2 = reservationRepository.save(
                Reservation.create("브라운", date.plusDays(1), time2, theme));
            reservationRepository.save(Reservation.create("제이슨", date.plusDays(2), time3, theme));

            // when
            List<ReservationByNameResponseDto> actual = reservationService.getReservationsByName("브라운");

            // then
            assertThat(actual).containsExactly(
                new ReservationByNameResponseDto(savedReservation1.getId(), "브라운", date,
                    new ReservationTimeResponseDto(time1.getId(), time1.getStartAt(), false),
                    new ReservationThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(),
                        theme.getImageUrl(), false), ReservationStatus.EDITABLE, ""),
                new ReservationByNameResponseDto(savedReservation2.getId(), "브라운", date.plusDays(1),
                    new ReservationTimeResponseDto(time2.getId(), time2.getStartAt(), false),
                    new ReservationThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(),
                        theme.getImageUrl(), false), ReservationStatus.EDITABLE, "")
            );
        }

        @Test
        void 이름으로_조회된_예약이_없으면_빈_목록을_반환한다() {
            // when
            List<ReservationByNameResponseDto> actual = reservationService.getReservationsByName("브라운");

            // then
            assertThat(actual).isEmpty();
        }

        @Test
        void 삭제된_예약_시간과_테마에_연결된_예약은_수정을_권장한다() {
            // given
            LocalDate date = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(
                Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
            Reservation savedReservation = reservationRepository.save(Reservation.create("브라운", date, time, theme));
            timeRepository.deleteTimeById(time.getId());
            themeRepository.deleteThemeById(theme.getId());

            // when
            List<ReservationByNameResponseDto> actual = reservationService.getReservationsByName("브라운");

            // then
            assertThat(actual).containsExactly(
                new ReservationByNameResponseDto(savedReservation.getId(), "브라운", date,
                    new ReservationTimeResponseDto(time.getId(), time.getStartAt(), true),
                    new ReservationThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(),
                        theme.getImageUrl(), true), ReservationStatus.EDIT_RECOMMENDED,
                    "현재 예약의 시간 또는 테마가 더 이상 제공되지 않습니다. 다른 예약 정보로 수정해주세요.")
            );
        }

        @Test
        void 지난_예약은_수정하거나_삭제할_수_없다() {
            // given
            LocalDate date = LocalDate.now(fixedClock).minusDays(1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(
                Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
            Reservation savedReservation = reservationRepository.save(Reservation.create("브라운", date, time, theme));

            // when
            List<ReservationByNameResponseDto> actual = reservationService.getReservationsByName("브라운");

            // then
            assertThat(actual).containsExactly(
                new ReservationByNameResponseDto(savedReservation.getId(), "브라운", date,
                    new ReservationTimeResponseDto(time.getId(), time.getStartAt(), false),
                    new ReservationThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(),
                        theme.getImageUrl(), false), ReservationStatus.LOCKED, "지난 예약은 수정하거나 취소할 수 없습니다.")
            );
        }

        @Test
        void 취소된_예약은_취소_상태를_반환한다() {
            // given
            LocalDate date = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(
                Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
            Reservation savedReservation = reservationRepository.save(Reservation.create("브라운", date, time, theme));
            reservationRepository.update(savedReservation.cancel());
            // when
            List<ReservationByNameResponseDto> actual = reservationService.getReservationsByName("브라운");

            // then
            assertThat(actual).containsExactly(
                new ReservationByNameResponseDto(savedReservation.getId(), "브라운", date,
                    new ReservationTimeResponseDto(time.getId(), time.getStartAt(), false),
                    new ReservationThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(),
                        theme.getImageUrl(), false), ReservationStatus.CANCELED, "취소된 예약입니다.")
            );
        }
    }

    @Nested
    class SaveReservationTest {

        @Nested
        class Success {

            @Test
            void 성공() {
                // given
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예",
                    LocalDate.of(2026, 5, 1),
                    time.getId(),
                    theme.getId()
                );

                // when
                ReservationCreateResponseDto actual = reservationService.saveReservation(request);

                // then
                assertThat(actual).isEqualTo(
                    new ReservationCreateResponseDto(1L, "보예", LocalDate.of(2026, 5, 1), time.getId(), theme.getId()));
                assertThat(reservationRepository.findReservationsByDeletedAtIsNull()).hasSize(1);
            }

            @Test
            void 같은_날짜_시간이어도_테마가_다르면_예약할_수_있다() {
                // given
                LocalDate date = LocalDate.of(2026, 5, 1);
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Theme otherTheme = themeRepository.save(
                    Theme.create("다른 테마", "다른 설명", "https://roomescape.com/images/themes/other-banner.png"));
                reservationRepository.save(Reservation.create("기존 예약자", date, time, theme));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예",
                    date,
                    time.getId(),
                    otherTheme.getId()
                );

                // when
                ReservationCreateResponseDto actual = reservationService.saveReservation(request);

                // then
                assertThat(actual).isEqualTo(new ReservationCreateResponseDto(
                    2L,
                    "보예",
                    date,
                    time.getId(),
                    otherTheme.getId()
                ));
                assertThat(reservationRepository.findReservationsByDeletedAtIsNull()).hasSize(2);
            }

            @Test
            void 같은_날짜_테마여도_시간이_다르면_예약할_수_있다() {
                // given
                LocalDate date = LocalDate.of(2026, 5, 1);
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                Time otherTime = timeRepository.save(Time.create(LocalTime.of(16, 30)));
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                reservationRepository.save(Reservation.create("기존 예약자", date, time, theme));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예",
                    date,
                    otherTime.getId(),
                    theme.getId()
                );

                // when
                ReservationCreateResponseDto actual = reservationService.saveReservation(request);

                // then
                assertThat(actual).isEqualTo(new ReservationCreateResponseDto(
                    2L,
                    "보예",
                    date,
                    otherTime.getId(),
                    theme.getId()
                ));
                assertThat(reservationRepository.findReservationsByDeletedAtIsNull()).hasSize(2);
            }

            @Test
            void 같은_시간_테마여도_날짜가_다르면_예약할_수_있다() {
                // given
                LocalDate date = LocalDate.of(2026, 5, 1);
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                reservationRepository.save(Reservation.create("기존 예약자", date, time, theme));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예",
                    date.plusDays(1),
                    time.getId(),
                    theme.getId()
                );

                // when
                ReservationCreateResponseDto actual = reservationService.saveReservation(request);

                // then
                assertThat(actual).isEqualTo(new ReservationCreateResponseDto(
                    2L,
                    "보예",
                    date.plusDays(1),
                    time.getId(),
                    theme.getId()
                ));
                assertThat(reservationRepository.findReservationsByDeletedAtIsNull()).hasSize(2);
            }
        }

        @Nested
        class Failed {

            @Test
            void 같은_날짜_시간_테마에_이미_예약이_있으면_예외가_발생한다() {
                // given
                LocalDate date = LocalDate.of(2026, 5, 1);
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                reservationRepository.save(Reservation.create("기존 예약자", date, time, theme));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto("보예", date, time.getId(),
                    theme.getId());

                // when & then
                assertThatThrownBy(() -> reservationService.saveReservation(request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("이미 예약된 날짜, 시간, 테마입니다.");
            }

            @Test
            void timeId가_존재하지_않으면_예외가_발생한다() {
                // given
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png")
                );
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예",
                    LocalDate.of(2026, 5, 1),
                    999L,
                    theme.getId()
                );

                // when & then
                assertThatThrownBy(() -> reservationService.saveReservation(request))
                    .isInstanceOf(GeneralNotFoundException.class)
                    .hasMessage("조회할 자원이 존재하지 않습니다.");
            }

            @Test
            void themeId가_존재하지_않으면_예외가_발생한다() {
                // given
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예",
                    LocalDate.of(2026, 5, 1),
                    time.getId(),
                    999L
                );

                // when & then
                assertThatThrownBy(() -> reservationService.saveReservation(request))
                    .isInstanceOf(GeneralNotFoundException.class)
                    .hasMessage("조회할 자원이 존재하지 않습니다.");
            }

            @Test
            void timeId와_themeId가_모두_존재하지_않으면_파라미터_에러를_모두_포함한다() {
                // given
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예",
                    LocalDate.of(2026, 5, 1),
                    999L,
                    999L
                );

                // when & then
                assertThatThrownBy(() -> reservationService.saveReservation(request))
                    .isInstanceOfSatisfying(GeneralNotFoundException.class, exception -> {
                        assertThat(exception.getParameterErrors())
                            .extracting(ParameterErrorResponseDto::parameter)
                            .containsExactly("timeId", "themeId");
                        assertThat(exception.getParameterErrors())
                            .extracting(ParameterErrorResponseDto::message)
                            .containsExactly("존재 하지 않는 시간대입니다.", "존재 하지 않는 테마입니다.");
                    });
            }
        }
    }

    @Nested
    class SaveWaitingReservationTest {

        @Nested
        class Success {

            @Test
            void 성공() {
                // given
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예",
                    LocalDate.of(2026, 5, 1),
                    time.getId(),
                    theme.getId()
                );

                // when
                ReservationCreateResponseDto actual = reservationService.saveWaitingReservation(request);

                // then
                assertThat(actual).isEqualTo(
                    new ReservationCreateResponseDto(1L, "보예", LocalDate.of(2026, 5, 1), time.getId(), theme.getId()));
                assertThat(reservationRepository.findReservationsByDeletedAtIsNull()).hasSize(1);
            }

            @Test
            void ACTIVE_예약이_있어도_WAITING_예약을_저장할_수_있다() {
                // given
                LocalDate date = LocalDate.of(2026, 5, 1);
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                reservationRepository.save(Reservation.create("기존예약자", date, time, theme));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예", date, time.getId(), theme.getId());

                // when
                ReservationCreateResponseDto actual = reservationService.saveWaitingReservation(request);

                // then
                assertThat(actual).isEqualTo(
                    new ReservationCreateResponseDto(2L, "보예", date, time.getId(), theme.getId()));
                assertThat(reservationRepository.findReservationsByDeletedAtIsNull()).hasSize(2);
            }

            @Test
            void 같은_날짜_시간_테마에_다른_이름으로_WAITING_예약을_저장할_수_있다() {
                // given
                LocalDate date = LocalDate.of(2026, 5, 1);
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                reservationRepository.save(Reservation.create("기존예약자", date, time, theme));
                ReservationCreateRequestDto request1 = new ReservationCreateRequestDto(
                    "대기자1", date, time.getId(), theme.getId());
                ReservationCreateRequestDto request2 = new ReservationCreateRequestDto(
                    "대기자2", date, time.getId(), theme.getId());
                reservationService.saveWaitingReservation(request1);

                // when
                ReservationCreateResponseDto actual = reservationService.saveWaitingReservation(request2);

                // then
                assertThat(actual.name()).isEqualTo("대기자2");
                assertThat(reservationRepository.findReservationsByDeletedAtIsNull()).hasSize(3);
            }
        }

        @Nested
        class Failed {

            @Test
            void 같은_날짜_시간_테마_이름으로_이미_대기_중이면_예외가_발생한다() {
                // given
                LocalDate date = LocalDate.of(2026, 5, 1);
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예", date, time.getId(), theme.getId());
                reservationService.saveWaitingReservation(request);

                // when & then
                assertThatThrownBy(() -> reservationService.saveWaitingReservation(request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("이미 대기 중인 이름, 날짜, 시간, 테마입니다.");
            }

            @Test
            void timeId가_존재하지_않으면_예외가_발생한다() {
                // given
                Theme theme = themeRepository.save(
                    Theme.create("피온", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예", LocalDate.of(2026, 5, 1), 999L, theme.getId());

                // when & then
                assertThatThrownBy(() -> reservationService.saveWaitingReservation(request))
                    .isInstanceOf(GeneralNotFoundException.class)
                    .hasMessage("조회할 자원이 존재하지 않습니다.");
            }

            @Test
            void themeId가_존재하지_않으면_예외가_발생한다() {
                // given
                Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예", LocalDate.of(2026, 5, 1), time.getId(), 999L);

                // when & then
                assertThatThrownBy(() -> reservationService.saveWaitingReservation(request))
                    .isInstanceOf(GeneralNotFoundException.class)
                    .hasMessage("조회할 자원이 존재하지 않습니다.");
            }

            @Test
            void timeId와_themeId가_모두_존재하지_않으면_파라미터_에러를_모두_포함한다() {
                // given
                ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                    "보예", LocalDate.of(2026, 5, 1), 999L, 999L);

                // when & then
                assertThatThrownBy(() -> reservationService.saveWaitingReservation(request))
                    .isInstanceOfSatisfying(GeneralNotFoundException.class, exception -> {
                        assertThat(exception.getParameterErrors())
                            .extracting(ParameterErrorResponseDto::parameter)
                            .containsExactly("timeId", "themeId");
                        assertThat(exception.getParameterErrors())
                            .extracting(ParameterErrorResponseDto::message)
                            .containsExactly("존재 하지 않는 시간대입니다.", "존재 하지 않는 테마입니다.");
                    });
            }
        }
    }

    @Nested
    class UpdateReservationTest {

        @Nested
        class Success {

            @Test
            void 성공() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Time updateTime = timeRepository.save(Time.create(LocalTime.of(13, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Theme updateTheme = themeRepository.save(
                    Theme.create("변경 테마", "변경 설명", "https://roomescape.com/images/themes/update.png"));
                Reservation savedReservation = reservationRepository.save(Reservation.create("제이슨", date, time, theme));
                ReservationUpdateRequestDto request = new ReservationUpdateRequestDto("제이슨", date.plusDays(1),
                    updateTime.getId(), updateTheme.getId());

                // when
                ReservationCreateResponseDto actual = reservationService.updateReservation(savedReservation.getId(),
                    request);

                // then
                assertThat(actual).isEqualTo(new ReservationCreateResponseDto(savedReservation.getId(), "제이슨",
                    date.plusDays(1), updateTime.getId(), updateTheme.getId()));
            }

            @Test
            void 선택_필드가_null이면_기존_값을_유지한다() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(Reservation.create("제이슨", date, time, theme));
                ReservationUpdateRequestDto request = new ReservationUpdateRequestDto("제이슨", null, null, null);

                // when
                ReservationCreateResponseDto actual = reservationService.updateReservation(savedReservation.getId(),
                    request);

                // then
                assertThat(actual).isEqualTo(new ReservationCreateResponseDto(savedReservation.getId(), "제이슨",
                    date, time.getId(), theme.getId()));
            }

            @Test
            void 자기_자신_예약은_중복_검사에서_제외한다() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(Reservation.create("제이슨", date, time, theme));
                ReservationUpdateRequestDto request = new ReservationUpdateRequestDto("제이슨", date, time.getId(),
                    theme.getId());

                // when
                ReservationCreateResponseDto actual = reservationService.updateReservation(savedReservation.getId(),
                    request);

                // then
                assertThat(actual).isEqualTo(new ReservationCreateResponseDto(savedReservation.getId(), "제이슨",
                    date, time.getId(), theme.getId()));
            }
        }

        @Nested
        class Failed {

            @Test
            void 예약_ID가_존재하지_않으면_예외가_발생한다() {
                // given
                ReservationUpdateRequestDto request = new ReservationUpdateRequestDto("제이슨",
                    LocalDate.now(fixedClock).plusDays(1), null, null);

                // when & then
                assertThatThrownBy(() -> reservationService.updateReservation(999L, request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("예약을 찾을 수 없습니다.");
            }

            @Test
            void 조회한_예약과_예약자_이름이_다르면_예외가_발생한다() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(Reservation.create("제이슨", date, time, theme));
                ReservationUpdateRequestDto request = new ReservationUpdateRequestDto("시오", date, null, null);

                // when & then
                assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("예약을 변경할 권한이 없습니다.");
            }

            @Test
            void 이미_취소된_예약이면_예외가_발생한다() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(Reservation.create("제이슨", date, time, theme));
                reservationRepository.update(savedReservation.cancel());
                ReservationUpdateRequestDto request = new ReservationUpdateRequestDto("제이슨", date, null, null);

                // when & then
                assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("활성된 예약이 아닙니다.");
            }

            @Test
            void 지난_예약이면_예외가_발생한다() {
                // given
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(
                    Reservation.create("제이슨", LocalDate.now(fixedClock).minusDays(1), time, theme));
                ReservationUpdateRequestDto request = new ReservationUpdateRequestDto("제이슨",
                    LocalDate.now(fixedClock).plusDays(1), null, null);

                // when & then
                assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("지난 예약은 변경할 수 없습니다.");
            }

            @Test
            void 변경할_예약_시간과_테마가_존재하지_않으면_파라미터_에러를_모두_포함한다() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(Reservation.create("제이슨", date, time, theme));
                ReservationUpdateRequestDto request = new ReservationUpdateRequestDto("제이슨", null, 999L, 999L);

                // when & then
                assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
                    .isInstanceOfSatisfying(GeneralNotFoundException.class, exception -> {
                        assertThat(exception.getMessage()).isEqualTo("수정할 자원이 존재하지 않습니다.");
                        assertThat(exception.getParameterErrors())
                            .extracting(ParameterErrorResponseDto::parameter)
                            .containsExactly("timeId", "themeId");
                        assertThat(exception.getParameterErrors())
                            .extracting(ParameterErrorResponseDto::message)
                            .containsExactly("존재 하지 않는 시간대입니다.", "존재 하지 않는 테마입니다.");
                    });
            }

            @Test
            void 기존_예약_시간과_테마가_삭제되었고_변경하지_않으면_예외가_발생한다() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(Reservation.create("제이슨", date, time, theme));
                timeRepository.deleteTimeById(time.getId());
                themeRepository.deleteThemeById(theme.getId());
                ReservationUpdateRequestDto request = new ReservationUpdateRequestDto("제이슨", null, null, null);

                // when & then
                assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
                    .isInstanceOf(GeneralNotFoundException.class)
                    .hasMessage("수정할 자원이 존재하지 않습니다.");
            }

            @Test
            void 같은_날짜_시간_테마에_다른_예약이_있으면_예외가_발생한다() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                reservationRepository.save(Reservation.create("제이슨", date, time, theme));
                Reservation savedReservation = reservationRepository.save(
                    Reservation.create("시오", date.plusDays(1), time, theme));
                ReservationUpdateRequestDto request = new ReservationUpdateRequestDto("시오", date, time.getId(),
                    theme.getId());

                // when & then
                assertThatThrownBy(() -> reservationService.updateReservation(savedReservation.getId(), request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("이미 예약된 날짜, 시간, 테마입니다.");
            }
        }
    }

    @Nested
    class CancelReservationTest {

        @Nested
        class Success {

            @Test
            void 성공() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(Reservation.create("제이슨", date, time, theme));

                // when
                ReservationCancelResponseDto actual = reservationService.cancelReservation(savedReservation.getId(),
                    "제이슨");

                // then
                assertThat(actual.id()).isEqualTo(savedReservation.getId());
                assertThat(actual.name()).isEqualTo("제이슨");
                assertThat(actual.date()).isEqualTo(date);
                assertThat(actual.timeId()).isEqualTo(time.getId());
                assertThat(actual.themeId()).isEqualTo(theme.getId());
            }
        }

        @Nested
        class Failed {

            @Test
            void 예약_ID가_존재하지_않으면_예외가_발생한다() {
                // when & then
                assertThatThrownBy(() -> reservationService.cancelReservation(999L, "제이슨"))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("예약을 찾을 수 없습니다.");
            }

            @Test
            void 조회한_예약과_예약자_이름이_다르면_예외가_발생한다() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(Reservation.create("제이슨", date, time, theme));

                // when & then
                assertThatThrownBy(() -> reservationService.cancelReservation(savedReservation.getId(), "시오"))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("예약을 취소할 권한이 없습니다.");
            }

            @Test
            void 이미_취소된_예약이면_예외가_발생한다() {
                // given
                LocalDate date = LocalDate.now(fixedClock).plusDays(1);
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(Reservation.create("제이슨", date, time, theme));
                reservationRepository.update(savedReservation.cancel());

                // when & then
                assertThatThrownBy(() -> reservationService.cancelReservation(savedReservation.getId(), "제이슨"))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("이미 취소된 예약입니다.");
            }

            @Test
            void 지난_예약이면_예외가_발생한다() {
                // given
                Time time = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(
                    Reservation.create("제이슨", LocalDate.now(fixedClock).minusDays(1), time, theme));

                // when & then
                assertThatThrownBy(() -> reservationService.cancelReservation(savedReservation.getId(), "제이슨"))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("지난 예약은 취소할 수 없습니다.");
            }
        }
    }

    @Nested
    class DeleteReservationByIdTest {

        @Nested
        class Success {

            @Test
            void 성공() {
                // given
                Time time1 = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                Time time2 = timeRepository.save(Time.create(LocalTime.of(13, 0)));
                Theme theme = themeRepository.save(
                    Theme.create("테마 이름", "테마 설명", "https://roomescape.com/images/themes/ring-banner.png"));
                Reservation savedReservation = reservationRepository.save(
                    Reservation.create("제이슨", LocalDate.of(2026, 5, 2), time1, theme));
                reservationRepository.save(Reservation.create("시오", LocalDate.of(2026, 5, 3), time2, theme));

                // when
                reservationService.deleteReservationById(savedReservation.getId());

                // then
                List<ReservationResponseDto> actual = reservationService.getReservations();
                assertThat(actual)
                    .hasSize(1)
                    .extracting(ReservationResponseDto::name)
                    .containsExactly("시오");
            }
        }

        @Nested
        class Failed {

            @Test
            void 예약_ID가_존재하지_않으면_예외가_발생한다() {
                // when & then
                assertThatThrownBy(() -> reservationService.deleteReservationById(999L))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("예약을 찾을 수 없습니다.");
            }
        }
    }
}
