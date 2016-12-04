package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = CommonParams.FORKS, jvmArgsAppend = {"-XX:+UseParallelGC", "-Xms4g", "-Xmx4g"})
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class AddDeleteOperations {

    @State(Scope.Benchmark)
    public static class SearchWrapper {
        private QuickSearch<String> searchInstance;

        // Intentionally unsafe in multithread scenario
        private int counter;

        @Setup
        public void setup() {
            searchInstance = QuickSearch.builder().build();
        }
    }

    @Threads(1)
    @Benchmark
    public void single(SearchWrapper wrapper) {
        String item = String.format("Item-%d", wrapper.counter);
        String keywords = String.format("cat%d dog%d", wrapper.counter, wrapper.counter++);

        if (!wrapper.searchInstance.addItem(item, keywords))
            throw new IllegalStateException("Couldn't add item");

        // Purposefully create new String instance
        wrapper.searchInstance.removeItem(new String(item));
    }

    @Threads(8)
    @Benchmark
    public void multi(SearchWrapper wrapper) {
        String item = String.format("Item-%d", wrapper.counter);
        String keywords = String.format("cat%d dog%d", wrapper.counter, wrapper.counter++);

        if (!wrapper.searchInstance.addItem(item, keywords))
            throw new IllegalStateException("Couldn't add item");

        // Purposefully create new String instance
        wrapper.searchInstance.removeItem(new String(item));
    }
}
