import Foundation
import CallKit
import AVFoundation

@objc(CallNoSleep)
class CallNoSleep: CDVPlugin {
    
    var callController = CXCallController()
    var callObserver: CXCallObserver!
    var audioSession: AVAudioSession!
    
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
        UIApplication.shared.isIdleTimerDisabled = true
        self.commandDelegate.send(CDVPluginResult(status: .ok), callbackId: command.callbackId)
    }
    
    @objc(allowSleep:)
    func allowSleep(command: CDVInvokedUrlCommand) {
        UIApplication.shared.isIdleTimerDisabled = false
        self.commandDelegate.send(CDVPluginResult(status: .ok), callbackId: command.callbackId)
    }
}
