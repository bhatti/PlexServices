package com.plexobject.fsm;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.plexobject.domain.Pair;

public class FSMTest {
    private final TransitionMappings mappings = new TransitionMappings();

    @Before
    public void setup() {
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
    }

    @Test
    public void testBasicStates() {
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
        assertEquals(6, instance.getBreadCrumbs().size());
        assertEquals(Pair.of(State.of("WELCOME"), null), instance
                .getBreadCrumbs().get(0));
        assertEquals(Pair.of(State.of("INDIVIDUAL"), "selectIndividual"),
                instance.getBreadCrumbs().get(1));
        assertEquals(Pair.of(State.of("FINANCIAL_EXPERIENCE"), "saveProfile"),
                instance.getBreadCrumbs().get(2));
        assertEquals(Pair.of(State.of("DISCLAIMER"), "saveFinancialInfo"),
                instance.getBreadCrumbs().get(3));
        assertEquals(Pair.of(State.of("PREVIEW"), "acceptDisclaimer"), instance
                .getBreadCrumbs().get(4));
        assertEquals(Pair.of(State.of("ESIGN"), "acceptPreview"), instance
                .getBreadCrumbs().get(5));
    }

    @Test(expected = IllegalStateException.class)
    public void testDuplicateWithoutResolver() {
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
}
