package src.service;

import src.exception.InvalidChargingRateException;

public class FastChargingStation extends ChargingStation {
    private final float fastChargingRate;

    public FastChargingStation(String stationId, int noOfConnectors, float powerCapacity, float fastChargingRate)
            throws InvalidChargingRateException {
        super(stationId, noOfConnectors, powerCapacity);

        if (!Float.isFinite(fastChargingRate) || fastChargingRate <= 0 || fastChargingRate > powerCapacity) {
            throw new InvalidChargingRateException("Invalid fast charging rate: " + fastChargingRate + " (max " + powerCapacity + " kW)");
        }

        this.fastChargingRate = fastChargingRate;
        setRatePerKWh(18.0);
    }

    public float getFastChargingRate() {
        return fastChargingRate;
    }

    @Override
    public String toString() {
        return super.toString() + " [FastChargingRate=" + fastChargingRate + "kW]";
    }
}
