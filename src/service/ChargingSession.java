package src.service;

import java.io.IOException;
import java.time.LocalDateTime;

public class ChargingSession implements Runnable {
    private final ChargingStation station;
    private final int connectorId;
    private int batteryPercentage;
    private final int targetPercentage;
    private final double energyConsumedKWh;
    private final int chargeStep;
    private LocalDateTime inTime;
    private LocalDateTime outTime;
    private final String userName;

    public ChargingSession(ChargingStation station, int connectorId, int initialBatteryPercentage, int targetPercentage, double energyConsumedKWh) {
        this(station, connectorId, initialBatteryPercentage, targetPercentage, energyConsumedKWh, null);
    }

    public ChargingSession(ChargingStation station, int connectorId, int initialBatteryPercentage, int targetPercentage, double energyConsumedKWh, String userName) {
        this.station = station;
        this.connectorId = connectorId;
        this.batteryPercentage = Math.max(0, Math.min(100, initialBatteryPercentage));
        this.targetPercentage = Math.max(this.batteryPercentage, Math.min(100, targetPercentage));
        this.energyConsumedKWh = energyConsumedKWh;
        this.userName = userName;
        if (station instanceof FastChargingStation) {
            this.chargeStep = 20;
        } else {
            this.chargeStep = 10;
        }
    }

    private String getProgressBar(int percent) {
        int bars = 20;
        int filled = (percent * bars) / 100;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bars; i++) {
            if (i < filled) {
                sb.append("#");
            } else {
                sb.append("-");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void run() {
        inTime = LocalDateTime.now();
        System.out.printf("  Battery: %3d%% %s%n", batteryPercentage, getProgressBar(batteryPercentage));
        while (batteryPercentage < targetPercentage) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            batteryPercentage = Math.min(targetPercentage, batteryPercentage + chargeStep);
            if (batteryPercentage >= targetPercentage) {
                System.out.printf("  Battery: %3d%% %s (Complete)%n", batteryPercentage, getProgressBar(batteryPercentage));
                break;
            } else {
                System.out.printf("  Battery: %3d%% %s%n", batteryPercentage, getProgressBar(batteryPercentage));
            }
        }
        outTime = LocalDateTime.now();
        try {
            station.exportInvoice(connectorId, energyConsumedKWh, inTime, outTime);
        } catch (IOException e) {
            System.err.println("Failed to export invoice: " + e.getMessage());
        }
    }

    public LocalDateTime getInTime() {
        return inTime;
    }

    public LocalDateTime getOutTime() {
        return outTime;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public ChargingStation getStation() {
        return station;
    }

    public int getConnectorId() {
        return connectorId;
    }
}
