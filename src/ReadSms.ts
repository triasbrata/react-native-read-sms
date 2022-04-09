import {
  NativeEventEmitter,
  NativeModules,
  PermissionsAndroid,
  Platform,
} from "react-native";
type ReadSMSCallback = (
  status: string,
  sms: Record<string, any>,
  error?: string
) => void;
export async function startReadSMS(callback: ReadSMSCallback): Promise<void> {
  if (Platform.OS === "android") {
    const nativeEventEmitter = new NativeEventEmitter();
    if (nativeEventEmitter.listenerCount("received_sms") > 0) {
      nativeEventEmitter.removeAllListeners("received_sms");
    }
    const hasPermission = await hasSMSPermission();
    if (hasPermission) {
      NativeModules.ReadSms.startReadSMS(
        () => {
          nativeEventEmitter.addListener(
            "received_sms",
            (sms: Record<string, any>) => {
              console.log({ sms });
              callback("success", sms);
            }
          );
        },
        (error: string) => {
          callback("error", {}, error);
        }
      );
    } else {
      callback("error", {}, "Required RECEIVE_SMS and READ_SMS permission");
    }
  } else {
    callback("error", "", "ReadSms Plugin is only for android platform");
  }
}

const hasSMSPermission = async () => {
  if (Platform.OS === "android" && Platform.Version < 23) {
    return true;
  }
  const hasReceiveSmsPermission = await PermissionsAndroid.check(
    PermissionsAndroid.PERMISSIONS.RECEIVE_SMS
  );
  const hasReadSmsPermission = await PermissionsAndroid.check(
    PermissionsAndroid.PERMISSIONS.READ_SMS
  );
  if (hasReceiveSmsPermission && hasReadSmsPermission) return true;
  return false;
};

export async function requestReadSMSPermission(): Promise<boolean> {
  if (Platform.OS === "android") {
    const hasPermission = await hasSMSPermission();
    if (hasPermission) return true;
    const status = await PermissionsAndroid.requestMultiple([
      PermissionsAndroid.PERMISSIONS.RECEIVE_SMS,
      PermissionsAndroid.PERMISSIONS.READ_SMS,
    ]);
    console.log(status["android.permission.RECEIVE_SMS"]);
    // if (status === PermissionsAndroid.RESULTS.GRANTED) return true;
    // if (status === PermissionsAndroid.RESULTS.DENIED) {
    //   console.log("Read Sms permission denied by user.", status);
    // } else if (status === PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN) {
    //   console.log("Read Sms permission revoked by user.", status);
    // }
    return false;
  }
  return true;
}

export function stopReadSMS() {
  if (Platform.OS === "android") {
    NativeModules.ReadSms.stopReadSMS();
  }
}
