package com.erzhiqianyi.reactor;

import com.erzhiqianyi.reactor.domain.User;
import com.erzhiqianyi.reactor.domain.VipUser;
import com.erzhiqianyi.reactor.repository.ReactiveRepository;
import com.erzhiqianyi.reactor.repository.ReactiveUserRepository;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Part02TransformTest {

    private Part02Transform part02Transform;
    private ReactiveRepository reactiveRepository;

    @Before

    public void init() {
        part02Transform = new Part02Transform();
        reactiveRepository = new ReactiveUserRepository();
    }

    @Test
    public void capitalizeOne() {
        Mono<User> userMono = reactiveRepository.findFirst();
        Mono<User> mono = part02Transform.capitalizeOne(userMono).log();
        StepVerifier.create(mono)
                .expectNext(new User("SWHITE", "SKYLER", "WHITE"))
                .verifyComplete();
    }

    @Test
    public void capitalizeMany() {
        Flux<User> flux = reactiveRepository.findAll();
        StepVerifier.create(part02Transform.capitalizeMany(flux).log())
                .expectNext(
                        new User("SWHITE", "SKYLER", "WHITE"),
                        new User("JPINKMAN", "JESSE", "PINKMAN"),
                        new User("WWHITE", "WALTER", "WHITE"),
                        new User("SGOODMAN", "SAUL", "GOODMAN"))
                .verifyComplete();
    }

    @Test
    public void castOne() {
        VipUser vipUser = new VipUser("SWHITE", "SKYLER", "WHITE", 1);
        Mono<VipUser> mono = Mono.just(vipUser).log();
        User user = vipUser;
        StepVerifier.create(part02Transform.castOne(mono))
                .expectNext(user)
                .verifyComplete();
    }


    @Test
    public void castMany() {
        VipUser vipUser = new VipUser("SWHITE", "SKYLER", "WHITE", 1);
        Flux<VipUser> flux = Flux.just(vipUser).log();
        User user = vipUser;
        StepVerifier.create(part02Transform.castMany(flux))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    public void index() {
        Flux<String> flux = Flux.just("one", "two", "three");
        Flux<Tuple2<Long, String>> tuple2Flux = part02Transform.index(flux).log();
        StepVerifier.create(tuple2Flux.map(Tuple2::getT1))
                .expectNext(0l, 1l, 2l)
                .verifyComplete();

        tuple2Flux.subscribe(item -> {
            System.out.println(item.getT1());
            System.out.println(item.getT2());
        });
    }

    @Test
    public void monoFlatMap() {
        String str = "one";
        Mono<String> mono = Mono.just(str);
        StepVerifier.create(part02Transform.monoFlatMap(mono))
                .expectNext(str.length())
                .verifyComplete();
    }

    @Test
    public void fluxFlatMap() {
        String str = "one";
        Flux<String> flux = Flux.just(str);
        StepVerifier.create(part02Transform.fluxFlatMap(flux).log())
                .expectNext(str.length())
                .verifyComplete();
    }

    @Test
    public void flatMapString() {
        String str = "one";
        Flux<String> flux = part02Transform.flatMapString(str).log();
        StepVerifier.create(flux)
                .expectNext("o", "n", "e")
                .verifyComplete();

    }

    @Test
    public void flatMapHandle() {
        String str = "one";
        Flux<String> flux = part02Transform.flatMapHandle(Flux.just(str)).log();
        StepVerifier.create(flux)
                .expectNext("result:" + str)
                .verifyComplete();
    }

    @Test
    public void flatMapEmpty() {
        Flux<String> flux = part02Transform.flatMapEmpty(
                Flux.just("one", "two", "three", "four")).log();
        StepVerifier.create(flux)
                .expectNext("three", "four")
                .verifyComplete();
    }

    @Test
    public void flatMapSequential() {
        Flux<String> flux = part02Transform.flatMapSequential(
                Flux.just("one_one", "two_four", "three", "four", "one", "two")).log();
        StepVerifier.create(flux)
                .expectNext("one_one", "two_four", "three", "four", "one", "two")
                .verifyComplete();
    }

    @Test
    public void flatMapMany() {
        Flux<String> flux = part02Transform.flatMapMany(Mono.just("one")).log();
        StepVerifier.create(flux)
                .expectNext("o", "n", "e")
                .verifyComplete();
    }

    @Test
    public void startWith() {
        Flux<String> flux = Flux.just("two", "three");
        Flux<String> startFlux = part02Transform.startWith(flux, "one").log();
        StepVerifier.create(startFlux)
                .expectNext("one", "two", "three")
                .verifyComplete();
    }

    @Test
    public void concatWith() {
        Flux<String> flux = Flux.just("one", "two");
        Flux<String> other = Flux.just("three", "four");
        Flux<String> concat = part02Transform.concatWith(flux, other).log();
        StepVerifier.create(concat)
                .expectNext("one", "two", "three", "four")
                .verifyComplete();
    }

    @Test
    public void collectList() {
        Flux<String> flux = Flux.just("one", "two", "three", "four");
        Mono<List<String>> mono = part02Transform.collectList(flux).log();
        List<String> list = Arrays.asList("one", "two", "three", "four");
        StepVerifier.create(mono)
                .expectNext(list)
                .verifyComplete();
    }

    @Test
    public void collectSortedList() {
        Flux<String> flux = Flux.just("one", "two", "three", "four");
        Mono<List<String>> mono = part02Transform
                .collectSortedList(flux, Comparator.comparing(String::length))
                .log();
        List<String> list = Arrays.asList("one", "two", "four", "three");
        StepVerifier.create(mono)
                .expectNext(list)
                .verifyComplete();
    }

    @Test
    public void collectMap() {
        Flux<String> flux = Flux.just("one", "three", "four");
        Mono<Map<Integer, String>> mono = part02Transform.collectMap(flux).log();
        Map<Integer, String> map = Stream.of("one", "three", "four")
                .collect(Collectors.toMap(String::length, item -> item));
        StepVerifier.create(mono)
                .expectNext(map)
                .verifyComplete();
    }

    @Test
    public void collectMultimap() {
        List<String> list = Arrays.asList("one", "two", "three", "four", "five", "six", "seven");
        Mono<Map<Integer, Collection<String>>> mono = part02Transform.collectMultimap(Flux.fromIterable(list)).log();
        Map<Integer, Collection<String>> map = list.stream()
                .collect(Collectors.groupingBy(String::length, Collectors.toCollection(ArrayList::new)));
        StepVerifier.create(mono)
                .expectNext(map)
                .verifyComplete();

    }

    @Test
    public void count() {
        List<String> list = Arrays.asList("one", "two", "three", "four", "five", "six", "seven");
        Mono<Long> mono = part02Transform.count(Flux.fromIterable(list));
        StepVerifier.create(mono)
                .expectNext(Long.valueOf(list.size()))
                .verifyComplete();
    }

    @Test
    public void all() {
        List<String> list = Arrays.asList("one", "two", "three", "four", "five", "six", "seven");
        boolean allMatch = list.stream().allMatch(s -> s.length() > 2);
        Mono<Boolean> mono = part02Transform.all(Flux.fromIterable(list), s -> s.length() > 2);
        StepVerifier.create(mono)
                .expectNext(allMatch)
                .verifyComplete();
    }

    @Test
    public void any() {
        List<String> list = Arrays.asList("one", "two", "three", "four", "five", "six", "seven");
        Flux<String> flux = Flux.fromIterable(list);
        Mono<Boolean> mono = part02Transform.any(flux, s -> s.length() > 4).log();
        StepVerifier.create(mono)
                .expectNext(true)
                .verifyComplete();
        mono = part02Transform.any(flux, s -> s.length() > 10).log();
        StepVerifier.create(mono)
                .expectNext(false)
                .verifyComplete();

    }

    @Test
    public void hasElements() {
        List<String> list = Arrays.asList("one", "two", "three", "four", "five", "six", "seven");
        Mono<Boolean> mono = part02Transform.hasElements(Flux.fromIterable(list));
        StepVerifier.create(mono)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void hasElement() {
        List<String> list = Arrays.asList("one", "two", "three", "four", "five", "six", "seven");
        Flux<String> flux = Flux.fromIterable(list);
        Mono<Boolean> mono = part02Transform.hasElement(flux, "one").log();
        StepVerifier.create(mono)
                .expectNext(true)
                .verifyComplete();
        mono = part02Transform.hasElement(flux, "nine").log();
        StepVerifier.create(mono)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    public void concat() {
        List<String> list = Arrays.asList("one", "two", "three");
        List<String> another = Arrays.asList("four", "five", "six", "seven");
        Flux<String> flux = Flux.fromIterable(list);
        Flux<String> concatFlux = part02Transform.concat(flux, another).log();

        StepVerifier.create(concatFlux)
                .expectNext("one", "two", "three", "four", "five", "six", "seven")
                .verifyComplete();
    }

    @Test
    public void concatDelayError() {
        List<String> list = Arrays.asList("one", "two", "three");
        List<String> another = Arrays.asList("four", "five", "six", "seven");
        Flux<String> flux = Flux.fromIterable(list);
        Flux<String> concatFlux = part02Transform.concatDelayError(flux, another).log();
        StepVerifier.create(concatFlux)
                .expectNext("one", "two", "three", "four", "five", "six", "seven")
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    public void mergeSequential() {
        String[] array = {"one", "three", "four",};
        Flux<String> mergeFlux = part02Transform.mergeSequential(array).log();
        StepVerifier.create(mergeFlux)
                .expectNext("one", "three", "four")
                .expectComplete()
                .verify();
    }

    @Test
    public void merge() {
        String[] array = {"one", "three", "four",};
        Flux<String> mergeFlux = part02Transform.merge(array).log();
        StepVerifier.create(mergeFlux)
                .expectNext("one", "four", "three")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeWith() {
        Flux<String> flux = Flux.just("zero");
        String[] array = {"one", "three", "four",};
        Flux<String> mergeFlux = part02Transform.mergeWith(flux, array).log();
        StepVerifier.create(mergeFlux)
                .expectNext("zero", "one", "four", "three")
                .expectComplete()
                .verify();
    }

    @Test
    public void zip() {
        Flux<Integer> flux = Flux.just(1, 2, 3, 4);
        Flux<String> anotherFlux = Flux.just("one", "two", "three", "four");
        Flux<Tuple2<Integer, String>> tuple2Flux = part02Transform.zip(flux, anotherFlux).log();
        StepVerifier.create(tuple2Flux.map(Tuple2::toString))
                .expectNext("[1,one]", "[2,two]", "[3,three]", "[4,four]")
                .expectComplete()
                .verify();
    }


    @Test
    public void zipWith() {
        Flux<Integer> flux = Flux.just(1, 2, 3, 4);
        Flux<String> anotherFlux = Flux.just("one", "two", "three", "four");
        Flux<Tuple2<Integer, String>> tuple2Flux = part02Transform.zipWith(flux, anotherFlux).log();
        StepVerifier.create(tuple2Flux.map(Tuple2::toString))
                .expectNext("[1,one]", "[2,two]", "[3,three]", "[4,four]")
                .expectComplete()
                .verify();
    }

    @Test
    public void monoZip() {
        Mono<String> one = Mono.just("one");
        Mono<Integer> two = Mono.just(1);
        Mono<Tuple2<Integer, String>> tuple2Mono = part02Transform.monoZip(two, one).log();
        StepVerifier.create(tuple2Mono.map(Tuple2::toString))
                .expectNext("[1,one]")
                .expectComplete()
                .verify();
    }

    @Test
    public void monoZipWith() {
        Mono<String> one = Mono.just("one");
        Mono<Integer> two = Mono.just(1);
        Mono<Tuple2<Integer, String>> tuple2Mono = part02Transform.monoZipWith(two, one).log();
        StepVerifier.create(tuple2Mono.map(Tuple2::toString))
                .expectNext("[1,one]")
                .expectComplete()
                .verify();
    }

    @Test
    public void and() {
        Mono<String> one = Mono.just("one");
        Mono<Integer> two = Mono.just(1);
        Mono<Void> mono = part02Transform.and(two, one).log();
        StepVerifier.create(mono)
                .expectComplete()
                .verify();
    }

    @Test
    public void when() {
        Mono<String> one = Mono.just("one");
        Mono<String> two = Mono.just("two");
        Mono<Void> mono = part02Transform.when(two, one).log();
        StepVerifier.create(mono)
                .expectComplete()
                .verify();
    }

    @Test
    public void combineLatest() {
        Flux<String> one = Flux.just("A", "B", "C", "D");
        Flux<String> another = Flux.just("B", "E");
        Flux<String> flux = part02Transform.combineLatest(one, another);
        flux.subscribe(System.out::println);
    }

    @Test
    public void firstFlux() {
        Flux<String> one = Flux.just("A", "B", "C", "D");
        Flux<String> another = Flux.just("B", "E");
        Flux<String> flux = part02Transform.firstFlux(one, another).log();
        StepVerifier.create(flux)
                .expectNext("A", "B", "C", "D")
                .verifyComplete();
        flux = part02Transform.firstFlux(another, one).log();
        StepVerifier.create(flux)
                .expectNext("B", "E")
                .verifyComplete();

    }


    @Test
    public void firstMono() {
        Mono<String> one = Mono.just("one");
        Mono<String> another = Mono.just("another");
        Mono<String> mono = part02Transform.firstMono(one, another).log();
        StepVerifier.create(mono)
                .expectNext("one")
                .verifyComplete();

        mono = part02Transform.firstMono(another, one).log();
        StepVerifier.create(mono)
                .expectNext("another")
                .verifyComplete();
    }

    @Test
    public void orFlux() {
        Flux<String> one = Flux.just("A", "B", "C", "D");
        Flux<String> another = Flux.just("B", "E");
        Flux<String> flux = part02Transform.orFlux(one, another).log();
        StepVerifier.create(flux)
                .expectNext("A", "B", "C", "D")
                .verifyComplete();
        flux = part02Transform.orFlux(another, Flux.empty()).or(one).log();
        StepVerifier.create(flux)
                .expectNext("B", "E")
                .verifyComplete();
    }

    @Test
    public void orMono() {
        Mono<String> one = Mono.just("one");
        Mono<String> two = Mono.just("two");
        Mono<String> another = Mono.delay(Duration.ofSeconds(2)).flatMap(c -> two);
        Mono<String> flux = part02Transform.orMono(one, another).log();
        StepVerifier.create(flux)
                .expectNext("one")
                .verifyComplete();
        flux = part02Transform.orMono(another, one).log();
        StepVerifier.create(flux)
                .expectNext("one")
                .verifyComplete();
    }

    @Test
    public void switchMap() {
        Flux<String> flux = Flux.just("one","two","three","four","five","six");
        flux = part02Transform.switchMap(flux);
        flux.subscribe(System.out::println);
    }

    @Test
    public void  switchOnNext(){

    }
}

