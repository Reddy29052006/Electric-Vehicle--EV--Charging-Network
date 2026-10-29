package src.com.ev.network;

import java.io.IOException;
import java.time.LocalDateTime;

public interface BillablePower {
    double DEFAULT_RATE_PER_KWH = 12.50;

    double getRatePerKWh();

    void setRatePerKWh(double rate);

    double calculateBill(double energyConsumedKWh);

    void exportInvoice(int connectorId, double energyConsumedKWh, LocalDateTime inTime, LocalDateTime outTime,
            String userName) throws IOException;
}
