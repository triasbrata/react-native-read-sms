declare type ReadSMSCallback = (status: string, sms: Record<string, any>, error?: string) => void;
export declare function startReadSMS(callback: ReadSMSCallback): Promise<void>;
export declare function requestReadSMSPermission(): Promise<boolean>;
export declare function stopReadSMS(): void;
export {};
