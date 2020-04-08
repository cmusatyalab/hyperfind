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
 * Immutable implementation of {@link OptionGroup}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableOptionGroup.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableOptionGroup.of()}.
 */
@Generated(from = "OptionGroup", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableOptionGroup
    implements OptionGroup {
  private final String displayName;
  private final ImmutableList<Option> options;

  private ImmutableOptionGroup(
      String displayName,
      Iterable<? extends Option> options) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.options = ImmutableList.copyOf(options);
  }

  private ImmutableOptionGroup(
      ImmutableOptionGroup original,
      String displayName,
      ImmutableList<Option> options) {
    this.displayName = displayName;
    this.options = options;
  }

  /**
   * @return The value of the {@code displayName} attribute
   */
  @Override
  public String displayName() {
    return displayName;
  }

  /**
   * @return The value of the {@code options} attribute
   */
  @Override
  public ImmutableList<Option> options() {
    return options;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link OptionGroup#displayName() displayName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for displayName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableOptionGroup withDisplayName(String value) {
    String newValue = Objects.requireNonNull(value, "displayName");
    if (this.displayName.equals(newValue)) return this;
    return new ImmutableOptionGroup(this, newValue, this.options);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link OptionGroup#options() options}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableOptionGroup withOptions(Option... elements) {
    ImmutableList<Option> newValue = ImmutableList.copyOf(elements);
    return new ImmutableOptionGroup(this, this.displayName, newValue);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link OptionGroup#options() options}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of options elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableOptionGroup withOptions(Iterable<? extends Option> elements) {
    if (this.options == elements) return this;
    ImmutableList<Option> newValue = ImmutableList.copyOf(elements);
    return new ImmutableOptionGroup(this, this.displayName, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableOptionGroup} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableOptionGroup
        && equalTo((ImmutableOptionGroup) another);
  }

  private boolean equalTo(ImmutableOptionGroup another) {
    return displayName.equals(another.displayName)
        && options.equals(another.options);
  }

  /**
   * Computes a hash code from attributes: {@code displayName}, {@code options}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + displayName.hashCode();
    h += (h << 5) + options.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code OptionGroup} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("OptionGroup")
        .omitNullValues()
        .add("displayName", displayName)
        .add("options", options)
        .toString();
  }

  /**
   * Construct a new immutable {@code OptionGroup} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param options The value for the {@code options} attribute
   * @return An immutable OptionGroup instance
   */
  public static ImmutableOptionGroup of(String displayName, List<Option> options) {
    return of(displayName, (Iterable<? extends Option>) options);
  }

  /**
   * Construct a new immutable {@code OptionGroup} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param options The value for the {@code options} attribute
   * @return An immutable OptionGroup instance
   */
  public static ImmutableOptionGroup of(String displayName, Iterable<? extends Option> options) {
    return new ImmutableOptionGroup(displayName, options);
  }

  /**
   * Creates an immutable copy of a {@link OptionGroup} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable OptionGroup instance
   */
  public static ImmutableOptionGroup copyOf(OptionGroup instance) {
    if (instance instanceof ImmutableOptionGroup) {
      return (ImmutableOptionGroup) instance;
    }
    return ImmutableOptionGroup.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableOptionGroup ImmutableOptionGroup}.
   * <pre>
   * ImmutableOptionGroup.builder()
   *    .displayName(String) // required {@link OptionGroup#displayName() displayName}
   *    .addOptions|addAllOptions(edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Option) // {@link OptionGroup#options() options} elements
   *    .build();
   * </pre>
   * @return A new ImmutableOptionGroup builder
   */
  public static ImmutableOptionGroup.Builder builder() {
    return new ImmutableOptionGroup.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableOptionGroup ImmutableOptionGroup}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "OptionGroup", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DISPLAY_NAME = 0x1L;
    private long initBits = 0x1L;

    private @Nullable String displayName;
    private ImmutableList.Builder<Option> options = ImmutableList.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code OptionGroup} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(OptionGroup instance) {
      Objects.requireNonNull(instance, "instance");
      displayName(instance.displayName());
      addAllOptions(instance.options());
      return this;
    }

    /**
     * Initializes the value for the {@link OptionGroup#displayName() displayName} attribute.
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
     * Adds one element to {@link OptionGroup#options() options} list.
     * @param element A options element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addOptions(Option element) {
      this.options.add(element);
      return this;
    }

    /**
     * Adds elements to {@link OptionGroup#options() options} list.
     * @param elements An array of options elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addOptions(Option... elements) {
      this.options.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link OptionGroup#options() options} list.
     * @param elements An iterable of options elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder options(Iterable<? extends Option> elements) {
      this.options = ImmutableList.builder();
      return addAllOptions(elements);
    }

    /**
     * Adds elements to {@link OptionGroup#options() options} list.
     * @param elements An iterable of options elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllOptions(Iterable<? extends Option> elements) {
      this.options.addAll(elements);
      return this;
    }

    /**
     * Builds a new {@link ImmutableOptionGroup ImmutableOptionGroup}.
     * @return An immutable instance of OptionGroup
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableOptionGroup build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableOptionGroup(null, displayName, options.build());
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DISPLAY_NAME) != 0) attributes.add("displayName");
      return "Cannot build OptionGroup, some of required attributes are not set " + attributes;
    }
  }
}
