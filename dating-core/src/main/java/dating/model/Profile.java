package dating.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Immutable user profile. Interests are normalised to lower case and stored sorted,
 * so the profile behaves deterministically and compares by value.
 *
 * @param id         unique non-negative id
 * @param name       display name, not blank
 * @param age        age in years, at least {@link #MIN_AGE}
 * @param city       city, not blank
 * @param gender     gender
 * @param lookingFor genders this person is interested in
 * @param intent     what the person is looking for
 * @param interests  interests (normalised, sorted, unmodifiable)
 */
public record Profile(int id, String name, int age, String city, Gender gender,
                      Set<Gender> lookingFor, Intent intent, Set<String> interests) {

    /** Minimal allowed age. */
    public static final int MIN_AGE = 18;

    /** Validates and defensively copies the fields. */
    public Profile {
        if (id < 0) {
            throw new IllegalArgumentException("id must be non-negative");
        }
        requireText(name, "name");
        requireText(city, "city");
        if (age < MIN_AGE) {
            throw new IllegalArgumentException("age must be at least " + MIN_AGE + ", got " + age);
        }
        Objects.requireNonNull(gender, "gender");
        Objects.requireNonNull(intent, "intent");
        lookingFor = Collections.unmodifiableSortedSet(new TreeSet<>(lookingFor));
        interests = Collections.unmodifiableSortedSet(normalizeAll(interests));
    }

    /**
     * Normalises an interest name: trims and lower-cases it.
     *
     * @param interest raw interest
     * @return normalised interest
     */
    public static String normalize(String interest) {
        return interest.trim().toLowerCase(Locale.ROOT);
    }

    private static TreeSet<String> normalizeAll(Collection<String> raw) {
        return raw.stream().map(Profile::normalize).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link Profile}. */
    public static final class Builder {
        private int id;
        private String name;
        private int age;
        private String city;
        private Gender gender;
        private final Set<Gender> lookingFor = new TreeSet<>();
        private Intent intent = Intent.ANY;
        private final Set<String> interests = new TreeSet<>();

        private Builder() {
        }

        /**
         * @param id profile id
         * @return this builder
         */
        public Builder id(int id) {
            this.id = id;
            return this;
        }

        /**
         * @param name display name
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * @param age age in years
         * @return this builder
         */
        public Builder age(int age) {
            this.age = age;
            return this;
        }

        /**
         * @param city city
         * @return this builder
         */
        public Builder city(String city) {
            this.city = city;
            return this;
        }

        /**
         * @param gender gender
         * @return this builder
         */
        public Builder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        /**
         * @param genders genders the person is looking for
         * @return this builder
         */
        public Builder lookingFor(Gender... genders) {
            Collections.addAll(this.lookingFor, genders);
            return this;
        }

        /**
         * @param intent search intent
         * @return this builder
         */
        public Builder intent(Intent intent) {
            this.intent = intent;
            return this;
        }

        /**
         * @param interests interests to add
         * @return this builder
         */
        public Builder interests(String... interests) {
            Collections.addAll(this.interests, interests);
            return this;
        }

        /**
         * @return the validated profile
         * @throws IllegalArgumentException if a field is invalid
         */
        public Profile build() {
            return new Profile(id, name, age, city, gender, lookingFor, intent, interests);
        }
    }
}
