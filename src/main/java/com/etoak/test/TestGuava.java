package com.etoak.test;

import com.etoak.po.Book;
import com.etoak.po.Person;
import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.*;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.out;

/**
 * Getting Started with Google Guava
 * Created by xiao1 on 2017/7/20.
 */
public class TestGuava {

    public static void main(String[] args) {
//        joinerExec();
//        splitterExec();
//        charsetsExec();
//        stringsExec();
//        charMatcherExec();
//        preconditionsExec();
//        objectsExec();
//        functionExec();
//        fluentIterableExec();
//        listsExec();
//        setsExec();
//        mapsExec();
//        multimapExec();
//        biMapExec();
//        tableExec();
//        rangeExec();
//        immutableExec();
//        orderingExec();
//        monitorExec();
//        listenableFutureExec();
        futureCallbackExec();
    }

    static class FutureCallbackImpl implements FutureCallback<String> {
        private StringBuilder builder = new StringBuilder("---");

        @Override
        public void onSuccess(String result) {
            builder.append(result).append(" successfully");
        }

        @Override
        public void onFailure(Throwable t) {
            builder.append(t.toString());
        }

        private String getCallbackResult() {
            return builder.toString();
        }
    }

    /**
     * TODO note
     */
    private static void futureCallbackExec() {
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
        ListenableFuture<String> listenableFuture = executorService.submit(() -> {
            TimeUnit.SECONDS.sleep(1);
            return "Task completed";
        });
        FutureCallbackImpl futureCallback = new FutureCallbackImpl();
        Futures.addCallback(listenableFuture, futureCallback, executorService);
        System.out.println(futureCallback.getCallbackResult());

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }

