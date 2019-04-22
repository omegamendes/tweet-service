package com.example.tweetservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class TweetServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TweetServiceApplication.class, args);
	}

}

@Component
class TweetRunner implements ApplicationRunner {

	private final TweetRepository tweetRepository;

	TweetRunner(TweetRepository tweetRepository) {
		this.tweetRepository = tweetRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		Author viktor = new Author("@viktorlang");
		Author jboner = new Author("@jboner");
		Author starbuxman = new Author("@starbuxman");



		Flux<Tweet> tweets = Flux.just(
			new Tweet("woot, @Konrad will be talking #enterprise #integration done right!!", viktor),
			new Tweet("woot, @lore ipsul lorensloht!!", viktor),
			new Tweet("woot, This is so cool #akka", viktor),
			new Tweet("jbooner, Cross data and replication #akka", jboner),
			new Tweet("aaaa, @im on #spring team", starbuxman),
			new Tweet("Whatever happens dont let go of my #hand \n #fail", starbuxman)
		);

		this.tweetRepository.deleteAll()
				.thenMany(tweets.flatMap(tweetRepository::save))
				.thenMany(tweetRepository.findAll())
//				.subscribeOn(Schedulers.fromExecutor(Executors.newSingleThreadExecutor()))
				.subscribe(System.out::println);
	}
}

@Component
interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class Tweet {

	@Id
	private String text;

	private Author author;

	private Set<HashTag> hashTags;

	Tweet(String t, Author a) {
		this.text = t;
		this.author = a;
		this.hashTags = Stream.of(this.text.split(" "))
				.filter(x -> x.startsWith("#"))
				.map(x -> new HashTag(x.replaceAll("[^#\\w]", "")))
				.collect(Collectors.toSet());
	}
}


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class Author {

	@Id
	private String handle;
}


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class HashTag {

	@Id
	private String tag;
}