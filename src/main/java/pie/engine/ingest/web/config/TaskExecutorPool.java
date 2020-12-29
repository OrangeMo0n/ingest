package pie.engine.ingest.web.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class TaskExecutorPool {
    @Value("${pool.corePoolSize}")
    private Integer corePoolSize;

    @Value("${pool.maxPoolSize}")
    private Integer maxPoolSize;

    @Value("${pool.keepAliveSeconds}")
    private Integer keepAliveSeconds;

    @Value("${pool.queueCapacity}")
    private Integer queueCapacity;

    @Bean
    public Executor batchQueryAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("Ingest batch query-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        return executor;
    }
}
