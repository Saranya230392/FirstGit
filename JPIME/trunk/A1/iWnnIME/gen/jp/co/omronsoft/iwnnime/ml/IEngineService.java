/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Android\\Workspace\\JPIME\\trunk\\A1\\iWnnIME\\src\\jp\\co\\omronsoft\\iwnnime\\ml\\IEngineService.aidl
 */
package jp.co.omronsoft.iwnnime.ml;
/**
 * iWnnEngine Service.
 */
public interface IEngineService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements jp.co.omronsoft.iwnnime.ml.IEngineService
{
private static final java.lang.String DESCRIPTOR = "jp.co.omronsoft.iwnnime.ml.IEngineService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an jp.co.omronsoft.iwnnime.ml.IEngineService interface,
 * generating a proxy if needed.
 */
public static jp.co.omronsoft.iwnnime.ml.IEngineService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof jp.co.omronsoft.iwnnime.ml.IEngineService))) {
return ((jp.co.omronsoft.iwnnime.ml.IEngineService)iin);
}
return new jp.co.omronsoft.iwnnime.ml.IEngineService.Stub.Proxy(obj);
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
case TRANSACTION_init:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
this.init(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_predict:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _result = this.predict(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setEnableConsecutivePhraseLevelConversion:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _arg1;
_arg1 = (0!=data.readInt());
int _result = this.setEnableConsecutivePhraseLevelConversion(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getNextCandidate:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
android.os.Bundle _result = this.getNextCandidate(_arg0, _arg1);
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
case TRANSACTION_getNextCandidateWithAnnotation:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
android.os.Bundle _result = this.getNextCandidateWithAnnotation(_arg0, _arg1);
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
case TRANSACTION_getNextCandidateWithAnnotation2:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
android.os.Bundle _result = this.getNextCandidateWithAnnotation2(_arg0, _arg1, _arg2);
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
case TRANSACTION_learnCandidate:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
boolean _result = this.learnCandidate(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_convert:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
android.os.Bundle _result = this.convert(_arg0, _arg1, _arg2);
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
case TRANSACTION_convertWithAnnotation:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
android.os.Bundle _result = this.convertWithAnnotation(_arg0, _arg1, _arg2);
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
case TRANSACTION_convertWithAnnotation2:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
android.os.Bundle _result = this.convertWithAnnotation2(_arg0, _arg1, _arg2, _arg3);
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
case TRANSACTION_addWord:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
int _result = this.addWord(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_addWordDetail:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
int _arg3;
_arg3 = data.readInt();
int _arg4;
_arg4 = data.readInt();
int _arg5;
_arg5 = data.readInt();
int _result = this.addWordDetail(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_searchWords:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _result = this.searchWords(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_searchWordsDetail:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _result = this.searchWordsDetail(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_deleteWord:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
boolean _result = this.deleteWord(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_writeoutDictionary:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.writeoutDictionary(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_initializeDictionary:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.initializeDictionary(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setUserDictionary:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setUserDictionary(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setLearnDictionary:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setLearnDictionary(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setNormalDictionary:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setNormalDictionary(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_learnCandidateNoStore:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
boolean _result = this.learnCandidateNoStore(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_learnCandidateNoConnect:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
boolean _result = this.learnCandidateNoConnect(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_learnWord:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
boolean _result = this.learnWord(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_learnWordNoStore:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
boolean _result = this.learnWordNoStore(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_learnWordNoConnect:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
boolean _result = this.learnWordNoConnect(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setDictionary:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
boolean _arg4;
_arg4 = (0!=data.readInt());
boolean _arg5;
_arg5 = (0!=data.readInt());
boolean _arg6;
_arg6 = (0!=data.readInt());
boolean _arg7;
_arg7 = (0!=data.readInt());
boolean _arg8;
_arg8 = (0!=data.readInt());
boolean _arg9;
_arg9 = (0!=data.readInt());
boolean _result = this.setDictionary(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setDictionaryDecoratedPict:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
boolean _arg4;
_arg4 = (0!=data.readInt());
boolean _arg5;
_arg5 = (0!=data.readInt());
boolean _arg6;
_arg6 = (0!=data.readInt());
boolean _arg7;
_arg7 = (0!=data.readInt());
boolean _arg8;
_arg8 = (0!=data.readInt());
boolean _arg9;
_arg9 = (0!=data.readInt());
boolean _arg10;
_arg10 = (0!=data.readInt());
boolean _result = this.setDictionaryDecoratedPict(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, _arg10);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_undo:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.undo(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isGijiDic:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
boolean _result = this.isGijiDic(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setGijiFilter:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int[] _arg1;
_arg1 = data.createIntArray();
boolean _result = this.setGijiFilter(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
reply.writeIntArray(_arg1);
return true;
}
case TRANSACTION_startInput:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.startInput(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getStatus:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.getStatus(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getDictionaryType:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.getDictionaryType(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getErrorCode:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.getErrorCode(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_disconnect:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.disconnect(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_isAlive:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.isAlive(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements jp.co.omronsoft.iwnnime.ml.IEngineService
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
     * Initialize the internal state of the iWnn engine.
     *
     * @param packageName   PackageName.
     * @param password      Password.
     * @param initLevel     Initialize level.
     */
@Override public void init(java.lang.String packageName, java.lang.String password, int initLevel) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(password);
_data.writeInt(initLevel);
mRemote.transact(Stub.TRANSACTION_init, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Make the prediction candidates.
     *
     * You can get prediction candidates by the iWnn engine based on
     * given reading strings.
     * Gets the strings of prediction candidates
     * by calling {@code getNextCandidate()}.
     * To realize the function of wildcard prediction,
     * specify the values of minLen and maxLen of the desired input strings.
     * <p>For example,
     * <ul>
     *   <li>To get strings of at least and no more than 5 characters, set minLen and maxLen to 5. </li>
     *   <li>To get strings of more than 3 characters, set minLen to 3 and maxLen to -1.</li>
     * </ul>
     *
     * @param packageName   PackageName.
     * @param stroke        The stroke (YOMI) of word.
     * @param minLen        The minimum length of a word to predict.
     *                      (minLen <= 0  : no limit)
     * @param maxLen        The maximum length of a word to predict.
     *                      (maxLen <= -1 : no limit)
     * @return              Plus value if there are candidates;
     *                      0 if there is no candidate;
     *                      Minus value if an error occurs.
     */
@Override public int predict(java.lang.String packageName, java.lang.String stroke, int minLen, int maxLen) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(stroke);
_data.writeInt(minLen);
_data.writeInt(maxLen);
mRemote.transact(Stub.TRANSACTION_predict, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Set enable consecutive phrase level conversion.
     *
     * Set whether to implement the consecutive phrase level conversion.
     * If you do not call this API once, disable consecutive phrase level conversion.
     *
     * @param packageName   PackageName.
     * @param enable        {@code true} Enable consecutive phrase level conversion.
     *                      {@code false} Disable consecutive phrase level conversion.
     * @return              0 if success setting.
     *                      Minus value if an error occurs.
     */
@Override public int setEnableConsecutivePhraseLevelConversion(java.lang.String packageName, boolean enable) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeInt(((enable)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setEnableConsecutivePhraseLevelConversion, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Gets candidate strings for either prediction or conversion.
     *
     * Only gets the number of candidate strings specified in the parameters.
     * By repeatedly calling this API, you can get
     * the all the available candidates.
     * If there are no more candidates left, the API returns {@code null}.
     * The "reading" and "notation" will both be stored
     * as an array in {@code Bundle}.
     * Calls {@code Bundle#getStringArray("stroke")} to get the "reading",
     * and  set "candidate" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the iWnn engine will only return the number of existent ones.
     *
     * @param packageName           PackageName.
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate".
     */
@Override public android.os.Bundle getNextCandidate(java.lang.String packageName, int numberOfCandidates) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeInt(numberOfCandidates);
mRemote.transact(Stub.TRANSACTION_getNextCandidate, _data, _reply, 0);
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
/**
     * Gets candidate strings  and DecoEmoji text (Annotation is set to strings)
     * for either prediction or conversion.
     *
     * Only gets the number of candidate strings specified in the parameters.
     * By repeatedly calling this API, you can get
     * the all the available candidates.
     * If there are no more candidates left, the API returns {@code null}.
     * The "reading" and "notation" will both be stored
     * as an array in {@code Bundle}.
     * Calls {@code Bundle#getStringArray("stroke")} to get the "reading",
     * and  set "candidate" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the iWnn engine will only return the number of existent ones.
     *
     * @param packageName           PackageName.
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate",
     *                              and DecoEmoji text (Annotation is set to strings)
     *                              are "annotation_candidate".
     */
@Override public android.os.Bundle getNextCandidateWithAnnotation(java.lang.String packageName, int numberOfCandidates) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeInt(numberOfCandidates);
mRemote.transact(Stub.TRANSACTION_getNextCandidateWithAnnotation, _data, _reply, 0);
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
/**
     * Gets candidate strings  and DecoEmoji text (Annotation is set to strings)
     * for either prediction or conversion.
     *
     * Only gets the number of candidate strings specified in the parameters.
     * By repeatedly calling this API, you can get
     * the all the available candidates.
     * If there are no more candidates left, the API returns {@code null}.
     * The "reading" and "notation" will both be stored
     * as an array in {@code Bundle}.
     * Calls {@code Bundle#getStringArray("stroke")} to get the "reading",
     * and  set "candidate" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the iWnn engine will only return the number of existent ones.
     *
     * @param packageName           PackageName.
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @param emojitype             Emoji type
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate",
     *                              and DecoEmoji text (Annotation is set to strings)
     *                              are "annotation_candidate".
     */
@Override public android.os.Bundle getNextCandidateWithAnnotation2(java.lang.String packageName, int numberOfCandidates, int emojitype) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeInt(numberOfCandidates);
_data.writeInt(emojitype);
mRemote.transact(Stub.TRANSACTION_getNextCandidateWithAnnotation2, _data, _reply, 0);
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
/**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     *
     * The index of the parameter starts at 0 using the order
     * in which candidates were obtained by {@code getNextCandidate()}.
     * For example, if the third candidate obtained by getNextCandidate()
     * is to be learned, it will be set to index 2.
     *
     * @param packageName   PackageName.
     * @param index         The index of words by getNextCandidate().
     * @return              True if success, false otherwise.
     */
@Override public boolean learnCandidate(java.lang.String packageName, int index) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeInt(index);
mRemote.transact(Stub.TRANSACTION_learnCandidate, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Convert the strings.
     *
     * The iWnn Engine performs a conversion by using the specified reading
     * strings.
     * The result is obtained by getNextCandidate()
     * Convert the specified string into "stroke" and returns the number of
     * phrases.
     * If there are no candidates it returns 0.
     *
     * @param packageName   PackageName.
     * @param stroke        The stroke (YOMI) of word.
     * @param divide        Divide position.
     * @return              Candidate words.
     *                      If you want to get strokes of word,
     *                      call Bundle#getStringArray("stroke").
     *                      Similarly, candidates are "candidate".
     */
@Override public android.os.Bundle convert(java.lang.String packageName, java.lang.String stroke, int divide) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(stroke);
_data.writeInt(divide);
mRemote.transact(Stub.TRANSACTION_convert, _data, _reply, 0);
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
/**
     * Convert the strings and DecoEmoji text (Annotation is set to strings).
     *
     * The iWnn Engine performs a conversion by using the specified reading
     * strings.
     * The result is obtained by getNextCandidate()
     * Convert the specified string into "stroke" and returns the number of
     * phrases.
     * If there are no candidates it returns 0.
     *
     * @param packageName   PackageName.
     * @param stroke        The stroke (YOMI) of word.
     * @param divide        Divide position.
     * @return              Convert words.
     *                      If you want to get strokes of word,
     *                      call Bundle#getStringArray("stroke").
     *                      Similarly, candidates are "candidate",
     *                      and DecoEmoji text (Annotation is set to strings)
     *                      are "annotation_candidate".
     */
@Override public android.os.Bundle convertWithAnnotation(java.lang.String packageName, java.lang.String stroke, int divide) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(stroke);
_data.writeInt(divide);
mRemote.transact(Stub.TRANSACTION_convertWithAnnotation, _data, _reply, 0);
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
/**
     * Convert the strings and DecoEmoji text (Annotation is set to strings).
     *
     * The iWnn Engine performs a conversion by using the specified reading
     * strings.
     * The result is obtained by getNextCandidate()
     * Convert the specified string into "stroke" and returns the number of
     * phrases.
     * If there are no candidates it returns 0.
     *
     * @param packageName   PackageName.
     * @param stroke        The stroke (YOMI) of word.
     * @param divide        Divide position.
     * @param emojitype     Emoji type
     * @return              Convert words.
     *                      If you want to get strokes of word,
     *                      call Bundle#getStringArray("stroke").
     *                      Similarly, candidates are "candidate",
     *                      and DecoEmoji text (Annotation is set to strings)
     *                      are "annotation_candidate".
     */
@Override public android.os.Bundle convertWithAnnotation2(java.lang.String packageName, java.lang.String stroke, int divide, int emojitype) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(stroke);
_data.writeInt(divide);
_data.writeInt(emojitype);
mRemote.transact(Stub.TRANSACTION_convertWithAnnotation2, _data, _reply, 0);
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
/**
     * Registers a word into either the User dictionary or the Learning
     * dictionary.
     *
     * Registers reading and notation into either the User dictionary or the
     * Learning dictionary.
     * Executes a word registration by specifying the reading string into
     * "stroke" and the notation string into "candidate".
     *
     * @param packageName   PackageName.
     * @param candidate     Notation of words to learn.
     * @param stroke        Stroke of words to learn.
     * @return              Success:0  Failure:minus
     */
@Override public int addWord(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(candidate);
_data.writeString(stroke);
mRemote.transact(Stub.TRANSACTION_addWord, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Registers a word into either the User dictionary or the Learning
     * dictionary.
     *
     * Registers reading and notation into either the User dictionary or the
     * Learning dictionary.
     * Executes a word registration by specifying the reading string into
     * "stroke" and the notation string into "candidate".
     *
     * @param packageName  PackageName.
     * @param candidate    Notation of words to learn.
     * @param stroke       Stroke of words to learn.
     * @param hinsi        Index of the lexical category group
     * @param type         Type of dictionaries(0:user dictionary, 1:learning dictionary, 2:pseudo dictionary)
     * @param relation     Relation learning flag(learning relations with the previous registered word. 0:don't learn 1:do learn)
     * @return             Success:0  Failure:minus
     */
@Override public int addWordDetail(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke, int hinsi, int type, int relation) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(candidate);
_data.writeString(stroke);
_data.writeInt(hinsi);
_data.writeInt(type);
_data.writeInt(relation);
mRemote.transact(Stub.TRANSACTION_addWordDetail, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Searches for words from the User dictionary or the Learning dictionary.
     *
     * Executes by specifying a reading string into "stroke".
     * The result is obtained by getNextCandidate()
     * All words can be searched for if an empty string is specified as the
     * arguments.
     *
     * @param packageName   PackageName.
     * @param stroke        Stroke of word to search from dictionary.
     * @return              found:1  Not found:0  Failure:minus
     */
@Override public int searchWords(java.lang.String packageName, java.lang.String stroke) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(stroke);
mRemote.transact(Stub.TRANSACTION_searchWords, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Searches for words from the User dictionary or the Learning dictionary.
     *
     * Executes by specifying a reading string into "stroke".
     * The result is obtained by getNextCandidate()
     * All words can be searched for if an empty string is specified as the
     * arguments.
     *
     * @param packageName   PackageName.
     * @param stroke        Stroke of word to search from dictionary.
     * @param method        Way of searching the dictionary
     * @param order         Order of searching the dictionary
     * @return              found:1  Not found:0  Failure:minus
     */
@Override public int searchWordsDetail(java.lang.String packageName, java.lang.String stroke, int method, int order) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(stroke);
_data.writeInt(method);
_data.writeInt(order);
mRemote.transact(Stub.TRANSACTION_searchWordsDetail, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Deletes a word registered in the User dictionary or the Learning
     * dictionary.
     *
     * Executes a word deletion by specifying the reading string into "stroke"
     * and the notation string into "candidate".
     * 
     * @param packageName   PackageName.
     * @param candidate     Notation of word to delete from dictionary.
     * @param stroke        Stroke of word to delete from dictionary.
     * @return              Success:true  Failure:false
     */
@Override public boolean deleteWord(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(candidate);
_data.writeString(stroke);
mRemote.transact(Stub.TRANSACTION_deleteWord, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Backup the User dictionary or the Learning dictionary.
     *
     * @param packageName   PackageName.
     * @return              Success:true  Failure:false
     */
@Override public boolean writeoutDictionary(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_writeoutDictionary, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Initialize the User dictionary or the Learning dictionary.
     *
     * @param packageName   PackageName.
     * @return              Success:true  Failure:fals
     */
@Override public boolean initializeDictionary(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_initializeDictionary, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Sets the User dictionary.
     *
     * Sets the User dictionary as the one to be used by the iWnn Engine.
     *
     * @param packageName   PackageName.
     */
@Override public void setUserDictionary(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_setUserDictionary, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Sets the Learning dictionary.
     *
     * Sets the Learning dictionary as the one to be used by the iWnn Engine.
     *
     * @param packageName   PackageName.
     */
@Override public void setLearnDictionary(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_setLearnDictionary, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Sets the Normal dictionary.
     *
     * Sets the Normal dictionary as the one to be used by the iWnn Engine.
     *
     * @param packageNamePackageName.
     */
@Override public void setNormalDictionary(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_setNormalDictionary, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     * Holds the pre-confirmed infomation without saving them into the Learning
     * dictionary.
     * The index of the parameter starts at 1 using the order
     * in which candidates were obtained by *getNextCandidate()*.
     * For example, if the third candidate obtained by
     * getNextCandidate() is to be learned, it will be set to index 3.
     *
     * @param packageName   PackageName.
     * @param index         Index of words to learn.
     * @return              True if success, false otherwise.
     */
@Override public boolean learnCandidateNoStore(java.lang.String packageName, int index) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeInt(index);
mRemote.transact(Stub.TRANSACTION_learnCandidateNoStore, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     * Not learns the predictive candidates connection.
     *
     * The index of the parameter starts at 0 using the order
     * in which candidates were obtained by {@code getNextCandidate()}.
     * For example, if the third candidate obtained by getNextCandidate()
     * is to be learned, it will be set to index 2.
     *
     * @param packageName   PackageName.
     * @param index         Index of words to learn.
     * @return              True if success, false otherwise.
     */
@Override public boolean learnCandidateNoConnect(java.lang.String packageName, int index) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeInt(index);
mRemote.transact(Stub.TRANSACTION_learnCandidateNoConnect, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Learns the candidate.
     *
     * @param packageName   PackageName.
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     */
@Override public boolean learnWord(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(candidate);
_data.writeString(stroke);
mRemote.transact(Stub.TRANSACTION_learnWord, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Learns the candidate.
     * Holds the pre-confirmed infomation without saving them into the Learning
     * dictionary.
     *
     * @param packageName   PackageName.
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     * @return              Success:true Failure:false
     */
@Override public boolean learnWordNoStore(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(candidate);
_data.writeString(stroke);
mRemote.transact(Stub.TRANSACTION_learnWordNoStore, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Learns the candidate.
     * Not learns the predictive candidates connection.
     *
     * @param packageName   PackageName.
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     */
@Override public boolean learnWordNoConnect(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(candidate);
_data.writeString(stroke);
mRemote.transact(Stub.TRANSACTION_learnWordNoConnect, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Set the type of dictionary .
     *
     * @param packageName        PackageName.
     * @param configurationFile  Configuration file path.
     * @param language           Type of language.
     * @param dictionary         Type of dictionary.
     * @param flexibleSearch     Enable the flexible search.
     * @param tenKeyType         Is ten keyboard type.
     * @param emojiFilter        Emoji code is filter.
     * @param emailFilter        Email candidates is filter.
     * @param convertCandidates  Set the convert candidates.
     * @param learnNumber        Learn the numeric mixing candidate.
     * @return                   Success:true Failure:false
     */
@Override public boolean setDictionary(java.lang.String packageName, java.lang.String configurationFile, int language, int dictionary, boolean flexibleSearch, boolean tenKeyType, boolean emojiFilter, boolean emailFilter, boolean convertCandidates, boolean learnNumber) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(configurationFile);
_data.writeInt(language);
_data.writeInt(dictionary);
_data.writeInt(((flexibleSearch)?(1):(0)));
_data.writeInt(((tenKeyType)?(1):(0)));
_data.writeInt(((emojiFilter)?(1):(0)));
_data.writeInt(((emailFilter)?(1):(0)));
_data.writeInt(((convertCandidates)?(1):(0)));
_data.writeInt(((learnNumber)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setDictionary, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Set the type of dictionary with the decoemoji function.
     *
     * @param packageName        PackageName.
     * @param configurationFile  Configuration file path.
     * @param language           Type of language.
     * @param dictionary         Type of dictionary.
     * @param flexibleSearch     Enable the flexible search.
     * @param tenKeyType         Is ten keyboard type.
     * @param emojiFilter        Emoji code is filter.
     * @param decoemojiFilter    DecoEmoji code is filter.
     * @param emailFilter        Email candidates is filter.
     * @param convertCandidates  Set the convert candidates.
     * @param learnNumber        Learn the numeric mixing candidate.
     * @return                   Success:0  Failure:minus
     */
@Override public boolean setDictionaryDecoratedPict(java.lang.String packageName, java.lang.String configurationFile, int language, int dictionary, boolean flexibleSearch, boolean tenKeyType, boolean emojiFilter, boolean decoemojiFilter, boolean emailFilter, boolean convertCandidates, boolean learnNumber) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeString(configurationFile);
_data.writeInt(language);
_data.writeInt(dictionary);
_data.writeInt(((flexibleSearch)?(1):(0)));
_data.writeInt(((tenKeyType)?(1):(0)));
_data.writeInt(((emojiFilter)?(1):(0)));
_data.writeInt(((decoemojiFilter)?(1):(0)));
_data.writeInt(((emailFilter)?(1):(0)));
_data.writeInt(((convertCandidates)?(1):(0)));
_data.writeInt(((learnNumber)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setDictionaryDecoratedPict, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Undo of learning.
     *
     * @param packageName   PackageName.
     * @return              Success:true  Failure:false
     */
@Override public boolean undo(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_undo, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Check a candidate word if it's on the pseudo dictionary.
     *
     * @param packageName  PackageName.
     * @param index        Index of the candidate word
     * @return             true : If the word is on the pseudo dictionary,
     *                     false: If not
     */
@Override public boolean isGijiDic(java.lang.String packageName, int index) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeInt(index);
mRemote.transact(Stub.TRANSACTION_isGijiDic, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Set a pseudo dictionary filter.
     *
     * @param packageName  PackageName.
     * @param type         Pseudo dictionary filter.
     * @return             Success:true  Failure:false
     */
@Override public boolean setGijiFilter(java.lang.String packageName, int[] type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeIntArray(type);
mRemote.transact(Stub.TRANSACTION_setGijiFilter, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
_reply.readIntArray(type);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Starts the input.
     *
     * @param packageName   PackageName.
     */
@Override public void startInput(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_startInput, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * @hide used for testing only
     *
     * @param packageName   PackageName.
     */
@Override public int getStatus(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_getStatus, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * @hide used for testing only
     *
     * @param packageName   PackageName.
     */
@Override public int getDictionaryType(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_getDictionaryType, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Get error code.
     *
     * @param packageName   PackageName.
     * @return              Error Code
     */
@Override public int getErrorCode(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_getErrorCode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * disconnect from service.
     *
     * @param packageName   PackageName.
     */
@Override public void disconnect(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * You can know the connector is still connecting the service.
     *
     * @param packageName   PackageName.
     * @return              Alive:true  NoAlive:false
     */
@Override public boolean isAlive(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_isAlive, _data, _reply, 0);
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
static final int TRANSACTION_init = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_predict = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_setEnableConsecutivePhraseLevelConversion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getNextCandidate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getNextCandidateWithAnnotation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getNextCandidateWithAnnotation2 = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_learnCandidate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_convert = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_convertWithAnnotation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_convertWithAnnotation2 = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_addWord = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_addWordDetail = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_searchWords = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_searchWordsDetail = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_deleteWord = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_writeoutDictionary = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_initializeDictionary = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_setUserDictionary = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_setLearnDictionary = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_setNormalDictionary = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_learnCandidateNoStore = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_learnCandidateNoConnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_learnWord = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_learnWordNoStore = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_learnWordNoConnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_setDictionary = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
static final int TRANSACTION_setDictionaryDecoratedPict = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
static final int TRANSACTION_undo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
static final int TRANSACTION_isGijiDic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
static final int TRANSACTION_setGijiFilter = (android.os.IBinder.FIRST_CALL_TRANSACTION + 29);
static final int TRANSACTION_startInput = (android.os.IBinder.FIRST_CALL_TRANSACTION + 30);
static final int TRANSACTION_getStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 31);
static final int TRANSACTION_getDictionaryType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 32);
static final int TRANSACTION_getErrorCode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 33);
static final int TRANSACTION_disconnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 34);
static final int TRANSACTION_isAlive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 35);
}
/**
     * Initialize the internal state of the iWnn engine.
     *
     * @param packageName   PackageName.
     * @param password      Password.
     * @param initLevel     Initialize level.
     */
public void init(java.lang.String packageName, java.lang.String password, int initLevel) throws android.os.RemoteException;
/**
     * Make the prediction candidates.
     *
     * You can get prediction candidates by the iWnn engine based on
     * given reading strings.
     * Gets the strings of prediction candidates
     * by calling {@code getNextCandidate()}.
     * To realize the function of wildcard prediction,
     * specify the values of minLen and maxLen of the desired input strings.
     * <p>For example,
     * <ul>
     *   <li>To get strings of at least and no more than 5 characters, set minLen and maxLen to 5. </li>
     *   <li>To get strings of more than 3 characters, set minLen to 3 and maxLen to -1.</li>
     * </ul>
     *
     * @param packageName   PackageName.
     * @param stroke        The stroke (YOMI) of word.
     * @param minLen        The minimum length of a word to predict.
     *                      (minLen <= 0  : no limit)
     * @param maxLen        The maximum length of a word to predict.
     *                      (maxLen <= -1 : no limit)
     * @return              Plus value if there are candidates;
     *                      0 if there is no candidate;
     *                      Minus value if an error occurs.
     */
public int predict(java.lang.String packageName, java.lang.String stroke, int minLen, int maxLen) throws android.os.RemoteException;
/**
     * Set enable consecutive phrase level conversion.
     *
     * Set whether to implement the consecutive phrase level conversion.
     * If you do not call this API once, disable consecutive phrase level conversion.
     *
     * @param packageName   PackageName.
     * @param enable        {@code true} Enable consecutive phrase level conversion.
     *                      {@code false} Disable consecutive phrase level conversion.
     * @return              0 if success setting.
     *                      Minus value if an error occurs.
     */
public int setEnableConsecutivePhraseLevelConversion(java.lang.String packageName, boolean enable) throws android.os.RemoteException;
/**
     * Gets candidate strings for either prediction or conversion.
     *
     * Only gets the number of candidate strings specified in the parameters.
     * By repeatedly calling this API, you can get
     * the all the available candidates.
     * If there are no more candidates left, the API returns {@code null}.
     * The "reading" and "notation" will both be stored
     * as an array in {@code Bundle}.
     * Calls {@code Bundle#getStringArray("stroke")} to get the "reading",
     * and  set "candidate" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the iWnn engine will only return the number of existent ones.
     *
     * @param packageName           PackageName.
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate".
     */
public android.os.Bundle getNextCandidate(java.lang.String packageName, int numberOfCandidates) throws android.os.RemoteException;
/**
     * Gets candidate strings  and DecoEmoji text (Annotation is set to strings)
     * for either prediction or conversion.
     *
     * Only gets the number of candidate strings specified in the parameters.
     * By repeatedly calling this API, you can get
     * the all the available candidates.
     * If there are no more candidates left, the API returns {@code null}.
     * The "reading" and "notation" will both be stored
     * as an array in {@code Bundle}.
     * Calls {@code Bundle#getStringArray("stroke")} to get the "reading",
     * and  set "candidate" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the iWnn engine will only return the number of existent ones.
     *
     * @param packageName           PackageName.
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate",
     *                              and DecoEmoji text (Annotation is set to strings)
     *                              are "annotation_candidate".
     */
public android.os.Bundle getNextCandidateWithAnnotation(java.lang.String packageName, int numberOfCandidates) throws android.os.RemoteException;
/**
     * Gets candidate strings  and DecoEmoji text (Annotation is set to strings)
     * for either prediction or conversion.
     *
     * Only gets the number of candidate strings specified in the parameters.
     * By repeatedly calling this API, you can get
     * the all the available candidates.
     * If there are no more candidates left, the API returns {@code null}.
     * The "reading" and "notation" will both be stored
     * as an array in {@code Bundle}.
     * Calls {@code Bundle#getStringArray("stroke")} to get the "reading",
     * and  set "candidate" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the iWnn engine will only return the number of existent ones.
     *
     * @param packageName           PackageName.
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @param emojitype             Emoji type
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate",
     *                              and DecoEmoji text (Annotation is set to strings)
     *                              are "annotation_candidate".
     */
public android.os.Bundle getNextCandidateWithAnnotation2(java.lang.String packageName, int numberOfCandidates, int emojitype) throws android.os.RemoteException;
/**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     *
     * The index of the parameter starts at 0 using the order
     * in which candidates were obtained by {@code getNextCandidate()}.
     * For example, if the third candidate obtained by getNextCandidate()
     * is to be learned, it will be set to index 2.
     *
     * @param packageName   PackageName.
     * @param index         The index of words by getNextCandidate().
     * @return              True if success, false otherwise.
     */
public boolean learnCandidate(java.lang.String packageName, int index) throws android.os.RemoteException;
/**
     * Convert the strings.
     *
     * The iWnn Engine performs a conversion by using the specified reading
     * strings.
     * The result is obtained by getNextCandidate()
     * Convert the specified string into "stroke" and returns the number of
     * phrases.
     * If there are no candidates it returns 0.
     *
     * @param packageName   PackageName.
     * @param stroke        The stroke (YOMI) of word.
     * @param divide        Divide position.
     * @return              Candidate words.
     *                      If you want to get strokes of word,
     *                      call Bundle#getStringArray("stroke").
     *                      Similarly, candidates are "candidate".
     */
public android.os.Bundle convert(java.lang.String packageName, java.lang.String stroke, int divide) throws android.os.RemoteException;
/**
     * Convert the strings and DecoEmoji text (Annotation is set to strings).
     *
     * The iWnn Engine performs a conversion by using the specified reading
     * strings.
     * The result is obtained by getNextCandidate()
     * Convert the specified string into "stroke" and returns the number of
     * phrases.
     * If there are no candidates it returns 0.
     *
     * @param packageName   PackageName.
     * @param stroke        The stroke (YOMI) of word.
     * @param divide        Divide position.
     * @return              Convert words.
     *                      If you want to get strokes of word,
     *                      call Bundle#getStringArray("stroke").
     *                      Similarly, candidates are "candidate",
     *                      and DecoEmoji text (Annotation is set to strings)
     *                      are "annotation_candidate".
     */
public android.os.Bundle convertWithAnnotation(java.lang.String packageName, java.lang.String stroke, int divide) throws android.os.RemoteException;
/**
     * Convert the strings and DecoEmoji text (Annotation is set to strings).
     *
     * The iWnn Engine performs a conversion by using the specified reading
     * strings.
     * The result is obtained by getNextCandidate()
     * Convert the specified string into "stroke" and returns the number of
     * phrases.
     * If there are no candidates it returns 0.
     *
     * @param packageName   PackageName.
     * @param stroke        The stroke (YOMI) of word.
     * @param divide        Divide position.
     * @param emojitype     Emoji type
     * @return              Convert words.
     *                      If you want to get strokes of word,
     *                      call Bundle#getStringArray("stroke").
     *                      Similarly, candidates are "candidate",
     *                      and DecoEmoji text (Annotation is set to strings)
     *                      are "annotation_candidate".
     */
public android.os.Bundle convertWithAnnotation2(java.lang.String packageName, java.lang.String stroke, int divide, int emojitype) throws android.os.RemoteException;
/**
     * Registers a word into either the User dictionary or the Learning
     * dictionary.
     *
     * Registers reading and notation into either the User dictionary or the
     * Learning dictionary.
     * Executes a word registration by specifying the reading string into
     * "stroke" and the notation string into "candidate".
     *
     * @param packageName   PackageName.
     * @param candidate     Notation of words to learn.
     * @param stroke        Stroke of words to learn.
     * @return              Success:0  Failure:minus
     */
public int addWord(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke) throws android.os.RemoteException;
/**
     * Registers a word into either the User dictionary or the Learning
     * dictionary.
     *
     * Registers reading and notation into either the User dictionary or the
     * Learning dictionary.
     * Executes a word registration by specifying the reading string into
     * "stroke" and the notation string into "candidate".
     *
     * @param packageName  PackageName.
     * @param candidate    Notation of words to learn.
     * @param stroke       Stroke of words to learn.
     * @param hinsi        Index of the lexical category group
     * @param type         Type of dictionaries(0:user dictionary, 1:learning dictionary, 2:pseudo dictionary)
     * @param relation     Relation learning flag(learning relations with the previous registered word. 0:don't learn 1:do learn)
     * @return             Success:0  Failure:minus
     */
public int addWordDetail(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke, int hinsi, int type, int relation) throws android.os.RemoteException;
/**
     * Searches for words from the User dictionary or the Learning dictionary.
     *
     * Executes by specifying a reading string into "stroke".
     * The result is obtained by getNextCandidate()
     * All words can be searched for if an empty string is specified as the
     * arguments.
     *
     * @param packageName   PackageName.
     * @param stroke        Stroke of word to search from dictionary.
     * @return              found:1  Not found:0  Failure:minus
     */
public int searchWords(java.lang.String packageName, java.lang.String stroke) throws android.os.RemoteException;
/**
     * Searches for words from the User dictionary or the Learning dictionary.
     *
     * Executes by specifying a reading string into "stroke".
     * The result is obtained by getNextCandidate()
     * All words can be searched for if an empty string is specified as the
     * arguments.
     *
     * @param packageName   PackageName.
     * @param stroke        Stroke of word to search from dictionary.
     * @param method        Way of searching the dictionary
     * @param order         Order of searching the dictionary
     * @return              found:1  Not found:0  Failure:minus
     */
public int searchWordsDetail(java.lang.String packageName, java.lang.String stroke, int method, int order) throws android.os.RemoteException;
/**
     * Deletes a word registered in the User dictionary or the Learning
     * dictionary.
     *
     * Executes a word deletion by specifying the reading string into "stroke"
     * and the notation string into "candidate".
     * 
     * @param packageName   PackageName.
     * @param candidate     Notation of word to delete from dictionary.
     * @param stroke        Stroke of word to delete from dictionary.
     * @return              Success:true  Failure:false
     */
public boolean deleteWord(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke) throws android.os.RemoteException;
/**
     * Backup the User dictionary or the Learning dictionary.
     *
     * @param packageName   PackageName.
     * @return              Success:true  Failure:false
     */
public boolean writeoutDictionary(java.lang.String packageName) throws android.os.RemoteException;
/**
     * Initialize the User dictionary or the Learning dictionary.
     *
     * @param packageName   PackageName.
     * @return              Success:true  Failure:fals
     */
public boolean initializeDictionary(java.lang.String packageName) throws android.os.RemoteException;
/**
     * Sets the User dictionary.
     *
     * Sets the User dictionary as the one to be used by the iWnn Engine.
     *
     * @param packageName   PackageName.
     */
public void setUserDictionary(java.lang.String packageName) throws android.os.RemoteException;
/**
     * Sets the Learning dictionary.
     *
     * Sets the Learning dictionary as the one to be used by the iWnn Engine.
     *
     * @param packageName   PackageName.
     */
public void setLearnDictionary(java.lang.String packageName) throws android.os.RemoteException;
/**
     * Sets the Normal dictionary.
     *
     * Sets the Normal dictionary as the one to be used by the iWnn Engine.
     *
     * @param packageNamePackageName.
     */
public void setNormalDictionary(java.lang.String packageName) throws android.os.RemoteException;
/**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     * Holds the pre-confirmed infomation without saving them into the Learning
     * dictionary.
     * The index of the parameter starts at 1 using the order
     * in which candidates were obtained by *getNextCandidate()*.
     * For example, if the third candidate obtained by
     * getNextCandidate() is to be learned, it will be set to index 3.
     *
     * @param packageName   PackageName.
     * @param index         Index of words to learn.
     * @return              True if success, false otherwise.
     */
public boolean learnCandidateNoStore(java.lang.String packageName, int index) throws android.os.RemoteException;
/**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     * Not learns the predictive candidates connection.
     *
     * The index of the parameter starts at 0 using the order
     * in which candidates were obtained by {@code getNextCandidate()}.
     * For example, if the third candidate obtained by getNextCandidate()
     * is to be learned, it will be set to index 2.
     *
     * @param packageName   PackageName.
     * @param index         Index of words to learn.
     * @return              True if success, false otherwise.
     */
public boolean learnCandidateNoConnect(java.lang.String packageName, int index) throws android.os.RemoteException;
/**
     * Learns the candidate.
     *
     * @param packageName   PackageName.
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     */
public boolean learnWord(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke) throws android.os.RemoteException;
/**
     * Learns the candidate.
     * Holds the pre-confirmed infomation without saving them into the Learning
     * dictionary.
     *
     * @param packageName   PackageName.
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     * @return              Success:true Failure:false
     */
public boolean learnWordNoStore(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke) throws android.os.RemoteException;
/**
     * Learns the candidate.
     * Not learns the predictive candidates connection.
     *
     * @param packageName   PackageName.
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     */
public boolean learnWordNoConnect(java.lang.String packageName, java.lang.String candidate, java.lang.String stroke) throws android.os.RemoteException;
/**
     * Set the type of dictionary .
     *
     * @param packageName        PackageName.
     * @param configurationFile  Configuration file path.
     * @param language           Type of language.
     * @param dictionary         Type of dictionary.
     * @param flexibleSearch     Enable the flexible search.
     * @param tenKeyType         Is ten keyboard type.
     * @param emojiFilter        Emoji code is filter.
     * @param emailFilter        Email candidates is filter.
     * @param convertCandidates  Set the convert candidates.
     * @param learnNumber        Learn the numeric mixing candidate.
     * @return                   Success:true Failure:false
     */
public boolean setDictionary(java.lang.String packageName, java.lang.String configurationFile, int language, int dictionary, boolean flexibleSearch, boolean tenKeyType, boolean emojiFilter, boolean emailFilter, boolean convertCandidates, boolean learnNumber) throws android.os.RemoteException;
/**
     * Set the type of dictionary with the decoemoji function.
     *
     * @param packageName        PackageName.
     * @param configurationFile  Configuration file path.
     * @param language           Type of language.
     * @param dictionary         Type of dictionary.
     * @param flexibleSearch     Enable the flexible search.
     * @param tenKeyType         Is ten keyboard type.
     * @param emojiFilter        Emoji code is filter.
     * @param decoemojiFilter    DecoEmoji code is filter.
     * @param emailFilter        Email candidates is filter.
     * @param convertCandidates  Set the convert candidates.
     * @param learnNumber        Learn the numeric mixing candidate.
     * @return                   Success:0  Failure:minus
     */
public boolean setDictionaryDecoratedPict(java.lang.String packageName, java.lang.String configurationFile, int language, int dictionary, boolean flexibleSearch, boolean tenKeyType, boolean emojiFilter, boolean decoemojiFilter, boolean emailFilter, boolean convertCandidates, boolean learnNumber) throws android.os.RemoteException;
/**
     * Undo of learning.
     *
     * @param packageName   PackageName.
     * @return              Success:true  Failure:false
     */
public boolean undo(java.lang.String packageName) throws android.os.RemoteException;
/**
     * Check a candidate word if it's on the pseudo dictionary.
     *
     * @param packageName  PackageName.
     * @param index        Index of the candidate word
     * @return             true : If the word is on the pseudo dictionary,
     *                     false: If not
     */
public boolean isGijiDic(java.lang.String packageName, int index) throws android.os.RemoteException;
/**
     * Set a pseudo dictionary filter.
     *
     * @param packageName  PackageName.
     * @param type         Pseudo dictionary filter.
     * @return             Success:true  Failure:false
     */
public boolean setGijiFilter(java.lang.String packageName, int[] type) throws android.os.RemoteException;
/**
     * Starts the input.
     *
     * @param packageName   PackageName.
     */
public void startInput(java.lang.String packageName) throws android.os.RemoteException;
/**
     * @hide used for testing only
     *
     * @param packageName   PackageName.
     */
public int getStatus(java.lang.String packageName) throws android.os.RemoteException;
/**
     * @hide used for testing only
     *
     * @param packageName   PackageName.
     */
public int getDictionaryType(java.lang.String packageName) throws android.os.RemoteException;
/**
     * Get error code.
     *
     * @param packageName   PackageName.
     * @return              Error Code
     */
public int getErrorCode(java.lang.String packageName) throws android.os.RemoteException;
/**
     * disconnect from service.
     *
     * @param packageName   PackageName.
     */
public void disconnect(java.lang.String packageName) throws android.os.RemoteException;
/**
     * You can know the connector is still connecting the service.
     *
     * @param packageName   PackageName.
     * @return              Alive:true  NoAlive:false
     */
public boolean isAlive(java.lang.String packageName) throws android.os.RemoteException;
}
