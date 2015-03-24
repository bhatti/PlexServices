package com.plexobject.metrics;

import java.io.Serializable;
import java.util.Random;

import com.plexobject.domain.Preconditions;

/**
 * This class selects random selection
 * 
 * @author shahzad bhatti
 */
public class Sampler implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Random rand;
    private final double threshold;

    /**
     * Creates a new sampler using the default system {@link Random}.
     * 
     * @param selectPercent
     *            Percentage to randomly select, must be between 0 and 100
     *            (inclusive).
     */
    public Sampler(float selectPercent) {
        this(selectPercent, new Random());
    }

    /**
     * Creates a new sampler using the provided {@link Random}.
     * 
     * @param selectPercent
     *            Percentage to randoml select, must be between 0 and 100
     *            (inclusive).
     * @param rand
     *            The random utility to use for generating random numbers.
     */
    public Sampler(float selectPercent, Random rand) {
        Preconditions.checkArgument((selectPercent >= 0)
                && (selectPercent <= 100), "Invalid selectPercent value: "
                + selectPercent);

        this.threshold = selectPercent / 100;
        this.rand = Preconditions.checkNotNull(rand, "random is null");
    }

    public boolean select() {
        return rand.nextDouble() < threshold;
    }

    @Override
    public String toString() {
        return "Sampler [" + select() + "]";
    }
    
}
