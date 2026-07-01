package org.prism.autowork.other.capability;


// TODO: Not a Capability yet. Make it capability when 0.4.2
public interface IHaveExperience {
    int getXp();
    int extractXp(int amount, boolean simulate);
    int putXp(int amount, boolean simulate);
}