    private static void listenableFutureExec() {
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
        ListenableFuture<String> listenableFuture = executorService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "finish";
        });

        listenableFuture.addListener(() -> {
            try {
                System.out.println("submit---" + listenableFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, executorService);
    }

    private static final int MAX_SIZE = 10;
    private List<String> list = new ArrayList<>();
    private Monitor monitor = new Monitor();
    private Monitor.Guard listBelowCapacity = new Monitor.Guard(monitor) {
        @Override
        public boolean isSatisfied() {
            return list.size() < MAX_SIZE;
        }
    };

    private void addToList(String item) throws InterruptedException {
        monitor.enterWhen(listBelowCapacity);
        try {
            list.add(item);
            System.out.println("添加元素[" + item + "]成功, 当前size=" + list.size());
        } finally {
            monitor.leave();
        }
    }

    private void addToListSkipWait(String item) throws InterruptedException {
        boolean ok = monitor.tryEnterIf(listBelowCapacity);
        System.out.println("Thread[" + Thread.currentThread().getName() + "] item=" + item + ", 获取令牌：ok=" + ok);
        if (!ok) {
            return;
        }

        try {
            list.add(item);
            System.out.println("添加元素[" + item + "]成功, 当前size=" + list.size());
        } finally {
            monitor.leave();
        }
    }

    private static void monitorExec() {
        final TestGuava testGuava = new TestGuava();
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                for (int j = 0; j < 6; j++) {
                    try {
//                        testGuava.addToList(j + "--->" + Thread.currentThread().getName());
                        testGuava.addToListSkipWait(j + "--->" + Thread.currentThread().getName());
                        TimeUnit.MILLISECONDS.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("-------------------");

        testGuava.list.forEach(out::println);
    }

    private static void orderingExec() {
        Person person = new Person("Moss", 28, "M");
        Person person1 = new Person("Jen", 24, "F");
        Person person2 = new Person("Roy", 26, "M");
        List<Person> personList = Lists.newArrayList(person, person1, person2);

        Function<Person, Comparable> ageFunction = new Function<Person, Comparable>() {
            @Override
            public Comparable apply(Person input) {
                return input.getAge();
            }
        };
        System.out.println("natural");
        Ordering.natural().onResultOf(ageFunction).sortedCopy(personList).forEach(out::println);
        System.out.println("-------------");
        Ordering.natural().onResultOf(ageFunction).reverse().sortedCopy(personList).forEach(out::println);
        System.out.println();

        System.out.println("usingToString");
        Ordering.usingToString().sortedCopy(personList).forEach(out::println);
        System.out.println("-------------");
        Ordering.usingToString().reverse().sortedCopy(personList).forEach(out::println);
        System.out.println();

        Comparator<Person> personComparator = new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return Ints.compare(o1.getAge(), o2.getAge());
            }
        };
        System.out.println("from");
        Ordering.from(personComparator).greatestOf(personList, 1).forEach(out::println);
        System.out.println("-------------");
        Ordering.from(personComparator).leastOf(personList, 1).forEach(out::println);
    }

    private static void immutableExec() {
        ImmutableListMultimap<Object, Object> immutableListMultimap = ImmutableListMultimap.builder()
                .put(1, "Foo").putAll(2, "Foo", "Bar", "Baz").putAll(4, "Huey", "Duey", "Luey").put(3, "Single").build();
        System.out.println(immutableListMultimap);
    }

    private static void rangeExec() {
        System.out.println("closed:" + Range.closed(1, 10));
        System.out.println("open:" + Range.open(1, 10));
        System.out.println("openClosed:" + Range.openClosed(1, 10));
        System.out.println("closedOpen:" + Range.closedOpen(1, 10));
        System.out.println("range:" + Range.range(1, BoundType.CLOSED, 10, BoundType.CLOSED));
        System.out.println("all:" + Range.all());
        System.out.println("atLeast:" + Range.atLeast(1));
        System.out.println("atMost:" + Range.atMost(10));
        System.out.println("greaterThan:" + Range.greaterThan(1));
        System.out.println("lessThan:" + Range.lessThan(10));
        System.out.println("downTo:" + Range.downTo(10, BoundType.OPEN));
        System.out.println("upTo:" + Range.upTo(1, BoundType.OPEN));
        System.out.println("singleton:" + Range.singleton(1));
        System.out.println("encloseAll:" + Range.encloseAll(ImmutableList.of(10, 1)));
        System.out.println();

        FluentIterable.from(ImmutableList.of(new Person("Moss", 28, "M"), new Person("Jen", 30, "F")))
                .filter(Predicates.compose(Range.closed(30, 50), new Function<Person, Integer>() {
                    @Override
                    public Integer apply(Person input) {
                        return input.getAge();
                    }
                })).forEach(out::println);
    }

    private static void tableExec() {
        HashBasedTable<Integer, Integer, String> hashBasedTable = HashBasedTable.create();
        hashBasedTable.put(1, 1, "Rook");
        hashBasedTable.put(1, 2, "Knight");
        hashBasedTable.put(1, 3, "Bishop");
        System.out.println(hashBasedTable.contains(1, 1));
        System.out.println(hashBasedTable.containsColumn(2));
        System.out.println(hashBasedTable.containsRow(1));
        System.out.println(hashBasedTable.containsValue("Rook"));
        System.out.println(hashBasedTable);
        hashBasedTable.remove(1, 3);
        System.out.println(hashBasedTable);
        System.out.println(hashBasedTable.get(3, 4));
        System.out.println();

        System.out.println(hashBasedTable.column(1));
        System.out.println(hashBasedTable.row(1));
        System.out.println();

        Set<Table.Cell<Integer, Integer, String>> cellSet = hashBasedTable.cellSet();
        System.out.println("cell");
        System.out.println(cellSet);
        cellSet.forEach(cell -> out.println(cell.getRowKey() + "---" + cell.getColumnKey() + "---" + cell.getValue()));
        System.out.println();

        Set<Integer> rowKeySet = hashBasedTable.rowKeySet();
        System.out.println("row");
        System.out.println(rowKeySet);
        Map<Integer, Map<Integer, String>> rowMap = hashBasedTable.rowMap();
        System.out.println(rowMap);
        System.out.println();

        Set<Integer> columnKeySet = hashBasedTable.columnKeySet();
        System.out.println("column");
        System.out.println(columnKeySet);
        Map<Integer, Map<Integer, String>> columnMap = hashBasedTable.columnMap();
        System.out.println(columnMap);
        System.out.println();

        ImmutableTable.Builder<Integer, Integer, String> builder = ImmutableTable.builder();
        ImmutableTable<Integer, Integer, String> immutableTable = builder.put(1, 1, "v1").put(1, 2, "v2").build();
        System.out.println("ImmutableTable");
        System.out.println(immutableTable);
        System.out.println();

        ImmutableList<Integer> rowKeys = ImmutableList.of(3, 2, 1);
        ImmutableList<Integer> columnKeys = ImmutableList.of(1, 3, 2, 4);
        ArrayTable<Integer, Integer, String> arrayTable = ArrayTable.create(rowKeys, columnKeys);
        rowKeys.forEach(rowKey -> columnKeys.forEach(columnKey -> arrayTable.put(rowKey, columnKey, Integer.toString(rowKey) + columnKey)));
        System.out.println("ArrayTable");
        System.out.println(arrayTable);
        arrayTable.cellSet().forEach(cell -> out.println(cell.getRowKey() + "---" + cell.getColumnKey() + "---" + cell.getValue()));
        System.out.println();

        arrayTable.eraseAll();
        ImmutableList<String> values = ImmutableList.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        for (int i = 0; i < rowKeys.size(); i++) {
            for (int j = 0; j < columnKeys.size(); j++) {
                arrayTable.put(rowKeys.get(i), columnKeys.get(j), values.get(i * columnKeys.size() + j));
            }
        }
        System.out.println("ArrayTable1");
        System.out.println(arrayTable);
        arrayTable.cellSet().forEach(cell -> out.println(cell.getRowKey() + "---" + cell.getColumnKey() + "---" + cell.getValue()));
        System.out.println();

        arrayTable.eraseAll();
        LinkedList<String> values1 = Lists.newLinkedList(values);
        for (Integer rowKey : rowKeys) {
            for (Integer columnKey : columnKeys) {
                arrayTable.put(rowKey, columnKey, values1.poll());
            }
        }
        System.out.println("ArrayTable2");
        System.out.println(arrayTable);
        arrayTable.cellSet().forEach(cell -> out.println(cell.getRowKey() + "---" + cell.getColumnKey() + "---" + cell.getValue()));
        System.out.println();

        arrayTable.eraseAll();
        values1.addAll(values);
        rowKeys.forEach(rowKey -> columnKeys.forEach(columnKey -> arrayTable.put(rowKey, columnKey, values1.poll())));
        System.out.println("ArrayTable3");
        System.out.println(arrayTable);
        arrayTable.cellSet().forEach(cell -> out.println(cell.getRowKey() + "---" + cell.getColumnKey() + "---" + cell.getValue()));
    }

    private static void biMapExec() {
        HashBiMap<String, String> hashBiMap = HashBiMap.create();
        hashBiMap.put("1", "Tom");
        hashBiMap.forcePut("2", "Tom");
        System.out.println(hashBiMap);

        HashBiMap<Integer, String> hashBiMap1 = HashBiMap.create();
        hashBiMap1.put(1, "Tom");
        hashBiMap1.put(2, "Harry");
        Assert.assertThat(hashBiMap1.get(1), CoreMatchers.is("Tom"));
        Assert.assertThat(hashBiMap1.get(2), CoreMatchers.is("Harry"));
        System.out.println(hashBiMap1);
        BiMap<String, Integer> inverse = hashBiMap1.inverse();
        Assert.assertThat(inverse.get("Tom"), CoreMatchers.is(1));
        Assert.assertThat(inverse.get("Harry"), CoreMatchers.is(2));
        System.out.println(inverse);
    }

    private static void multimapExec() {
        ArrayListMultimap<String, String> arrayListMultimap = ArrayListMultimap.create();
        arrayListMultimap.put("Foo", "1");
        arrayListMultimap.put("Foo", "2");
        arrayListMultimap.put("Foo", "3");
        Assert.assertEquals(arrayListMultimap.get("Foo"), Lists.newArrayList("1", "2", "3"));

        ArrayListMultimap<String, String> arrayListMultimap1 = ArrayListMultimap.create();
        arrayListMultimap1.put("Bar", "1");
        arrayListMultimap1.put("Bar", "2");
        arrayListMultimap1.put("Bar", "2");
        arrayListMultimap1.put("Bar", "3");
        arrayListMultimap1.put("Bar", "3");
        arrayListMultimap1.put("Bar", "3");
        ArrayList<String> list = Lists.newArrayList("1", "2", "2", "3", "3", "3");
        Assert.assertEquals(arrayListMultimap1.get("Bar"), list);

        ArrayListMultimap<String, String> arrayListMultimap2 = ArrayListMultimap.create();
        arrayListMultimap2.put("Foo", "1");
        arrayListMultimap2.put("Foo", "2");
        arrayListMultimap2.put("Foo", "3");
        arrayListMultimap2.put("Bar", "1");
        arrayListMultimap2.put("Bar", "2");
        arrayListMultimap2.put("Bar", "3");
        System.out.println(arrayListMultimap2.size());
        System.out.println(arrayListMultimap2.values());
        Map<String, Collection<String>> asMap = arrayListMultimap2.asMap();
        System.out.println(asMap);
        System.out.println();

        HashMultimap<String, String> hashMultimap = HashMultimap.create();
        hashMultimap.put("Foo", "1");
        hashMultimap.put("Foo", "2");
        hashMultimap.put("Bar", "1");
        hashMultimap.put("Bar", "2");
        hashMultimap.put("Bar", "2");
        hashMultimap.put("Bar", "3");
        hashMultimap.put("Bar", "3");
        hashMultimap.put("Bar", "3");
        System.out.println(hashMultimap);
        LinkedHashMultimap<Object, Object> linkedHashMultimap = LinkedHashMultimap.create();
        linkedHashMultimap.put("Foo", "1");
        linkedHashMultimap.put("Foo", "2");
        linkedHashMultimap.put("Bar", "1");
        linkedHashMultimap.put("Bar", "2");
        linkedHashMultimap.put("Bar", "2");
        linkedHashMultimap.put("Bar", "3");
        linkedHashMultimap.put("Bar", "3");
        linkedHashMultimap.put("Bar", "3");
        System.out.println(linkedHashMultimap);
        System.out.println();

        TreeMultimap<Integer, String> treeMultimap = TreeMultimap.create();
        treeMultimap.put(3, "3");
        treeMultimap.put(3, "1");
        treeMultimap.put(2, "1");
        treeMultimap.put(3, "2");
        treeMultimap.put(1, "1");
        treeMultimap.put(2, "2");
        System.out.println(treeMultimap);
        System.out.println();

        System.out.println(ImmutableMultimap.of("k1", "v1", "k1", "v1"));
        System.out.println(ImmutableListMultimap.of("k1", "v1", "k1", "v1"));
        System.out.println(ImmutableSetMultimap.of("k1", "v1", "k1", "v1"));
    }

    private static void mapsExec() {
        Set<Book> books = ImmutableSet.of(new Book("10"), new Book("11"), new Book("12"), new Book("13"), new Book("14"));
        books.forEach(out::println);
        System.out.println();

        Map<String, Book> bookMap = new LinkedHashMap<>();
        for (Book book : books) {
            bookMap.put(book.getIsbn(), book);
        }
        System.out.println(bookMap.getClass());
        bookMap.forEach((k, v) -> out.println(k + "---" + v));
        System.out.println();

        Map<String, Book> bookMap1 = Maps.uniqueIndex(books, new Function<Book, String>() {
            @Override
            public String apply(Book input) {
                return input.getIsbn();
            }
        });
        System.out.println(bookMap1.getClass());
        bookMap1.forEach((k, v) -> out.println(k + "---" + v));
        System.out.println();

        Map<Book, String> bookMap2 = Maps.asMap(books, new Function<Book, String>() {
            @Override
            public String apply(Book input) {
                return input.getIsbn();
            }
        });
        System.out.println(bookMap2.getClass());
        bookMap2.forEach((k, v) -> out.println(k + "---" + v));
        System.out.println();

        Map<Book, String> bookMap3 = Maps.toMap(books, new Function<Book, String>() {
            @Override
            public String apply(Book input) {
                return input.getIsbn();
            }
        });
        System.out.println(bookMap3.getClass());
        bookMap3.forEach((k, v) -> out.println(k + "---" + v));
        System.out.println();

        bookMap.forEach((s, book) -> book.setPublisher("publisher" + s));
        Map<String, String> transformEntries = Maps.transformEntries(bookMap, new Maps.EntryTransformer<String, Book, String>() {
            @Override
            public String transformEntry(String key, Book value) {
                return value.getPublisher();
            }
        });
        System.out.println("transformEntries");
        transformEntries.forEach((k, v) -> out.println(k + "---" + v));
        System.out.println();

        Map<String, String> transformValues = Maps.transformValues(bookMap, new Function<Book, String>() {
            @Override
            public String apply(Book input) {
                return input.getPublisher();
            }
        });
        System.out.println("transformValues");
        transformValues.forEach((k, v) -> out.println(k + "---" + v));
    }

    private static void setsExec() {
        Set<String> s1 = Sets.newHashSet("1", "2", "3");
        Set<String> s2 = Sets.newHashSet("2", "3", "4");
        System.out.println("s1=" + s1);
        System.out.println("s2=" + s2);

        Sets.SetView<String> difference = Sets.difference(s1, s2);
        System.out.println("difference---" + difference);

        Sets.SetView<String> symmetricDifference = Sets.symmetricDifference(s1, s2);
        System.out.println("symmetricDifference---" + symmetricDifference);

        Sets.SetView<String> intersection = Sets.intersection(s1, s2);
        System.out.println("intersection---" + intersection);

        Sets.SetView<String> union = Sets.union(s1, s2);
        System.out.println("union---" + union);
    }

    private static void listsExec() {
        Person person = new Person("Moss");
        Person person1 = new Person("Jen");
        Person person2 = new Person("Roy");
        Person person3 = new Person("Denholm");
        Person person4 = new Person("Richmond");
        List<Person> personList = Lists.newArrayList(person, person1, person2, person3, person4);
        System.out.println(personList);
        List<List<Person>> partition = Lists.partition(personList, 2);
        System.out.println(partition.size());
        partition.forEach(out::println);
        System.out.println(partition);
    }

    private static void fluentIterableExec() {
        Person person1 = new Person("Wilma", 30, "F");
        Person person2 = new Person("Fred", 32, "M");
        Person person3 = new Person("Betty", 31, "F");
        Person person4 = new Person("Barney", 33, "M");
        List<Person> personList = Lists.newArrayList(person1, person2, person3, person4);
        Iterable<Person> personsFilteredByAge = FluentIterable.from(personList).filter(new Predicate<Person>() {
            @Override
            public boolean apply(Person input) {
                return input.getAge() > 31;
            }
        });

        Assert.assertThat(Iterables.contains(personsFilteredByAge, person2), CoreMatchers.is(true));
        Assert.assertThat(Iterables.contains(personsFilteredByAge, person4), CoreMatchers.is(true));
        Assert.assertThat(Iterables.contains(personsFilteredByAge, person1), CoreMatchers.is(false));
        Assert.assertThat(Iterables.contains(personsFilteredByAge, person3), CoreMatchers.is(false));
        personsFilteredByAge.forEach(out::println);


        List<String> transformedPersonList = FluentIterable.from(personList).transform(new Function<Person, String>() {
            @Override
            public String apply(Person input) {
                return Joiner.on('#').join(input.getName(), input.getAge());
            }
        }).toList();

        Assert.assertThat(transformedPersonList.get(1), CoreMatchers.is("Fred#32"));
        transformedPersonList.forEach(out::println);
    }

    private static void functionExec() {
        Function<Date, String> function = new Function<Date, String>() {
            @Override
            public String apply(Date input) {
                return new SimpleDateFormat("dd/mm/yyyy").format(input);
            }
        };
        System.out.println(function.apply(new Date()));
    }

    private static void objectsExec() {
        Book book = new Book(new Person("name"), "title", "publisher", "isbn", 12.11);
        System.out.println(book.equals(new Book(new Person("name"), "title", "publisher", "isbn", 12.1)));
        System.out.println(book.hashCode());
        System.out.println(book.compareTo(new Book(new Person("name"), "title", "publisher", "isbn", 12.1)));

        System.out.println();

        System.out.println(book.equals(new Book(new Person("name2"), "title", "publisher", "isbn", 12.11)));
        System.out.println(book.hashCode());
        System.out.println(book.compareTo(new Book(new Person("name2"), "title", "publisher", "isbn", 12.11)));

        System.out.println();

        System.out.println(book.equals(new Book(new Person("name"), "title", "publisher", "isbn", 12.11)));
        System.out.println(book.hashCode());
        System.out.println(book.compareTo(new Book(new Person("name"), "title", "publisher", "isbn", 12.11)));
    }

    private static void preconditionsExec() {
        boolean condition = false;
        try {
            Preconditions.checkArgument(condition, "1", "2", "3");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Preconditions.checkState(condition, "1", "2", "3");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String str = null;
        try {
            Preconditions.checkNotNull(str, "1", "2", "3");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Preconditions.checkElementIndex(2, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Preconditions.checkPositionIndexes(0, 2, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void charMatcherExec() {
        String tabsAndSpaces = "String  with    spaces    and  tabs";
        System.out.println(CharMatcher.whitespace().collapseFrom(tabsAndSpaces, ' '));

        tabsAndSpaces = "    String with   spaces    and   tabs";
        System.out.println(CharMatcher.whitespace().trimAndCollapseFrom(tabsAndSpaces, ' '));

        String lettersAndNumbers = "\"foo989yxbar234\"";
        System.out.println(CharMatcher.javaDigit().retainFrom(lettersAndNumbers));

        lettersAndNumbers = "foo989 xbar 234";
        System.out.println(CharMatcher.javaDigit().or(CharMatcher.whitespace()).retainFrom(lettersAndNumbers));
    }

    private static void stringsExec() {
        StringBuilder builder = new StringBuilder("foo");
        char c = 'x';
        for (int i = 0; i < 3; i++) {
            builder.append(c);
        }
        System.out.println(builder);
        System.out.println(Strings.padEnd("foo", 6, 'x'));
        for (int i = 0; i < 10000; i++) {
            String str = Integer.toString(i);
            System.out.println(Strings.padStart(str, 5, '0'));
        }

        System.out.println(Strings.repeat("0", 10));
        System.out.println(Strings.isNullOrEmpty(null));
        System.out.println(Strings.isNullOrEmpty(""));
        System.out.println(Strings.isNullOrEmpty("isNullOrEmpty"));
        System.out.println(Strings.nullToEmpty(null));
        System.out.println(Strings.nullToEmpty(""));
        System.out.println(Strings.nullToEmpty("nullToEmpty"));
        System.out.println(Strings.emptyToNull(null));
        System.out.println(Strings.emptyToNull(""));
        System.out.println(Strings.emptyToNull("emptyToNull"));
    }

    private static void charsetsExec() {
        System.out.println(Charsets.ISO_8859_1);
        System.out.println(Charsets.US_ASCII);
        System.out.println(Charsets.UTF_8);
        System.out.println(Charsets.UTF_16);
        System.out.println(Charsets.UTF_16BE);
        System.out.println(Charsets.UTF_16LE);
    }

    private static void splitterExec() {
        Splitter on = Splitter.on("#");
        for (String str : on.trimResults().split("1 #2#3 #4")) {
            System.out.println(str);
        }

        for (String str : on.split("1#   2#   3#")) {
            System.out.println(str);
        }

        String startString = "Washington D.C=Redskins#New York City=Giants#Philadelphia=Eagles#Dallas=Cowboys";
        Map<String, String> testMap = Maps.newLinkedHashMap();
        testMap.put("Washington D.C", "Redskins");
        testMap.put("New York City", "Giants");
        testMap.put("Philadelphia", "Eagles");
        testMap.put("Dallas", "Cowboys");
        Map<String, String> splitMap = Splitter.on("#").withKeyValueSeparator("=").split(startString);
        System.out.println(testMap);
        System.out.println(splitMap);
        Assert.assertThat(testMap, CoreMatchers.is(splitMap));
    }

    private static void joinerExec() {
        String delimiter = "|";
        List<Integer> list = Lists.newArrayList(1, 2, 3, null, 4, 5);

        noJoiner(list, delimiter);
        joiner(list, delimiter);
    }

    private static void joiner(List<Integer> list, String delimiter) {
        System.out.println(Joiner.on(delimiter).skipNulls().join(list));
        System.out.println(Joiner.on(delimiter).skipNulls().appendTo(new StringBuilder(), list));
        System.out.println(Joiner.on(delimiter).useForNull("missing").join(list));

        try (FileWriter fileWriter = new FileWriter(new File(("D://file/date.txt")))) {
            List<LocalDate> dateList = getDates();
            Joiner.on("#").useForNull(" ").appendTo(fileWriter, dateList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String joinMap = Joiner.on("&").withKeyValueSeparator("=").join(ImmutableMap.of("name", "sheldon", "age", "23", "sex", "S"));
        System.out.println(joinMap);

        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("name", "penny");
        map.put("age", "25");
        map.put("addr", null);
        map.put(null, "M");
        map.put(null, null);
        System.out.println(Joiner.on("&").useForNull("missing").withKeyValueSeparator("=").join(map));
    }

    private static List<LocalDate> getDates() {
        LocalDate today = LocalDate.now();
        List<LocalDate> dateList = Lists.newArrayList();
        for (int i = 0; i < 3; i++) {
            dateList.add(today.plusDays(i));
            dateList.add(null);
        }
        System.out.println(dateList);
        return dateList;
    }

    private static void noJoiner(List<Integer> list, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (Integer i : list) {
            if (i != null) {
                builder.append(i).append(delimiter);
            }
        }
        builder.setLength(builder.length() - delimiter.length());
        System.out.println(builder);
    }
}

