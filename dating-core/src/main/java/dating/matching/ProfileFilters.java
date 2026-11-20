package dating.matching;

import dating.model.Gender;
import dating.model.Profile;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Ready-made profile predicates. They are plain {@link Predicate}s, so they can be
 * combined with {@code and}, {@code or} and {@code negate}.
 */
public final class ProfileFilters {
    private ProfileFilters() {
    }

    /**
     * @param min minimal age, inclusive
     * @param max maximal age, inclusive
     * @return predicate matching ages in {@code [min, max]}
     */
    public static Predicate<Profile> ageBetween(int min, int max) {
        return p -> p.age() >= min && p.age() <= max;
    }

    /**
     * @param city city name (case-insensitive)
     * @return predicate matching profiles living in the city
     */
    public static Predicate<Profile> inCity(String city) {
        return p -> p.city().equalsIgnoreCase(city);
    }

    /**
     * @param cities city names (case-insensitive)
     * @return predicate matching profiles living in any of the cities
     */
    public static Predicate<Profile> inAnyCity(Collection<String> cities) {
        return cities.stream().map(ProfileFilters::inCity).reduce(p -> false, Predicate::or);
    }

    /**
     * @param interest an interest (normalised before the check)
     * @return predicate matching profiles that have the interest
     */
    public static Predicate<Profile> hasInterest(String interest) {
        String normalized = Profile.normalize(interest);
        return p -> p.interests().contains(normalized);
    }

    /**
     * @param gender gender
     * @return predicate matching profiles of that gender
     */
    public static Predicate<Profile> ofGender(Gender gender) {
        return p -> p.gender() == gender;
    }

    /**
     * Mutual compatibility: each side is interested in the other's gender
     * and their search intents are compatible.
     *
     * @param me the searching profile
     * @return predicate matching profiles compatible with {@code me}
     */
    public static Predicate<Profile> compatibleWith(Profile me) {
        Set<Gender> mine = me.lookingFor();
        return p -> mine.contains(p.gender())
                && p.lookingFor().contains(me.gender())
                && me.intent().compatibleWith(p.intent());
    }
}
