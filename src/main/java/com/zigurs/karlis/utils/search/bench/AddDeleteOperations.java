package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Threads(8)
@Fork(CommonParams.FORKS)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class AddDeleteOperations {

    @State(Scope.Benchmark)
    public static class SearchWrapper {
        private QuickSearch<String> searchInstance;

        private int counter;

        @Setup
        public void setup() {
            this.searchInstance = QuickSearch.builder().build();
        }
    }

    @Benchmark
    public void addAndRemove(SearchWrapper wrapper) {
        String item = String.format("Item-%d", wrapper.counter);
        String keywords = String.format("cat%d dog%d", wrapper.counter, wrapper.counter++);

        if (!wrapper.searchInstance.addItem(item, keywords))
            throw new IllegalStateException("Couldn't add item");

        // Purposefully create new String instance
        wrapper.searchInstance.removeItem(new String(item));
    }
}
