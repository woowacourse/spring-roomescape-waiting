package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.auth.LoginMember;
import roomescape.dto.reservation.MyReservationWithRankResponse;
import roomescape.dto.reservation.ReservationDto;
import roomescape.dto.reservation.ReservationFilterParam;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Transactional
@Service
public class ReservationService {

    private static final int MAX_RESERVATIONS_PER_TIME = 1;
    private static final int INCREMENT_VALUE_FOR_RANK = 1;

    private final Clock clock;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
            final Clock clock,
            final ReservationRepository reservationRepository,
            final MemberRepository memberRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository
    ) {
        this.clock = clock;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;

    }

    public ReservationResponse createReservation(final ReservationDto reservationDto) {
        final Member member = memberRepository.findById(reservationDto.memberId())
                .orElseThrow(() -> new IllegalArgumentException(reservationDto.memberId() + "에 해당하는 사용자가 없습니다."));
        final ReservationTime time = reservationTimeRepository.findById(reservationDto.timeId())
                .orElseThrow(() -> new IllegalArgumentException(reservationDto.timeId() + "에 해당하는 예약 시간이 없습니다."));
        final Theme theme = themeRepository.findById(reservationDto.themeId())
                .orElseThrow(() -> new IllegalArgumentException(reservationDto.themeId() + "에 해당하는 테마가 없습니다."));

        final Reservation reservation = reservationDto.toModel(member, time, theme, ReservationStatus.RESERVED);
        validateDate(reservation.getDate());
        validateDuplicatedReservation(reservation);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    private void validateDate(final LocalDate date) {
        final LocalDate now = LocalDate.now(clock);
        if (date.isBefore(now) || date.equals(now)) {
            throw new IllegalArgumentException("이전 날짜 혹은 당일은 예약할 수 없습니다.");
        }
    }

    private void validateDuplicatedReservation(final Reservation reservation) {
        final int count = reservationRepository.countByDateAndTimeIdAndThemeId(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId()
        );

        if (count >= MAX_RESERVATIONS_PER_TIME) {
            throw new IllegalArgumentException("해당 시간대에 예약이 모두 찼습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        final List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllBy(final ReservationFilterParam filterParam) {
        final List<Reservation> reservations = reservationRepository.findByThemeIdAndMemberIdAndDateBetweenAndStatus(
                filterParam.themeId(), filterParam.memberId(),
                filterParam.dateFrom(), filterParam.dateTo(), ReservationStatus.RESERVED
        );
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void delete(final Long id) {
        final boolean isExist = reservationRepository.existsById(id);
        if (!isExist) {
            throw new IllegalArgumentException("해당 ID의 예약이 없습니다.");
        }
        reservationRepository.deleteById(id);
    }

    public List<MyReservationWithRankResponse> findMyReservationsAndWaitings(final LoginMember loginMember) {
        final List<Reservation> reservationsByMemberId = reservationRepository.findByMemberId(loginMember.id());
        final List<Reservation> reservations = reservationRepository.findAll();
        return reservationsByMemberId.stream()
                .map(reservation -> new MyReservationWithRankResponse(reservation, calculateRank(reservations, reservation)))
                .toList();
    }

    private Long calculateRank(final List<Reservation> reservations, final Reservation reservation) {
        return reservations.stream()
                .filter(r -> Objects.equals(r.getDate(), reservation.getDate()) &&
                        Objects.equals(r.getTheme().getId(), reservation.getTheme().getId()) &&
                        Objects.equals(r.getTime().getId(), reservation.getTime().getId()) &&
                        r.getStatus() == reservation.getStatus() &&
                        r.getId() < reservation.getId())
                .count() + INCREMENT_VALUE_FOR_RANK;
    }
}
