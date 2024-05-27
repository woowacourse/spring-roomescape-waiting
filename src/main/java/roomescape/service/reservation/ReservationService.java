package roomescape.service.reservation;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaitingWithRank;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.exception.AuthenticationException;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.request.ReservationSaveRequest;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationCreateValidator reservationCreateValidator;
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationCreateValidator reservationCreateValidator,
                              ReservationRepository reservationRepository) {
        this.reservationCreateValidator = reservationCreateValidator;
        this.reservationRepository = reservationRepository;
    }

    public Reservation createReservation(ReservationSaveRequest request,
                                         Member member) {
        ReservationTime reservationTime =
                reservationCreateValidator.getValidReservationTime(request.timeId());
        reservationCreateValidator.validateDateIsFuture(request.date(), reservationTime);
        Theme theme = reservationCreateValidator.getValidTheme(request.themeId());
        reservationCreateValidator.validateAlreadyBooked(
                request.date(),
                request.timeId(),
                request.themeId(),
                request.reservationStatus()
        );
        reservationCreateValidator.validateOwnReservationExist(
                member,
                theme,
                reservationTime,
                request.date()
        );

        Reservation reservation = request.toEntity(
                reservationTime,
                theme,
                member
        );
        return reservationRepository.save(reservation);
    }

    public List<Reservation> findReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> searchReservations(long memberId,
                                                long themeId,
                                                LocalDate dateFrom,
                                                LocalDate dateTo) {
        return reservationRepository.findByMemberIdAndThemeIdAndDateBetween(
                memberId,
                themeId,
                dateFrom,
                dateTo
        );
    }

    public List<ReservationWaitingWithRank> findMemberReservations(long memberId) {
        return reservationRepository.findReservationWaitingWithRankByMemberId(memberId);
    }

    public List<Reservation> findByReservationStatus(ReservationStatus reservationStatus) {
        return reservationRepository.findByReservationStatus(reservationStatus);
    }

    @Transactional
    public void deleteReservation(long id, Member member) {
        Reservation deleteReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 아이디 입니다."));
        if (member.getRole() == Role.MEMBER) {
            validateIsOwnReservation(member, deleteReservation);
            validateIsWaiting(deleteReservation);
        }
        reservationRepository.deleteById(id);

        if (deleteReservation.isReserved()) {
            reservationRepository.findNextWaiting(deleteReservation.getTheme(),
                            deleteReservation.getReservationTime(), deleteReservation.getDate(), Limit.of(1))
                    .ifPresent(reservation -> reservation.changeReservationStatus(ReservationStatus.RESERVED));
        }
    }

    private void validateIsOwnReservation(Member member, Reservation deleteReservation) {
        if (deleteReservation.isNotOwnedBy(member)) {
            throw new AuthenticationException("본인의 예약만 삭제할 수 있습니다.");
        }
    }

    private void validateIsWaiting(Reservation reservation) {
        if(reservation.isReserved()) {
            throw new AuthenticationException("예약 취소는 관리자에게 문의해주세요.");
        }
    }
}
