package com.antkorwin.strongsoftweak;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

/**
 * Created on 23.06.2018.
 *
 * @author Korovin Anatoliy
 */
public class GcUtils {

    public static int fullFinalizationPhantom() {

        final CountDownLatch finalizerRan = new CountDownLatch(1);

        ReferenceQueue<? super Object> queue = new ReferenceQueue<>();
        PhantomReference<Object> ref =
                new PhantomReference<>(
                        new Object() {
                            @Override
                            protected void finalize() {
                                finalizerRan.countDown();
                            }
                        },
                        queue);

        int gcIterationCnt = awaitForLatchAndReference(finalizerRan, ref);

        await().atMost(1, SECONDS)
               .pollInterval(10, NANOSECONDS)
               .until(() -> queue.poll() != null);

        return gcIterationCnt;
    }

    private static int awaitForLatchAndReference(CountDownLatch latch, Reference<?> reference) {

        final long deadline = System.nanoTime() + SECONDS.toNanos(10L);
        boolean finalizationCalled = false;
        int gcIteration = 0;

        if (latch.getCount() == 0) {
            finalizationCalled = true;
        }

        while (System.nanoTime() - deadline < 0) {

            System.runFinalization();
            System.gc();
            gcIteration++;

            if (!finalizationCalled) {
                try {
                    finalizationCalled = latch.await(1L, SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (finalizationCalled && reference.isEnqueued()) {
                return gcIteration;
            }
        }

        throw new RuntimeException("Latch failed to count down by timeout");
    }

    public static int fullFinalizationWeak() {

        CountDownLatch finalizerLatch = new CountDownLatch(1);
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        WeakReference<Object> weakRef = new WeakReference<>(new Object(), queue);
        return awaitForLatchAndReference(finalizerLatch, weakRef);
    }
}
