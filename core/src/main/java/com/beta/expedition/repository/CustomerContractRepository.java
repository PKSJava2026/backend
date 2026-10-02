package com.beta.expedition.repository;

import com.beta.expedition.model.CustomerContract;
import com.beta.expedition.util.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerContractRepository extends ContractRepository<CustomerContract> {

    public CustomerContractRepository(DatabaseManager db) {
        super(db);
    }

    @Override
    protected String table() {
        return "customer_contracts";
    }

    @Override
    protected String counterpartyColumn() {
        return "customer_id";
    }

    @Override
    protected long counterpartyId(CustomerContract contract) {
        return contract.getCustomerId();
    }

    @Override
    protected CustomerContract map(ResultSet rs) throws SQLException {
        CustomerContract contract = new CustomerContract();
        fillCommon(contract, rs);
        contract.setCustomerId(rs.getLong("customer_id"));
        return contract;
    }
}
