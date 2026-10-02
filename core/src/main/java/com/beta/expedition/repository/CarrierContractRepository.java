package com.beta.expedition.repository;

import com.beta.expedition.model.CarrierContract;
import com.beta.expedition.util.DatabaseManager;

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
        return contract;
    }
}
