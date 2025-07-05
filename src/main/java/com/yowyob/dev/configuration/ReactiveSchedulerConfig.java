package com.yowyob.dev.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class ReactiveSchedulerConfig {

    @Bean
    public Scheduler auctionJobScheduler() {
        // 4 threads pour le job scheduler, à ajuster selon la charge
        return Schedulers.newBoundedElastic(4, Integer.MAX_VALUE, "auction-job-scheduler");
    }
}

