package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 * <p>
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     * <p>
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        SieveActorActor startActor = new SieveActorActor(2);

        finish(() -> {
            for (int i = 3; i <= limit; i += 2) {
                startActor.send(i);
            }
        });

        SieveActorActor nextActor = startActor;
        int primesCount = 0;

        while (nextActor != null) {
            primesCount += nextActor.primeCount;
            nextActor = nextActor.nextActor;
        }

       // System.out.println(String.format("CountPrimes, limit: %d, primesCount: %d", limit, primesCount));

        return primesCount;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        private static final int MAX_PRIME = 2000;
        private SieveActorActor nextActor;
        private int[] locPrimes = new int[MAX_PRIME];
        private int primeCount = 1;

        public SieveActorActor(int value) {
            this.locPrimes[0] = value;
        }

        /**
         * Process a single message sent to this actor.
         * <p>
         * TODO complete this method.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            final Integer candidate = (Integer) msg;

            if (candidate > 0) {
                boolean isPrime = isPrime(candidate);

                if (isPrime) {
                    if (candidate < MAX_PRIME) {
                        locPrimes[primeCount] = candidate;
                        primeCount++;
                    } else if (nextActor != null) {
                        nextActor.send(candidate);
                    } else {
                        nextActor = new SieveActorActor(candidate);
                    }
                }
            } else {
                if (nextActor != null) {
                    nextActor.send(candidate);
                }
            }
        }

        private boolean isPrime(Integer val) {
            for (int i = 0; i < primeCount; i++) {
                if (val % locPrimes[i] == 0) {
                    return false;
                }
            }

            return true;
        }
    }
}
