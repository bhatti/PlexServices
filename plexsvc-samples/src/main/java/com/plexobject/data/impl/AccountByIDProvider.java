package com.plexobject.data.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.plexobject.data.DataField;
import com.plexobject.data.DataFieldRow;
import com.plexobject.data.DataProvider;
import com.plexobject.data.DataProviderException;
import com.plexobject.data.MetaFields;

public class AccountByIDProvider implements DataProvider {

    @Override
    public Collection<DataFieldRow> produce(Collection<DataFieldRow> inputRows,
            MetaFields expectedOutputFields) throws DataProviderException {
        assert expectedOutputFields.equals(getOutputFields());
        Collection<DataFieldRow> outputRows = new ArrayList<>();
        for (DataFieldRow inputRow : inputRows) {
            long id = inputRow.getValueAsLong(AccountFieldNames.AccountID
                    .name());
            DataFieldRow row = DataFieldRow.of();
            row.addField(new DataField(AccountFieldNames.AccountID
                    .getMetaField(), id));
            row.addField(new DataField(AccountFieldNames.AccountName
                    .getMetaField(), id + "-name"));
            row.addField(new DataField(AccountFieldNames.AccountType
                    .getMetaField(), id + "-type"));
        }
        return outputRows;
    }

    @Override
    public MetaFields getInputFields() {
        return AccountFieldNames.getInputFields();
    }

    @Override
    public MetaFields getOutputFields() {
        return AccountFieldNames.getOutputFields();
    }

}
