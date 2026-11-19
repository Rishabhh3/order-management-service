package com.rishabh.ordermanagementservice.instrument;

import com.rishabh.ordermanagementservice.common.NotFoundException;
import com.rishabh.ordermanagementservice.common.RedisConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class InstrumentService {

    private final InstrumentRepository repository;
    private final RedisTemplate<String, Object> redis;
    private final StringRedisTemplate stringRedisTemplate;

    public InstrumentService(InstrumentRepository repository, RedisTemplate<String, Object> redis, StringRedisTemplate stringRedisTemplate){
        this.repository = repository;
        this.redis = redis;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    // controller is calling this in service and service is calling repository  and then handing results back to handler
    // now even though in repository the jpa will handle this but because controller cannot directly talk to
    // repo , so service also needs to have it, even though it will call repo for same
    // so controller calls, serice.abc and in service either write logic or repo.abc and either write logic in repo or inherit
    public List<InstrumentResponse> findAll(){
        return  repository.findAll().stream()
                .map(InstrumentResponse::from)
                .toList();
    }

    public InstrumentResponse getBySymbol(String symbol){
        String normalized = symbol.trim().toUpperCase();
        String key = "instrument:" + normalized;

        Object cached = redis.opsForValue().get(key);
        if(cached instanceof InstrumentResponse response){
            return response;
        }

        InstrumentResponse response = repository.findBySymbol(normalized)
                .map(InstrumentResponse::from)
                .orElseThrow(() -> new NotFoundException("No instrument found with id: " + normalized));

        redis.opsForValue().set(key, response, Duration.ofMinutes(10));

        return response;

    }

    public Map<String, Object> getLivePrice(String symbol){
        String normalized = symbol.trim().toUpperCase();
        String key = "instrument:" + normalized;

        String cached = stringRedisTemplate.opsForValue().get(key);

        if(cached != null){
                return Map.of("symbol", normalized, "price", new BigDecimal(cached), "source", "redis");
        }

        Instrument instrument = repository.findBySymbol(normalized)
                .orElseThrow(() -> new NotFoundException("No instrument found: " + symbol));

        return Map.of("symbol", normalized, "price",instrument.getLast_price(), "source","postgres");
    }
}
