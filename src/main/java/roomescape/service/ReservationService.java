package roomescape.service;

import static roomescape.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.domain.reservation.ReservationStatus.STANDBY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.ReservationWithRank;
import roomescape.service.dto.FindReservationWithRankDto;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationTimeRepository reservationTimeRepository,
        ThemeRepository themeRepository, MemberRepository memberRepository) {

        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Reservation reserve(Long memberId, String rawDate, Long timeId, Long themeId) {
        return save(memberId, rawDate, timeId, themeId, RESERVED);
    }

    public Reservation standby(Long memberId, String rawDate, Long timeId, Long themeId) {
        return save(memberId, rawDate, timeId, themeId, STANDBY);
    }

    private Reservation save(Long memberId, String rawDate, Long timeId, Long themeId, ReservationStatus status) {
        Member member = findMember(memberId);
        ReservationTime time = findTime(timeId);
        Theme theme = findTheme(themeId);
        LocalDateTime createdAt = LocalDateTime.now();
        Reservation reservation = new Reservation(member, rawDate, createdAt, time, theme, status);

        validatePastReservation(reservation.getDate(), time);

        // TODO: 코드 구조 개선 필요
        if (status == RESERVED) {
            validateDuplication(reservation.getDate(), timeId, themeId);
        }
        if (status == STANDBY) {
            validateAlreadyBookedByMember(reservation);
        }

        return reservationRepository.save(reservation);
    }

    private Member findMember(Long memberId) {
        if (memberId == null) {
            throw new RoomescapeException("사용자 ID는 null일 수 없습니다.");
        }
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new RoomescapeException("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    private ReservationTime findTime(Long timeId) {
        if (timeId == null) {
            throw new RoomescapeException("시간 ID는 null일 수 없습니다.");
        }
        return reservationTimeRepository.findById(timeId)
            .orElseThrow(() -> new RoomescapeException("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    private Theme findTheme(Long themeId) {
        if (themeId == null) {
            throw new RoomescapeException("테마 ID는 null일 수 없습니다.");
        }
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new RoomescapeException("입력한 테마 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    private void validatePastReservation(LocalDate date, ReservationTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new RoomescapeException("과거 예약을 추가할 수 없습니다.");
        }
        if (date.isEqual(LocalDate.now()) && time.isBeforeNow()) {
            throw new RoomescapeException("과거 예약을 추가할 수 없습니다.");
        }
    }

    private void validateDuplication(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new RoomescapeException("해당 시간에 예약이 이미 존재합니다.");
        }
    }

    private void validateAlreadyBookedByMember(Reservation reservation) {
        Boolean exists = reservationRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(
            reservation.getMember().getId(),
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId()
        );

        if (exists) {
            throw new RoomescapeException("이미 예약하셨습니다. 대기 없이 이용 가능합니다.");
        }
    }

    public void deleteById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException("예약이 존재하지 않아 삭제할 수 없습니다."));
        delete(reservation);
    }

    public void deleteStandby(Long reservationId, Member member) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RoomescapeException("예약이 존재하지 않아 삭제할 수 없습니다."));

        if (reservation.isNotReservedBy(member)) {
            throw new RoomescapeException("자신의 예약만 삭제할 수 있습니다.");
        }

        delete(reservation);
    }

    private void delete(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
        List<Reservation> firstWaiting = reservationRepository.findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAtAsc(
            reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        firstWaiting.forEach(Reservation::reserve);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAllByStatus(RESERVED);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom.isAfter(dateTo)) {
            throw new RoomescapeException("날짜 조회 범위가 올바르지 않습니다.");
        }
        return reservationRepository.findAllByThemeIdAndMemberIdAndDateIsBetween(themeId, memberId, dateFrom, dateTo);
    }

    @Transactional(readOnly = true)
    public List<FindReservationWithRankDto> findAllWithRankByMemberId(Long memberId) {
        List<ReservationWithRank> reservations =
            reservationRepository.findReservationsWithRankByMemberId(memberId);

        return reservations.stream()
            .map(data -> new FindReservationWithRankDto(data.reservation(), data.rank()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllStandby() {
        return reservationRepository.findAllByStatus(STANDBY);
    }
}
