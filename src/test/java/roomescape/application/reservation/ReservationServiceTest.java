package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.application.member.MemberResult;
import roomescape.application.reservation.dto.CreateReservationParam;
import roomescape.application.reservation.dto.ReservationResult;
import roomescape.application.reservation.dto.ReservationSearchParam;
import roomescape.application.reservation.dto.ReservationTimeResult;
import roomescape.application.reservation.dto.ThemeResult;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationServiceTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Clock clock = Clock.fixed(Instant.parse("2025-05-08T13:00:00Z"), ZoneId.of("Asia/Seoul"));

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;");
        reservationService = new ReservationService(
                reservationTimeRepository,
                reservationRepository,
                themeRepository,
                memberRepository,
                clock
        );
    }

    @DisplayName("예약을 생성할 수 있다")
    @Test
    void 예약을_생성할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));

        CreateReservationParam param = new CreateReservationParam(LocalDate.now(clock).plusDays(1), time.getId(),
                theme.getId(), member.getId());

        // when
        Long id = reservationService.create(param);

        // then
        assertThat(reservationRepository.findById(id)).isPresent();
    }

    @DisplayName("존재하지 않는 회원으로 예약 생성 시 예외가 발생한다")
    @Test
    void 존재하지_않는_회원으로_예약할_수_없다() {
        // given
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        CreateReservationParam param = new CreateReservationParam(LocalDate.now(clock), time.getId(), theme.getId(),
                999L);

        // when
        // then
        assertThatThrownBy(() -> reservationService.create(param))
                .isInstanceOf(NotFoundEntityException.class)
                .hasMessage("999에 해당하는 member 튜플이 없습니다.");
    }

    @Test
    void 존재하지_않는_예약시간으로_예약할_수_없다() {
        // given
        Long invalidTimeId = 999L;
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        CreateReservationParam param = new CreateReservationParam(LocalDate.now(clock), invalidTimeId, theme.getId(),
                member.getId());

        // when
        // then
        assertThatThrownBy(() -> reservationService.create(param))
                .isInstanceOf(NotFoundEntityException.class)
                .hasMessage("999에 해당하는 reservation_time 튜플이 없습니다.");
    }

    @Test
    void 존재하지_않는_테마로_예약할_수_없다() {
        // given
        Long invalidThemeId = 999L;
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        CreateReservationParam param = new CreateReservationParam(LocalDate.now(clock), time.getId(), invalidThemeId,
                member.getId());

        // when
        // then
        assertThatThrownBy(() -> reservationService.create(param))
                .isInstanceOf(NotFoundEntityException.class)
                .hasMessage("999에 해당하는 theme 튜플이 없습니다.");
    }

    @DisplayName("중복된 날짜와 시간으로 예약 시 예외가 발생한다")
    @Test
    void 중복된_예약은_불가능하다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));

        reservationRepository.save(new Reservation(member, LocalDate.now(clock), time, theme));

        CreateReservationParam param = new CreateReservationParam(LocalDate.now(clock), time.getId(), theme.getId(),
                member.getId());

        // when
        // then
        assertThatThrownBy(() -> reservationService.create(param))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("날짜와 시간이 중복된 예약이 존재합니다.");
    }

    @DisplayName("예약을 삭제할 수 있다")
    @Test
    void 예약을_삭제할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.now(clock), time, theme));

        // when
        reservationService.deleteById(reservation.getId());

        // then
        assertThat(reservationRepository.findById(reservation.getId())).isNotPresent();
    }

    @Test
    void 전체_예약을_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock), time1, theme));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock), time2, theme));

        // when
        List<ReservationResult> reservationResults = reservationService.findAll();

        // then
        assertThat(reservationResults)
                .isEqualTo(List.of(
                                new ReservationResult(
                                        1L,
                                        new MemberResult(1L, "벨로"),
                                        LocalDate.now(clock),
                                        new ReservationTimeResult(1L, LocalTime.of(13, 0)),
                                        new ThemeResult(1L, "테마", "설명", "이미지")
                                ),
                                new ReservationResult(
                                        2L,
                                        new MemberResult(1L, "벨로"),
                                        LocalDate.now(clock),
                                        new ReservationTimeResult(2L, LocalTime.of(14, 0)),
                                        new ThemeResult(1L, "테마", "설명", "이미지")
                                )
                        )
                );
    }

    @DisplayName("예약 id로 조회할 수 있다")
    @Test
    void 예약을_id로_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.now(clock), time, theme));

        // when
        ReservationResult result = reservationService.findById(reservation.getId());

        // then
        assertThat(result.memberResult().name()).isEqualTo("벨로");
    }

    @Test
    void 검색_조건에_맞는_예약을_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock), time1, theme));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock).plusDays(1), time2, theme));
        ReservationSearchParam reservationSearchParam = new ReservationSearchParam(
                theme.getId(),
                member.getId(),
                LocalDate.now(clock),
                LocalDate.now(clock)
        );

        // when
        List<ReservationResult> reservationResults = reservationService.findReservationsBy(reservationSearchParam);

        // then
        assertThat(reservationResults)
                .isEqualTo(List.of(
                                new ReservationResult(
                                        1L,
                                        new MemberResult(1L, "벨로"),
                                        LocalDate.now(clock),
                                        new ReservationTimeResult(1L, LocalTime.of(13, 0)),
                                        new ThemeResult(1L, "테마", "설명", "이미지")
                                )
                        )
                );
    }
}
