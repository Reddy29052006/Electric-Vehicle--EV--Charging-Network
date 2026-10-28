package src.service;

import src.exception.ConnectorAvailableException;
import src.exception.ConnectorOccupiedException;

public class Connector {
    private final int connectorId;
    private boolean occupied;

    public Connector(int connectorId) {
        this.connectorId = connectorId;
        this.occupied = false;
    }

    public void occupy() throws ConnectorOccupiedException {
        if (occupied) {
            throw new ConnectorOccupiedException("Connector " + connectorId + " is already occupied.");
        }
        occupied = true;
    }

    public void release() throws ConnectorAvailableException {
        if (!occupied) {
            throw new ConnectorAvailableException("Connector " + connectorId + " is already available.");
        }
        occupied = false;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getStatus() {
        return occupied ? "Occupied" : "Available";
    }

    public int getConnectorId() {
        return connectorId;
    }
}
