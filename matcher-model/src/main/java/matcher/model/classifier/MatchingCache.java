package matcher.model.classifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import matcher.model.type.Matchable;

public class MatchingCache {
	@SuppressWarnings("unchecked")
	public <T, U extends Matchable<U>> T get(CacheToken<T> token, U a, U b) {
		return (T) cache.get(new CacheKey<U>(token, a, b));
	}

	@SuppressWarnings("unchecked")
	public <T, U extends Matchable<U>> T compute(CacheToken<T> token, U a, U b, BiFunction<U, U, T> f) {
		return (T) cache.computeIfAbsent(new CacheKey<U>(token, a, b), k -> f.apply((U) k.a, (U) k.b));
	}

	@SuppressWarnings("unchecked")
	public <T, U extends Matchable<U>> T compute(CacheToken<T> token, U a, Function<U, T> f) {
		return (T) cache.computeIfAbsent(new CacheKey<U>(token, a, null), k -> f.apply((U) k.a));
	}

	public void clear() {
		// replacing the map is constant time, clearing it walks its whole (never shrinking) table
		cache = new ConcurrentHashMap<>();
	}

	public static final class CacheToken<t> { }

	private static class CacheKey<T extends Matchable<T>> {
		CacheKey(CacheToken<?> token, T a, T b) {
			this.token = token;
			this.a = a;
			this.b = b;
		}

		@Override
		public int hashCode() {
			return token.hashCode() ^ a.hashCode() ^ (b != null ? b.hashCode() : 0);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() != CacheKey.class) return false;

			CacheKey<?> o = (CacheKey<?>) obj;

			return token == o.token && a == o.a && b == o.b;
		}

		final CacheToken<?> token;
		final T a;
		final T b;
	}

	private volatile Map<CacheKey<?>, Object> cache = new ConcurrentHashMap<>();
}
