package edu.cmu.cs.diamond.hyperfind.connector.api.bundle;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Doubles;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link NumberOption}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableNumberOption.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableNumberOption.of()}.
 */
@Generated(from = "NumberOption", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableNumberOption
    extends NumberOption {
  private final String displayName;
  private final String name;
  private final double defaultValue;
  private final double min;
  private final double max;
  private final double step;
  private final double disabledValue;
  private final @Nullable Boolean initiallyEnabled;

  private ImmutableNumberOption(
      String displayName,
      String name,
      double defaultValue,
      double min,
      double max,
      double step,
      double disabledValue,
      Optional<Boolean> initiallyEnabled) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.name = Objects.requireNonNull(name, "name");
    this.defaultValue = defaultValue;
    this.min = min;
    this.max = max;
    this.step = step;
    this.disabledValue = disabledValue;
    this.initiallyEnabled = initiallyEnabled.orElse(null);
  }

  private ImmutableNumberOption(
      ImmutableNumberOption original,
      String displayName,
      String name,
      double defaultValue,
      double min,
      double max,
      double step,
      double disabledValue,
      @Nullable Boolean initiallyEnabled) {
    this.displayName = displayName;
    this.name = name;
    this.defaultValue = defaultValue;
    this.min = min;
    this.max = max;
    this.step = step;
    this.disabledValue = disabledValue;
    this.initiallyEnabled = initiallyEnabled;
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
  public double defaultValue() {
    return defaultValue;
  }

  /**
   * @return The value of the {@code min} attribute
   */
  @Override
  public double min() {
    return min;
  }

  /**
   * @return The value of the {@code max} attribute
   */
  @Override
  public double max() {
    return max;
  }

  /**
   * @return The value of the {@code step} attribute
   */
  @Override
  public double step() {
    return step;
  }

  /**
   * @return The value of the {@code disabledValue} attribute
   */
  @Override
  public double disabledValue() {
    return disabledValue;
  }

  /**
   * @return The value of the {@code initiallyEnabled} attribute
   */
  @Override
  public Optional<Boolean> initiallyEnabled() {
    return Optional.ofNullable(initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link NumberOption#displayName() displayName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for displayName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableNumberOption withDisplayName(String value) {
    String newValue = Objects.requireNonNull(value, "displayName");
    if (this.displayName.equals(newValue)) return this;
    return new ImmutableNumberOption(
        this,
        newValue,
        this.name,
        this.defaultValue,
        this.min,
        this.max,
        this.step,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link NumberOption#name() name} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for name
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableNumberOption withName(String value) {
    String newValue = Objects.requireNonNull(value, "name");
    if (this.name.equals(newValue)) return this;
    return new ImmutableNumberOption(
        this,
        this.displayName,
        newValue,
        this.defaultValue,
        this.min,
        this.max,
        this.step,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link NumberOption#defaultValue() defaultValue} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for defaultValue
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableNumberOption withDefaultValue(double value) {
    if (Double.doubleToLongBits(this.defaultValue) == Double.doubleToLongBits(value)) return this;
    return new ImmutableNumberOption(
        this,
        this.displayName,
        this.name,
        value,
        this.min,
        this.max,
        this.step,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link NumberOption#min() min} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for min
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableNumberOption withMin(double value) {
    if (Double.doubleToLongBits(this.min) == Double.doubleToLongBits(value)) return this;
    return new ImmutableNumberOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        value,
        this.max,
        this.step,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link NumberOption#max() max} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for max
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableNumberOption withMax(double value) {
    if (Double.doubleToLongBits(this.max) == Double.doubleToLongBits(value)) return this;
    return new ImmutableNumberOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        this.min,
        value,
        this.step,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link NumberOption#step() step} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for step
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableNumberOption withStep(double value) {
    if (Double.doubleToLongBits(this.step) == Double.doubleToLongBits(value)) return this;
    return new ImmutableNumberOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        this.min,
        this.max,
        value,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link NumberOption#disabledValue() disabledValue} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for disabledValue
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableNumberOption withDisabledValue(double value) {
    if (Double.doubleToLongBits(this.disabledValue) == Double.doubleToLongBits(value)) return this;
    return new ImmutableNumberOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        this.min,
        this.max,
        this.step,
        value,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a <i>present</i> value for the optional {@link NumberOption#initiallyEnabled() initiallyEnabled} attribute.
   * @param value The value for initiallyEnabled
   * @return A modified copy of {@code this} object
   */
  public final ImmutableNumberOption withInitiallyEnabled(boolean value) {
    @Nullable Boolean newValue = value;
    if (Objects.equals(this.initiallyEnabled, newValue)) return this;
    return new ImmutableNumberOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        this.min,
        this.max,
        this.step,
        this.disabledValue,
        newValue);
  }

  /**
   * Copy the current immutable object by setting an optional value for the {@link NumberOption#initiallyEnabled() initiallyEnabled} attribute.
   * An equality check is used on inner nullable value to prevent copying of the same value by returning {@code this}.
   * @param optional A value for initiallyEnabled
   * @return A modified copy of {@code this} object
   */
  public final ImmutableNumberOption withInitiallyEnabled(Optional<Boolean> optional) {
    @Nullable Boolean value = optional.orElse(null);
    if (Objects.equals(this.initiallyEnabled, value)) return this;
    return new ImmutableNumberOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        this.min,
        this.max,
        this.step,
        this.disabledValue,
        value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableNumberOption} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableNumberOption
        && equalTo((ImmutableNumberOption) another);
  }

  private boolean equalTo(ImmutableNumberOption another) {
    return displayName.equals(another.displayName)
        && name.equals(another.name)
        && Double.doubleToLongBits(defaultValue) == Double.doubleToLongBits(another.defaultValue)
        && Double.doubleToLongBits(min) == Double.doubleToLongBits(another.min)
        && Double.doubleToLongBits(max) == Double.doubleToLongBits(another.max)
        && Double.doubleToLongBits(step) == Double.doubleToLongBits(another.step)
        && Double.doubleToLongBits(disabledValue) == Double.doubleToLongBits(another.disabledValue)
        && Objects.equals(initiallyEnabled, another.initiallyEnabled);
  }

  /**
   * Computes a hash code from attributes: {@code displayName}, {@code name}, {@code defaultValue}, {@code min}, {@code max}, {@code step}, {@code disabledValue}, {@code initiallyEnabled}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + displayName.hashCode();
    h += (h << 5) + name.hashCode();
    h += (h << 5) + Doubles.hashCode(defaultValue);
    h += (h << 5) + Doubles.hashCode(min);
    h += (h << 5) + Doubles.hashCode(max);
    h += (h << 5) + Doubles.hashCode(step);
    h += (h << 5) + Doubles.hashCode(disabledValue);
    h += (h << 5) + Objects.hashCode(initiallyEnabled);
    return h;
  }

  /**
   * Prints the immutable value {@code NumberOption} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("NumberOption")
        .omitNullValues()
        .add("displayName", displayName)
        .add("name", name)
        .add("defaultValue", defaultValue)
        .add("min", min)
        .add("max", max)
        .add("step", step)
        .add("disabledValue", disabledValue)
        .add("initiallyEnabled", initiallyEnabled)
        .toString();
  }

  /**
   * Construct a new immutable {@code NumberOption} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param name The value for the {@code name} attribute
   * @param defaultValue The value for the {@code defaultValue} attribute
   * @param min The value for the {@code min} attribute
   * @param max The value for the {@code max} attribute
   * @param step The value for the {@code step} attribute
   * @param disabledValue The value for the {@code disabledValue} attribute
   * @param initiallyEnabled The value for the {@code initiallyEnabled} attribute
   * @return An immutable NumberOption instance
   */
  public static ImmutableNumberOption of(String displayName, String name, double defaultValue, double min, double max, double step, double disabledValue, Optional<Boolean> initiallyEnabled) {
    return new ImmutableNumberOption(displayName, name, defaultValue, min, max, step, disabledValue, initiallyEnabled);
  }

  /**
   * Creates an immutable copy of a {@link NumberOption} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable NumberOption instance
   */
  public static ImmutableNumberOption copyOf(NumberOption instance) {
    if (instance instanceof ImmutableNumberOption) {
      return (ImmutableNumberOption) instance;
    }
    return ImmutableNumberOption.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableNumberOption ImmutableNumberOption}.
   * <pre>
   * ImmutableNumberOption.builder()
   *    .displayName(String) // required {@link NumberOption#displayName() displayName}
   *    .name(String) // required {@link NumberOption#name() name}
   *    .defaultValue(double) // required {@link NumberOption#defaultValue() defaultValue}
   *    .min(double) // required {@link NumberOption#min() min}
   *    .max(double) // required {@link NumberOption#max() max}
   *    .step(double) // required {@link NumberOption#step() step}
   *    .disabledValue(double) // required {@link NumberOption#disabledValue() disabledValue}
   *    .initiallyEnabled(Boolean) // optional {@link NumberOption#initiallyEnabled() initiallyEnabled}
   *    .build();
   * </pre>
   * @return A new ImmutableNumberOption builder
   */
  public static ImmutableNumberOption.Builder builder() {
    return new ImmutableNumberOption.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableNumberOption ImmutableNumberOption}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "NumberOption", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DISPLAY_NAME = 0x1L;
    private static final long INIT_BIT_NAME = 0x2L;
    private static final long INIT_BIT_DEFAULT_VALUE = 0x4L;
    private static final long INIT_BIT_MIN = 0x8L;
    private static final long INIT_BIT_MAX = 0x10L;
    private static final long INIT_BIT_STEP = 0x20L;
    private static final long INIT_BIT_DISABLED_VALUE = 0x40L;
    private long initBits = 0x7fL;

    private @Nullable String displayName;
    private @Nullable String name;
    private double defaultValue;
    private double min;
    private double max;
    private double step;
    private double disabledValue;
    private @Nullable Boolean initiallyEnabled;

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
     * Fill a builder with attribute values from the provided {@code edu.cmu.cs.diamond.hyperfind.connector.api.bundle.NumberOption} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(NumberOption instance) {
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
      if (object instanceof NumberOption) {
        NumberOption instance = (NumberOption) object;
        min(instance.min());
        max(instance.max());
        disabledValue(instance.disabledValue());
        if ((bits & 0x1L) == 0) {
          displayName(instance.displayName());
          bits |= 0x1L;
        }
        defaultValue(instance.defaultValue());
        Optional<Boolean> initiallyEnabledOptional = instance.initiallyEnabled();
        if (initiallyEnabledOptional.isPresent()) {
          initiallyEnabled(initiallyEnabledOptional);
        }
        if ((bits & 0x2L) == 0) {
          name(instance.name());
          bits |= 0x2L;
        }
        step(instance.step());
      }
    }

    /**
     * Initializes the value for the {@link NumberOption#displayName() displayName} attribute.
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
     * Initializes the value for the {@link NumberOption#name() name} attribute.
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
     * Initializes the value for the {@link NumberOption#defaultValue() defaultValue} attribute.
     * @param defaultValue The value for defaultValue 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder defaultValue(double defaultValue) {
      this.defaultValue = defaultValue;
      initBits &= ~INIT_BIT_DEFAULT_VALUE;
      return this;
    }

    /**
     * Initializes the value for the {@link NumberOption#min() min} attribute.
     * @param min The value for min 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder min(double min) {
      this.min = min;
      initBits &= ~INIT_BIT_MIN;
      return this;
    }

    /**
     * Initializes the value for the {@link NumberOption#max() max} attribute.
     * @param max The value for max 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder max(double max) {
      this.max = max;
      initBits &= ~INIT_BIT_MAX;
      return this;
    }

    /**
     * Initializes the value for the {@link NumberOption#step() step} attribute.
     * @param step The value for step 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder step(double step) {
      this.step = step;
      initBits &= ~INIT_BIT_STEP;
      return this;
    }

    /**
     * Initializes the value for the {@link NumberOption#disabledValue() disabledValue} attribute.
     * @param disabledValue The value for disabledValue 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder disabledValue(double disabledValue) {
      this.disabledValue = disabledValue;
      initBits &= ~INIT_BIT_DISABLED_VALUE;
      return this;
    }

    /**
     * Initializes the optional value {@link NumberOption#initiallyEnabled() initiallyEnabled} to initiallyEnabled.
     * @param initiallyEnabled The value for initiallyEnabled
     * @return {@code this} builder for chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder initiallyEnabled(boolean initiallyEnabled) {
      this.initiallyEnabled = initiallyEnabled;
      return this;
    }

    /**
     * Initializes the optional value {@link NumberOption#initiallyEnabled() initiallyEnabled} to initiallyEnabled.
     * @param initiallyEnabled The value for initiallyEnabled
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder initiallyEnabled(Optional<Boolean> initiallyEnabled) {
      this.initiallyEnabled = initiallyEnabled.orElse(null);
      return this;
    }

    /**
     * Builds a new {@link ImmutableNumberOption ImmutableNumberOption}.
     * @return An immutable instance of NumberOption
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableNumberOption build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableNumberOption(null, displayName, name, defaultValue, min, max, step, disabledValue, initiallyEnabled);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DISPLAY_NAME) != 0) attributes.add("displayName");
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      if ((initBits & INIT_BIT_DEFAULT_VALUE) != 0) attributes.add("defaultValue");
      if ((initBits & INIT_BIT_MIN) != 0) attributes.add("min");
      if ((initBits & INIT_BIT_MAX) != 0) attributes.add("max");
      if ((initBits & INIT_BIT_STEP) != 0) attributes.add("step");
      if ((initBits & INIT_BIT_DISABLED_VALUE) != 0) attributes.add("disabledValue");
      return "Cannot build NumberOption, some of required attributes are not set " + attributes;
    }
  }
}
