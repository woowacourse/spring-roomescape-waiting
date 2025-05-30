package roomescape.service;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationThemeRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.service.dto.MyPageReservationResponse;
import roomescape.service.dto.ReservationRecipe;
import roomescape.service.dto.ReservationResponse;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final MemberRepository memberRepository;
    private final EntityManager entityManager;

    @Transactional
    public ReservationResponse addReservation(final ReservationRecipe recipe) {
        long timeId = recipe.timeId();
        final long themeId = recipe.themeId();
        final LocalDate date = recipe.date();
        validateDuplicateReservation(date, timeId, themeId);
        final Member member = memberRepository.findById(recipe.memberId())
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 사용자 입니다."));
        final ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약 시간 입니다."));
        final ReservationTheme theme = reservationThemeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 테마 입니다."));
        final Reservation reservation = new Reservation(member, date, time, theme);
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.fromV2(saved);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::fromV2)
                .toList();
    }

    @Transactional
    public List<ReservationResponse> getFilteredReservations(final Long memberId, final Long themeId,
                                                             final LocalDate dateFrom, final LocalDate dateTo) {
        final List<Reservation> reservations = reservationRepository.findByMemberIdAndThemeIdAndDateFromAndDateTo(
                memberId, themeId, dateFrom, dateTo);
        return reservations.stream()
                .map(ReservationResponse::fromV2)
                .toList();
    }

    @Transactional
    public List<MyPageReservationResponse> getReservationsByMemberId(final Long memberId) {
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 사용자 입니다."));
        final List<Reservation> myReservations = reservationRepository.findByMemberId(member.getId());
        final List<ReservationWaiting> myReservationWaitings = reservationWaitingRepository.findByMemberId(
                member.getId());
        return myPageReservationResponses(myReservations, myReservationWaitings);
    }

    private List<MyPageReservationResponse> myPageReservationResponses(final List<Reservation> myReservations,
                                                                       final List<ReservationWaiting> myReservationWaitings) {
        final List<MyPageReservationResponse> myPageReservationResponses = myReservations.stream()
                .map(MyPageReservationResponse::from)
                .collect(Collectors.toList());
        List<MyPageReservationResponse> myPageReservationWaitingResponses = myReservationWaitings.stream()
                .map(myReservationWaiting -> MyPageReservationResponse.of(myReservationWaiting,
                        getWaitingOrderByMember(myReservationWaiting.getMember())))
                .toList();
        myPageReservationResponses.addAll(myPageReservationWaitingResponses);
        return myPageReservationResponses;
    }

    @Transactional
    public void removeReservation(final long id) {
        validateExistsById(id);
        reservationRepository.findById(id).ifPresent(
                reservation -> {
                    reservationRepository.deleteById(id);
                    entityManager.flush();
                    convertWaitingToReservation(reservation);
                }
        );
    }

    private void convertWaitingToReservation(final Reservation reservation) {
        reservationWaitingRepository.findFirstByThemeIdAndTimeIdAndDateOrderByCreatedAtAsc(
                reservation.getTheme().getId(), reservation.getTime().getId(), reservation.getDate())
                .ifPresent(reservationWaiting -> {
                    reservationRepository.save(new Reservation(reservationWaiting.getMember(), reservationWaiting.getDate(), reservationWaiting.getTime(), reservationWaiting.getTheme()));
                    reservationWaitingRepository.deleteById(reservationWaiting.getId());
                });
    }

    private void validateDuplicateReservation(final LocalDate localDate, final long timeId, final long themeId) {
        if (reservationRepository.existByDateAndTimeIdAndThemeId(localDate, timeId, themeId)) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 예약 입니다.");
        }
    }

    private void validateExistsById(final long id) {
        if (!reservationRepository.existById(id)) {
            throw new NoSuchElementException("[ERROR] 존재하지 않는 예약 입니다.");
        }
    }

    private long getWaitingOrderByMember(final Member member) {
        return reservationWaitingRepository.findWaitingOrderById(member.getId());
    }
}
