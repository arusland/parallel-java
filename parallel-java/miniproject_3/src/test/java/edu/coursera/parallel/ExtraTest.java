package edu.coursera.parallel;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

/**
 * @author Ruslan Absalyamov
 * @since 2017-11-01
 */
public class ExtraTest extends TestCase {
    public void test2() {
        ExecutorService service = Executors.newCachedThreadPool();
        int n = 10;
        int nsteps = 300;
        Random rnd = new Random();
        double[] newX0 = new double[n];
        double[] oldX0 = new double[n];
        for (int i = 0; i < oldX0.length; i++) {
            oldX0[i] = rnd.nextDouble();
        }

        Phaser[] ph = new Phaser[n + 2];
        for (int i = 0; i < ph.length; i++) {
            ph[i] = new Phaser(1);
        }

        for (int i = 0; i < n; i++) {
            int index = i;
            service.submit(() -> {
                double[] newX = newX0;
                double[] oldX = oldX0;

                for (int iter = 0; iter < nsteps; iter++) {
                    newX[index] = (oldX[index - 1] + oldX[index + 1]) / 2;
                    ph[index].arrive();
                    if (index > 1) ph[index - 1].awaitAdvance(iter);
                    if (index < n - 1) ph[index + 1].awaitAdvance(iter);
                    double[] tmp = newX;
                    newX = oldX;
                    oldX = tmp;
                }
            });
        }

        System.out.println("newX" + Arrays.toString(newX0));
        System.out.println("oldX" + Arrays.toString(oldX0));
    }

    public void testPhaser() throws InterruptedException {
        // initialize phaser ph	for use by n tasks ("parties")
        int n = 10;
        ExecutorService service = Executors.newCachedThreadPool();
        Phaser ph = new Phaser(n);

        for (int i = 0; i < n; i++) {
            int index = i;
            service.submit(() -> {
                System.out.println(String.format("HELLO %d", index));
                int phase = ph.arrive();

                System.out.println(String.format("MIDLE %d", index));

                String myId = lookup(index); // convert int to a string

                ph.awaitAdvance(phase);
                System.out.println(String.format("BYE %d, myId %s, phase %d", index, myId, phase));
            });
        }

        System.out.println("Waiting....");

        Thread.sleep(3000);
    }

    private String lookup(int i) {
        return "X" + i;
    }
}
