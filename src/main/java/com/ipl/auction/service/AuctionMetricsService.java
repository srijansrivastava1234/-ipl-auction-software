package com.ipl.auction.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class AuctionMetricsService {

    private final Counter bidsPlacedCounter;
    private final Counter playersSoldCounter;
    private final Counter playersUnsoldCounter;

    public AuctionMetricsService(MeterRegistry meterRegistry) {
        this.bidsPlacedCounter = Counter.builder("auction.bids.total")
                .description("Total number of bids placed in the auction system")
                .register(meterRegistry);

        this.playersSoldCounter = Counter.builder("auction.players.sold")
                .description("Total number of players successfully sold under the hammer")
                .register(meterRegistry);

        this.playersUnsoldCounter = Counter.builder("auction.players.unsold")
                .description("Total number of players marked unsold")
                .register(meterRegistry);
    }

    public void recordBidPlaced() {
        bidsPlacedCounter.increment();
    }

    public void recordPlayerSold() {
        playersSoldCounter.increment();
    }

    public void recordPlayerUnsold() {
        playersUnsoldCounter.increment();
    }

    public double getTotalBids() {
        return bidsPlacedCounter.count();
    }

    public double getTotalSold() {
        return playersSoldCounter.count();
    }

    public double getTotalUnsold() {
        return playersUnsoldCounter.count();
    }
}
