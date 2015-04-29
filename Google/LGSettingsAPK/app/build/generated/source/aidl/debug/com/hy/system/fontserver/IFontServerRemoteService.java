/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Android\\Workspace\\Google\\LGSettingsAPK\\app\\src\\main\\aidl\\com\\hy\\system\\fontserver\\IFontServerRemoteService.aidl
 */
package com.hy.system.fontserver;
public interface IFontServerRemoteService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.hy.system.fontserver.IFontServerRemoteService
{
private static final java.lang.String DESCRIPTOR = "com.hy.system.fontserver.IFontServerRemoteService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.hy.system.fontserver.IFontServerRemoteService interface,
 * generating a proxy if needed.
 */
public static com.hy.system.fontserver.IFontServerRemoteService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.hy.system.fontserver.IFontServerRemoteService))) {
return ((com.hy.system.fontserver.IFontServerRemoteService)iin);
}
return new com.hy.system.fontserver.IFontServerRemoteService.Stub.Proxy(obj);
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
case TRANSACTION_updateFontServer:
{
data.enforceInterface(DESCRIPTOR);
this.updateFontServer();
reply.writeNoException();
return true;
}
case TRANSACTION_selectDefaultTypeface:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.selectDefaultTypeface(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getDefaultTypefaceIndex:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getDefaultTypefaceIndex();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getNumAllFonts:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getNumAllFonts();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getNumEmbeddedFonts:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getNumEmbeddedFonts();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getSummary:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getSummary();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getAllFontNames:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String[] _result = this.getAllFontNames();
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_getAllFontWebFaceNames:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String[] _result = this.getAllFontWebFaceNames();
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_getAllFontFullPath:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String[] _result = this.getAllFontFullPath();
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_getFontFullPath:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.getFontFullPath(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getDownloadFontSrcPath:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getDownloadFontSrcPath();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getDownloadFontDstPath:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getDownloadFontDstPath();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getSystemDefaultTypefaceIndex:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getSystemDefaultTypefaceIndex();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getDownloadFontAppName:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.getDownloadFontAppName(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_setDefault:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.setDefault();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.hy.system.fontserver.IFontServerRemoteService
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
@Override public void updateFontServer() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_updateFontServer, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void selectDefaultTypeface(int fontIndex) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(fontIndex);
mRemote.transact(Stub.TRANSACTION_selectDefaultTypeface, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getDefaultTypefaceIndex() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDefaultTypefaceIndex, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getNumAllFonts() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getNumAllFonts, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getNumEmbeddedFonts() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getNumEmbeddedFonts, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getSummary() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSummary, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String[] getAllFontNames() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAllFontNames, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String[] getAllFontWebFaceNames() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAllFontWebFaceNames, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String[] getAllFontFullPath() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAllFontFullPath, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getFontFullPath(int fontIndex) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(fontIndex);
mRemote.transact(Stub.TRANSACTION_getFontFullPath, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getDownloadFontSrcPath() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDownloadFontSrcPath, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getDownloadFontDstPath() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDownloadFontDstPath, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getSystemDefaultTypefaceIndex() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSystemDefaultTypefaceIndex, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// 20140205 gaenoo.lee@lge.com Apply Font type delete feature [S]

@Override public java.lang.String getDownloadFontAppName(int fontIndex) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(fontIndex);
mRemote.transact(Stub.TRANSACTION_getDownloadFontAppName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// 20140205 gaenoo.lee@lge.com Apply Font type delete feature [E]

@Override public int setDefault() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_setDefault, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_updateFontServer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_selectDefaultTypeface = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getDefaultTypefaceIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getNumAllFonts = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getNumEmbeddedFonts = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getSummary = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getAllFontNames = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getAllFontWebFaceNames = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getAllFontFullPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getFontFullPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_getDownloadFontSrcPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getDownloadFontDstPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_getSystemDefaultTypefaceIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_getDownloadFontAppName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_setDefault = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
}
public void updateFontServer() throws android.os.RemoteException;
public void selectDefaultTypeface(int fontIndex) throws android.os.RemoteException;
public int getDefaultTypefaceIndex() throws android.os.RemoteException;
public int getNumAllFonts() throws android.os.RemoteException;
public int getNumEmbeddedFonts() throws android.os.RemoteException;
public java.lang.String getSummary() throws android.os.RemoteException;
public java.lang.String[] getAllFontNames() throws android.os.RemoteException;
public java.lang.String[] getAllFontWebFaceNames() throws android.os.RemoteException;
public java.lang.String[] getAllFontFullPath() throws android.os.RemoteException;
public java.lang.String getFontFullPath(int fontIndex) throws android.os.RemoteException;
public java.lang.String getDownloadFontSrcPath() throws android.os.RemoteException;
public java.lang.String getDownloadFontDstPath() throws android.os.RemoteException;
public int getSystemDefaultTypefaceIndex() throws android.os.RemoteException;
// 20140205 gaenoo.lee@lge.com Apply Font type delete feature [S]

public java.lang.String getDownloadFontAppName(int fontIndex) throws android.os.RemoteException;
// 20140205 gaenoo.lee@lge.com Apply Font type delete feature [E]

public int setDefault() throws android.os.RemoteException;
}
