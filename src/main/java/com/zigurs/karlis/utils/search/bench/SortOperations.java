package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.PartialSorter;
import org.openjdk.jmh.annotations.*;

import java.util.*;
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

    private final Comparator<Map.Entry<String, Double>> normalReverseComparator = (o1, o2) -> -o1.getValue().compareTo(o2.getValue());
    private final Comparator<Map.Entry<String, Double>> eagerDiscardComparator = (o1, o2) -> o1.getValue().compareTo(o2.getValue()) < 0 ? 1 : -1;

    @State(Scope.Thread)
    public static class ListWrapper {

        private final List<Map.Entry<String, Double>> testList = new ArrayList<>();

        @Param({"100", "1000", "10000", "100000"})
        private int listSize;

        @Param({"1", "10", "1000"})
        private int maxItems;

        /*
         * Weird. The constructor isn't invoked?
         */


        /*
         * Re-shuffle the list for every benchmark call.
         *
         * Expensive, but fair.
         */
        @Setup(Level.Invocation)
        public void setup() {
            if (testList.isEmpty())
                populateList();
            Collections.shuffle(testList);
        }

        private void populateList() {
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
    }

    @Benchmark
    public boolean partialSort(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = PartialSorter.sortAndLimit(
                wrapper.testList,
                wrapper.maxItems,
                normalReverseComparator
        );

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean partialSortEagerDiscard(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = PartialSorter.sortAndLimit(
                wrapper.testList,
                wrapper.maxItems,
                eagerDiscardComparator
        );

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean collectionsSort(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList;
        Collections.sort(list, normalReverseComparator);

        list = list.subList(0, Math.min(list.size(), wrapper.maxItems));

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean collectionsSortEagerDiscard(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList;
        Collections.sort(list, eagerDiscardComparator);

        list = list.subList(0, Math.min(list.size(), wrapper.maxItems));

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean streamSort(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.stream()
                .sorted(normalReverseComparator)
                .limit(wrapper.maxItems)
                .collect(Collectors.toList());

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean streamSortEagerDiscard(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.stream()
                .sorted(eagerDiscardComparator)
                .limit(wrapper.maxItems)
                .collect(Collectors.toList());

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean parallelSort(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.parallelStream()
                .sorted(normalReverseComparator)
                .limit(wrapper.maxItems)
                .collect(Collectors.toList());

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean parallelSortEagerDiscard(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.parallelStream()
                .sorted(eagerDiscardComparator)
                .limit(wrapper.maxItems)
                .collect(Collectors.toList());

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }
}
