package com.rishabh.ordermanagementservice.instrument;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Component
public class PriceSimulator {

    private final InstrumentRepository repository;
    private final StringRedisTemplate redis;
    private final Random random = new Random();

    public PriceSimulator(InstrumentRepository repository, StringRedisTemplate redis) {
        this.repository = repository;
        this.redis = redis;
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        List<Instrument> instruments = repository.findAll();

        for (Instrument instrument : instruments) {
            String key = "price:" + instrument.getSymbol();

            String current = redis.opsForValue().get(key);
            BigDecimal base = (current != null)
                    ? new BigDecimal(current)
                    : instrument.getLast_price();

            double movePercent = (random.nextDouble() - 0.5) * 0.004;
            BigDecimal moved = base.add(base.multiply(BigDecimal.valueOf(movePercent)))
                    .setScale(2, RoundingMode.HALF_UP);

            redis.opsForValue().set(key, moved.toPlainString());
        }
    }
}