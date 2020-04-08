package edu.cmu.cs.diamond.hyperfind.connector.api.bundle;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link Bundle}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableBundle.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableBundle.of()}.
 */
@Generated(from = "Bundle", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableBundle implements Bundle {
  private final String displayName;
  private final BundleType type;
  private final ImmutableList<OptionGroup> options;
  private final BundleState state;
  private final FilterBuilder filterBuilder;

  private ImmutableBundle(
      String displayName,
      BundleType type,
      Iterable<? extends OptionGroup> options,
      BundleState state,
      FilterBuilder filterBuilder) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.type = Objects.requireNonNull(type, "type");
    this.options = ImmutableList.copyOf(options);
    this.state = Objects.requireNonNull(state, "state");
    this.filterBuilder = Objects.requireNonNull(filterBuilder, "filterBuilder");
  }

  private ImmutableBundle(
      ImmutableBundle original,
      String displayName,
      BundleType type,
      ImmutableList<OptionGroup> options,
      BundleState state,
      FilterBuilder filterBuilder) {
    this.displayName = displayName;
    this.type = type;
    this.options = options;
    this.state = state;
    this.filterBuilder = filterBuilder;
  }

  /**
   * @return The value of the {@code displayName} attribute
   */
  @Override
  public String displayName() {
    return displayName;
  }

  /**
   * @return The value of the {@code type} attribute
   */
  @Override
  public BundleType type() {
    return type;
  }

  /**
   * @return The value of the {@code options} attribute
   */
  @Override
  public ImmutableList<OptionGroup> options() {
    return options;
  }

  /**
   * @return The value of the {@code state} attribute
   */
  @Override
  public BundleState state() {
    return state;
  }

  /**
   * @return The value of the {@code filterBuilder} attribute
   */
  @Override
  public FilterBuilder filterBuilder() {
    return filterBuilder;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Bundle#displayName() displayName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for displayName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBundle withDisplayName(String value) {
    String newValue = Objects.requireNonNull(value, "displayName");
    if (this.displayName.equals(newValue)) return this;
    return new ImmutableBundle(this, newValue, this.type, this.options, this.state, this.filterBuilder);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Bundle#type() type} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for type
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBundle withType(BundleType value) {
    if (this.type == value) return this;
    BundleType newValue = Objects.requireNonNull(value, "type");
    if (this.type.equals(newValue)) return this;
    return new ImmutableBundle(this, this.displayName, newValue, this.options, this.state, this.filterBuilder);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link Bundle#options() options}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableBundle withOptions(OptionGroup... elements) {
    ImmutableList<OptionGroup> newValue = ImmutableList.copyOf(elements);
    return new ImmutableBundle(this, this.displayName, this.type, newValue, this.state, this.filterBuilder);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link Bundle#options() options}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of options elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableBundle withOptions(Iterable<? extends OptionGroup> elements) {
    if (this.options == elements) return this;
    ImmutableList<OptionGroup> newValue = ImmutableList.copyOf(elements);
    return new ImmutableBundle(this, this.displayName, this.type, newValue, this.state, this.filterBuilder);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Bundle#state() state} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for state
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBundle withState(BundleState value) {
    if (this.state == value) return this;
    BundleState newValue = Objects.requireNonNull(value, "state");
    return new ImmutableBundle(this, this.displayName, this.type, this.options, newValue, this.filterBuilder);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Bundle#filterBuilder() filterBuilder} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for filterBuilder
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBundle withFilterBuilder(FilterBuilder value) {
    if (this.filterBuilder == value) return this;
    FilterBuilder newValue = Objects.requireNonNull(value, "filterBuilder");
    return new ImmutableBundle(this, this.displayName, this.type, this.options, this.state, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableBundle} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableBundle
        && equalTo((ImmutableBundle) another);
  }

  private boolean equalTo(ImmutableBundle another) {
    return displayName.equals(another.displayName)
        && type.equals(another.type)
        && options.equals(another.options)
        && state.equals(another.state)
        && filterBuilder.equals(another.filterBuilder);
  }

  /**
   * Computes a hash code from attributes: {@code displayName}, {@code type}, {@code options}, {@code state}, {@code filterBuilder}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + displayName.hashCode();
    h += (h << 5) + type.hashCode();
    h += (h << 5) + options.hashCode();
    h += (h << 5) + state.hashCode();
    h += (h << 5) + filterBuilder.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code Bundle} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Bundle")
        .omitNullValues()
        .add("displayName", displayName)
        .add("type", type)
        .add("options", options)
        .add("state", state)
        .add("filterBuilder", filterBuilder)
        .toString();
  }

  /**
   * Construct a new immutable {@code Bundle} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param type The value for the {@code type} attribute
   * @param options The value for the {@code options} attribute
   * @param state The value for the {@code state} attribute
   * @param filterBuilder The value for the {@code filterBuilder} attribute
   * @return An immutable Bundle instance
   */
  public static ImmutableBundle of(String displayName, BundleType type, List<OptionGroup> options, BundleState state, FilterBuilder filterBuilder) {
    return of(displayName, type, (Iterable<? extends OptionGroup>) options, state, filterBuilder);
  }

  /**
   * Construct a new immutable {@code Bundle} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param type The value for the {@code type} attribute
   * @param options The value for the {@code options} attribute
   * @param state The value for the {@code state} attribute
   * @param filterBuilder The value for the {@code filterBuilder} attribute
   * @return An immutable Bundle instance
   */
  public static ImmutableBundle of(String displayName, BundleType type, Iterable<? extends OptionGroup> options, BundleState state, FilterBuilder filterBuilder) {
    return new ImmutableBundle(displayName, type, options, state, filterBuilder);
  }

  /**
   * Creates an immutable copy of a {@link Bundle} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable Bundle instance
   */
  public static ImmutableBundle copyOf(Bundle instance) {
    if (instance instanceof ImmutableBundle) {
      return (ImmutableBundle) instance;
    }
    return ImmutableBundle.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableBundle ImmutableBundle}.
   * <pre>
   * ImmutableBundle.builder()
   *    .displayName(String) // required {@link Bundle#displayName() displayName}
   *    .type(edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BundleType) // required {@link Bundle#type() type}
   *    .addOptions|addAllOptions(edu.cmu.cs.diamond.hyperfind.connector.api.bundle.OptionGroup) // {@link Bundle#options() options} elements
   *    .state(edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BundleState) // required {@link Bundle#state() state}
   *    .filterBuilder(edu.cmu.cs.diamond.hyperfind.connector.api.bundle.FilterBuilder) // required {@link Bundle#filterBuilder() filterBuilder}
   *    .build();
   * </pre>
   * @return A new ImmutableBundle builder
   */
  public static ImmutableBundle.Builder builder() {
    return new ImmutableBundle.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableBundle ImmutableBundle}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "Bundle", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DISPLAY_NAME = 0x1L;
    private static final long INIT_BIT_TYPE = 0x2L;
    private static final long INIT_BIT_STATE = 0x4L;
    private static final long INIT_BIT_FILTER_BUILDER = 0x8L;
    private long initBits = 0xfL;

    private @Nullable String displayName;
    private @Nullable BundleType type;
    private ImmutableList.Builder<OptionGroup> options = ImmutableList.builder();
    private @Nullable BundleState state;
    private @Nullable FilterBuilder filterBuilder;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code Bundle} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(Bundle instance) {
      Objects.requireNonNull(instance, "instance");
      displayName(instance.displayName());
      type(instance.type());
      addAllOptions(instance.options());
      state(instance.state());
      filterBuilder(instance.filterBuilder());
      return this;
    }

    /**
     * Initializes the value for the {@link Bundle#displayName() displayName} attribute.
     * @param displayName The value for displayName 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder displayName(String displayName) {
      this.displayName = Objects.requireNonNull(displayName, "displayName");
      initBits &= ~INIT_BIT_DISPLAY_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link Bundle#type() type} attribute.
     * @param type The value for type 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder type(BundleType type) {
      this.type = Objects.requireNonNull(type, "type");
      initBits &= ~INIT_BIT_TYPE;
      return this;
    }

    /**
     * Adds one element to {@link Bundle#options() options} list.
     * @param element A options element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addOptions(OptionGroup element) {
      this.options.add(element);
      return this;
    }

    /**
     * Adds elements to {@link Bundle#options() options} list.
     * @param elements An array of options elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addOptions(OptionGroup... elements) {
      this.options.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link Bundle#options() options} list.
     * @param elements An iterable of options elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder options(Iterable<? extends OptionGroup> elements) {
      this.options = ImmutableList.builder();
      return addAllOptions(elements);
    }

    /**
     * Adds elements to {@link Bundle#options() options} list.
     * @param elements An iterable of options elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllOptions(Iterable<? extends OptionGroup> elements) {
      this.options.addAll(elements);
      return this;
    }

    /**
     * Initializes the value for the {@link Bundle#state() state} attribute.
     * @param state The value for state 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder state(BundleState state) {
      this.state = Objects.requireNonNull(state, "state");
      initBits &= ~INIT_BIT_STATE;
      return this;
    }

    /**
     * Initializes the value for the {@link Bundle#filterBuilder() filterBuilder} attribute.
     * @param filterBuilder The value for filterBuilder 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder filterBuilder(FilterBuilder filterBuilder) {
      this.filterBuilder = Objects.requireNonNull(filterBuilder, "filterBuilder");
      initBits &= ~INIT_BIT_FILTER_BUILDER;
      return this;
    }

    /**
     * Builds a new {@link ImmutableBundle ImmutableBundle}.
     * @return An immutable instance of Bundle
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableBundle build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableBundle(null, displayName, type, options.build(), state, filterBuilder);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DISPLAY_NAME) != 0) attributes.add("displayName");
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      if ((initBits & INIT_BIT_STATE) != 0) attributes.add("state");
      if ((initBits & INIT_BIT_FILTER_BUILDER) != 0) attributes.add("filterBuilder");
      return "Cannot build Bundle, some of required attributes are not set " + attributes;
    }
  }
}
