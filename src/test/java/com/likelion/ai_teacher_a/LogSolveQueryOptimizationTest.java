package com.likelion.ai_teacher_a;
/*
import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.logsolve.entity.LogSolve;
import com.likelion.ai_teacher_a.domain.logsolve.repository.LogSolveRepository;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import com.likelion.ai_teacher_a.domain.userJr.repository.UserJrRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://dpg-d1jikqemcj7s739t2grg-a.oregon-postgres.render.com/ai_mathteacher_a",
        "spring.datasource.username=ai_mathteacher_a_user",
        "spring.datasource.password=O6cbLCT40qqMA4WXgp4s3hqSaOhnuOUW",
        "spring.jpa.hibernate.ddl-auto=update",  // 여기에 추가!
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL8Dialect",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "cloud.aws.credentials.access-key=FAKE_ACCESS_KEY",
        "cloud.aws.credentials.secret-key=FAKE_SECRET_KEY",
        "cloud.aws.region.static=ap-northeast-2",
        "cloud.aws.s3.bucket=test-bucket"
})
@Transactional
public class LogSolveQueryOptimizationTest {

    @Autowired
    private LogSolveRepository logSolveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserJrRepository userJrRepository;

    @PersistenceContext
    private EntityManager em;

    private Statistics statistics;

    @BeforeEach
    void init() {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        session.getFactory().getStatistics().setStatisticsEnabled(true);
        statistics = session.getFactory().getStatistics();
        statistics.clear();
    }

    @Test
    public void findAllByUserJr_should_not_have_n_plus_one_problem() {
        // given
        User user = userRepository.findAll().stream().findFirst().orElseThrow();
        UserJr userJr = userJrRepository.findAll().stream()
                .filter(jr -> jr.getUser().getId().equals(user.getId()))
                .findFirst().orElseThrow();

        // when
        Page<LogSolve> page = logSolveRepository.findAllByUserJr(PageRequest.of(0, 10), userJr);
        for (LogSolve log : page.getContent()) {
            Image image = log.getImage(); // Lazy일 경우 여기서 쿼리 다발생함
            if (image != null) {
                System.out.println("Image URL: " + image.getUrl());
            }
        }

        // then
        long queryCount = statistics.getPrepareStatementCount();
        long entityCount = statistics.getEntityLoadCount();
        long collectionCount = statistics.getCollectionLoadCount();

        System.out.println("\n📌 Hibernate 쿼리 총 개수: " + queryCount);
        System.out.println("📦 엔티티 로딩 수: " + entityCount);
        System.out.println("📚 컬렉션 로딩 수: " + collectionCount);

        assertThat(queryCount).isLessThanOrEqualTo(5L);
    }
}
*/
