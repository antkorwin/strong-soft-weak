package com.antkorwin.strongsoftweak;

import com.antkorwin.commonutils.gc.GcUtils;
import com.antkorwin.commonutils.gc.LeakDetector;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * Created on 23.06.2018.
 *
 * @author Korovin Anatoliy
 */
public class PhantomReferenceTest {


    @Test
    public void testQueuePollAfterFinalizationGC() throws InterruptedException {
        // Arrange
        Foo foo = new Foo();
        ReferenceQueue<Foo> queue = new ReferenceQueue<>();
        PhantomReference<Foo> ref = new PhantomReference<>(foo, queue);

        // Act
        foo = null;
        int gcPass = GcUtils.fullFinalization();

        // Asserts
        Assertions.assertThat(ref.isEnqueued()).isTrue();
        Assertions.assertThat(gcPass).isLessThanOrEqualTo(3); //GcUtils
        Assertions.assertThat(queue.poll()).isEqualTo(ref);
    }

    @Test
    public void testWithoutFinalize() {
        // Arrange
        Object instance = new Object();
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        PhantomReference<Object> ref = new PhantomReference<>(instance, queue);

        // Act
        instance = null;

        GcUtils.fullFinalization();

        // Asserts
        Assertions.assertThat(ref.isEnqueued()).isTrue();
        Assertions.assertThat(queue.poll()).isEqualTo(ref);
    }


    @Test
    public void testWithoutLeaks() {
        // Arrange
        Foo foo = new Foo();
        LeakDetector leakDetector = new LeakDetector(foo);

        // Act
        foo = null;

        // Asserts
        leakDetector.assertMemoryLeaksNotExist();
    }

    @Test
    public void testWithLeak() {
        // Arrange
        Foo foo = new Foo();
        Foo bar = foo;
        LeakDetector leakDetector = new LeakDetector(foo);

        // Act
        foo = null;

        // Asserts
        leakDetector.assertMemoryLeaksExist();
    }


    private class Foo {
        @Override
        protected void finalize() throws Throwable {
            System.out.println("FIN!");
            super.finalize();
        }
    }
}
