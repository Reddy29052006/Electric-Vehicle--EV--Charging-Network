package src;

import src.exception.ConnectorOccupiedException;
import src.exception.InvalidChargingRateException;
import src.service.ChargingStation;
import src.service.FastChargingStation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main1 {
    public static void main(String[] args) {
        try {
            FastChargingStation fastStation1 = new FastChargingStation("FAST-001", 4, 150.0f, 120.0f);
            FastChargingStation fastStation2 = new FastChargingStation("FAST-002", 4, 120.0f, 100.0f);
            ChargingStation normalStation1 = new ChargingStation("NORMAL-001", 4, 50.0f);
            ChargingStation normalStation2 = new ChargingStation("NORMAL-002", 4, 40.0f);

            List<ChargingStation> allStations = new ArrayList<>();
            allStations.add(fastStation1);
            allStations.add(fastStation2);
            allStations.add(normalStation1);
            allStations.add(normalStation2);

            List<ChargingStation> filteredStations = allStations.stream()
                    .filter(st -> st.getAvailablePower() > 50)
                    .collect(Collectors.toList());
            filteredStations
                    .forEach(st -> System.out.println(st.getStationId() + ": " + st.getAvailablePower() + " kW"));

            fastStation1.occupyConnector(1);
            Thread chargingThread = fastStation1.startChargingSession(1, 20, 80, 24.0, "Demo User");
            if (chargingThread != null) {
                chargingThread.join();
            }
            fastStation1.releaseConnector(1);

            try {
                normalStation1.occupyConnector(2);
                try {
                    normalStation1.occupyConnector(2);
                } catch (ConnectorOccupiedException e) {
                    System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
                normalStation1.releaseConnector(2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (InvalidChargingRateException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
