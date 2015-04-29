/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Android\\Workspace\\JPIME\\tags\\20130920\\iWnnIME\\src\\jp\\co\\omronsoft\\iwnnime\\ml\\IMorphemeService.aidl
 */
package jp.co.omronsoft.iwnnime.ml;
/**
 * API for iWnn Morphological analysis.
 */
public interface IMorphemeService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements jp.co.omronsoft.iwnnime.ml.IMorphemeService
{
private static final java.lang.String DESCRIPTOR = "jp.co.omronsoft.iwnnime.ml.IMorphemeService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an jp.co.omronsoft.iwnnime.ml.IMorphemeService interface,
 * generating a proxy if needed.
 */
public static jp.co.omronsoft.iwnnime.ml.IMorphemeService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof jp.co.omronsoft.iwnnime.ml.IMorphemeService))) {
return ((jp.co.omronsoft.iwnnime.ml.IMorphemeService)iin);
}
return new jp.co.omronsoft.iwnnime.ml.IMorphemeService.Stub.Proxy(obj);
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
case TRANSACTION_splitWord:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
android.os.Bundle _result = this.splitWord(_arg0, _arg1);
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
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements jp.co.omronsoft.iwnnime.ml.IMorphemeService
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
/**
     * Get the result of Morphological analysis.
     * 
     * @param input     Input string.
     * @param readingsMax   Maximum count of getting readings.
     * @return The result of Morphological analysis.
     */
@Override public android.os.Bundle splitWord(java.lang.String input, int readingsMax) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(input);
_data.writeInt(readingsMax);
mRemote.transact(Stub.TRANSACTION_splitWord, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.os.Bundle.CREATOR.createFromParcel(_reply);
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
}
static final int TRANSACTION_splitWord = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
/**
     * Get the result of Morphological analysis.
     * 
     * @param input     Input string.
     * @param readingsMax   Maximum count of getting readings.
     * @return The result of Morphological analysis.
     */
public android.os.Bundle splitWord(java.lang.String input, int readingsMax) throws android.os.RemoteException;
}