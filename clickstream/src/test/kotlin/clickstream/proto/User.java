// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: User.proto

package clickstream.proto;

/**
 * <pre>
 * Corresponds to the details for the user.
 * </pre>
 *
 * Protobuf type {@code clickstream.proto.User}
 */
public  final class User extends
    com.google.protobuf.GeneratedMessageLite<
        User, User.Builder> implements
    // @@protoc_insertion_point(message_implements:clickstream.proto.User)
    UserOrBuilder {
  private User() {
    guid_ = "";
    name_ = "";
    gender_ = "";
    email_ = "";
  }
  public static final int GUID_FIELD_NUMBER = 1;
  private String guid_;
  /**
   * <pre>
   * Unique identy of the user.
   * </pre>
   *
   * <code>string guid = 1;</code>
   * @return The guid.
   */
  @Override
  public String getGuid() {
    return guid_;
  }
  /**
   * <pre>
   * Unique identy of the user.
   * </pre>
   *
   * <code>string guid = 1;</code>
   * @return The bytes for guid.
   */
  @Override
  public com.google.protobuf.ByteString
      getGuidBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(guid_);
  }
  /**
   * <pre>
   * Unique identy of the user.
   * </pre>
   *
   * <code>string guid = 1;</code>
   * @param value The guid to set.
   */
  private void setGuid(
      String value) {
    Class<?> valueClass = value.getClass();
  
    guid_ = value;
  }
  /**
   * <pre>
   * Unique identy of the user.
   * </pre>
   *
   * <code>string guid = 1;</code>
   */
  private void clearGuid() {
    
    guid_ = getDefaultInstance().getGuid();
  }
  /**
   * <pre>
   * Unique identy of the user.
   * </pre>
   *
   * <code>string guid = 1;</code>
   * @param value The bytes for guid to set.
   */
  private void setGuidBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    guid_ = value.toStringUtf8();
    
  }

  public static final int NAME_FIELD_NUMBER = 2;
  private String name_;
  /**
   * <pre>
   * User's full name.
   * </pre>
   *
   * <code>string name = 2;</code>
   * @return The name.
   */
  @Override
  public String getName() {
    return name_;
  }
  /**
   * <pre>
   * User's full name.
   * </pre>
   *
   * <code>string name = 2;</code>
   * @return The bytes for name.
   */
  @Override
  public com.google.protobuf.ByteString
      getNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(name_);
  }
  /**
   * <pre>
   * User's full name.
   * </pre>
   *
   * <code>string name = 2;</code>
   * @param value The name to set.
   */
  private void setName(
      String value) {
    Class<?> valueClass = value.getClass();
  
    name_ = value;
  }
  /**
   * <pre>
   * User's full name.
   * </pre>
   *
   * <code>string name = 2;</code>
   */
  private void clearName() {
    
    name_ = getDefaultInstance().getName();
  }
  /**
   * <pre>
   * User's full name.
   * </pre>
   *
   * <code>string name = 2;</code>
   * @param value The bytes for name to set.
   */
  private void setNameBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    name_ = value.toStringUtf8();
    
  }

  public static final int AGE_FIELD_NUMBER = 3;
  private int age_;
  /**
   * <pre>
   * User's age.
   * </pre>
   *
   * <code>int32 age = 3;</code>
   * @return The age.
   */
  @Override
  public int getAge() {
    return age_;
  }
  /**
   * <pre>
   * User's age.
   * </pre>
   *
   * <code>int32 age = 3;</code>
   * @param value The age to set.
   */
  private void setAge(int value) {
    
    age_ = value;
  }
  /**
   * <pre>
   * User's age.
   * </pre>
   *
   * <code>int32 age = 3;</code>
   */
  private void clearAge() {
    
    age_ = 0;
  }

  public static final int GENDER_FIELD_NUMBER = 4;
  private String gender_;
  /**
   * <pre>
   * User's gender.
   * </pre>
   *
   * <code>string gender = 4;</code>
   * @return The gender.
   */
  @Override
  public String getGender() {
    return gender_;
  }
  /**
   * <pre>
   * User's gender.
   * </pre>
   *
   * <code>string gender = 4;</code>
   * @return The bytes for gender.
   */
  @Override
  public com.google.protobuf.ByteString
      getGenderBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(gender_);
  }
  /**
   * <pre>
   * User's gender.
   * </pre>
   *
   * <code>string gender = 4;</code>
   * @param value The gender to set.
   */
  private void setGender(
      String value) {
    Class<?> valueClass = value.getClass();
  
    gender_ = value;
  }
  /**
   * <pre>
   * User's gender.
   * </pre>
   *
   * <code>string gender = 4;</code>
   */
  private void clearGender() {
    
    gender_ = getDefaultInstance().getGender();
  }
  /**
   * <pre>
   * User's gender.
   * </pre>
   *
   * <code>string gender = 4;</code>
   * @param value The bytes for gender to set.
   */
  private void setGenderBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    gender_ = value.toStringUtf8();
    
  }

  public static final int PHONE_NUMBER_FIELD_NUMBER = 5;
  private long phoneNumber_;
  /**
   * <pre>
   * User's phone number.
   * </pre>
   *
   * <code>int64 phone_number = 5;</code>
   * @return The phoneNumber.
   */
  @Override
  public long getPhoneNumber() {
    return phoneNumber_;
  }
  /**
   * <pre>
   * User's phone number.
   * </pre>
   *
   * <code>int64 phone_number = 5;</code>
   * @param value The phoneNumber to set.
   */
  private void setPhoneNumber(long value) {
    
    phoneNumber_ = value;
  }
  /**
   * <pre>
   * User's phone number.
   * </pre>
   *
   * <code>int64 phone_number = 5;</code>
   */
  private void clearPhoneNumber() {
    
    phoneNumber_ = 0L;
  }

  public static final int EMAIL_FIELD_NUMBER = 6;
  private String email_;
  /**
   * <pre>
   * User's email address.
   * </pre>
   *
   * <code>string email = 6;</code>
   * @return The email.
   */
  @Override
  public String getEmail() {
    return email_;
  }
  /**
   * <pre>
   * User's email address.
   * </pre>
   *
   * <code>string email = 6;</code>
   * @return The bytes for email.
   */
  @Override
  public com.google.protobuf.ByteString
      getEmailBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(email_);
  }
  /**
   * <pre>
   * User's email address.
   * </pre>
   *
   * <code>string email = 6;</code>
   * @param value The email to set.
   */
  private void setEmail(
      String value) {
    Class<?> valueClass = value.getClass();
  
    email_ = value;
  }
  /**
   * <pre>
   * User's email address.
   * </pre>
   *
   * <code>string email = 6;</code>
   */
  private void clearEmail() {
    
    email_ = getDefaultInstance().getEmail();
  }
  /**
   * <pre>
   * User's email address.
   * </pre>
   *
   * <code>string email = 6;</code>
   * @param value The bytes for email to set.
   */
  private void setEmailBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    email_ = value.toStringUtf8();
    
  }

  public static final int APP_FIELD_NUMBER = 7;
  private App app_;
  /**
   * <pre>
   * User's app details.
   * </pre>
   *
   * <code>.clickstream.proto.App app = 7;</code>
   */
  @Override
  public boolean hasApp() {
    return app_ != null;
  }
  /**
   * <pre>
   * User's app details.
   * </pre>
   *
   * <code>.clickstream.proto.App app = 7;</code>
   */
  @Override
  public App getApp() {
    return app_ == null ? App.getDefaultInstance() : app_;
  }
  /**
   * <pre>
   * User's app details.
   * </pre>
   *
   * <code>.clickstream.proto.App app = 7;</code>
   */
  private void setApp(App value) {
    value.getClass();
  app_ = value;
    
    }
  /**
   * <pre>
   * User's app details.
   * </pre>
   *
   * <code>.clickstream.proto.App app = 7;</code>
   */
  @SuppressWarnings({"ReferenceEquality"})
  private void mergeApp(App value) {
    value.getClass();
  if (app_ != null &&
        app_ != App.getDefaultInstance()) {
      app_ =
        App.newBuilder(app_).mergeFrom(value).buildPartial();
    } else {
      app_ = value;
    }
    
  }
  /**
   * <pre>
   * User's app details.
   * </pre>
   *
   * <code>.clickstream.proto.App app = 7;</code>
   */
  private void clearApp() {  app_ = null;
    
  }

  public static final int DEVICE_FIELD_NUMBER = 8;
  private Device device_;
  /**
   * <pre>
   * User's device details.
   * </pre>
   *
   * <code>.clickstream.proto.Device device = 8;</code>
   */
  @Override
  public boolean hasDevice() {
    return device_ != null;
  }
  /**
   * <pre>
   * User's device details.
   * </pre>
   *
   * <code>.clickstream.proto.Device device = 8;</code>
   */
  @Override
  public Device getDevice() {
    return device_ == null ? Device.getDefaultInstance() : device_;
  }
  /**
   * <pre>
   * User's device details.
   * </pre>
   *
   * <code>.clickstream.proto.Device device = 8;</code>
   */
  private void setDevice(Device value) {
    value.getClass();
  device_ = value;
    
    }
  /**
   * <pre>
   * User's device details.
   * </pre>
   *
   * <code>.clickstream.proto.Device device = 8;</code>
   */
  @SuppressWarnings({"ReferenceEquality"})
  private void mergeDevice(Device value) {
    value.getClass();
  if (device_ != null &&
        device_ != Device.getDefaultInstance()) {
      device_ =
        Device.newBuilder(device_).mergeFrom(value).buildPartial();
    } else {
      device_ = value;
    }
    
  }
  /**
   * <pre>
   * User's device details.
   * </pre>
   *
   * <code>.clickstream.proto.Device device = 8;</code>
   */
  private void clearDevice() {  device_ = null;
    
  }

  public static final int DEVICE_TIMESTAMP_FIELD_NUMBER = 9;
  private com.google.protobuf.Timestamp deviceTimestamp_;
  /**
   * <pre>
   * Timestamp for the event.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
   */
  @Override
  public boolean hasDeviceTimestamp() {
    return deviceTimestamp_ != null;
  }
  /**
   * <pre>
   * Timestamp for the event.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
   */
  @Override
  public com.google.protobuf.Timestamp getDeviceTimestamp() {
    return deviceTimestamp_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : deviceTimestamp_;
  }
  /**
   * <pre>
   * Timestamp for the event.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
   */
  private void setDeviceTimestamp(com.google.protobuf.Timestamp value) {
    value.getClass();
  deviceTimestamp_ = value;
    
    }
  /**
   * <pre>
   * Timestamp for the event.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
   */
  @SuppressWarnings({"ReferenceEquality"})
  private void mergeDeviceTimestamp(com.google.protobuf.Timestamp value) {
    value.getClass();
  if (deviceTimestamp_ != null &&
        deviceTimestamp_ != com.google.protobuf.Timestamp.getDefaultInstance()) {
      deviceTimestamp_ =
        com.google.protobuf.Timestamp.newBuilder(deviceTimestamp_).mergeFrom(value).buildPartial();
    } else {
      deviceTimestamp_ = value;
    }
    
  }
  /**
   * <pre>
   * Timestamp for the event.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
   */
  private void clearDeviceTimestamp() {  deviceTimestamp_ = null;
    
  }

  public static User parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static User parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static User parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static User parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static User parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static User parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static User parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static User parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static User parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static User parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static User parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static User parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(User prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * Corresponds to the details for the user.
   * </pre>
   *
   * Protobuf type {@code clickstream.proto.User}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        User, Builder> implements
      // @@protoc_insertion_point(builder_implements:clickstream.proto.User)
      UserOrBuilder {
    // Construct using com.clickstream.app.proto.User.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * Unique identy of the user.
     * </pre>
     *
     * <code>string guid = 1;</code>
     * @return The guid.
     */
    @Override
    public String getGuid() {
      return instance.getGuid();
    }
    /**
     * <pre>
     * Unique identy of the user.
     * </pre>
     *
     * <code>string guid = 1;</code>
     * @return The bytes for guid.
     */
    @Override
    public com.google.protobuf.ByteString
        getGuidBytes() {
      return instance.getGuidBytes();
    }
    /**
     * <pre>
     * Unique identy of the user.
     * </pre>
     *
     * <code>string guid = 1;</code>
     * @param value The guid to set.
     * @return This builder for chaining.
     */
    public Builder setGuid(
        String value) {
      copyOnWrite();
      instance.setGuid(value);
      return this;
    }
    /**
     * <pre>
     * Unique identy of the user.
     * </pre>
     *
     * <code>string guid = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearGuid() {
      copyOnWrite();
      instance.clearGuid();
      return this;
    }
    /**
     * <pre>
     * Unique identy of the user.
     * </pre>
     *
     * <code>string guid = 1;</code>
     * @param value The bytes for guid to set.
     * @return This builder for chaining.
     */
    public Builder setGuidBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setGuidBytes(value);
      return this;
    }

    /**
     * <pre>
     * User's full name.
     * </pre>
     *
     * <code>string name = 2;</code>
     * @return The name.
     */
    @Override
    public String getName() {
      return instance.getName();
    }
    /**
     * <pre>
     * User's full name.
     * </pre>
     *
     * <code>string name = 2;</code>
     * @return The bytes for name.
     */
    @Override
    public com.google.protobuf.ByteString
        getNameBytes() {
      return instance.getNameBytes();
    }
    /**
     * <pre>
     * User's full name.
     * </pre>
     *
     * <code>string name = 2;</code>
     * @param value The name to set.
     * @return This builder for chaining.
     */
    public Builder setName(
        String value) {
      copyOnWrite();
      instance.setName(value);
      return this;
    }
    /**
     * <pre>
     * User's full name.
     * </pre>
     *
     * <code>string name = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearName() {
      copyOnWrite();
      instance.clearName();
      return this;
    }
    /**
     * <pre>
     * User's full name.
     * </pre>
     *
     * <code>string name = 2;</code>
     * @param value The bytes for name to set.
     * @return This builder for chaining.
     */
    public Builder setNameBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setNameBytes(value);
      return this;
    }

    /**
     * <pre>
     * User's age.
     * </pre>
     *
     * <code>int32 age = 3;</code>
     * @return The age.
     */
    @Override
    public int getAge() {
      return instance.getAge();
    }
    /**
     * <pre>
     * User's age.
     * </pre>
     *
     * <code>int32 age = 3;</code>
     * @param value The age to set.
     * @return This builder for chaining.
     */
    public Builder setAge(int value) {
      copyOnWrite();
      instance.setAge(value);
      return this;
    }
    /**
     * <pre>
     * User's age.
     * </pre>
     *
     * <code>int32 age = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearAge() {
      copyOnWrite();
      instance.clearAge();
      return this;
    }

    /**
     * <pre>
     * User's gender.
     * </pre>
     *
     * <code>string gender = 4;</code>
     * @return The gender.
     */
    @Override
    public String getGender() {
      return instance.getGender();
    }
    /**
     * <pre>
     * User's gender.
     * </pre>
     *
     * <code>string gender = 4;</code>
     * @return The bytes for gender.
     */
    @Override
    public com.google.protobuf.ByteString
        getGenderBytes() {
      return instance.getGenderBytes();
    }
    /**
     * <pre>
     * User's gender.
     * </pre>
     *
     * <code>string gender = 4;</code>
     * @param value The gender to set.
     * @return This builder for chaining.
     */
    public Builder setGender(
        String value) {
      copyOnWrite();
      instance.setGender(value);
      return this;
    }
    /**
     * <pre>
     * User's gender.
     * </pre>
     *
     * <code>string gender = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearGender() {
      copyOnWrite();
      instance.clearGender();
      return this;
    }
    /**
     * <pre>
     * User's gender.
     * </pre>
     *
     * <code>string gender = 4;</code>
     * @param value The bytes for gender to set.
     * @return This builder for chaining.
     */
    public Builder setGenderBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setGenderBytes(value);
      return this;
    }

    /**
     * <pre>
     * User's phone number.
     * </pre>
     *
     * <code>int64 phone_number = 5;</code>
     * @return The phoneNumber.
     */
    @Override
    public long getPhoneNumber() {
      return instance.getPhoneNumber();
    }
    /**
     * <pre>
     * User's phone number.
     * </pre>
     *
     * <code>int64 phone_number = 5;</code>
     * @param value The phoneNumber to set.
     * @return This builder for chaining.
     */
    public Builder setPhoneNumber(long value) {
      copyOnWrite();
      instance.setPhoneNumber(value);
      return this;
    }
    /**
     * <pre>
     * User's phone number.
     * </pre>
     *
     * <code>int64 phone_number = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearPhoneNumber() {
      copyOnWrite();
      instance.clearPhoneNumber();
      return this;
    }

    /**
     * <pre>
     * User's email address.
     * </pre>
     *
     * <code>string email = 6;</code>
     * @return The email.
     */
    @Override
    public String getEmail() {
      return instance.getEmail();
    }
    /**
     * <pre>
     * User's email address.
     * </pre>
     *
     * <code>string email = 6;</code>
     * @return The bytes for email.
     */
    @Override
    public com.google.protobuf.ByteString
        getEmailBytes() {
      return instance.getEmailBytes();
    }
    /**
     * <pre>
     * User's email address.
     * </pre>
     *
     * <code>string email = 6;</code>
     * @param value The email to set.
     * @return This builder for chaining.
     */
    public Builder setEmail(
        String value) {
      copyOnWrite();
      instance.setEmail(value);
      return this;
    }
    /**
     * <pre>
     * User's email address.
     * </pre>
     *
     * <code>string email = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearEmail() {
      copyOnWrite();
      instance.clearEmail();
      return this;
    }
    /**
     * <pre>
     * User's email address.
     * </pre>
     *
     * <code>string email = 6;</code>
     * @param value The bytes for email to set.
     * @return This builder for chaining.
     */
    public Builder setEmailBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setEmailBytes(value);
      return this;
    }

    /**
     * <pre>
     * User's app details.
     * </pre>
     *
     * <code>.clickstream.proto.App app = 7;</code>
     */
    @Override
    public boolean hasApp() {
      return instance.hasApp();
    }
    /**
     * <pre>
     * User's app details.
     * </pre>
     *
     * <code>.clickstream.proto.App app = 7;</code>
     */
    @Override
    public App getApp() {
      return instance.getApp();
    }
    /**
     * <pre>
     * User's app details.
     * </pre>
     *
     * <code>.clickstream.proto.App app = 7;</code>
     */
    public Builder setApp(App value) {
      copyOnWrite();
      instance.setApp(value);
      return this;
      }
    /**
     * <pre>
     * User's app details.
     * </pre>
     *
     * <code>.clickstream.proto.App app = 7;</code>
     */
    public Builder setApp(
        App.Builder builderForValue) {
      copyOnWrite();
      instance.setApp(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * User's app details.
     * </pre>
     *
     * <code>.clickstream.proto.App app = 7;</code>
     */
    public Builder mergeApp(App value) {
      copyOnWrite();
      instance.mergeApp(value);
      return this;
    }
    /**
     * <pre>
     * User's app details.
     * </pre>
     *
     * <code>.clickstream.proto.App app = 7;</code>
     */
    public Builder clearApp() {  copyOnWrite();
      instance.clearApp();
      return this;
    }

    /**
     * <pre>
     * User's device details.
     * </pre>
     *
     * <code>.clickstream.proto.Device device = 8;</code>
     */
    @Override
    public boolean hasDevice() {
      return instance.hasDevice();
    }
    /**
     * <pre>
     * User's device details.
     * </pre>
     *
     * <code>.clickstream.proto.Device device = 8;</code>
     */
    @Override
    public Device getDevice() {
      return instance.getDevice();
    }
    /**
     * <pre>
     * User's device details.
     * </pre>
     *
     * <code>.clickstream.proto.Device device = 8;</code>
     */
    public Builder setDevice(Device value) {
      copyOnWrite();
      instance.setDevice(value);
      return this;
      }
    /**
     * <pre>
     * User's device details.
     * </pre>
     *
     * <code>.clickstream.proto.Device device = 8;</code>
     */
    public Builder setDevice(
        Device.Builder builderForValue) {
      copyOnWrite();
      instance.setDevice(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * User's device details.
     * </pre>
     *
     * <code>.clickstream.proto.Device device = 8;</code>
     */
    public Builder mergeDevice(Device value) {
      copyOnWrite();
      instance.mergeDevice(value);
      return this;
    }
    /**
     * <pre>
     * User's device details.
     * </pre>
     *
     * <code>.clickstream.proto.Device device = 8;</code>
     */
    public Builder clearDevice() {  copyOnWrite();
      instance.clearDevice();
      return this;
    }

    /**
     * <pre>
     * Timestamp for the event.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
     */
    @Override
    public boolean hasDeviceTimestamp() {
      return instance.hasDeviceTimestamp();
    }
    /**
     * <pre>
     * Timestamp for the event.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
     */
    @Override
    public com.google.protobuf.Timestamp getDeviceTimestamp() {
      return instance.getDeviceTimestamp();
    }
    /**
     * <pre>
     * Timestamp for the event.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
     */
    public Builder setDeviceTimestamp(com.google.protobuf.Timestamp value) {
      copyOnWrite();
      instance.setDeviceTimestamp(value);
      return this;
      }
    /**
     * <pre>
     * Timestamp for the event.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
     */
    public Builder setDeviceTimestamp(
        com.google.protobuf.Timestamp.Builder builderForValue) {
      copyOnWrite();
      instance.setDeviceTimestamp(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Timestamp for the event.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
     */
    public Builder mergeDeviceTimestamp(com.google.protobuf.Timestamp value) {
      copyOnWrite();
      instance.mergeDeviceTimestamp(value);
      return this;
    }
    /**
     * <pre>
     * Timestamp for the event.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp device_timestamp = 9;</code>
     */
    public Builder clearDeviceTimestamp() {  copyOnWrite();
      instance.clearDeviceTimestamp();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clickstream.proto.User)
  }
  @Override
  @SuppressWarnings({"unchecked", "fallthrough"})
  protected final Object dynamicMethod(
      MethodToInvoke method,
      Object arg0, Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new User();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          Object[] objects = new Object[] {
            "guid_",
            "name_",
            "age_",
            "gender_",
            "phoneNumber_",
            "email_",
            "app_",
            "device_",
            "deviceTimestamp_",
          };
          String info =
              "\u0000\t\u0000\u0000\u0001\t\t\u0000\u0000\u0000\u0001\u0208\u0002\u0208\u0003\u0004" +
              "\u0004\u0208\u0005\u0002\u0006\u0208\u0007\t\b\t\t\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<User> parser = PARSER;
        if (parser == null) {
          synchronized (User.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<User>(
                      DEFAULT_INSTANCE);
              PARSER = parser;
            }
          }
        }
        return parser;
    }
    case GET_MEMOIZED_IS_INITIALIZED: {
      return (byte) 1;
    }
    case SET_MEMOIZED_IS_INITIALIZED: {
      return null;
    }
    }
    throw new UnsupportedOperationException();
  }


  // @@protoc_insertion_point(class_scope:clickstream.proto.User)
  private static final User DEFAULT_INSTANCE;
  static {
    User defaultInstance = new User();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      User.class, defaultInstance);
  }

  public static User getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<User> PARSER;

  public static com.google.protobuf.Parser<User> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
