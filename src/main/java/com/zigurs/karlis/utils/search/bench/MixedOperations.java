package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Threads(8)
@Fork(CommonParams.FORKS)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class MixedOperations {

    @Param({"0", "1024", "-1"})
    private int cacheSize;

    private QuickSearch<String> searchInstance;

    @Setup
    public void setup() {
        this.searchInstance = QuickSearch.builder()
                .withCacheLimit(cacheSize)
                .build();
    }

    @GroupThreads(1)
    @Benchmark
    public boolean addAndRemove(Blackhole blackhole) {
        blackhole.consume(searchInstance.addItem("item", "one two three"));
        searchInstance.removeItem("item");
        return true;
    }

    @GroupThreads(7)
    @Benchmark
    public boolean read() {
        return searchInstance.findItems("item", 10).isEmpty();
    }
}
