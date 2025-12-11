package com.techub.rps.boundary.outgoing;

import com.techub.rps.control.exception.DomainException;
import com.techub.rps.control.model.Hand;
import com.techub.rps.control.ports.RandomHandProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;

@Component
@Slf4j
public class SecureRandomHandAdapter implements RandomHandProvider {

    private final SecureRandom secureRandom;
    private final List<Hand> availableHands;

    public SecureRandomHandAdapter() {
        this.secureRandom = new SecureRandom();
        this.availableHands = Hand.getAllHands();

        if (availableHands.isEmpty()) {
            throw DomainException.randomGenerationError("No hands available for random selection");
        }

        log.info("SecureRandomHandAdapter initialized with {} hands", availableHands.size());
    }

    @Override
    public Hand getRandomHand() {
        try {
            int randomIndex = secureRandom.nextInt(availableHands.size());
            Hand selectedHand = availableHands.get(randomIndex);
            log.trace("Generated random hand: {}", selectedHand);
            return selectedHand;
        } catch (Exception ex) {
            log.error("Failed to generate random hand", ex);
            throw DomainException.randomGenerationError("Failed to generate random hand", ex);
        }
    }
}
