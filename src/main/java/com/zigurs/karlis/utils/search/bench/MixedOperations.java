package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = CommonParams.FORKS, jvmArgsAppend = {"-XX:+UseParallelGC", "-Xms4g", "-Xmx4g"})
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class MixedOperations {

    @Group("rw")
    @GroupThreads(1)
    @Benchmark
    public boolean addAndRemove(SearchWrapper wrapper, Blackhole blackhole) {
        blackhole.consume(wrapper.searchInstance.addItem("item", "one two three"));
        wrapper.searchInstance.removeItem("item");
        return true;
    }

    @Group("rw")
    @GroupThreads(1)
    @Benchmark
    public boolean read(SearchWrapper wrapper) {
        return wrapper.searchInstance.findItems("item", 10).isEmpty();
    }

    @State(Scope.Benchmark)
    public static class SearchWrapper {
        private QuickSearch<String> searchInstance;

        @Setup
        public void setup() {
            this.searchInstance = QuickSearch.builder().build();
        }
    }
}
