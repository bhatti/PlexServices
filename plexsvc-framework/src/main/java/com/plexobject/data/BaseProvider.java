package com.plexobject.data;


public abstract class BaseProvider implements DataProvider {
    private final MetaFields requestFields;
    private final MetaFields responseFields;

    public BaseProvider(MetaFields requestFields, MetaFields responseFields) {
        this.requestFields = requestFields;
        this.responseFields = responseFields;
    }

    @Override
    public final MetaFields getRequestFields() {
        return requestFields;
    }

    @Override
    public final MetaFields getResponseFields() {
        return responseFields;
    }

    @Override
    public int compareTo(DataProvider other) {
        for (MetaField requestField : requestFields.getMetaFields()) {
            if (other.getResponseFields().contains(requestField)) {
                return +1;
            }
        }
        for (MetaField responseField : responseFields.getMetaFields()) {
            if (other.getRequestFields().contains(responseField)) {
                return -1;
            }
        }

        return 0;
    }

}
