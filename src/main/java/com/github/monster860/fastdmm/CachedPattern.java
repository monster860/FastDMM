package com.github.monster860.fastdmm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple wrapper to lazily compile a Pattern
 */
public class CachedPattern {

    private String regexp;
    private Pattern cachedPattern;
    private int flags;

    /**
     * Constructs a new cached regex pattern.
     *
     * @param regexp The regex.
     */
    public CachedPattern(String regexp) {
        this.regexp = regexp;
    }
    
    public CachedPattern() {}

    /**
     * Switches the ignoreCase setting on the matcher.
     *
     * @param setting The new setting.
     * @return The pattern it applies to.
     */
    public CachedPattern setIgnoreCase(boolean setting) {
        int newflags = flags;
        if (setting) {
            newflags |= Pattern.CASE_INSENSITIVE;
        } else {
            newflags &= ~Pattern.CASE_INSENSITIVE;
        }
        if (newflags != flags) {
            flags = newflags;
            cachedPattern = null;
        }
        return this;
    }

    /**
     * Returns the Java Regex Pattern object that has been cached.
     *
     * @return The Pattern.
     */
    public Pattern getPattern() {
        if (cachedPattern == null) {
            cachedPattern = Pattern.compile(regexp, flags);
        }
        return cachedPattern;
    }

    /**
     * Returns the stored regex.
     *
     * @return the regexp
     */
    public String getRegexp() {
        return regexp;
    }

    /**
     * Obtains the matcher for this pattern.
     *
     * @param input the input to match to.
     * @return the matcher.
     */
    public Matcher getMatcher(CharSequence input) {
        return getPattern().matcher(input);
    }

}
