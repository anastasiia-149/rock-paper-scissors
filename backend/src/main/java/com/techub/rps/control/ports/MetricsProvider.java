package com.techub.rps.control.ports;

import com.techub.rps.control.model.Game;
import io.micrometer.core.instrument.Timer;

public interface MetricsProvider {

    void recordGamePlayed(Game game);
    Timer.Sample startTimer();
    void stopTimer(Timer.Sample sample);
    void recordError(String errorType);
}
