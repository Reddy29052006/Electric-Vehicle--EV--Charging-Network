package src.service;

import src.exception.ConnectorAvailableException;
import src.exception.ConnectorOccupiedException;
import src.exception.InvalidConnectorException;

import java.util.ArrayList;

public class ConnectorManager {
    private final ArrayList<Connector> connectors;

    public ConnectorManager(int noOfConnectors) {
        connectors = new ArrayList<>(noOfConnectors);
        for (int i = 1; i <= noOfConnectors; i++) {
            connectors.add(new Connector(i));
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

    public ArrayList<String> getConnectorStatuses() {
        ArrayList<String> statuses = new ArrayList<>();
        for (Connector connector : connectors) {
            statuses.add(connector.getStatus());
        }
        return statuses;
    }

    private void validateIndex(int index) throws InvalidConnectorException {
        if (index < 1 || index > connectors.size()) {
            throw new InvalidConnectorException("Invalid connector index: " + index);
        }
    }
}
