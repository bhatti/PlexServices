package com.plexobject.fsm;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.plexobject.domain.Pair;

public class FSMTest {
    @Before
    public void setup() {
    }

    @Test
    public void testAndroidLifecycleStates() {
        TransitionMappings mappings = initAndroidStates();
        FSM instance = new FSM(State.of("Init"), mappings, null);
        assertEquals("Created", instance.nextStateOnEvent("onCreate", null)
                .getName());
        assertEquals("Started", instance.nextStateOnEvent("onStart", null)
                .getName());
        assertEquals("Resumed", instance.nextStateOnEvent("onResume", null)
                .getName());
        assertEquals("Paused", instance.nextStateOnEvent("onPause", null)
                .getName());
        assertEquals("Resumed", instance.nextStateOnEvent("onResume", null)
                .getName());
        assertEquals("Paused", instance.nextStateOnEvent("onPause", null)
                .getName());
        assertEquals("Stopped", instance.nextStateOnEvent("onStop", null)
                .getName());
        assertEquals("Started", instance.nextStateOnEvent("onRestart", null)
                .getName());
        assertEquals("Resumed", instance.nextStateOnEvent("onResume", null)
                .getName());
        assertEquals("Paused", instance.nextStateOnEvent("onPause", null)
                .getName());
        assertEquals("Stopped", instance.nextStateOnEvent("onStop", null)
                .getName());
        assertEquals("Destroyed", instance.nextStateOnEvent("onDestroy", null)
                .getName());
        List<Pair<State, String>> history = instance.getHistory();
        int n = 0;
        assertEquals(Pair.of(State.of("Init"), null), history.get(n++));
        assertEquals(Pair.of(State.of("Created"), "onCreate"), history.get(n++));
        assertEquals(Pair.of(State.of("Started"), "onStart"), history.get(n++));
        assertEquals(Pair.of(State.of("Resumed"), "onResume"), history.get(n++));
        assertEquals(Pair.of(State.of("Paused"), "onPause"), history.get(n++));
        assertEquals(Pair.of(State.of("Resumed"), "onResume"), history.get(n++));
        assertEquals(Pair.of(State.of("Paused"), "onPause"), history.get(n++));
        assertEquals(Pair.of(State.of("Stopped"), "onStop"), history.get(n++));
        assertEquals(Pair.of(State.of("Started"), "onRestart"), history.get(n++));
        assertEquals(Pair.of(State.of("Resumed"), "onResume"), history.get(n++));
        assertEquals(Pair.of(State.of("Paused"), "onPause"), history.get(n++));
        assertEquals(Pair.of(State.of("Stopped"), "onStop"), history.get(n++));
        assertEquals(Pair.of(State.of("Destroyed"), "onDestroy"), history.get(n++));
    }

    @Test
    public void testBasicStates() {
        TransitionMappings mappings = initSignupStates();
        FSM instance = new FSM(State.of("WELCOME"), mappings, null);
        assertEquals("INDIVIDUAL",
                instance.nextStateOnEvent("selectIndividual", null).getName());
        assertEquals("FINANCIAL_EXPERIENCE",
                instance.nextStateOnEvent("saveProfile", null).getName());
        assertEquals("DISCLAIMER",
                instance.nextStateOnEvent("saveFinancialInfo", null).getName());
        assertEquals("PREVIEW",
                instance.nextStateOnEvent("acceptDisclaimer", null).getName());
        assertEquals("ESIGN", instance.nextStateOnEvent("acceptPreview", null)
                .getName());
        assertEquals(6, instance.getHistory().size());
        assertEquals(Pair.of(State.of("WELCOME"), null), instance.getHistory()
                .get(0));
        assertEquals(Pair.of(State.of("INDIVIDUAL"), "selectIndividual"),
                instance.getHistory().get(1));
        assertEquals(Pair.of(State.of("FINANCIAL_EXPERIENCE"), "saveProfile"),
                instance.getHistory().get(2));
        assertEquals(Pair.of(State.of("DISCLAIMER"), "saveFinancialInfo"),
                instance.getHistory().get(3));
        assertEquals(Pair.of(State.of("PREVIEW"), "acceptDisclaimer"), instance
                .getHistory().get(4));
        assertEquals(Pair.of(State.of("ESIGN"), "acceptPreview"), instance
                .getHistory().get(5));
    }

    @Test(expected = IllegalStateException.class)
    public void testDuplicateWithoutResolver() {
        TransitionMappings mappings = initSignupStates();

        FSM instance = new FSM(State.of("WELCOME"), mappings, null);
        assertEquals("INDIVIDUAL",
                instance.nextStateOnEvent("selectIndividual", null).getName());
        assertEquals("FINANCIAL_EXPERIENCE",
                instance.nextStateOnEvent("saveProfile", null).getName());
        assertEquals("DISCLAIMER",
                instance.nextStateOnEvent("saveFinancialInfo", null).getName());
        assertEquals("PREVIEW",
                instance.nextStateOnEvent("acceptDisclaimer", null).getName());
        assertEquals("ESIGN", instance.nextStateOnEvent("acceptPreview", null)
                .getName());
        assertEquals("SUBMITTED", instance.nextStateOnEvent("submitApp", null)
                .getName());
    }

