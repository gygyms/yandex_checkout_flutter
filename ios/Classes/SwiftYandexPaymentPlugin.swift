import Flutter
import UIKit
import YandexCheckoutPayments
import YandexCheckoutPaymentsApi

extension UIViewController: TokenizationModuleOutput {
    public func didSuccessfullyPassedCardSec(on module: TokenizationModuleInput) {
    }
    
    public func tokenizationModule(_ module: TokenizationModuleInput,
                          didTokenize token: Tokens,
                          paymentMethodType: PaymentMethodType) {
    DispatchQueue.main.async { [weak self] in
        guard let self = self else { return }
        self.dismiss(animated: true)
    }
    // Отправьте токен в вашу систему
  }

    public func didFinish(on module: TokenizationModuleInput,
                 with error: YandexCheckoutPaymentsError?) {
      DispatchQueue.main.async { [weak self] in
          guard let self = self else { return }
          self.dismiss(animated: true)
      }
  }
}
public class SwiftYandexPaymentPlugin: NSObject, FlutterPlugin, TokenizationModuleOutput {
    public func didFinish(on module: TokenizationModuleInput, with error: YandexCheckoutPaymentsError?) {
        
    }
    
    public func didSuccessfullyPassedCardSec(on module: TokenizationModuleInput) {
        
    }
    
    public func tokenizationModule(_ module: TokenizationModuleInput, didTokenize token: Tokens, paymentMethodType: PaymentMethodType) {
        
    }
    
  
    
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "example.com/yandex", binaryMessenger: registrar.messenger())
    let instance = SwiftYandexPaymentPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    var data: [String: Any] = [:]
    var args:Dictionary = call.arguments as! Dictionary<String, Any?>
    data["paymentToken"]="iOS " + UIDevice.current.systemVersion;
    data["paymentMethodType"]="BANK_CARD";
    let clientApplicationKey = args["clientApplicationKey"] as! String
    let amount = Amount(value: 999.99, currency: .rub)
    let tokenizationModuleInputData =
            TokenizationModuleInputData(clientApplicationKey: clientApplicationKey,
                                              shopName: "Космические объекты",
                                              purchaseDescription: """
                                                                   Комета повышенной яркости, период обращения — 112 лет
                                                                   """,
                                              amount: amount,
                                              tokenizationSettings: TokenizationSettings(
                                                paymentMethodTypes: [PaymentMethodTypes.bankCard,PaymentMethodTypes.applePay], showYandexCheckoutLogo: true
                                                    ),
                                              isLoggingEnabled: true,
                                              savePaymentMethod: .on)
    let inputData: TokenizationFlow = .tokenization(tokenizationModuleInputData)
    let viewController = TokenizationAssembly.makeModule(inputData: inputData,
                                                         moduleOutput: self)
    //viewController.present(viewController, animated: true, completion: nil)
   /* if let navigationController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController {
               navigationController.pushViewController(viewController, animated: true)
           }
  */ UIApplication.shared.delegate?.window??.rootViewController?.present(viewController, animated: true, completion: nil)
    //result(data)
    }
}
