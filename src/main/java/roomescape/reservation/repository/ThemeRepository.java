package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Theme;

public interface ThemeRepository extends CrudRepository<Theme, Long> {

    default Theme getById(long id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 테마 번호를 입력하였습니다."));
    }

    @Modifying
    @Query("delete from Theme where id = :id")
    int deleteById(@Param("id") long id);
}
