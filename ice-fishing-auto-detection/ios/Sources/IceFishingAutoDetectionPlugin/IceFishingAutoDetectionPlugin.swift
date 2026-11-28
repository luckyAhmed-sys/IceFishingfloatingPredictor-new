import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(IceFishingAutoDetectionPlugin)
public class IceFishingAutoDetectionPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "IceFishingAutoDetectionPlugin"
    public let jsName = "IceFishingAutoDetection"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = IceFishingAutoDetection()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
}
