package src.service;

import java.io.IOException;
import java.time.LocalDateTime;

public class ChargingSession implements Runnable {
    private final ChargingStation station;
    private final int connectorId;
    private final int targetPercentage;
    private final double energyConsumedKWh;
    private final int chargeStep;
    private final String userName;
    private int batteryPercentage;

    public ChargingSession(ChargingStation station, int connectorId, int initialBatteryPercentage,
            int targetPercentage, double energyConsumedKWh, String userName) {
        this.station = station;
        this.connectorId = connectorId;
        this.batteryPercentage = Math.max(0, Math.min(100, initialBatteryPercentage));
        this.targetPercentage = Math.max(this.batteryPercentage, Math.min(100, targetPercentage));
        this.energyConsumedKWh = energyConsumedKWh;
        this.userName = userName;
        this.chargeStep = (station instanceof FastChargingStation) ? 20 : 10;
    }

    @Override
    public void run() {
        LocalDateTime inTime = LocalDateTime.now();
        printProgress();

        while (batteryPercentage < targetPercentage) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            batteryPercentage = Math.min(targetPercentage, batteryPercentage + chargeStep);
            printProgress();
        }

        LocalDateTime outTime = LocalDateTime.now();
        try {
            station.exportInvoice(connectorId, energyConsumedKWh, inTime, outTime, userName);
        } catch (IOException e) {
            System.err.println("Failed to export invoice: " + e.getMessage());
        }
    }

    private void printProgress() {
        String status = batteryPercentage >= targetPercentage ? " (Complete)" : "";
        System.out.printf("  Battery: %3d%% %s%s%n", batteryPercentage, getProgressBar(batteryPercentage), status);
    }

    private String getProgressBar(int percent) {
        int filled = percent * 20 / 100;
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 20; i++) {
            bar.append(i < filled ? '#' : '-');
        }
        return bar.append(']').toString();
    }
}
