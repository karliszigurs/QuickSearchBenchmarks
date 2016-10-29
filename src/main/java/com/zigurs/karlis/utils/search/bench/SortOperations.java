package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.PartialSorter;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Threads(1)
@Fork(CommonParams.FORKS)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class SortOperations {

    private List<Map.Entry<String, Double>> testList = new ArrayList<>();

    @Param({"100", "1000", "10000", "100000"})
    private int listSize;

    @Param({"1", "10", "1000"})
    private int maxItems;

    @Setup
    public void setup() {
        for (int i = 1; i <= listSize; i++) {
            final String key = String.format("Item-%d", i);
            final Double value = (double) i;

            testList.add(new Map.Entry<String, Double>() {
                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public Double getValue() {
                    return value;
                }

                @Override
                public Double setValue(Double value) {
                    throw new UnsupportedOperationException("Not allowed");
                }
            });
        }
    }

    @Benchmark
    public boolean partialSort() {
        List<Map.Entry<String, Double>> list = PartialSorter.sortAndLimit(getListCopy(), maxItems, Map.Entry.comparingByValue());

        if (list.get(0).getValue() != 1.0)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean collectionsSort() {
        List<Map.Entry<String, Double>> list = getListCopy();
        Collections.sort(list, Map.Entry.comparingByValue());

        list = list.subList(0, Math.min(list.size(), maxItems));

        if (list.get(0).getValue() != 1.0)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean streamSort() {
        List<Map.Entry<String, Double>> list = getListCopy().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(maxItems)
                .collect(Collectors.toList());

        if (list.get(0).getValue() != 1.0)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean parallelSort() {
        List<Map.Entry<String, Double>> list = getListCopy().parallelStream()
                .sorted(Map.Entry.comparingByValue())
                .limit(maxItems)
                .collect(Collectors.toList());

        if (list.get(0).getValue() != 1.0)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean listCopyCost() {
        return getListCopy().stream().findFirst().isPresent();
    }

    private List<Map.Entry<String, Double>> getListCopy() {
        List<Map.Entry<String, Double>> listCopy = new ArrayList<>(testList);
        Collections.shuffle(listCopy);
        return listCopy;
    }
}
