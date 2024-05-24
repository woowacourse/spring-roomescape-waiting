package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.exceptions.AuthException;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.ValidationException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRequest;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationOrWaitingResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            ReservationRepository ReservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            WaitingRepository waitingRepository
    ) {
        this.reservationRepository = ReservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse addReservation(
            ReservationRequest reservationRequest,
            MemberRequest memberRequest
    ) {
        ReservationTime reservationTime = reservationTimeRepository.getById(reservationRequest.timeId());
        Theme theme = themeRepository.getById(reservationRequest.themeId());

        Reservation reservation = new Reservation(
                reservationRequest.date(),
                reservationTime,
                theme,
                memberRequest.toMember()
        );
        validateIsBeforeNow(reservation);
        validateIsDuplicated(reservation);

        return new ReservationResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse addReservation(AdminReservationRequest adminReservationRequest) {
        Member member = memberRepository.getById(adminReservationRequest.memberId());
        ReservationTime reservationTime = reservationTimeRepository.getById(adminReservationRequest.timeId());
        Theme theme = themeRepository.getById(adminReservationRequest.themeId());

        Reservation reservation = new Reservation(
                adminReservationRequest.date(),
                reservationTime,
                theme,
                member
        );
        validateIsBeforeNow(reservation);
        validateIsDuplicated(reservation);

        return new ReservationResponse(reservationRepository.save(reservation));
    }

    private void validateIsBeforeNow(Reservation reservation) {
        if (reservation.isBeforeNow()) {
            throw new ValidationException("과거 시간은 예약할 수 없습니다.");
        }
    }

    private void validateIsDuplicated(Reservation reservation) {
        if (reservationRepository.existsByDateAndReservationTimeAndTheme(reservation.getDate(),
                reservation.getReservationTime(), reservation.getTheme())) {
            throw new DuplicationException("이미 예약이 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> searchReservations(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        Theme theme = themeRepository.getById(themeId);
        Member member = memberRepository.getById(memberId);
        return reservationRepository.findByThemeAndMember(theme, member)
                .stream()
                .filter(reservation -> reservation.isBetweenInclusive(dateFrom, dateTo))
                .map(ReservationResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationOrWaitingResponse> findReservationsByMember(MemberRequest memberRequest) {
        return reservationRepository.findByMember(memberRequest.toMember())
                .stream()
                .map(ReservationOrWaitingResponse::new)
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id, MemberRequest memberRequest) {
        reservationRepository.findById(id)
                .ifPresent(reservation -> {
                            validateDeleteAuth(memberRequest.toMember(), reservation);
                            reservationRepository.deleteById(id);
                            reorderWaitings(reservation);
                        }
                );
    }

    private void validateDeleteAuth(Member member, Reservation reservation) {
        if (reservation.isNotDeletableMemeber(member)) {
            throw new AuthException("예약을 삭제할 권한이 없습니다.");
        }
    }

    private void reorderWaitings(Reservation reservation) {
        Optional<Waiting> firstWaiting = waitingRepository.findFirstByDateAndReservationTimeAndThemeOrderByIdAsc(
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme()
        );

        if (firstWaiting.isPresent()) {
            Waiting waiting = firstWaiting.get();
            waitingRepository.deleteById(waiting.getId());
            reservationRepository.save(new Reservation(waiting));
        }
    }
}
