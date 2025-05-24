package roomescape.reservation.application;

import static roomescape.reservation.domain.ReservationStatus.BOOKED;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingRepository;
import roomescape.reservation.ui.dto.request.CreateBookedReservationRequest;
import roomescape.reservation.ui.dto.request.FilteredReservationsRequest;
import roomescape.reservation.ui.dto.response.ReservationResponse;
import roomescape.reservation.ui.dto.response.ReservationStatusResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
@RequiredArgsConstructor
public class AdminReservationService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    @Transactional
    public ReservationResponse create(final CreateBookedReservationRequest request) {
        final ReservationTime time = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberById(request.memberId());

        return ReservationResponse.from(
                createBookedReservation(request.date(), time, theme, member)
        );
    }

    private Reservation createBookedReservation(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        final ReservationSlot reservationSlot = ReservationSlot.of(date, time, theme);

        if (reservationRepository.existsByReservationSlot(reservationSlot)) {
            throw new AlreadyExistException("해당 예약 슬롯에 예약이 있습니다.");
        }

        final Reservation reservation = Reservation.of(reservationSlot, member, BOOKED);

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void deleteAsAdmin(final Long reservationId) {
        final Reservation reservation = getReservationById(reservationId);
        final Optional<Waiting> optionalWaiting = waitingRepository.findFirstByReservationSlotOrderByCreatedAt(
                reservation.getReservationSlot()
        );
        if (optionalWaiting.isPresent()) {
            final Waiting waiting = optionalWaiting.get();
            reservation.updateMember(waiting.getMember());
            waitingRepository.delete(waiting);
            return;
        }

        reservationRepository.deleteById(reservationId);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllByFilter(final FilteredReservationsRequest request) {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        return reservationRepository.findAllByThemeIdAndMemberIdAndDateRange(
                        request.themeId(), request.memberId(), request.dateFrom(), request.dateTo()
                )
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationStatusResponse> findAllReservationStatuses() {
        return Arrays.stream(ReservationStatus.values())
                .map(ReservationStatusResponse::from)
                .toList();
    }

    private ReservationTime getReservationTimeById(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 시간이 존재하지 않습니다."));
    }

    private Theme getThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 테마가 존재하지 않습니다."));
    }

    private Member getMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));
    }

    private Reservation getReservationById(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약을 찾을 수 없습니다."));
    }
}
