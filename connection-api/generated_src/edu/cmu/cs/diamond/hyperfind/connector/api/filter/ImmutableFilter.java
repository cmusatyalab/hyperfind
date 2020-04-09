package edu.cmu.cs.diamond.hyperfind.connector.api.filter;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import edu.cmu.cs.diamond.hyperfind.connector.api.Filter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link Filter}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableFilter.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableFilter.of()}.
 */
@Generated(from = "Filter", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableFilter implements Filter {
  private final byte[] code;
  private final ImmutableList<String> dependencies;
  private final ImmutableList<String> arguments;
  private final String name;
  private final double minScore;
  private final double maxScore;
  private final byte[] blob;

  private ImmutableFilter(
      byte[] code,
      Iterable<String> dependencies,
      Iterable<String> arguments,
      String name,
      double minScore,
      double maxScore,
      byte[] blob) {
    this.code = code.clone();
    this.dependencies = ImmutableList.copyOf(dependencies);
    this.arguments = ImmutableList.copyOf(arguments);
    this.name = Objects.requireNonNull(name, "name");
    this.minScore = minScore;
    this.maxScore = maxScore;
    this.blob = blob.clone();
  }

  private ImmutableFilter(
      ImmutableFilter original,
      byte[] code,
      ImmutableList<String> dependencies,
      ImmutableList<String> arguments,
      String name,
      double minScore,
      double maxScore,
      byte[] blob) {
    this.code = code;
    this.dependencies = dependencies;
    this.arguments = arguments;
    this.name = name;
    this.minScore = minScore;
    this.maxScore = maxScore;
    this.blob = blob;
  }

  /**
   * @return A cloned {@code code} array
   */
  @Override
  public byte[] code() {
    return code.clone();
  }

  /**
   * @return The value of the {@code dependencies} attribute
   */
  @Override
  public ImmutableList<String> dependencies() {
    return dependencies;
  }

  /**
   * @return The value of the {@code arguments} attribute
   */
  @Override
  public ImmutableList<String> arguments() {
    return arguments;
  }

  /**
   * @return The value of the {@code name} attribute
   */
  @Override
  public String name() {
    return name;
  }

  /**
   * @return The value of the {@code minScore} attribute
   */
  @Override
  public double minScore() {
    return minScore;
  }

  /**
   * @return The value of the {@code maxScore} attribute
   */
  @Override
  public double maxScore() {
    return maxScore;
  }

  /**
   * @return A cloned {@code blob} array
   */
  @Override
  public byte[] blob() {
    return blob.clone();
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link Filter#code() code}.
   * The array is cloned before being saved as attribute values.
   * @param elements The non-null elements for code
   * @return A modified copy of {@code this} object
   */
  public final ImmutableFilter withCode(byte... elements) {
    byte[] newValue = elements.clone();
    return new ImmutableFilter(
        this,
        newValue,
        this.dependencies,
        this.arguments,
        this.name,
        this.minScore,
        this.maxScore,
        this.blob);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link Filter#dependencies() dependencies}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableFilter withDependencies(String... elements) {
    ImmutableList<String> newValue = ImmutableList.copyOf(elements);
    return new ImmutableFilter(this, this.code, newValue, this.arguments, this.name, this.minScore, this.maxScore, this.blob);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link Filter#dependencies() dependencies}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of dependencies elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableFilter withDependencies(Iterable<String> elements) {
    if (this.dependencies == elements) return this;
    ImmutableList<String> newValue = ImmutableList.copyOf(elements);
    return new ImmutableFilter(this, this.code, newValue, this.arguments, this.name, this.minScore, this.maxScore, this.blob);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link Filter#arguments() arguments}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableFilter withArguments(String... elements) {
    ImmutableList<String> newValue = ImmutableList.copyOf(elements);
    return new ImmutableFilter(
        this,
        this.code,
        this.dependencies,
        newValue,
        this.name,
        this.minScore,
        this.maxScore,
        this.blob);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link Filter#arguments() arguments}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of arguments elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableFilter withArguments(Iterable<String> elements) {
    if (this.arguments == elements) return this;
    ImmutableList<String> newValue = ImmutableList.copyOf(elements);
    return new ImmutableFilter(
        this,
        this.code,
        this.dependencies,
        newValue,
        this.name,
        this.minScore,
        this.maxScore,
        this.blob);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Filter#name() name} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for name
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableFilter withName(String value) {
    String newValue = Objects.requireNonNull(value, "name");
    if (this.name.equals(newValue)) return this;
    return new ImmutableFilter(
        this,
        this.code,
        this.dependencies,
        this.arguments,
        newValue,
        this.minScore,
        this.maxScore,
        this.blob);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Filter#minScore() minScore} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for minScore
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableFilter withMinScore(double value) {
    if (Double.doubleToLongBits(this.minScore) == Double.doubleToLongBits(value)) return this;
    return new ImmutableFilter(this, this.code, this.dependencies, this.arguments, this.name, value, this.maxScore, this.blob);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Filter#maxScore() maxScore} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for maxScore
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableFilter withMaxScore(double value) {
    if (Double.doubleToLongBits(this.maxScore) == Double.doubleToLongBits(value)) return this;
    return new ImmutableFilter(this, this.code, this.dependencies, this.arguments, this.name, this.minScore, value, this.blob);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link Filter#blob() blob}.
   * The array is cloned before being saved as attribute values.
   * @param elements The non-null elements for blob
   * @return A modified copy of {@code this} object
   */
  public final ImmutableFilter withBlob(byte... elements) {
    byte[] newValue = elements.clone();
    return new ImmutableFilter(
        this,
        this.code,
        this.dependencies,
        this.arguments,
        this.name,
        this.minScore,
        this.maxScore,
        newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableFilter} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableFilter
        && equalTo((ImmutableFilter) another);
  }

  private boolean equalTo(ImmutableFilter another) {
    return Arrays.equals(code, another.code)
        && dependencies.equals(another.dependencies)
        && arguments.equals(another.arguments)
        && name.equals(another.name)
        && Double.doubleToLongBits(minScore) == Double.doubleToLongBits(another.minScore)
        && Double.doubleToLongBits(maxScore) == Double.doubleToLongBits(another.maxScore)
        && Arrays.equals(blob, another.blob);
  }

  /**
   * Computes a hash code from attributes: {@code code}, {@code dependencies}, {@code arguments}, {@code name}, {@code minScore}, {@code maxScore}, {@code blob}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + Arrays.hashCode(code);
    h += (h << 5) + dependencies.hashCode();
    h += (h << 5) + arguments.hashCode();
    h += (h << 5) + name.hashCode();
    h += (h << 5) + Doubles.hashCode(minScore);
    h += (h << 5) + Doubles.hashCode(maxScore);
    h += (h << 5) + Arrays.hashCode(blob);
    return h;
  }

  /**
   * Prints the immutable value {@code Filter} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Filter")
        .omitNullValues()
        .add("code", Arrays.toString(code))
        .add("dependencies", dependencies)
        .add("arguments", arguments)
        .add("name", name)
        .add("minScore", minScore)
        .add("maxScore", maxScore)
        .add("blob", Arrays.toString(blob))
        .toString();
  }

  /**
   * Construct a new immutable {@code Filter} instance.
   * @param code The value for the {@code code} attribute
   * @param dependencies The value for the {@code dependencies} attribute
   * @param arguments The value for the {@code arguments} attribute
   * @param name The value for the {@code name} attribute
   * @param minScore The value for the {@code minScore} attribute
   * @param maxScore The value for the {@code maxScore} attribute
   * @param blob The value for the {@code blob} attribute
   * @return An immutable Filter instance
   */
  public static ImmutableFilter of(byte[] code, List<String> dependencies, List<String> arguments, String name, double minScore, double maxScore, byte[] blob) {
    return of(code, (Iterable<String>) dependencies, (Iterable<String>) arguments, name, minScore, maxScore, blob);
  }

  /**
   * Construct a new immutable {@code Filter} instance.
   * @param code The value for the {@code code} attribute
   * @param dependencies The value for the {@code dependencies} attribute
   * @param arguments The value for the {@code arguments} attribute
   * @param name The value for the {@code name} attribute
   * @param minScore The value for the {@code minScore} attribute
   * @param maxScore The value for the {@code maxScore} attribute
   * @param blob The value for the {@code blob} attribute
   * @return An immutable Filter instance
   */
  public static ImmutableFilter of(byte[] code, Iterable<String> dependencies, Iterable<String> arguments, String name, double minScore, double maxScore, byte[] blob) {
    return new ImmutableFilter(code, dependencies, arguments, name, minScore, maxScore, blob);
  }

  /**
   * Creates an immutable copy of a {@link Filter} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable Filter instance
   */
  public static ImmutableFilter copyOf(Filter instance) {
    if (instance instanceof ImmutableFilter) {
      return (ImmutableFilter) instance;
    }
    return ImmutableFilter.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableFilter ImmutableFilter}.
   * <pre>
   * ImmutableFilter.builder()
   *    .code(byte) // required {@link Filter#code() code}
   *    .addDependencies|addAllDependencies(String) // {@link Filter#dependencies() dependencies} elements
   *    .addArguments|addAllArguments(String) // {@link Filter#arguments() arguments} elements
   *    .name(String) // required {@link Filter#name() name}
   *    .minScore(double) // required {@link Filter#minScore() minScore}
   *    .maxScore(double) // required {@link Filter#maxScore() maxScore}
   *    .blob(byte) // required {@link Filter#blob() blob}
   *    .build();
   * </pre>
   * @return A new ImmutableFilter builder
   */
  public static ImmutableFilter.Builder builder() {
    return new ImmutableFilter.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableFilter ImmutableFilter}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "Filter", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_CODE = 0x1L;
    private static final long INIT_BIT_NAME = 0x2L;
    private static final long INIT_BIT_MIN_SCORE = 0x4L;
    private static final long INIT_BIT_MAX_SCORE = 0x8L;
    private static final long INIT_BIT_BLOB = 0x10L;
    private long initBits = 0x1fL;

    private @Nullable byte[] code;
    private ImmutableList.Builder<String> dependencies = ImmutableList.builder();
    private ImmutableList.Builder<String> arguments = ImmutableList.builder();
    private @Nullable String name;
    private double minScore;
    private double maxScore;
    private @Nullable byte[] blob;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code Filter} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(Filter instance) {
      Objects.requireNonNull(instance, "instance");
      code(instance.code());
      addAllDependencies(instance.dependencies());
      addAllArguments(instance.arguments());
      name(instance.name());
      minScore(instance.minScore());
      maxScore(instance.maxScore());
      blob(instance.blob());
      return this;
    }

    /**
     * Initializes the value for the {@link Filter#code() code} attribute.
     * @param code The elements for code
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder code(byte... code) {
      this.code = code.clone();
      initBits &= ~INIT_BIT_CODE;
      return this;
    }

    /**
     * Adds one element to {@link Filter#dependencies() dependencies} list.
     * @param element A dependencies element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addDependencies(String element) {
      this.dependencies.add(element);
      return this;
    }

    /**
     * Adds elements to {@link Filter#dependencies() dependencies} list.
     * @param elements An array of dependencies elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addDependencies(String... elements) {
      this.dependencies.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link Filter#dependencies() dependencies} list.
     * @param elements An iterable of dependencies elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder dependencies(Iterable<String> elements) {
      this.dependencies = ImmutableList.builder();
      return addAllDependencies(elements);
    }

    /**
     * Adds elements to {@link Filter#dependencies() dependencies} list.
     * @param elements An iterable of dependencies elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllDependencies(Iterable<String> elements) {
      this.dependencies.addAll(elements);
      return this;
    }

    /**
     * Adds one element to {@link Filter#arguments() arguments} list.
     * @param element A arguments element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addArguments(String element) {
      this.arguments.add(element);
      return this;
    }

    /**
     * Adds elements to {@link Filter#arguments() arguments} list.
     * @param elements An array of arguments elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addArguments(String... elements) {
      this.arguments.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link Filter#arguments() arguments} list.
     * @param elements An iterable of arguments elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder arguments(Iterable<String> elements) {
      this.arguments = ImmutableList.builder();
      return addAllArguments(elements);
    }

    /**
     * Adds elements to {@link Filter#arguments() arguments} list.
     * @param elements An iterable of arguments elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllArguments(Iterable<String> elements) {
      this.arguments.addAll(elements);
      return this;
    }

    /**
     * Initializes the value for the {@link Filter#name() name} attribute.
     * @param name The value for name 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      initBits &= ~INIT_BIT_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link Filter#minScore() minScore} attribute.
     * @param minScore The value for minScore 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder minScore(double minScore) {
      this.minScore = minScore;
      initBits &= ~INIT_BIT_MIN_SCORE;
      return this;
    }

    /**
     * Initializes the value for the {@link Filter#maxScore() maxScore} attribute.
     * @param maxScore The value for maxScore 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder maxScore(double maxScore) {
      this.maxScore = maxScore;
      initBits &= ~INIT_BIT_MAX_SCORE;
      return this;
    }

    /**
     * Initializes the value for the {@link Filter#blob() blob} attribute.
     * @param blob The elements for blob
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder blob(byte... blob) {
      this.blob = blob.clone();
      initBits &= ~INIT_BIT_BLOB;
      return this;
    }

    /**
     * Builds a new {@link ImmutableFilter ImmutableFilter}.
     * @return An immutable instance of Filter
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableFilter build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableFilter(null, code, dependencies.build(), arguments.build(), name, minScore, maxScore, blob);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_CODE) != 0) attributes.add("code");
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      if ((initBits & INIT_BIT_MIN_SCORE) != 0) attributes.add("minScore");
      if ((initBits & INIT_BIT_MAX_SCORE) != 0) attributes.add("maxScore");
      if ((initBits & INIT_BIT_BLOB) != 0) attributes.add("blob");
      return "Cannot build Filter, some of required attributes are not set " + attributes;
    }
  }
}
