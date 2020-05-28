import android.app.Activity
import android.content.Intent
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import ru.yandex.money.android.sdk.*
import java.lang.Exception
import java.math.BigDecimal
import java.util.*

private const val CHANNEL = "example.com/yandex"
private const val REQUEST_TOKENIZE_CODE = 99
private const val REQUEST_CONFIRMATION_CODE = 98

const val PAYMENT_CHANNEL_METHOD = "showYandexPayment"
const val CONFIRMATION_CHANNEL_METHOD = "confirm"

class YandexCheckoutFlutter {
    private var registrar: PluginRegistry.Registrar
    private var channelResult: MethodChannel.Result?

    constructor(registrar: PluginRegistry.Registrar) {
        this.registrar = registrar
        this.channelResult = null

        registerForYandexPayment()
    }

    private fun registerForYandexPayment() {
        MethodChannel(registrar.view(), CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                PAYMENT_CHANNEL_METHOD -> try {
                    channelResult = result
                    showYandexPayment(parsePaymentParameters(call.arguments))
                } catch (e: Exception) {
                    result.error(e.javaClass.name, e.message, e)
                }
                CONFIRMATION_CHANNEL_METHOD -> try {
                    channelResult = result
                    val map = call.arguments as Map<*, *>
                    timeToStart3DS(map["url"] as String)
                } catch (e: Exception) {
                    result.error(e.javaClass.name, e.message, e)
                }
            }
        }

        registrar.addActivityResultListener { requestCode, resultCode, data : Intent? ->
            channelResult?.let { onActivityResult(requestCode, resultCode, data) } ?: false
        }
    }

    fun timeToStart3DS(url:String) {
        val intent: Intent = Checkout.create3dsIntent(
                registrar.activity(),
                url
        )
        registrar.activity().startActivityForResult(intent, REQUEST_CONFIRMATION_CODE)
    }

    private fun showYandexPayment(paymentParameters: PaymentParameters) {
        val tokenizeIntent = Checkout.createTokenizeIntent(
                registrar.activity(),
                paymentParameters
        )

        registrar.activity().startActivityForResult(
                tokenizeIntent, REQUEST_TOKENIZE_CODE)
    }

    private fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?
    ) : Boolean {
        //    if (requestCode != REQUEST_TOKENIZE_CODE || requestCode != REQUEST_CONFIRMATION_CODE) return false

        when (resultCode) {
            Activity.RESULT_OK -> {
                if (requestCode == REQUEST_TOKENIZE_CODE) {
                    if (data != null) {
                        val tokenizationResult = Checkout.createTokenizationResult(data)
                        channelResult?.success(mapOf(
                                "paymentMethodType" to tokenizationResult.paymentMethodType.toString(),
                                "paymentToken" to tokenizationResult.paymentToken
                        ))
                    } else {
                        channelResult?.error("RESULT_NULL", "Received tokenization result is null", null)
                    }
                }
                if (requestCode == REQUEST_CONFIRMATION_CODE) {
                    if (data != null) {
                        channelResult?.success("ok")
                    } else {
                        channelResult?.error("RESULT_NULL", "Received tokenization result is null", null)
                    }
                }
            }

            Activity.RESULT_CANCELED -> {
                channelResult?.error("RESULT_CANCELED", "Tokenization was canceled by user", null)
            }
        }

        return true
    }


    private fun parsePaymentParameters(channelArgs: Any) : PaymentParameters {
        val map = channelArgs as Map<*, *>

        return PaymentParameters(
                title = map["title"] as String,
                subtitle = map["subtitle"] as String,
                amount = parseAmount(map["amount"] as Map<*, *>),
                clientApplicationKey = map["clientApplicationKey"] as String,
                shopId = map["shopId"] as String,
                paymentMethodTypes = (map["paymentMethodTypes"] as List<*>?)
                        ?.map { PaymentMethodType.valueOf(it as String) }
                        ?.toSet()
                        ?: PaymentMethodType.values().toSet(),
                gatewayId = map["gatewayId"] as String?,
                customReturnUrl = map["customReturnUrl"] as String?,
                userPhoneNumber = map["userPhoneNumber"] as String?,
                googlePayParameters = parseGooglePayParameters(map) ?: GooglePayParameters(),
                savePaymentMethod = SavePaymentMethod.OFF
        )
    }

    private fun parseAmount(amountMap: Map<*, *>) : Amount {
        return Amount(
                value = BigDecimal.valueOf(amountMap["value"] as Double),
                currency = Currency.getInstance(amountMap["currency"] as String)
        )
    }

    private fun parseGooglePayParameters(map: Map<*, *>) : GooglePayParameters? {
        val googlePayParametersMap = (map["googlePayParameters"] as Map<*, *>?)
        val googlePayCardNetworks = (googlePayParametersMap?.get("allowedCardNetworks") as List<*>?)
                ?.map { GooglePayCardNetwork.valueOf(it as String) }
                ?.toSet()

        return if (googlePayCardNetworks != null)
            GooglePayParameters(googlePayCardNetworks)
        else null
    }
}
