package com.antkorwin.strongsoftweak;

import org.assertj.core.api.Assertions;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * Created on 23.06.2018.
 *
 * Provides the ability to check memory leaks for a particular object.
 *
 * @author Korovin Anatoliy
 */
public class LeakDetector extends PhantomReference<Object> {

    private final String description;

    /**
     * Initialization of the memory leaks detector.
     * @param referent the object(resource) for which we are looking for leaks.
     */
    public LeakDetector(Object referent) {
        super(referent, new ReferenceQueue<>());
        this.description = String.valueOf(referent);
    }

    /**
     * You must run this method after delete all references on the checkable object(resource)
     * If exists memory leaks then throws a fail.
     */
    public void assertLeaksExist() {
        GcUtils.fullFinalization();

        Assertions.assertThat(isEnqueued())
                  .as("Object: " + description + " was leaked")
                  .isTrue();
    }

}
