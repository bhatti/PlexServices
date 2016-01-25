package com.plexobject.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements Data Providers interface
 * 
 * @author shahzad bhatti
 *
 */
public class DataProvidersImpl implements DataProviders {
    private ConcurrentHashMap<MetaField, Set<DataProvider>> providersByOutputMetaField = new ConcurrentHashMap<>();

    @Override
    public Collection<DataFieldRow> produce(Collection<DataFieldRow> input,
            MetaFields expectedOutputFields) throws DataProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void register(DataProvider provider) {
        for (MetaField outputField : provider.getOutputFields().getMetaFields()) {
            synchronized (outputField) {
                Set<DataProvider> providers = providersByOutputMetaField
                        .get(outputField);
                if (providers == null) {
                    providers = new HashSet<DataProvider>();
                    Set<DataProvider> oldProviders = providersByOutputMetaField
                            .putIfAbsent(outputField, providers);
                    if (oldProviders != null) {
                        providers = oldProviders;
                    }
                }
                providers.add(provider);
            }
        }
    }

    @Override
    public void unregister(DataProvider provider) {
        for (MetaField outputField : provider.getOutputFields().getMetaFields()) {
            synchronized (outputField) {
                Set<DataProvider> providers = providersByOutputMetaField
                        .get(outputField);
                if (providers != null) {
                    providers.remove(provider);
                    if (providers.size() == 0) {
                        providersByOutputMetaField.remove(outputField);
                    }
                }
            }
        }
    }

    @Override
    public Collection<DataProvider> getDataProviders(MetaFields outputFields) {
        Set<DataProvider> allProviders = new HashSet<>();
        for (MetaField outputField : outputFields.getMetaFields()) {
            synchronized (outputField) {
                Set<DataProvider> providers = providersByOutputMetaField
                        .get(outputField);
                if (providers != null) {
                    allProviders.addAll(providers);
                }
            }
        }
        return allProviders;
    }

}