    @Test
    public void testDuplicateWihResolver() {
        TransitionMappings mappings = initSignupStates();

        FSM instance = new FSM(State.of("WELCOME"), mappings,
                new TransitionResolver() {
                    @Override
                    public State nextState(State currentState, String onEvent,
                            State[] validStates, Object args) {
                        return validStates[0];
                    }
                });
        assertEquals("INDIVIDUAL",
                instance.nextStateOnEvent("selectIndividual", null).getName());
        assertEquals("FINANCIAL_EXPERIENCE",
                instance.nextStateOnEvent("saveProfile", null).getName());
        assertEquals("DISCLAIMER",
                instance.nextStateOnEvent("saveFinancialInfo", null).getName());
        assertEquals("PREVIEW",
                instance.nextStateOnEvent("acceptDisclaimer", null).getName());
        assertEquals("ESIGN", instance.nextStateOnEvent("acceptPreview", null)
                .getName());
        assertEquals("SUBMITTED", instance.nextStateOnEvent("submitApp", null)
                .getName());
    }

    @Test
    public void addRemoveStateChangeListeners() {
        TransitionMappings mappings = initSignupStates();

        FSM instance = new FSM(State.of("WELCOME"), mappings, null);
        final List<State> states = new ArrayList<>();
        final StateChangeListener l = new StateChangeListener() {
            @Override
            public void stateChanged(State fromState, State newState,
                    String onEvent) {
                states.add(fromState);
                states.add(newState);
            }
        };
        instance.addStateChangeListener(l);
        instance.nextStateOnEvent("selectIndividual", null).getName();
        assertEquals(2, states.size());
        assertEquals(State.of("WELCOME"), states.get(0));
        assertEquals(State.of("INDIVIDUAL"), states.get(1));
        //
        instance.removeStateChangeListener(l);
        states.clear();
        instance.nextStateOnEvent("saveProfile", null).getName();
        assertEquals(0, states.size());
    }

    private TransitionMappings initSignupStates() {
        final TransitionMappings mappings = new TransitionMappings();

        mappings.register(new TransitionMapping("WELCOME", "selectIndividual",
                "INDIVIDUAL"));
        mappings.register(new TransitionMapping("INDIVIDUAL", "saveProfile",
                "FINANCIAL_EXPERIENCE"));
        mappings.register(new TransitionMapping("FINANCIAL_EXPERIENCE",
                "saveFinancialInfo", "DISCLAIMER"));
        mappings.register(new TransitionMapping("DISCLAIMER",
                "acceptDisclaimer", "PREVIEW"));
        mappings.register(new TransitionMapping("PREVIEW", "acceptPreview",
                "ESIGN"));
        mappings.register(new TransitionMapping("ESIGN", "submitApp",
                "SUBMITTED", "CANCEL"));
        //
        mappings.register(new TransitionMapping("WELCOME", "selectOrg",
                "ORGANIZATION"));
        mappings.register(new TransitionMapping("ORGANIZATION", "saveProfile",
                "FINANCIAL_EXPERIENCE"));
        mappings.register(new TransitionMapping("FINANCIAL_EXPERIENCE",
                "selectOfficers", "OFFICERS"));
        mappings.register(new TransitionMapping("OFFICERS", "saveOfficers",
                "DISCLAIMER"));

        //
        mappings.register(new TransitionMapping("WELCOME", "selectIRA", "IRA"));
        mappings.register(new TransitionMapping("IRA", "saveProfile",
                "FINANCIAL_EXPERIENCE"));
        mappings.register(new TransitionMapping("FINANCIAL_EXPERIENCE",
                "selectBeneficiaries", "BENEFICIARIES"));
        mappings.register(new TransitionMapping("BENEFICIARIES",
                "saveBeneficiaries", "DISCLAIMER"));
        return mappings;
    }

    private TransitionMappings initAndroidStates() {
        final TransitionMappings mappings = new TransitionMappings();
        mappings.register(new TransitionMapping("Init", "onCreate", "Created"));
        mappings.register(new TransitionMapping("Created", "onStart", "Started"));
        mappings.register(new TransitionMapping("Started", "onResume",
                "Resumed"));
        mappings.register(new TransitionMapping("Resumed", "onPause", "Paused"));
        mappings.register(new TransitionMapping("Paused", "onResume", "Resumed"));
        mappings.register(new TransitionMapping("Paused", "onStop", "Stopped"));
        mappings.register(new TransitionMapping("Stopped", "onRestart",
                "Started"));
        mappings.register(new TransitionMapping("Stopped", "onDestroy",
                "Destroyed"));
        return mappings;
    }
}
