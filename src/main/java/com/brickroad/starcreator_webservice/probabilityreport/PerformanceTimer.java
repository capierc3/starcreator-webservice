package com.brickroad.starcreator_webservice.probabilityreport;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.StopWatch;

@Setter
@Getter
public class PerformanceTimer {

    private StopWatch stopWatch;
    private int lapCounter;
    private long lap;
    private long lapDuration;
    private long totalTime;
    private String finalTime;

    public PerformanceTimer() {
        stopWatch = StopWatch.create();
        totalTime = 0;
        lapCounter = 0;
    }

    public void start() {
        stopWatch.start();
    }

    public void stop() {
        finalTime = stopWatch.formatTime();
        stopWatch.stop();
    }

    public void lap() {
        lapDuration = stopWatch.getTime() - lap;
        totalTime += lapDuration;
        lapCounter++;
        lap = stopWatch.getTime();
    }

    public double averageLap() {
        if (lapCounter == 0) return 0;
        return (double) totalTime / lapCounter;
    }
}
