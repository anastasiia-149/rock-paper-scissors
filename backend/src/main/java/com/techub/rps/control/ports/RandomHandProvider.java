package com.techub.rps.control.ports;

import com.techub.rps.control.model.Hand;

public interface RandomHandProvider {
    Hand getRandomHand();
}
