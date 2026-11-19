package dating.model;

/** What a person is looking for on the platform. */
public enum Intent {
    FRIENDSHIP, DATING, ANY;

    /**
     * @param other another intent
     * @return {@code true} if either side is {@link #ANY} or both are equal
     */
    public boolean compatibleWith(Intent other) {
        return this == ANY || other == ANY || this == other;
    }
}
