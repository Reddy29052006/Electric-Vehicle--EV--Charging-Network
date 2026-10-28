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

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                  ELECTRIC VEHICLE (EV) CHARGING NETWORK SYSTEM");
        System.out.println("================================================================================\n");

        // Create 4 charging stations - 2 fast charging stations and 2 normal charging
        // stations
        // Each station has 4 connectors
        List<ChargingStation> allStations = new ArrayList<ChargingStation>();
        List<FastChargingStation> fastChargingStations = new ArrayList<FastChargingStation>();
        List<ChargingStation> normalChargingStations = new ArrayList<ChargingStation>();

        try {
            // Fast charging stations
            FastChargingStation fastStation1 = new FastChargingStation("FAST-001", 4, 150.0f, 120.0f);
            FastChargingStation fastStation2 = new FastChargingStation("FAST-002", 4, 120.0f, 100.0f);
            fastChargingStations.add(fastStation1);
            fastChargingStations.add(fastStation2);
            allStations.add(fastStation1);
            allStations.add(fastStation2);

            // Normal charging stations
            ChargingStation normalStation1 = new ChargingStation("NORMAL-001", 4, 50.0f);
            ChargingStation normalStation2 = new ChargingStation("NORMAL-002", 4, 40.0f);
            normalChargingStations.add(normalStation1);
            normalChargingStations.add(normalStation2);
            allStations.add(normalStation1);
            allStations.add(normalStation2);

            System.out.println("[INITIALIZATION] 4 Charging Stations created with 4 connectors each:");
            System.out.println("  - 2 Fast Charging Stations: FAST-001, FAST-002");
            System.out.println("  - 2 Normal Charging Stations: NORMAL-001, NORMAL-002");
            System.out.println();

            System.out.println("Stations with power capacity > 50 kW ");
            allStations.stream()
                    .filter(st -> st.getPowerCapacity() > 50)
                    .forEach(st -> System.out.println("  - " + st.getStationId() + " : " + st.getPowerCapacity()
                            + " kW (" + st.getClass().getSimpleName() + ")"));
            System.out.println();

        } catch (InvalidChargingRateException e) {
            System.out.println("Error initializing stations: " + e.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("================================================================================");
            System.out.println("                        CHARGING STATION SELECTION");
            System.out.println("================================================================================");
            System.out.println("Select Station Type:");
            System.out.println("1. Fast Charging Station");
            System.out.println("2. Normal Charging Station");
            System.out.println("3. Exit");
            System.out.print("\nEnter your choice: ");

            try {
                int typeChoice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                if (typeChoice == 3) {
                    System.out.println("\nThank you for using EV Charging Network System. Goodbye!");
                    running = false;
                    continue;
                } else if (typeChoice == 1) {
                    // Show all fast charging stations
                    System.out.println(
                            "\n--------------------------------------------------------------------------------");
                    System.out.println("                    AVAILABLE FAST CHARGING STATIONS");
                    System.out.println(
                            "--------------------------------------------------------------------------------");
                    for (int i = 0; i < fastChargingStations.size(); i++) {
                        FastChargingStation fs = fastChargingStations.get(i);
                        System.out.println((i + 1) + ". Station ID: " + fs.getStationId()
                                + " | Power Capacity: " + fs.getAvailablePower() + " kW"
                                + " | Fast Charging Rate: " + fs.getFastChargingRate() + " kW"
                                + " | Connectors: " + fs.getConnectorStatuses().size());
                    }
                    System.out.print("\nSelect a Fast Charging Station (enter number): ");
                    int stationChoice = scanner.nextInt();
                    scanner.nextLine();

                    if (stationChoice >= 1 && stationChoice <= fastChargingStations.size()) {
                        FastChargingStation selectedStation = fastChargingStations.get(stationChoice - 1);
                        displayAvailableConnectors(selectedStation);
                    } else {
                        System.out.println("\nInvalid station selection!");
                    }

                } else if (typeChoice == 2) {
                    // Show all normal charging stations
                    System.out.println(
                            "\n--------------------------------------------------------------------------------");
                    System.out.println("                    AVAILABLE NORMAL CHARGING STATIONS");
                    System.out.println(
                            "--------------------------------------------------------------------------------");
                    for (int i = 0; i < normalChargingStations.size(); i++) {
                        ChargingStation ns = normalChargingStations.get(i);
                        System.out.println((i + 1) + ". Station ID: " + ns.getStationId()
                                + " | Power Capacity: " + ns.getAvailablePower() + " kW"
                                + " | Connectors: " + ns.getConnectorStatuses().size());
                    }
                    System.out.print("\nSelect a Normal Charging Station (enter number): ");
                    int stationChoice = scanner.nextInt();
                    scanner.nextLine();

                    if (stationChoice >= 1 && stationChoice <= normalChargingStations.size()) {
                        ChargingStation selectedStation = normalChargingStations.get(stationChoice - 1);
                        displayAvailableConnectors(selectedStation);
                    } else {
                        System.out.println("\nInvalid station selection!");
                    }

                } else {
                    System.out.println("\nInvalid choice! Please try again.");
                }

                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();

            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input! Please enter a number.");
                scanner.nextLine();
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    private static void displayAvailableConnectors(ChargingStation station) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("                        CONNECTORS AT " + station.getStationId());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Station Type: " + station.getClass().getSimpleName());
        System.out.println("Available Power: " + station.getAvailablePower() + " kW");
        System.out.println("\nConnector Status:");
        ArrayList<String> statuses = station.getConnectorStatuses();
        for (int i = 0; i < statuses.size(); i++) {
            String status = statuses.get(i);
            String displayStatus = "Occupied".equalsIgnoreCase(status) ? "[OCCUPIED]" : "[AVAILABLE]";
            System.out.println("  Connector " + (i + 1) + ": " + displayStatus);
        }
        System.out.println("\nTotal Connectors: " + statuses.size());

        // Select connector
        System.out.print("\nSelect an available connector (enter number): ");
        try {
            int connectorChoice = scanner.nextInt();
            scanner.nextLine();

            if (connectorChoice < 1 || connectorChoice > statuses.size()) {
                System.out.println("\nInvalid connector selection!");
                return;
            }

            if ("Occupied".equalsIgnoreCase(statuses.get(connectorChoice - 1))) {
                System.out.println("\nConnector " + connectorChoice + " is already occupied!");
                return;
            }

            // Get user details
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("                        USER DETAILS");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.print("Enter your full name: ");
            String userName = scanner.nextLine();

            System.out.print("Enter current battery percentage (0-100): ");
            int currentPercentage = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter target battery percentage (0-100): ");
            int targetPercentage = scanner.nextInt();
            scanner.nextLine();

            if (targetPercentage < currentPercentage) {
                System.out
                        .println("\nError: Target battery percentage cannot be less than current battery percentage!");
                return;
            }
            if (targetPercentage > 100) {
                System.out.println("\nError: Target battery percentage cannot be greater than 100!");
                return;
            }
            if (currentPercentage < 0 || currentPercentage > 100) {
                System.out.println("\nError: Current battery percentage must be between 0 and 100!");
                return;
            }

            System.out.print("Enter total capacity of battery (kWh): ");
            double batteryCapacity = scanner.nextDouble();
            scanner.nextLine();

            // Calculate energy consumed
            double energyConsumed = batteryCapacity * (targetPercentage - currentPercentage) / 100.0;
            if (energyConsumed < 0) {
                energyConsumed = 0;
            }

            // Book and start charging
            try {
                station.occupyConnector(connectorChoice);
                System.out.println("\n[BOOKING CONFIRMED]");
                System.out.println("User: " + userName);
                System.out.println(
                        "Station: " + station.getStationId() + " (" + station.getClass().getSimpleName() + ")");
                System.out.println("Connector: " + connectorChoice);
                System.out.println("Battery: " + currentPercentage + "% -> " + targetPercentage + "%");
                System.out.println("Battery Capacity: " + batteryCapacity + " kWh");
                System.out.println("Energy to be consumed: " + String.format("%.2f", energyConsumed) + " kWh");
                System.out.println("\nStarting charging session...\n");

                Thread chargingThread = station.startChargingSession(connectorChoice, currentPercentage,
                        targetPercentage, energyConsumed, userName);
                if (chargingThread != null) {
                    chargingThread.join();
                }

                // Release connector after charging
                try {
                    station.releaseConnector(connectorChoice);
                    System.out.println("\nConnector " + connectorChoice + " released.");
                } catch (Exception e) {
                    System.out.println("\nError releasing connector: " + e.getMessage());
                }

            } catch (Exception e) {
                System.out.println("\nError: " + e.getMessage());
            }

        } catch (InputMismatchException e) {
            System.out.println("\nInvalid input!");
            scanner.nextLine();
        }
    }
}
