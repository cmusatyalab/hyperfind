package edu.cmu.cs.diamond.hyperfind.connector.api.bundle;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Booleans;
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
 * Immutable implementation of {@link BooleanOption}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableBooleanOption.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableBooleanOption.of()}.
 */
@Generated(from = "BooleanOption", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableBooleanOption
    extends BooleanOption {
  private final String displayName;
  private final String name;
  private final boolean defaultValue;

  private ImmutableBooleanOption(String displayName, String name, boolean defaultValue) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.name = Objects.requireNonNull(name, "name");
    this.defaultValue = defaultValue;
  }

  private ImmutableBooleanOption(
      ImmutableBooleanOption original,
      String displayName,
      String name,
      boolean defaultValue) {
    this.displayName = displayName;
    this.name = name;
    this.defaultValue = defaultValue;
  }

  /**
   * @return The value of the {@code displayName} attribute
   */
  @Override
  public String displayName() {
    return displayName;
  }

  /**
   * @return The value of the {@code name} attribute
   */
  @Override
  public String name() {
    return name;
  }

  /**
   * @return The value of the {@code defaultValue} attribute
   */
  @Override
  public boolean defaultValue() {
    return defaultValue;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BooleanOption#displayName() displayName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for displayName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBooleanOption withDisplayName(String value) {
    String newValue = Objects.requireNonNull(value, "displayName");
    if (this.displayName.equals(newValue)) return this;
    return new ImmutableBooleanOption(this, newValue, this.name, this.defaultValue);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BooleanOption#name() name} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for name
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBooleanOption withName(String value) {
    String newValue = Objects.requireNonNull(value, "name");
    if (this.name.equals(newValue)) return this;
    return new ImmutableBooleanOption(this, this.displayName, newValue, this.defaultValue);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BooleanOption#defaultValue() defaultValue} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for defaultValue
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBooleanOption withDefaultValue(boolean value) {
    if (this.defaultValue == value) return this;
    return new ImmutableBooleanOption(this, this.displayName, this.name, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableBooleanOption} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableBooleanOption
        && equalTo((ImmutableBooleanOption) another);
  }

  private boolean equalTo(ImmutableBooleanOption another) {
    return displayName.equals(another.displayName)
        && name.equals(another.name)
        && defaultValue == another.defaultValue;
  }

  /**
   * Computes a hash code from attributes: {@code displayName}, {@code name}, {@code defaultValue}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + displayName.hashCode();
    h += (h << 5) + name.hashCode();
    h += (h << 5) + Booleans.hashCode(defaultValue);
    return h;
  }

  /**
   * Prints the immutable value {@code BooleanOption} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("BooleanOption")
        .omitNullValues()
        .add("displayName", displayName)
        .add("name", name)
        .add("defaultValue", defaultValue)
        .toString();
  }

  /**
   * Construct a new immutable {@code BooleanOption} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param name The value for the {@code name} attribute
   * @param defaultValue The value for the {@code defaultValue} attribute
   * @return An immutable BooleanOption instance
   */
  public static ImmutableBooleanOption of(String displayName, String name, boolean defaultValue) {
    return new ImmutableBooleanOption(displayName, name, defaultValue);
  }

  /**
   * Creates an immutable copy of a {@link BooleanOption} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable BooleanOption instance
   */
  public static ImmutableBooleanOption copyOf(BooleanOption instance) {
    if (instance instanceof ImmutableBooleanOption) {
      return (ImmutableBooleanOption) instance;
    }
    return ImmutableBooleanOption.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableBooleanOption ImmutableBooleanOption}.
   * <pre>
   * ImmutableBooleanOption.builder()
   *    .displayName(String) // required {@link BooleanOption#displayName() displayName}
   *    .name(String) // required {@link BooleanOption#name() name}
   *    .defaultValue(boolean) // required {@link BooleanOption#defaultValue() defaultValue}
   *    .build();
   * </pre>
   * @return A new ImmutableBooleanOption builder
   */
  public static ImmutableBooleanOption.Builder builder() {
    return new ImmutableBooleanOption.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableBooleanOption ImmutableBooleanOption}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "BooleanOption", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DISPLAY_NAME = 0x1L;
    private static final long INIT_BIT_NAME = 0x2L;
    private static final long INIT_BIT_DEFAULT_VALUE = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String displayName;
    private @Nullable String name;
    private boolean defaultValue;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Option} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(Option instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    /**
     * Fill a builder with attribute values from the provided {@code edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BooleanOption} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(BooleanOption instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      @Var long bits = 0;
      if (object instanceof Option) {
        Option instance = (Option) object;
        if ((bits & 0x2L) == 0) {
          name(instance.name());
          bits |= 0x2L;
        }
        if ((bits & 0x1L) == 0) {
          displayName(instance.displayName());
          bits |= 0x1L;
        }
      }
      if (object instanceof BooleanOption) {
        BooleanOption instance = (BooleanOption) object;
        if ((bits & 0x2L) == 0) {
          name(instance.name());
          bits |= 0x2L;
        }
        if ((bits & 0x1L) == 0) {
          displayName(instance.displayName());
          bits |= 0x1L;
        }
        defaultValue(instance.defaultValue());
      }
    }

    /**
     * Initializes the value for the {@link BooleanOption#displayName() displayName} attribute.
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
     * Initializes the value for the {@link BooleanOption#name() name} attribute.
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
     * Initializes the value for the {@link BooleanOption#defaultValue() defaultValue} attribute.
     * @param defaultValue The value for defaultValue 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder defaultValue(boolean defaultValue) {
      this.defaultValue = defaultValue;
      initBits &= ~INIT_BIT_DEFAULT_VALUE;
      return this;
    }

    /**
     * Builds a new {@link ImmutableBooleanOption ImmutableBooleanOption}.
     * @return An immutable instance of BooleanOption
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableBooleanOption build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableBooleanOption(null, displayName, name, defaultValue);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DISPLAY_NAME) != 0) attributes.add("displayName");
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      if ((initBits & INIT_BIT_DEFAULT_VALUE) != 0) attributes.add("defaultValue");
      return "Cannot build BooleanOption, some of required attributes are not set " + attributes;
    }
  }
}
