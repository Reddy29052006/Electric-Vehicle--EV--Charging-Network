package src.service;

import src.com.ev.network.BillablePower;
import src.exception.ConnectorAvailableException;
import src.exception.ConnectorOccupiedException;
import src.exception.InvalidConnectorException;
import src.exception.InvalidPowerException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ChargingStation implements BillablePower {
    private static final String INVOICE_FILE = "charging_bills.txt";

    private final String stationId;
    private final float powerCapacity;
    private float availablePower;
    private final ConnectorManager connectorManager;
    private double ratePerKWh;

    public ChargingStation(String stationId, int noOfConnectors, float powerCapacity) {
        if (stationId == null || stationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Station ID cannot be null or empty.");
        }
        if (noOfConnectors <= 0) {
            throw new IllegalArgumentException("Number of connectors must be positive.");
        }
        if (!Float.isFinite(powerCapacity) || powerCapacity <= 0) {
            throw new IllegalArgumentException("Power capacity must be positive and finite.");
        }

        this.stationId = stationId.trim().toUpperCase();
        this.powerCapacity = powerCapacity;
        this.availablePower = powerCapacity;
        this.connectorManager = new ConnectorManager(noOfConnectors);
        this.ratePerKWh = DEFAULT_RATE_PER_KWH;
    }

    public void occupyConnector(int index) throws InvalidConnectorException, ConnectorOccupiedException {
        connectorManager.occupyConnector(index);
    }

    public void releaseConnector(int index) throws InvalidConnectorException, ConnectorAvailableException {
        connectorManager.releaseConnector(index);
    }

    public void getConnectorStatus() {
        ArrayList<String> statuses = getConnectorStatuses();
        System.out.println("    Status Per Connector:");
        for (int i = 0; i < statuses.size(); i++) {
            String display = "Occupied".equalsIgnoreCase(statuses.get(i)) ? "[OCCUPIED]" : "[AVAILABLE]";
            System.out.printf("      - Connector #%d : %s%n", i + 1, display);
        }
    }

    public ArrayList<String> getConnectorStatuses() {
        return connectorManager.getConnectorStatuses();
    }

    public float getPowerCapacity() {
        return powerCapacity;
    }

    public void setAvailablePower(float availablePower) throws InvalidPowerException {
        if (!Float.isFinite(availablePower) || availablePower < 0 || availablePower > powerCapacity) {
            throw new InvalidPowerException("Available power must be between 0 and " + powerCapacity + ".");
        }
        this.availablePower = availablePower;
    }

    public float getAvailablePower() {
        return availablePower;
    }

    public String getStationId() {
        return stationId;
    }

    @Override
    public double getRatePerKWh() {
        return ratePerKWh;
    }

    @Override
    public void setRatePerKWh(double rate) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Rate must be positive.");
        }
        this.ratePerKWh = rate;
    }

    @Override
    public double calculateBill(double energyConsumedKWh) {
        return energyConsumedKWh * ratePerKWh;
    }

    @Override
    public void exportInvoice(int connectorId, double energyConsumedKWh,
            LocalDateTime inTime, LocalDateTime outTime, String userName) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String name = (userName == null || userName.trim().isEmpty()) ? "N/A" : userName.trim();
        double totalCost = calculateBill(energyConsumedKWh);

        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(INVOICE_FILE, true)))) {
            pw.println("============================================================");
            pw.println("                     EV CHARGING INVOICE                    ");
            pw.println("============================================================");
            pw.println("User Name          : " + name);
            pw.println("In-Time Timestamp  : " + inTime.format(formatter));
            pw.println("Out-Time Timestamp : " + outTime.format(formatter));
            pw.println("Station ID         : " + stationId);
            pw.println("Station Type       : " + getClass().getSimpleName());
            pw.println("Connector ID       : " + connectorId);
            pw.printf("Energy Consumed    : %.2f kWh%n", energyConsumedKWh);
            pw.printf("Tariff Rate        : Rs:%.2f / kWh%n", ratePerKWh);
            pw.printf("Total Amount Due   : Rs:%.2f%n", totalCost);
            pw.println("============================================================");
            pw.println();
        }
    }

    public Thread startChargingSession(int connectorId, int initialBatteryPercentage, int targetPercentage,
            double energyConsumedKWh, String userName) {
        ChargingSession session = new ChargingSession(this, connectorId, initialBatteryPercentage, targetPercentage,
                energyConsumedKWh, userName);
        Thread thread = new Thread(session, stationId + "-Connector-" + connectorId);
        thread.start();
        return thread;
    }

    @Override
    public String toString() {
        return "ChargingStation[ID=" + stationId + ", Power=" + availablePower + "/" + powerCapacity + "kW]";
    }
}
