/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Android\\Workspace\\Google\\LGSettingsAPK\\app\\src\\main\\aidl\\com\\lge\\nfclock\\service\\INfcSettingRemoteIF.aidl
 */
package com.lge.nfclock.service;
public interface INfcSettingRemoteIF extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.lge.nfclock.service.INfcSettingRemoteIF
{
private static final java.lang.String DESCRIPTOR = "com.lge.nfclock.service.INfcSettingRemoteIF";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.lge.nfclock.service.INfcSettingRemoteIF interface,
 * generating a proxy if needed.
 */
public static com.lge.nfclock.service.INfcSettingRemoteIF asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.lge.nfclock.service.INfcSettingRemoteIF))) {
return ((com.lge.nfclock.service.INfcSettingRemoteIF)iin);
}
return new com.lge.nfclock.service.INfcSettingRemoteIF.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_verifyNfcLockNumber:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.verifyNfcLockNumber(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_changeNfcLockNumber:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.changeNfcLockNumber(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_verifyNfcPinNumber:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.verifyNfcPinNumber(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_initializeNfcLockNumber:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.initializeNfcLockNumber(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getRetryCounterForNfcPinNumber:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getRetryCounterForNfcPinNumber();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_isExistLockFile:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isExistLockFile();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isAvailableOpenChannel:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isAvailableOpenChannel();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isAvailableCashbee:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isAvailableCashbee();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getUserLockStatus:
{
data.enforceInterface(DESCRIPTOR);
com.lge.nfclock.service.common.UserLockStatusType _result = this.getUserLockStatus();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getRemoteLockStatus:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _arg2;
_arg2 = (0!=data.readInt());
com.lge.nfclock.service.common.RemoteLockStatusType _result = this.getRemoteLockStatus(_arg0, _arg1, _arg2);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_setUserLock:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.lge.nfclock.service.common.LockResultType _result = this.setUserLock(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_setMdmLock:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.setMdmLock();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setMdmUnlock:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.setMdmUnlock();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_set3LMLock:
{
data.enforceInterface(DESCRIPTOR);
com.lge.nfclock.service.common.LockResultType _result = this.set3LMLock();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_set3LMUnlock:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.lge.nfclock.service.common.LockResultType _result = this.set3LMUnlock(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_isExistLockPWFile:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isExistLockPWFile();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_initLockNumber:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.initLockNumber(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_checkNfcApduReady:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.checkNfcApduReady();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setCen:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.setCen(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.lge.nfclock.service.INfcSettingRemoteIF
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public boolean verifyNfcLockNumber(java.lang.String oldLockNumber) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(oldLockNumber);
mRemote.transact(Stub.TRANSACTION_verifyNfcLockNumber, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean changeNfcLockNumber(java.lang.String oldLockNumber, java.lang.String newLockNumber) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(oldLockNumber);
_data.writeString(newLockNumber);
mRemote.transact(Stub.TRANSACTION_changeNfcLockNumber, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean verifyNfcPinNumber(java.lang.String pinNumber) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pinNumber);
mRemote.transact(Stub.TRANSACTION_verifyNfcPinNumber, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean initializeNfcLockNumber(java.lang.String newLockNumber) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(newLockNumber);
mRemote.transact(Stub.TRANSACTION_initializeNfcLockNumber, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getRetryCounterForNfcPinNumber() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getRetryCounterForNfcPinNumber, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isExistLockFile() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isExistLockFile, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isAvailableOpenChannel() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isAvailableOpenChannel, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isAvailableCashbee() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isAvailableCashbee, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public com.lge.nfclock.service.common.UserLockStatusType getUserLockStatus() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.lge.nfclock.service.common.UserLockStatusType _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getUserLockStatus, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.lge.nfclock.service.common.UserLockStatusType.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public com.lge.nfclock.service.common.RemoteLockStatusType getRemoteLockStatus(boolean refreshCLF, boolean refreshUIM, boolean refreshMDM) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.lge.nfclock.service.common.RemoteLockStatusType _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((refreshCLF)?(1):(0)));
_data.writeInt(((refreshUIM)?(1):(0)));
_data.writeInt(((refreshMDM)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_getRemoteLockStatus, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.lge.nfclock.service.common.RemoteLockStatusType.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public com.lge.nfclock.service.common.LockResultType setUserLock(java.lang.String pwd) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.lge.nfclock.service.common.LockResultType _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pwd);
mRemote.transact(Stub.TRANSACTION_setUserLock, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.lge.nfclock.service.common.LockResultType.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean setMdmLock() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_setMdmLock, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean setMdmUnlock() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_setMdmUnlock, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public com.lge.nfclock.service.common.LockResultType set3LMLock() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.lge.nfclock.service.common.LockResultType _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_set3LMLock, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.lge.nfclock.service.common.LockResultType.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public com.lge.nfclock.service.common.LockResultType set3LMUnlock(java.lang.String pw) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.lge.nfclock.service.common.LockResultType _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pw);
mRemote.transact(Stub.TRANSACTION_set3LMUnlock, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.lge.nfclock.service.common.LockResultType.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isExistLockPWFile() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isExistLockPWFile, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean initLockNumber(java.lang.String newLockNumber) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(newLockNumber);
mRemote.transact(Stub.TRANSACTION_initLockNumber, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean checkNfcApduReady() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_checkNfcApduReady, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean setCen(int state) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(state);
mRemote.transact(Stub.TRANSACTION_setCen, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_verifyNfcLockNumber = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_changeNfcLockNumber = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_verifyNfcPinNumber = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_initializeNfcLockNumber = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getRetryCounterForNfcPinNumber = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_isExistLockFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_isAvailableOpenChannel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_isAvailableCashbee = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getUserLockStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getRemoteLockStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_setUserLock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_setMdmLock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_setMdmUnlock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_set3LMLock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_set3LMUnlock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_isExistLockPWFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_initLockNumber = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_checkNfcApduReady = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_setCen = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
}
public boolean verifyNfcLockNumber(java.lang.String oldLockNumber) throws android.os.RemoteException;
public boolean changeNfcLockNumber(java.lang.String oldLockNumber, java.lang.String newLockNumber) throws android.os.RemoteException;
public boolean verifyNfcPinNumber(java.lang.String pinNumber) throws android.os.RemoteException;
public boolean initializeNfcLockNumber(java.lang.String newLockNumber) throws android.os.RemoteException;
public int getRetryCounterForNfcPinNumber() throws android.os.RemoteException;
public boolean isExistLockFile() throws android.os.RemoteException;
public boolean isAvailableOpenChannel() throws android.os.RemoteException;
public boolean isAvailableCashbee() throws android.os.RemoteException;
public com.lge.nfclock.service.common.UserLockStatusType getUserLockStatus() throws android.os.RemoteException;
public com.lge.nfclock.service.common.RemoteLockStatusType getRemoteLockStatus(boolean refreshCLF, boolean refreshUIM, boolean refreshMDM) throws android.os.RemoteException;
public com.lge.nfclock.service.common.LockResultType setUserLock(java.lang.String pwd) throws android.os.RemoteException;
public boolean setMdmLock() throws android.os.RemoteException;
public boolean setMdmUnlock() throws android.os.RemoteException;
public com.lge.nfclock.service.common.LockResultType set3LMLock() throws android.os.RemoteException;
public com.lge.nfclock.service.common.LockResultType set3LMUnlock(java.lang.String pw) throws android.os.RemoteException;
public boolean isExistLockPWFile() throws android.os.RemoteException;
public boolean initLockNumber(java.lang.String newLockNumber) throws android.os.RemoteException;
public boolean checkNfcApduReady() throws android.os.RemoteException;
public boolean setCen(int state) throws android.os.RemoteException;
}
