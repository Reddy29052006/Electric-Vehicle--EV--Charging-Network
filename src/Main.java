package src;

import src.exception.InvalidChargingRateException;
import src.service.ChargingStation;
import src.service.FastChargingStation;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<ChargingStation> fastStations = new ArrayList<>();
    private static final List<ChargingStation> normalStations = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                  ELECTRIC VEHICLE (EV) CHARGING NETWORK SYSTEM");
        System.out.println("================================================================================");

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

        System.out.println("\n[INITIALIZATION] 4 Charging Stations created with 4 connectors each.");
        System.out.println("Stations with power capacity > 50 kW:");
        allStations.stream()
                .filter(station -> station.getPowerCapacity() > 50)
                .forEach(station -> System.out.printf("  - %s : %.1f kW%n",
                        station.getStationId(), station.getPowerCapacity()));

        while (true) {
            System.out.println("\n================================================================================");
            System.out.println("                        CHARGING STATION SELECTION");
            System.out.println("================================================================================");
            System.out.println("1. Fast Charging Station");
            System.out.println("2. Normal Charging Station");
            System.out.println("3. Exit");

            int choice = readInt("Enter your choice: ");
            if (choice == 3) {
                System.out.println("\nThank you for using EV Charging Network System. Goodbye!");
                break;
            } else if (choice == 1) {
                selectStation(fastStations, "Fast");
            } else if (choice == 2) {
                selectStation(normalStations, "Normal");
            } else {
                System.out.println("Invalid choice! Please try again.");
            }
        }

        scanner.close();
    }

    private static void selectStation(List<ChargingStation> stations, String type) {
        System.out.println("\nAVAILABLE " + type.toUpperCase() + " CHARGING STATIONS");
        for (int i = 0; i < stations.size(); i++) {
            ChargingStation station = stations.get(i);
            System.out.printf("%d. %s | Power: %.1f kW | Connectors: %d%n",
                    i + 1, station.getStationId(), station.getAvailablePower(),
                    station.getConnectorStatuses().size());
        }

        int choice = readInt("Select a station: ");
        if (choice < 1 || choice > stations.size()) {
            System.out.println("Invalid station selection!");
            return;
        }
        startCharging(stations.get(choice - 1));
    }

    private static void startCharging(ChargingStation station) {
        ArrayList<String> statuses = station.getConnectorStatuses();
        System.out.println("\nCONNECTORS AT " + station.getStationId());
        for (int i = 0; i < statuses.size(); i++) {
            System.out.printf("  Connector %d: [%s]%n", i + 1, statuses.get(i).toUpperCase());
        }

        int connector = readInt("Select an available connector: ");
        if (connector < 1 || connector > statuses.size()) {
            System.out.println("Invalid connector selection!");
            return;
        }
        if ("Occupied".equalsIgnoreCase(statuses.get(connector - 1))) {
            System.out.println("Connector " + connector + " is already occupied!");
            return;
        }

        System.out.print("Enter your full name: ");
        String userName = scanner.nextLine().trim();

        int currentPercentage = readInt("Enter current battery percentage (0-100): ");
        int targetPercentage = readInt("Enter target battery percentage (0-100): ");
        double batteryCapacity = readDouble("Enter total capacity of battery (kWh): ");

        if (currentPercentage < 0 || currentPercentage > 100
                || targetPercentage < currentPercentage || targetPercentage > 100) {
            System.out.println("Invalid battery percentage!");
            return;
        }
        if (batteryCapacity <= 0) {
            System.out.println("Battery capacity must be positive!");
            return;
        }

        double energyConsumed = batteryCapacity * (targetPercentage - currentPercentage) / 100.0;

        try {
            station.occupyConnector(connector);
            System.out.println("\n[BOOKING CONFIRMED]");
            System.out.println("User: " + userName);
            System.out.println("Station: " + station.getStationId()
                    + " (" + station.getClass().getSimpleName() + ")");
            System.out.println("Connector: " + connector);
            System.out.println("Battery: " + currentPercentage + "% -> " + targetPercentage + "%");
            System.out.printf("Energy to be consumed: %.2f kWh%n%n", energyConsumed);

            Thread session = station.startChargingSession(connector, currentPercentage,
                    targetPercentage, energyConsumed, userName);
            session.join();

            station.releaseConnector(connector);
            System.out.println("\nConnector " + connector + " released.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = scanner.nextDouble();
                scanner.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
            }
        }
    }
}
