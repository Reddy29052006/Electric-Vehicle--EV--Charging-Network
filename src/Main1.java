package src;

import src.exception.ConnectorOccupiedException;
import src.exception.InvalidChargingRateException;
import src.service.ChargingStation;
import src.service.FastChargingStation;

import java.util.ArrayList;
import java.util.List;

public class Main1 {
    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("                  ELECTRIC VEHICLE (EV) CHARGING NETWORK SYSTEM");
        System.out.println("================================================================================");

        List<ChargingStation> fastStations = new ArrayList<>();
        List<ChargingStation> normalStations = new ArrayList<>();

        try {
            fastStations.add(new FastChargingStation("FAST-001", 4, 150.0f, 120.0f));
            fastStations.add(new FastChargingStation("FAST-002", 4, 120.0f, 100.0f));
            normalStations.add(new ChargingStation("NORMAL-001", 4, 50.0f));
            normalStations.add(new ChargingStation("NORMAL-002", 4, 40.0f));
        } catch (InvalidChargingRateException e) {
            System.out.println("Error initializing stations: " + e.getMessage());
            return;
        }

        List<ChargingStation> allStations = new ArrayList<>(fastStations);
        allStations.addAll(normalStations);

        System.out.println("\n all stations.");
        for (ChargingStation station : allStations) {
            System.out.printf("  - %s : %.1f kW  %d connectors available\n", station.getStationId(),
                    station.getPowerCapacity(), station.getConnectorStatuses().size());
        }

        System.out.println("Stations with power capacity > 50 kW:");
        allStations.stream()
                .filter(station -> station.getPowerCapacity() > 50)
                .forEach(station -> System.out.printf("  - %s : %.1f kW%n",
                        station.getStationId(), station.getPowerCapacity()));

        ChargingStation station = fastStations.get(0);
        int connectorId = 1;

        final Thread[] user1Session = new Thread[1];

        Thread user1 = new Thread(() -> {
            try {
                System.out.println("[USER-1] Requesting Connector " + connectorId + " at " + station.getStationId());
                station.occupyConnector(connectorId);
                System.out.println("[USER-1] Connector " + connectorId + " assigned. Starting charging...");
                String userName = "User1";
                int currentPercentage = 20;
                int targetPercentage = 80;
                double batteryCapacity = 40.0;
                double energyConsumed = batteryCapacity * (targetPercentage - currentPercentage) / 100.0;
                System.out.println("[USER-1] User: " + userName);
                System.out.printf("[USER-1] Battery: %d%% -> %d%% | Energy: %.2f kWh%n", currentPercentage,
                        targetPercentage, energyConsumed);
                user1Session[0] = station.startChargingSession(connectorId, currentPercentage, targetPercentage,
                        energyConsumed, userName);
                user1Session[0].join();
                station.releaseConnector(connectorId);
                System.out.println("[USER-1] Connector " + connectorId + " released.");
            } catch (Exception e) {
                System.out.println("[USER-1] Error: " + e.getMessage());
            }
        });

        Thread user2 = new Thread(() -> {
            try {
                Thread.sleep(150);
                System.out.println("\n[USER-2] Requesting same Connector " + connectorId + " at "
                        + station.getStationId() + " while USER-1 may be using it");
                station.occupyConnector(connectorId);
                System.out.println("[USER-2] Connector " + connectorId + " assigned. Starting charging...");
                String userName = "User2";
                int currentPercentage = 30;
                int targetPercentage = 70;
                double batteryCapacity = 35.0;
                double energyConsumed = batteryCapacity * (targetPercentage - currentPercentage) / 100.0;
                System.out.println("[USER-2] User: " + userName);
                System.out.printf("[USER-2] Battery: %d%% -> %d%% | Energy: %.2f kWh%n", currentPercentage,
                        targetPercentage, energyConsumed);
                Thread session = station.startChargingSession(connectorId, currentPercentage, targetPercentage,
                        energyConsumed, userName);
                session.join();
                station.releaseConnector(connectorId);
                System.out.println("[USER-2] Connector " + connectorId + " released.");
            } catch (ConnectorOccupiedException e) {
                System.out.println("\n[USER-2] ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                System.out.println("[USER-2] Cannot access connector while it is in use.");
            } catch (Exception e) {
                System.out.println("\n[USER-2] ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                System.out.println("[USER-2] Cannot access connector while it is in use.");
            }
        });

        user1.start();
        user2.start();

        user1.join();
        user2.join();
    }
}
