import Foundation
import CallKit
import AVFoundation
import MSAL

@objc(CallNoSleep)
class CallNoSleep: CDVPlugin {
    
    var callController = CXCallController()
    var callObserver: CXCallObserver!
    var audioSession: AVAudioSession!
    var msalClient: MSALPublicClientApplication?
    
    @objc(startCall:)
    func startCall(command: CDVInvokedUrlCommand) {
        let phoneNumber = command.arguments[0] as? String ?? ""
        let callHandle = CXHandle(type: .phoneNumber, value: phoneNumber)
        let startCallAction = CXStartCallAction(call: UUID(), handle: callHandle)
        let transaction = CXTransaction(action: startCallAction)
        
        callController.request(transaction) { error in
            if let error = error {
                self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: error.localizedDescription), callbackId: command.callbackId)
            } else {
                self.commandDelegate.send(CDVPluginResult(status: .ok), callbackId: command.callbackId)
            }
        }
    }
    
    @objc(preventSleep:)
    func preventSleep(command: CDVInvokedUrlCommand) {
        do {
            try UIApplication.shared.isIdleTimerDisabled = true
            self.commandDelegate.send(CDVPluginResult(status: .ok), callbackId: command.callbackId)
        } catch {
            self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "Failed to prevent sleep: \(error.localizedDescription)"), callbackId: command.callbackId)
        }
    }
    
    @objc(allowSleep:)
    func allowSleep(command: CDVInvokedUrlCommand) {
        do {
            try UIApplication.shared.isIdleTimerDisabled = false
            self.commandDelegate.send(CDVPluginResult(status: .ok), callbackId: command.callbackId)
        } catch {
            self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "Failed to allow sleep: \(error.localizedDescription)"), callbackId: command.callbackId)
        }
    }
    
    @objc(getAppInfo:)
    func getAppInfo(command: CDVInvokedUrlCommand) {
        let appInfo = [
            "versionName": Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "",
            "versionCode": Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "",
            "packageName": Bundle.main.bundleIdentifier ?? ""
        ]
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: appInfo)
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }
    
    @objc(loginWithMicrosoft:)
    func loginWithMicrosoft(command: CDVInvokedUrlCommand) {
        do {
            let config = try MSALPublicClientApplicationConfig(clientId: "YOUR_CLIENT_ID", redirectUri: nil, authority: nil)
            msalClient = try MSALPublicClientApplication(configuration: config)
            
            let webViewParameters = MSALWebviewParameters(authPresentationViewController: self.viewController)
            let interactiveParameters = MSALInteractiveTokenParameters(scopes: ["User.Read"], webviewParameters: webViewParameters)
            
            msalClient?.acquireToken(with: interactiveParameters) { (result, error) in
                if let error = error {
                    self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: error.localizedDescription), callbackId: command.callbackId)
                } else if let result = result {
                    self.commandDelegate.send(CDVPluginResult(status: .ok, messageAs: result.account.username), callbackId: command.callbackId)
                }
            }
        } catch {
            self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: error.localizedDescription), callbackId: command.callbackId)
        }
    }
}
