package com.beta.expedition.repository;

import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.model.CarrierContract;
import com.beta.expedition.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CarrierContractRepository extends ContractRepository<CarrierContract> {

    public CarrierContractRepository(DatabaseManager db) {
        super(db);
    }

    @Override
    protected String table() {
        return "carrier_contracts";
    }

    @Override
    protected String counterpartyColumn() {
        return "carrier_id";
    }

    @Override
    protected long counterpartyId(CarrierContract contract) {
        return contract.getCarrierId();
    }

    @Override
    protected CarrierContract map(ResultSet rs) throws SQLException {
        CarrierContract contract = new CarrierContract();
        fillCommon(contract, rs);
        contract.setCarrierId(rs.getLong("carrier_id"));
        contract.setCooperationId(rs.getObject("cooperation_id", Long.class));
        return contract;
    }

    public void linkCooperation(long contractId, long cooperationId) {
        String sql = "UPDATE carrier_contracts SET cooperation_id = ? WHERE id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cooperationId);
            statement.setLong(2, contractId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось связать договор с заявкой: " + e.getMessage(), e);
        }
    }
}
