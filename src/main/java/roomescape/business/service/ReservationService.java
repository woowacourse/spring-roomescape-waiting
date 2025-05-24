package roomescape.business.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.domain.Member;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.business.domain.Waiting;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidDateAndTimeException;
import roomescape.exception.NotFoundException;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.ReservationMineResponse;
import roomescape.presentation.dto.ReservationResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final QueryService queryService;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(final QueryService queryService,
                              final ReservationRepository reservationRepository,
                              final WaitingRepository waitingRepository
    ) {
        this.queryService = queryService;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResponse insert(final LocalDate date, final Long memberId, final Long timeId, final Long themeId) {
        validateIsDuplicate(date, timeId, themeId);
        final ReservationTime reservationTime = queryService.getReservationTimeById(timeId);
        validateDateAndTimeIsFuture(date, reservationTime.getStartAt());

        final Theme theme = queryService.getThemeById(themeId);
        final Member member = queryService.getMemberById(memberId);

        final Reservation reservation = new Reservation(date, member, reservationTime, theme);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateIsDuplicate(final LocalDate date, final Long playTimeId, final Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, playTimeId, themeId)) {
            throw new DuplicateException("추가 하려는 예약과 같은 날짜, 시간, 테마의 예약이 이미 존재합니다.");
        }
    }

    private void validateDateAndTimeIsFuture(final LocalDate date, final LocalTime time) {
        final LocalDateTime now = LocalDateTime.now();

        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time);
        if (reservationDateTime.isBefore(now)) {
            throw new InvalidDateAndTimeException("방탈출 예약 날짜와 시간이 현재보다 과거일 수 없습니다.");
        }
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllFilter(final Long memberId, final Long themeId, final LocalDate startDate,
                                                   final LocalDate endDate) {
        return reservationRepository.findAllByFilter(memberId, themeId, startDate, endDate)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(final Long id) {
        final Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 예약을 찾을 수 없습니다. 예약 id: %d".formatted(id)));

        if(reservation.getDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("이전 날짜의 예약은 삭제할 수 없습니다.");
        }
        if (Objects.equals(reservation.getDate(), LocalDate.now())) {
            if(reservation.getTime().getStartAt().isBefore(LocalTime.now())) {
                throw new BadRequestException("지난 시간의 예약을 삭제할 수 없습니다.");
            }
        }

        reservationRepository.deleteById(id);

        final Optional<Waiting> firstWaiting = waitingRepository.findFirstByDateAndThemeIdAndTimeIdOrderByCreatedAtAsc(
                reservation.getDate(), reservation.getTheme().getId(), reservation.getTime().getId());

        firstWaiting.ifPresent(waiting -> {
            waitingRepository.deleteById(waiting.getId());
            Reservation newReservation = new Reservation(waiting.getDate(), waiting.getMember(), waiting.getTime(),
                    waiting.getTheme());
            reservationRepository.save(newReservation);
        });
    }

    public List<ReservationMineResponse> findByMemberId(final Long memberId) {
        final List<ReservationMineResponse> reservations = reservationRepository.findByMemberId(memberId)
                .stream()
                .map(ReservationMineResponse::from)
                .toList();

        final List<ReservationMineResponse> waitings = waitingRepository.findWithRankByMemberId(memberId)
                .stream()
                .map(ReservationMineResponse::from)
                .toList();

        return Stream.concat(reservations.stream(), waitings.stream())
                .toList();
    }
}
