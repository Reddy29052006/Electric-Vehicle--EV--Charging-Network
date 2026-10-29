package src;

import src.exception.ConnectorOccupiedException;
import src.service.ChargingStation;
import src.service.FastChargingStation;

public class Main1 {
    public static void main(String[] args) throws Exception {
        ChargingStation station = new FastChargingStation("FAST-001", 4, 150.0f, 120.0f);
        int connectorId = 1;

        station.occupyConnector(connectorId);
        System.out.println("Connector " + connectorId + " occupied at " + station.getStationId());

        Thread session = station.startChargingSession(connectorId, 20, 80, 24.0, "Demo User");

        try {
            station.occupyConnector(connectorId);
            System.out.println("ERROR: connector was booked twice!");
        } catch (ConnectorOccupiedException e) {
            System.out.println("Double booking blocked: " + e.getMessage());
        }

        session.join();
        station.releaseConnector(connectorId);
        System.out.println("Connector " + connectorId + " released.");
    }
}
