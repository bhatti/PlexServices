package com.plexobject.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements Data Providers interface
 * 
 * @author shahzad bhatti
 *
 */
public class DataProvidersImpl implements DataProviders {
    private ConcurrentHashMap<MetaField, Set<DataProvider>> providersByOutputMetaField = new ConcurrentHashMap<>();

    @Override
    public void produce(final DataFieldRowSet requestFields,
            final DataFieldRowSet responseFields, DataConfiguration config)
            throws DataProviderException {
        long started = System.currentTimeMillis();
        // Get all data providers needed
        Collection<DataProvider> providers = getDataProviders(
                requestFields.getMetaFields(), responseFields.getMetaFields());
        System.out.println("providers " + providers);
        final ExecutorService executor = Executors.newFixedThreadPool(Math.min(
                providers.size(), 3));
        // Add intermediate data needed, e.g. we could call provider A that
        // returns some fields, which are used as input for provider B
        addIntermediateFields(requestFields, responseFields, providers);
        // go through all providers
        while (providers.size() > 0) {
            final Map<DataProvider, Boolean> waitingProviders = new HashMap<>();
            // We will execute providers asynchronously and then wait for
            // providers until they are finished
            executeProviders(requestFields, responseFields, config, providers,
                    waitingProviders, executor);
            // waiting for providers to finish
            waitForProviders(providers, waitingProviders);
        }
        executor.shutdown();
        long elapsed = System.currentTimeMillis() - started;
        System.out.println("ALL DONE " + elapsed);
    }

    @Override
    public void register(DataProvider provider) {
        for (MetaField outputField : provider.getResponseFields()
                .getMetaFields()) {
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
        for (MetaField outputField : provider.getResponseFields()
                .getMetaFields()) {
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
    public Collection<DataProvider> getDataProviders(MetaFields requestFields,
            MetaFields responseFields) {
        final List<DataProvider> providers = new ArrayList<>();
        getDataProviders(new MetaFields(requestFields.getMetaFields()),
                new MetaFields(responseFields.getMetaFields()), providers);
        Collections.sort(providers); // sort by dependency
        return providers;
    }

    private void getDataProviders(MetaFields requestFields,
            MetaFields responseFields, List<DataProvider> existingProviders) {
        responseFields.getMetaFields().removeAll(requestFields.getMetaFields());
        for (MetaField responseField : responseFields.getMetaFields()) {
            Set<DataProvider> providers = providersByOutputMetaField
                    .get(responseField);
            if (providers == null) {
                throw new DataProviderException("Failed to find provider for "
                        + responseField);
            }
            DataProvider provider = getBestDataProvider(providers,
                    requestFields);
            if (existingProviders.contains(provider)) {
                continue;
            }
            existingProviders.add(provider);

            MetaFields missingFields = requestFields
                    .getMissingMetaFields(provider.getRequestFields());

            // add output fields to requests so that we can use it for other
            // providers
            requestFields.addMetaFields(provider.getResponseFields());
            //
            if (missingFields.size() > 0) {
                getDataProviders(requestFields, missingFields,
                        existingProviders);
            }
        }
    }

    private DataProvider getBestDataProvider(Set<DataProvider> providers,
            MetaFields requestFields) {
        DataProvider bestProvider = null;
        int minCount = Integer.MAX_VALUE;
        for (DataProvider provider : providers) {
            int count = provider.getRequestFields().getMissingCount(
                    requestFields);
            if (count == 0) {
                return provider;
            }
            if (count < minCount) {
                minCount = count;
                bestProvider = provider;
            }
        }
        return bestProvider;
    }

    private void waitForProviders(Collection<DataProvider> providers,
            final Map<DataProvider, Boolean> waitingProviders) {
        for (final Map.Entry<DataProvider, Boolean> e : waitingProviders
                .entrySet()) {
            providers.remove(e.getKey());
            synchronized (e.getKey()) {
                Boolean completed = waitingProviders.get(e.getKey());
                while (!completed) {
                    try {
                        System.out.println("Waiting " + e.getKey() + " with "
                                + Thread.currentThread().getName());
                        e.getKey().wait();
                    } catch (InterruptedException ex) {
                    }
                    completed = waitingProviders.get(e.getKey());
                }
                System.out.println("Finished Waiting " + e.getKey() + " with "
                        + Thread.currentThread().getName());
            }
        }
    }

    private void executeProviders(final DataFieldRowSet requestFields,
            final DataFieldRowSet responseFields,
            final DataConfiguration config,
            final Collection<DataProvider> providers,
            final Map<DataProvider, Boolean> waitingProviders,
            final ExecutorService executor) {
        for (final DataProvider provider : providers) {
            if (requestFields.getMetaFields().getMissingCount(
                    provider.getRequestFields()) == 0) {
                synchronized (provider) {
                    waitingProviders.put(provider, false);
                }
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        executeProvider(requestFields, responseFields, config,
                                waitingProviders, provider);
                    }
                });

            } else {
                // System.out.println("Provider "
                // + provider
                // + " has available "
                // + availableFields
                // + ", missing "
                // + availableFields.getMissingMetaFields(provider
                // .getRequestFields()));
            }
        }
    }

    private void executeProvider(final DataFieldRowSet requestFields,
            final DataFieldRowSet responseFields,
            final DataConfiguration config,
            final Map<DataProvider, Boolean> waitingProviders,
            final DataProvider provider) {
        try {
            provider.produce(requestFields, responseFields, config);
            for (int i = 0; i < responseFields.size(); i++) {
                for (MetaField responseField : provider.getResponseFields()
                        .getMetaFields()) {
                    if (responseFields.getMetaFields().contains(responseField)) {
                        Object value = responseFields
                                .getField(responseField, i);
                        requestFields.addDataField(responseField, value, i);
                    }
                }
            }
            // System.out.println("---------------Executing " + provider
            // + " with " + Thread.currentThread().getName()
            // + "\n\tavailable " + availableFields + "\n\trequestFields "
            // + requestFields + "\n\tresponseFields " + responseFields);
        } catch (Exception e) {
            System.out.println("Failed to execute " + provider + " with input "
                    + requestFields);
            e.printStackTrace();
        } finally {
            synchronized (provider) {
                waitingProviders.put(provider, true);
                provider.notify();
            }
        }
    }

    private void addIntermediateFields(final DataFieldRowSet requestFields,
            final DataFieldRowSet responseFields,
            Collection<DataProvider> providers) {
        // add any intermediate data needed to the output
        for (final DataProvider provider : providers) {
            for (MetaField metaField : provider.getRequestFields()
                    .getMetaFields()) {
                if (!requestFields.getMetaFields().contains(metaField)) {
                    responseFields.getMetaFields().addMetaField(metaField);
                }
            }
        }
    }
}
