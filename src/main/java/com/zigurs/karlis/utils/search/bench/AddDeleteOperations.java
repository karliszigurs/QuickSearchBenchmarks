package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Threads(8)
@Fork(CommonParams.FORKS)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class AddDeleteOperations {

    private QuickSearch<String> searchInstance;

    private int counter;

    @Setup
    public void setup() {
        this.searchInstance = QuickSearch.builder().build();
    }

    @Benchmark
    public void addAndRemove() {
        String item = String.format("Item%d", counter);
        String keywords = String.format("cat%d dog%d", counter, counter++);

        if (!searchInstance.addItem(item, keywords))
            throw new IllegalStateException("Couldn't add item");

        searchInstance.removeItem(new String(item)); // Purposefully create new String instance
    }
}
