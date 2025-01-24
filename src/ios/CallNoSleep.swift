import Foundation
import CallKit
import AVFoundation

@objc(CallNoSleep)
class CallNoSleep: CDVPlugin {
    
    var callController = CXCallController()
    var callObserver: CXCallObserver!
    var audioSession: AVAudioSession!
    var provider: CXProvider!
    
    override func pluginInitialize() {
        super.pluginInitialize()
        
        let providerConfiguration = CXProviderConfiguration(localizedName: "CallNoSleep")
        providerConfiguration.supportsVideo = false
        providerConfiguration.maximumCallsPerCallGroup = 1
        providerConfiguration.supportedHandleTypes = [.phoneNumber]
        
        provider = CXProvider(configuration: providerConfiguration)
        provider.setDelegate(self, queue: nil)
        
        callObserver = CXCallObserver()
        callObserver.setDelegate(self, queue: nil)
        
        audioSession = AVAudioSession.sharedInstance()
    }
    
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
                self.configureAudioSession()
                self.commandDelegate.send(CDVPluginResult(status: .ok), callbackId: command.callbackId)
            }
        }
    }
    
    func configureAudioSession() {
        do {
            try audioSession.setCategory(.playAndRecord, mode: .voiceChat, options: [])
            try audioSession.setActive(true, options: [])
        } catch {
            print("Failed to configure audio session: \(error.localizedDescription)")
        }
    }
    
    @objc(preventSleep:)
    func preventSleep(command: CDVInvokedUrlCommand) {
        UIApplication.shared.isIdleTimerDisabled = true
        self.commandDelegate.send(CDVPluginResult(status: .ok), callbackId: command.callbackId)
    }
    
    @objc(allowSleep:)
    func allowSleep(command: CDVInvokedUrlCommand) {
        UIApplication.shared.isIdleTimerDisabled = false
        self.commandDelegate.send(CDVPluginResult(status: .ok), callbackId: command.callbackId)
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
}

extension CallNoSleep: CXProviderDelegate {
    func provider(_ provider: CXProvider, perform action: CXStartCallAction) {
        configureAudioSession()
        action.fulfill()
    }
    
    func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        action.fulfill()
    }
    
    func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        // Audio session activated
    }
    
    func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
        // Audio session deactivated
    }
}

extension CallNoSleep: CXCallObserverDelegate {
    func callObserver(_ callObserver: CXCallObserver, callChanged call: CXCall) {
        if call.hasEnded {
            do {
                try audioSession.setActive(false, options: [])
            } catch {
                print("Failed to deactivate audio session: \(error.localizedDescription)")
            }
        }
    }
}
