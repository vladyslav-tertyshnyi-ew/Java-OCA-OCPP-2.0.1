package eu.chargetime.ocpp.model.dataTypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.chargetime.ocpp.model.Validatable;
import eu.chargetime.ocpp.model.dataTypes.enums.AuthorizationStatusEnumType;
import eu.chargetime.ocpp.model.validation.OCPP2PrimDatatypes;
import eu.chargetime.ocpp.model.validation.RequiredValidator;
import eu.chargetime.ocpp.model.validation.Validator;
import eu.chargetime.ocpp.model.validation.ValidatorBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;
import java.util.List;


/**
 * ID_ Token
 * urn:x-oca:ocpp:uid:2:233247
 * Contains status information about an identifier.
 * It is advised to not stop charging for a token that expires during charging, as ExpiryDate is only used for caching purposes. If ExpiryDate is not given, the status has no end date.
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "customData",
        "status",
        "cacheExpiryDateTime",
        "chargingPriority",
        "language1",
        "evseId",
        "groupIdToken",
        "language2",
        "personalMessage"
})
@Getter
@EqualsAndHashCode
public class IdTokenInfo implements Validatable {

    private transient Validator<Object> requiredValidator = new RequiredValidator();
    private transient Validator language1Validator = new ValidatorBuilder().addRule(OCPP2PrimDatatypes.string8()).build();
    private transient Validator language2Validator = new ValidatorBuilder().addRule(OCPP2PrimDatatypes.string8()).build();

    /**
     * This class does not get 'AdditionalProperties = false' in the schema generation, so it can be extended with arbitrary JSON properties to allow adding custom data.
     *
     */
    @JsonProperty("customData")
    public CustomData customData;
    /**
     * ID_ Token. Status. Authorization_ Status
     * urn:x-oca:ocpp:uid:1:569372
     * Current status of the ID Token.
     *
     * (Required)
     *
     */
    @JsonProperty("status")
    public AuthorizationStatusEnumType status;
    /**
     * ID_ Token. Expiry. Date_ Time
     * urn:x-oca:ocpp:uid:1:569373
     * Date and Time after which the token must be considered invalid.
     *
     *
     */
    @JsonProperty("cacheExpiryDateTime")
    public Date cacheExpiryDateTime;
    /**
     * Priority from a business point of view. Default priority is 0, The range is from -9 to 9. Higher values indicate a higher priority. The chargingPriority in &lt;<transactioneventresponse,TransactionEventResponse>> overrules this one.
     *
     *
     */
    @JsonProperty("chargingPriority")
    public Integer chargingPriority;
    /**
     * ID_ Token. Language1. Language_ Code
     * urn:x-oca:ocpp:uid:1:569374
     * Preferred user interface language of identifier user. Contains a language code as defined in &lt;<ref-RFC5646,[RFC5646]>>.
     *
     *
     *
     */
    @JsonProperty("language1")
    public String language1;
    /**
     * Only used when the IdToken is only valid for one or more specific EVSEs, not for the entire Charging Station.
     *
     *
     *
     */
    @JsonProperty("evseId")
    public List<Integer> evseId;
    /**
     * Contains a case insensitive identifier to use for the authorization and the type of authorization to support multiple forms of identifiers.
     *
     *
     */
    @JsonProperty("groupIdToken")
    public IdTokenType groupIdTokenType;
    /**
     * ID_ Token. Language2. Language_ Code
     * urn:x-oca:ocpp:uid:1:569375
     * Second preferred user interface language of identifier user. Don’t use when language1 is omitted, has to be different from language1. Contains a language code as defined in &lt;<ref-RFC5646,[RFC5646]>>.
     *
     *
     */
    @JsonProperty("language2")
    public String language2;
    /**
     * Message_ Content
     * urn:x-enexis:ecdm:uid:2:234490
     * Contains message details, for a message to be displayed on a Charging Station.
     *
     *
     *
     */
    @JsonProperty("personalMessage")
    public MessageContent personalMessage;

    public IdTokenInfo(AuthorizationStatusEnumType status) {
        requiredValidator.validate(status);
        this.status = status;
    }

    public void setCustomData(CustomData customData) {
        this.customData = customData;
    }

    public void setStatus(AuthorizationStatusEnumType status) {
        requiredValidator.validate(status);
        this.status = status;
    }

    public void setCacheExpiryDateTime(Date cacheExpiryDateTime) {
        this.cacheExpiryDateTime = cacheExpiryDateTime;
    }

    public void setChargingPriority(Integer chargingPriority) {
        this.chargingPriority = chargingPriority;
    }

    public void setLanguage1(String language1) {
        language1Validator.validate(language1);
        this.language1 = language1;
    }

    public void setEvseId(List<Integer> evseId) {
        this.evseId = evseId;
    }

    public void setGroupIdTokenType(IdTokenType groupIdTokenType) {
        this.groupIdTokenType = groupIdTokenType;
    }

    public void setLanguage2(String language2) {
        language2Validator.validate(language2);
        this.language2 = language2;
    }

    public void setPersonalMessage(MessageContent personalMessage) {
        this.personalMessage = personalMessage;
    }

    @Override
    public boolean validate() {
        return requiredValidator.safeValidate(status);
    }
}
