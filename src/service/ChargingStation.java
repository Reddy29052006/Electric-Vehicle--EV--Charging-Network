package src.service;

import src.exception.*;
import src.com.ev.network.BillablePower;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChargingStation implements BillablePower {
    private final String stationId;
    private final float powerCapacity;
    private float availablePower;
    private final ConnectorManager connectorManager;
    private final ArrayList<String> connectorStatuses;
    private final Map<Integer, LocalDateTime> connectorInTimes;
    private double ratePerKWh;

    public ChargingStation(String stationId, int noOfConnectors, float powerCapacity) {
        if (stationId == null || stationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Station ID cannot be null or empty.");
        }
        this.stationId = stationId.trim().toUpperCase();

        if (noOfConnectors <= 0) {
            throw new IllegalArgumentException("Number of connectors must be positive.");
        }
        if (!Float.isFinite(powerCapacity) || powerCapacity <= 0) {
            throw new IllegalArgumentException("Power capacity must be positive and finite.");
        }

        this.powerCapacity = powerCapacity;
        this.availablePower = powerCapacity;
        this.connectorManager = new ConnectorManager(noOfConnectors);
        this.ratePerKWh = DEFAULT_RATE_PER_KWH;
        this.connectorInTimes = new HashMap<>();
        this.connectorStatuses = new ArrayList<>(noOfConnectors);
        for (int i = 0; i < noOfConnectors; i++) {
            this.connectorStatuses.add("Available");
        }
    }

    public void occupyConnector(int index) throws InvalidConnectorException, ConnectorOccupiedException {
        validateConnectorIndex(index);
        if ("Occupied".equalsIgnoreCase(connectorStatuses.get(index - 1))) {
            throw new ConnectorOccupiedException("Connector " + index + " at station " + stationId + " is occupied.");
        }
        connectorManager.occupyConnector(index);
        connectorStatuses.set(index - 1, "Occupied");
        connectorInTimes.put(index, LocalDateTime.now());
    }

    public void releaseConnector(int index) throws InvalidConnectorException, ConnectorAvailableException {
        validateConnectorIndex(index);
        connectorManager.releaseConnector(index);
        connectorStatuses.set(index - 1, "Available");
        connectorInTimes.remove(index);
    }

    public LocalDateTime getConnectorInTime(int index) {
        return connectorInTimes.get(index);
    }

    private void validateConnectorIndex(int index) throws InvalidConnectorException {
        if (index < 1 || index > connectorStatuses.size()) {
            throw new InvalidConnectorException("Invalid connector index: " + index);
        }
    }

    public void getConnectorStatus() {
        System.out.println("    Status Per Connector:");
        for (int i = 0; i < connectorStatuses.size(); i++) {
            String status = connectorStatuses.get(i);
            String display = "Occupied".equalsIgnoreCase(status) ? "[OCCUPIED]" : "[AVAILABLE]";
            System.out.println(String.format("      - Connector #%d : %-11s", i + 1, display));
        }
    }

    public ArrayList<String> getConnectorStatuses() {
        return new ArrayList<>(connectorStatuses);
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
    public void exportInvoice(String fileName, int connectorId, double energyConsumedKWh, double totalCost,
            LocalDateTime inTime, LocalDateTime outTime) throws IOException {
        exportInvoice(fileName, connectorId, energyConsumedKWh, totalCost, inTime, outTime, null);
    }

    public void exportInvoice(String fileName, int connectorId, double energyConsumedKWh, double totalCost,
            LocalDateTime inTime, LocalDateTime outTime, String userName) throws IOException {
        File file = new File(fileName);
        try (FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter pw = new PrintWriter(bw)) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String inTimeStr = (inTime != null) ? inTime.format(formatter)
                    : LocalDateTime.now().minusMinutes(30).format(formatter);
            String outTimeStr = (outTime != null) ? outTime.format(formatter) : LocalDateTime.now().format(formatter);

            pw.println("============================================================");
            pw.println("                     EV CHARGING INVOICE                    ");
            pw.println("============================================================");
            pw.println("User Name          : " + (userName != null && !userName.trim().isEmpty() ? userName : "N/A"));
            pw.println("In-Time Timestamp  : " + inTimeStr);
            pw.println("Out-Time Timestamp : " + outTimeStr);
            pw.println("Station ID         : " + stationId);
            pw.println("Station Type       : " + this.getClass().getSimpleName());
            pw.println("Connector ID       : " + connectorId);
            pw.printf("Energy Consumed    : %.2f kWh%n", energyConsumedKWh);
            pw.printf("Tariff Rate        : Rs:%.2f / kWh%n", ratePerKWh);
            pw.printf("Total Amount Due   : Rs:%.2f%n", totalCost);
            pw.println("============================================================");
            pw.println();
        }
    }

    @Override
    public void exportInvoice(String fileName, int connectorId, double energyConsumedKWh, double totalCost)
            throws IOException {
        LocalDateTime inTime = connectorInTimes.getOrDefault(connectorId, LocalDateTime.now().minusMinutes(30));
        LocalDateTime outTime = LocalDateTime.now();
        exportInvoice(fileName, connectorId, energyConsumedKWh, totalCost, inTime, outTime);
    }

    @Override
    public void exportInvoice(int connectorId, double energyConsumedKWh, LocalDateTime inTime, LocalDateTime outTime)
            throws IOException {
        double totalCost = calculateBill(energyConsumedKWh);
        exportInvoice("charging_bills.txt", connectorId, energyConsumedKWh, totalCost, inTime, outTime);
    }

    @Override
    public void exportInvoice(int connectorId, double energyConsumedKWh) throws IOException {
        LocalDateTime inTime = connectorInTimes.getOrDefault(connectorId, LocalDateTime.now().minusMinutes(30));
        LocalDateTime outTime = LocalDateTime.now();
        exportInvoice(connectorId, energyConsumedKWh, inTime, outTime);
    }

    public Thread startChargingSession(int connectorId, int initialBatteryPercentage, int targetPercentage,
            double energyConsumedKWh) {
        return startChargingSession(connectorId, initialBatteryPercentage, targetPercentage, energyConsumedKWh, null);
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
