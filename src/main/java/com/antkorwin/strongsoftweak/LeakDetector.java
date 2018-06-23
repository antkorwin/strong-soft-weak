package com.antkorwin.strongsoftweak;

import org.assertj.core.api.Assertions;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * Created on 23.06.2018.
 *
 * @author Korovin Anatoliy
 */
public class LeakDetector extends PhantomReference<Object> {

    private final String description;

    public LeakDetector(Object referent) {
        super(referent, new ReferenceQueue<>());
        this.description = String.valueOf(referent);
    }

    public void assertLeakExists() {
        GcUtils.fullFinalizationPhantom();

        Assertions.assertThat(isEnqueued())
                  .as("Object: " + description + " was leaked")
                  .isTrue();
    }

}
