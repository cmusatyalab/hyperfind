package edu.cmu.cs.diamond.hyperfind.connector.api;

import com.google.common.base.MoreObjects;
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
 * Immutable implementation of {@link ObjectId}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableObjectId.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableObjectId.of()}.
 */
@Generated(from = "ObjectId", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableObjectId implements ObjectId {
  private final String objectId;
  private final String deviceName;
  private final String hostname;

  private ImmutableObjectId(String objectId, String deviceName, String hostname) {
    this.objectId = Objects.requireNonNull(objectId, "objectId");
    this.deviceName = Objects.requireNonNull(deviceName, "deviceName");
    this.hostname = Objects.requireNonNull(hostname, "hostname");
  }

  private ImmutableObjectId(
      ImmutableObjectId original,
      String objectId,
      String deviceName,
      String hostname) {
    this.objectId = objectId;
    this.deviceName = deviceName;
    this.hostname = hostname;
  }

  /**
   * @return The value of the {@code objectId} attribute
   */
  @Override
  public String objectId() {
    return objectId;
  }

  /**
   * @return The value of the {@code deviceName} attribute
   */
  @Override
  public String deviceName() {
    return deviceName;
  }

  /**
   * @return The value of the {@code hostname} attribute
   */
  @Override
  public String hostname() {
    return hostname;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ObjectId#objectId() objectId} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for objectId
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableObjectId withObjectId(String value) {
    String newValue = Objects.requireNonNull(value, "objectId");
    if (this.objectId.equals(newValue)) return this;
    return new ImmutableObjectId(this, newValue, this.deviceName, this.hostname);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ObjectId#deviceName() deviceName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for deviceName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableObjectId withDeviceName(String value) {
    String newValue = Objects.requireNonNull(value, "deviceName");
    if (this.deviceName.equals(newValue)) return this;
    return new ImmutableObjectId(this, this.objectId, newValue, this.hostname);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ObjectId#hostname() hostname} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for hostname
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableObjectId withHostname(String value) {
    String newValue = Objects.requireNonNull(value, "hostname");
    if (this.hostname.equals(newValue)) return this;
    return new ImmutableObjectId(this, this.objectId, this.deviceName, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableObjectId} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableObjectId
        && equalTo((ImmutableObjectId) another);
  }

  private boolean equalTo(ImmutableObjectId another) {
    return objectId.equals(another.objectId)
        && deviceName.equals(another.deviceName)
        && hostname.equals(another.hostname);
  }

  /**
   * Computes a hash code from attributes: {@code objectId}, {@code deviceName}, {@code hostname}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + objectId.hashCode();
    h += (h << 5) + deviceName.hashCode();
    h += (h << 5) + hostname.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code ObjectId} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ObjectId")
        .omitNullValues()
        .add("objectId", objectId)
        .add("deviceName", deviceName)
        .add("hostname", hostname)
        .toString();
  }

  /**
   * Construct a new immutable {@code ObjectId} instance.
   * @param objectId The value for the {@code objectId} attribute
   * @param deviceName The value for the {@code deviceName} attribute
   * @param hostname The value for the {@code hostname} attribute
   * @return An immutable ObjectId instance
   */
  public static ImmutableObjectId of(String objectId, String deviceName, String hostname) {
    return new ImmutableObjectId(objectId, deviceName, hostname);
  }

  /**
   * Creates an immutable copy of a {@link ObjectId} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ObjectId instance
   */
  public static ImmutableObjectId copyOf(ObjectId instance) {
    if (instance instanceof ImmutableObjectId) {
      return (ImmutableObjectId) instance;
    }
    return ImmutableObjectId.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableObjectId ImmutableObjectId}.
   * <pre>
   * ImmutableObjectId.builder()
   *    .objectId(String) // required {@link ObjectId#objectId() objectId}
   *    .deviceName(String) // required {@link ObjectId#deviceName() deviceName}
   *    .hostname(String) // required {@link ObjectId#hostname() hostname}
   *    .build();
   * </pre>
   * @return A new ImmutableObjectId builder
   */
  public static ImmutableObjectId.Builder builder() {
    return new ImmutableObjectId.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableObjectId ImmutableObjectId}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ObjectId", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_OBJECT_ID = 0x1L;
    private static final long INIT_BIT_DEVICE_NAME = 0x2L;
    private static final long INIT_BIT_HOSTNAME = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String objectId;
    private @Nullable String deviceName;
    private @Nullable String hostname;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code ObjectId} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ObjectId instance) {
      Objects.requireNonNull(instance, "instance");
      objectId(instance.objectId());
      deviceName(instance.deviceName());
      hostname(instance.hostname());
      return this;
    }

    /**
     * Initializes the value for the {@link ObjectId#objectId() objectId} attribute.
     * @param objectId The value for objectId 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder objectId(String objectId) {
      this.objectId = Objects.requireNonNull(objectId, "objectId");
      initBits &= ~INIT_BIT_OBJECT_ID;
      return this;
    }

    /**
     * Initializes the value for the {@link ObjectId#deviceName() deviceName} attribute.
     * @param deviceName The value for deviceName 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder deviceName(String deviceName) {
      this.deviceName = Objects.requireNonNull(deviceName, "deviceName");
      initBits &= ~INIT_BIT_DEVICE_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link ObjectId#hostname() hostname} attribute.
     * @param hostname The value for hostname 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder hostname(String hostname) {
      this.hostname = Objects.requireNonNull(hostname, "hostname");
      initBits &= ~INIT_BIT_HOSTNAME;
      return this;
    }

    /**
     * Builds a new {@link ImmutableObjectId ImmutableObjectId}.
     * @return An immutable instance of ObjectId
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableObjectId build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableObjectId(null, objectId, deviceName, hostname);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_OBJECT_ID) != 0) attributes.add("objectId");
      if ((initBits & INIT_BIT_DEVICE_NAME) != 0) attributes.add("deviceName");
      if ((initBits & INIT_BIT_HOSTNAME) != 0) attributes.add("hostname");
      return "Cannot build ObjectId, some of required attributes are not set " + attributes;
    }
  }
}
