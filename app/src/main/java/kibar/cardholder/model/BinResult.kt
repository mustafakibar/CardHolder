package kibar.cardholder.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * POJO for https://binlist.net/
 */

open class BinResult {
    @SerializedName("number")
    @Expose
    var number: Number? = null

    @SerializedName("scheme")
    @Expose
    var scheme: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("brand")
    @Expose
    var brand: String? = null

    @SerializedName("prepaid")
    @Expose
    var prepaid: Boolean? = null

    @SerializedName("country")
    @Expose
    var country: Country? = null

    @SerializedName("bank")
    @Expose
    var bank: Bank? = null

    val isValid get() = number?.luhn ?: false
}

class Number {
    @SerializedName("length")
    @Expose
    var length: Int? = null

    @SerializedName("luhn")
    @Expose
    var luhn: Boolean? = null
}

class Bank {
    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("url")
    @Expose
    var url: String? = null

    @SerializedName("phone")
    @Expose
    var phone: String? = null

    @SerializedName("city")
    @Expose
    var city: String? = null
}

class Country {
    @SerializedName("numeric")
    @Expose
    var numeric: String? = null

    @SerializedName("alpha2")
    @Expose
    var alpha2: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("emoji")
    @Expose
    var emoji: String? = null

    @SerializedName("currency")
    @Expose
    var currency: String? = null

    @SerializedName("latitude")
    @Expose
    var latitude: Int? = null

    @SerializedName("longitude")
    @Expose
    var longitude: Int? = null
}