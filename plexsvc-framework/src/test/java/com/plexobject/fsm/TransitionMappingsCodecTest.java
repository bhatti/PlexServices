package com.plexobject.fsm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TransitionMappingsCodecTest {
    @Test
    public void testEncodeDecode() {
        TransitionMappings original = new TransitionMappings();
        original.register(new TransitionMapping("WELCOME",
                "selectIndividual", "INDIVIDUAL"));
        original.register(new TransitionMapping("INDIVIDUAL", "saveProfile",
                "FINANCIAL_EXPERIENCE"));
        original.register(new TransitionMapping("FINANCIAL_EXPERIENCE",
                "saveFinancialInfo", "DISCLAIMER"));
        original.register(new TransitionMapping("DISCLAIMER",
                "acceptDisclaimer", "PREVIEW"));
        original.register(new TransitionMapping("PREVIEW", "acceptPreview",
                "ESIGN"));
        original.register(new TransitionMapping("ESIGN", "submitApp",
                "SUBMITTED"));
        //
        original.register(new TransitionMapping("WELCOME", "selectOrg",
                "ORGANIZATION"));
        original.register(new TransitionMapping("ORGANIZATION",
                "saveProfile", "FINANCIAL_EXPERIENCE"));
        original.register(new TransitionMapping("FINANCIAL_EXPERIENCE",
                "selectOfficers", "OFFICERS"));
        original.register(new TransitionMapping("OFFICERS", "saveOfficers",
                "DISCLAIMER"));
        original.register(new TransitionMapping("DISCLAIMER",
                "acceptDisclaimer", "PREVIEW"));
        original.register(new TransitionMapping("PREVIEW", "acceptPreview",
                "ESIGN"));
        original.register(new TransitionMapping("ESIGN", "submitApp",
                "SUBMITTED"));

        //
        original.register(new TransitionMapping("WELCOME", "selectIRA",
                "IRA"));
        original.register(new TransitionMapping("IRA", "saveProfile",
                "FINANCIAL_EXPERIENCE"));
        original.register(new TransitionMapping("FINANCIAL_EXPERIENCE",
                "selectBeneficiaries", "BENEFICIARIES"));
        original.register(new TransitionMapping("BENEFICIARIES",
                "saveBeneficiaries", "DISCLAIMER"));
        original.register(new TransitionMapping("DISCLAIMER",
                "acceptDisclaimer", "PREVIEW"));
        original.register(new TransitionMapping("PREVIEW", "acceptPreview",
                "ESIGN"));
        original.register(new TransitionMapping("ESIGN", "submitApp",
                "SUBMITTED"));
        TransitionMappings copy = TransitionMappingsCodec.decode(TransitionMappingsCodec.encode(original));
        assertEquals(original.getTransitions().size(), copy.getTransitions().size());
        for (TransitionMapping m : original.getTransitions()) {
            assertTrue(copy.getTransitions().contains(m));
        }
    }
}
