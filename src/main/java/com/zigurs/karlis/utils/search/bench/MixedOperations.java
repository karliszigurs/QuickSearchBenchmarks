package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Fork(CommonParams.FORKS)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class MixedOperations {

    @State(Scope.Benchmark)
    public static class SearchWrapper {
        @Param({"0", "1024", "-1"})
        private int cacheSize;

        private QuickSearch<String> searchInstance;

        @Setup
        public void setup() {
            this.searchInstance = QuickSearch.builder()
                    .withCacheLimit(cacheSize)
                    .build();
        }
    }

    @Group("w")
    @GroupThreads(1)
    @Benchmark
    public boolean addAndRemove(SearchWrapper wrapper, Blackhole blackhole) {
        blackhole.consume(wrapper.searchInstance.addItem("item", "one two three"));
        wrapper.searchInstance.removeItem("item");
        return true;
    }

    @Group("w")
    @GroupThreads(7)
    @Benchmark
    public boolean read(SearchWrapper wrapper) {
        return wrapper.searchInstance.findItems("item", 10).isEmpty();
    }
}
