package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.collections.ImmutableSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = CommonParams.FORKS, jvmArgsAppend = {"-XX:+UseParallelGC", "-Xms4g", "-Xmx4g"})
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class SetOperations {

    @State(Scope.Benchmark)
    public static class SetWrapper {

        public enum WhichOne {HASH, IMMUTABLE}

        public HashSet<String> hashSet;
        public ImmutableSet<String> immutableSet;

        @Param({"HASH", "IMMUTABLE"})
        public WhichOne targetInstance;

        @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
        public int targetSize;

        public Random random;

        @Setup
        public void setup() {
            random = new Random();
            hashSet = createHashSet(targetSize);
            immutableSet = ImmutableSet.fromCollection(hashSet);
        }

        private HashSet<String> createHashSet(int targetSize) {
            HashSet<String> set = new HashSet<>(targetSize);
            for (int i = 0; i < targetSize; i++)
                set.add(String.format("Item-%d", i));
            return set;
        }

        public boolean contains() {
            switch (targetInstance) {
                case HASH:
                    return hashSet.contains(randomItem());
                case IMMUTABLE:
                    return immutableSet.contains(randomItem());
            }
            throw new IllegalStateException("uhm");
        }

        public boolean remove() {
            switch (targetInstance) {
                case HASH:
                    return hashSet.remove(randomItem());
                case IMMUTABLE:
                    immutableSet.createInstanceByRemoving(randomItem());
                    return true;
            }
            throw new IllegalStateException("uhm");
        }

        public boolean add() {
            switch (targetInstance) {
                case HASH:
                    return hashSet.add(randomItem());
                case IMMUTABLE:
                    immutableSet = immutableSet.createInstanceByAdding(randomItem());
                    return true;
            }
            throw new IllegalStateException("uhm");
        }

        private String randomItem() {
            /* 50/50 present or missing */
            return String.format("Item-%d", random.nextInt(targetSize * 2));
        }
    }

    @Threads(1)
    @Benchmark
    public boolean contains_st(SetWrapper wrapper) {
        return wrapper.contains();
    }

    @Threads(8)
    @Benchmark
    public boolean contains_mt(SetWrapper wrapper) {
        return wrapper.contains();
    }

    @Threads(1)
    @Benchmark
    public boolean add_st(SetWrapper wrapper) {
        return wrapper.add();
    }

    @Threads(8)
    @Benchmark
    public boolean add_mt(SetWrapper wrapper) {
        return wrapper.add();
    }

    @Threads(1)
    @Benchmark
    public boolean remove_st(SetWrapper wrapper) {
        return wrapper.remove();
    }

    @Threads(8)
    @Benchmark
    public boolean remove_mt(SetWrapper wrapper) {
        return wrapper.remove();
    }
}
