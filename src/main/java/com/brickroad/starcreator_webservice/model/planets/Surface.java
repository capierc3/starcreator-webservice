package com.brickroad.starcreator_webservice.model.planets;

import com.brickroad.starcreator_webservice.model.enums.TerrainType;

import java.util.ArrayList;

public class Surface {

    private final ArrayList<SurfaceProperty> surfaceComposite;
    private double liquidAmt;
    private String liquidType;

    public Surface() {
        surfaceComposite = new ArrayList<>();
    }

    public void addComposite(TerrainType type, int percent) {
        surfaceComposite.add(new SurfaceProperty(type, percent));
    }

    public boolean compositeContainsType(TerrainType type) {
        return surfaceComposite.stream().anyMatch(sp -> sp.getType().equals(type));
    }

    public void compositeUpdatePercent(TerrainType type, int percent) {
        surfaceComposite.stream().filter(sp -> sp.getType().equals(type)).forEach(sp -> sp.updatePercent(percent));
    }

    public ArrayList<SurfaceProperty> getSurfaceComposite() {
        return surfaceComposite;
    }

    public double getLiquidAmt() {
        return liquidAmt;
    }

    public void setLiquidAmt(double liquidAmt) {
        this.liquidAmt = liquidAmt;
    }

    public String getLiquidType() {
        return liquidType;
    }

    public void setLiquidType(String liquidType) {
        this.liquidType = liquidType;
    }
}
