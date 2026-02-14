package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.Ring;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class RingDataCollector {

    private final Map<String, Integer> ringTypes = new HashMap<>();

    public void analyzeData(Ring ring) {
        ringTypes.merge(ring.getRingType(), 1, Integer::sum);
    }
}
