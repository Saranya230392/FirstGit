package com.lge.nfclock.service;

import com.lge.nfclock.service.common.UserLockStatusType;
import com.lge.nfclock.service.common.RemoteLockStatusType;
import com.lge.nfclock.service.common.LockResultType;

interface INfcSettingRemoteIF {

    boolean verifyNfcLockNumber(String oldLockNumber);
    boolean changeNfcLockNumber(String oldLockNumber, String newLockNumber);
    boolean verifyNfcPinNumber(String pinNumber);
    boolean initializeNfcLockNumber(String newLockNumber);
    int getRetryCounterForNfcPinNumber();
    boolean isExistLockFile();
    boolean isAvailableOpenChannel();
    boolean isAvailableCashbee();

    UserLockStatusType   getUserLockStatus();
    RemoteLockStatusType getRemoteLockStatus(boolean refreshCLF, boolean refreshUIM, boolean refreshMDM);
    LockResultType       setUserLock(String pwd);
    boolean    setMdmLock();
    boolean    setMdmUnlock();
    LockResultType set3LMLock();
    LockResultType set3LMUnlock(String pw);
    boolean    isExistLockPWFile();
    boolean    initLockNumber(String newLockNumber);
    boolean    checkNfcApduReady();
    boolean    setCen(int state);
}
