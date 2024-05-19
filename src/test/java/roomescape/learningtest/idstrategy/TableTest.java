package roomescape.learningtest.idstrategy;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest //DataJpaTest 를 사용하면 테스트 전체에 @Transactional 이 사용되어 버린다.
public class TableTest {

    @Autowired
    private SomeEntityRepository someEntityRepository;

    @Test
    @Transactional
    void test() {
        SomeEntity saved = save();
        find(saved.getId());
    }

    //@Transactional
    SomeEntity save() {
        return someEntityRepository.save(new SomeEntity("value"));
    }

    //@Transactional
    SomeEntity find(long id) {
        return someEntityRepository.findById(id).orElseThrow();
    }
}
