package com.antkorwin.strongsoftweak;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

/**
 * Created on 23.06.2018.
 *
 * Utils to writing a tests which depends on behavior of the Garbage Collector.
 *
 * @author Korovin Anatoliy
 */
public class GcUtils {

    /**
     *
     * Runs garbage collector as many times as needed to release all
     * unused Phantom & Weak references and invocation of finalize methods.
     *
     * This method can be useful when you testing a something depended on GC.
     *
     * IMPLEMENTATION-NOTES:
     * This method creates a new PhantomReference on unreferenced object
     * and then runs a GC multiple times until this object is finalize.
     * Also, this method waits for enqueue the PhantomReference and expect
     * to get this reference by polling a queue. This sequence of actions
     * are not synchronous, therefore we need to wait in some places.
     *
     * @return a count of the applied GC iterations
     */
    public static int fullFinalization() {

        final CountDownLatch finalizerLatch = new CountDownLatch(1);

        ReferenceQueue<? super Object> queue = new ReferenceQueue<>();
        PhantomReference<Object> ref =
                new PhantomReference<>(
                        new Object() {
                            @Override
                            protected void finalize() {
                                finalizerLatch.countDown();
                            }
                        },
                        queue);

        int gcIterationCnt = awaitForLatchAndReference(finalizerLatch, ref);

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


    /**
     * This method tries to allocate maximum available memory in runtime,
     * and is catching an OutOfMemoryError.
     */
    public static void tryToAllocateAllAvailableMemory() {
        try {
            Object[] ignored = new Object[(int) Runtime.getRuntime().maxMemory()];
        } catch (OutOfMemoryError e) {
            System.out.println("exception: " + e.getMessage());
        }
    }

}
