package src.service;

import java.util.ArrayList;
import java.util.List;
import src.exception.ConnectorAvailableException;
import src.exception.ConnectorOccupiedException;
import src.exception.InvalidConnectorException;

public class ConnectorManager {
    private final List<Connector> connectors;

    public ConnectorManager(int noOfConnectors) {
        connectors = new ArrayList<>(noOfConnectors);
        for (int i = 0; i < noOfConnectors; i++) {
            connectors.add(new Connector(i + 1));
        }
    }

    public void occupyConnector(int index) throws InvalidConnectorException, ConnectorOccupiedException {
        validateIndex(index);
        connectors.get(index - 1).occupy();
    }

    public void releaseConnector(int index) throws InvalidConnectorException, ConnectorAvailableException {
        validateIndex(index);
        connectors.get(index - 1).release();
    }

    private void validateIndex(int index) throws InvalidConnectorException {
        if (index < 1 || index > connectors.size()) {
            throw new InvalidConnectorException("Invalid connector index: " + index);
        }
    }

    public List<String> getConnectorStatuses() {
        List<String> statuses = new ArrayList<>();
        for (Connector connector : connectors) {
            statuses.add(connector.getStatus());
        }
        return statuses;
    }

    public void displayConnectorStatus() {
        for (Connector connector : connectors) {
            System.out.println("Connector " + connector.getConnectorId() + ": " + connector.getStatus());
        }
    }

    public int getConnectorCount() {
        return connectors.size();
    }
}
