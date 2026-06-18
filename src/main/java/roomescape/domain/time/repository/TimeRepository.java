package roomescape.domain.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.error.type.TimeErrorType;
import roomescape.global.error.exception.GeneralException;

@Repository
public interface TimeRepository extends JpaRepository<Time, Long> {

    Time save(Time time);

    List<Time> findAllByDeletedAtIsNull();

    Optional<Time> findTimeByIdAndDeletedAtIsNull(Long id);

    boolean existsTimeByIdAndDeletedAtIsNull(Long id);

    boolean existsTimeByStartAtAndDeletedAtIsNull(LocalTime startAt);

    default void deleteTimeById(Long id) {
        int updatedRowCount = softDeleteById(id);
        if (updatedRowCount == 0) {
            throw new GeneralException(TimeErrorType.TIME_NOT_FOUND);
        }
    }

    @Modifying
    @Query("""
        UPDATE Time t
        SET t.deletedAt = CURRENT_TIMESTAMP
        WHERE t.id = :id
          AND t.deletedAt IS NULL
        """)
    int softDeleteById(@Param("id") Long id);
}
