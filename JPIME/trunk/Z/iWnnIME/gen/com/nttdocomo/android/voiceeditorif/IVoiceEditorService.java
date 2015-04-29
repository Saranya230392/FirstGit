/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Android\\Workspace\\JPIME\\trunk\\Z\\iWnnIME\\src\\com\\nttdocomo\\android\\voiceeditorif\\IVoiceEditorService.aidl
 */
package com.nttdocomo.android.voiceeditorif;
/**
 * This interface provides the API for VoiceEditorService.
 */
public interface IVoiceEditorService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.nttdocomo.android.voiceeditorif.IVoiceEditorService
{
private static final java.lang.String DESCRIPTOR = "com.nttdocomo.android.voiceeditorif.IVoiceEditorService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.nttdocomo.android.voiceeditorif.IVoiceEditorService interface,
 * generating a proxy if needed.
 */
public static com.nttdocomo.android.voiceeditorif.IVoiceEditorService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.nttdocomo.android.voiceeditorif.IVoiceEditorService))) {
return ((com.nttdocomo.android.voiceeditorif.IVoiceEditorService)iin);
}
return new com.nttdocomo.android.voiceeditorif.IVoiceEditorService.Stub.Proxy(obj);
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
case TRANSACTION_disconnect:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.disconnect();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_isConnected:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isConnected();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_init:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.init();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setDictionary:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _arg2;
_arg2 = (0!=data.readInt());
int _result = this.setDictionary(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_searchCandidate:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
int _result = this.searchCandidate(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getCandidate:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
android.os.Bundle _result = this.getCandidate(_arg0);
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
case TRANSACTION_memorizeCandidate:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _arg2;
_arg2 = (0!=data.readInt());
int _result = this.memorizeCandidate(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_addWord:
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
int _result = this.addWord(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_searchWord:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.searchWord(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_startInputDecoEmoji:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.startInputDecoEmoji();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_isPseudoCandidate:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.isPseudoCandidate(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getIMESettingInfo:
{
data.enforceInterface(DESCRIPTOR);
android.os.Bundle _result = this.getIMESettingInfo();
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
private static class Proxy implements com.nttdocomo.android.voiceeditorif.IVoiceEditorService
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
     * Disconnect from service.
     *
     * @return              Success:0  Failure:-1
     */
@Override public int disconnect() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
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
     * Check the connector is still connecting the service.
     *
     * @return              Alive:true  NoAlive:false
     */
@Override public boolean isConnected() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isConnected, _data, _reply, 0);
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
     * Initialize the internal state of the VoiceEditor.
     * <BR>For initialization
     * <ul>
     * <li>Result of the candidate.</li>
     * <li>Result of the prediction.</li>
     * <li>Result of the consecutive clauses.</li>
     * </ul>
     * @return              Success:0  Failure:-1
     */
@Override public int init() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_init, _data, _reply, 0);
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
     * Setting the search condition candidate.
     *
     * @param dictionaryType     Type of used dictionary.
     * @param emojiFilter        true if an emoji filter will be enable.
     * @param decoemojiFilter    true if an decoemoji filter will be enable.
     * @return                   Success:0 Failure:-1
     */
@Override public int setDictionary(int dictionaryType, boolean emojiFilter, boolean decoemojiFilter) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(dictionaryType);
_data.writeInt(((emojiFilter)?(1):(0)));
_data.writeInt(((decoemojiFilter)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setDictionary, _data, _reply, 0);
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
     * Make the prediction candidates.
     *
     * You can get prediction candidates by the engine based on
     * given reading strings.
     * Gets the strings of prediction candidates
     * by calling {@link #getCandidate(int)}.
     * To realize the function of wildcard prediction,
     * specify the values of minLen and maxLen of the desired input strings.
     * <p>For example,
     * <ul>
     *   <li>To get strings of at least and no more than 5 characters, set minLen and maxLen to 5. </li>
     *   <li>To get strings of more than 3 characters, set minLen to 3 and maxLen to -1.</li>
     * </ul>
     *
     * @param phonetic      The phonetic (YOMI) of word.
     * @param minLen        The minimum length of a word to predict.
     *                      (minLen <= 0  : no limit)
     * @param maxLen        The maximum length of a word to predict.
     *                      (maxLen <= -1 : no limit)
     * @return              Plus value if there are candidates;
     *                      0 if there is no candidate;
     *                      -1 if an error occurs.
     */
@Override public int searchCandidate(java.lang.String phonetic, int minLen, int maxLen) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(phonetic);
_data.writeInt(minLen);
_data.writeInt(maxLen);
mRemote.transact(Stub.TRANSACTION_searchCandidate, _data, _reply, 0);
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
     * The "candidate" and "stroke" will both be stored
     * as an array in {@link Bundle}.
     * Calls {@code Bundle#getStringArray("candidate")} to get the "reading",
     * and  set "stroke" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the engine will only return the number of existent ones.
     *
     * @param maxCandidates         The maximum number of candidates
     *                                  returned by this method.
     * @return                      Candidate words.
     *                              If you want to get phonetic of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate".
     *                              If there is no candidate words result,
     *                              'Bundle # getStringArray ("candidate")' return zero-length array.
     */
@Override public android.os.Bundle getCandidate(int maxCandidates) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(maxCandidates);
mRemote.transact(Stub.TRANSACTION_getCandidate, _data, _reply, 0);
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
     * Learn prediction candidates and consecutive clauses.
     *
     * The index of the parameter starts at 0 using the order
     * in which candidates were obtained by {@link getCandidate(int)}.
     * For example, if the third candidate obtained by getCandidate()
     * is to be learned, it will be set to index 2.
     *
     * @param index                 The index of words by calling "getCandidate(int)".
     * @param learnFlag             true if learning predictions candidate will be enable.
     * @param connectFlag           true if learning consecutive clauses will be enable.
     * @return                      Success:0  Failure:-1
     */
@Override public int memorizeCandidate(int index, boolean learnFlag, boolean connectFlag) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(index);
_data.writeInt(((learnFlag)?(1):(0)));
_data.writeInt(((connectFlag)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_memorizeCandidate, _data, _reply, 0);
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
     * Add a word to the learning dictionary.
     *
     * Registers reading and notation into the Learning dictionary.
     * Executes a word registration by specifying the reading string into
     * "phonetic" and the notation string into "candidate".
     *
     * @param candidate     Notation of words to learn.
     * @param phonetic      phonetic of words to learn.
     * @param attribute     Attribute value of the word. Index of the lexical category group.
     * @param relation      Relation learning flag
     *                      (learning relations with the previous registered word.
     *                       0:don't learn 1:do learn)
     * @return              Success:0  Failure:-1
     */
@Override public int addWord(java.lang.String candidate, java.lang.String phonetic, int attribute, int relation) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(candidate);
_data.writeString(phonetic);
_data.writeInt(attribute);
_data.writeInt(relation);
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
     * Search words on the user dictionary.
     *
     * Get search results by calling {@link #getCandidate(int)}.
     * Search method is a matching front.
     *
     * @param phonetic      phonetic of word to search from dictionary.
     * @return              found:1  Not found:0  Failure:-1
     */
@Override public int searchWord(java.lang.String phonetic) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(phonetic);
mRemote.transact(Stub.TRANSACTION_searchWord, _data, _reply, 0);
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
     * Input start of "Deco Emoji".
     *
     * @return              Success:0  Failure:-1
     */
@Override public int startInputDecoEmoji() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startInputDecoEmoji, _data, _reply, 0);
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
     * Check a candidate word if it's on the pseudo dictionary.
     *
     * @param index         The index of words by {@link #getCandidate(int)}.
     * @return              true If the candidate is pseudo.
     *                      false If the candidate is not pseudo.
     */
@Override public boolean isPseudoCandidate(int index) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(index);
mRemote.transact(Stub.TRANSACTION_isPseudoCandidate, _data, _reply, 0);
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
     * Get IMESetting Information.
     *
     * @return              IMESetting Information.
     *                      If you want to get IMESetting Information,
     *                      call Bundle#getBoolean("keySound") or 
     *                      call Bundle#getBoolean("keyVibration") or 
     *                      call Bundle#getBoolean("previewPopup").
     */
@Override public android.os.Bundle getIMESettingInfo() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getIMESettingInfo, _data, _reply, 0);
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
     * Get the result of Morphological analysis.
     *
     * @param input         String to morphological analysis.
     * @param readingsMax   Maximum count of getting readings.
     * @return              The result of Morphological analysis.
     * <BR>For values
     * <ul>
     * <li>Calls {@code Bundle#getInt("result")} to get the Result of the processing.</li>
     * <li>Calls {@code Bundle#getStringArray("strings")} to get the Notation of words.</li>
     * <li>Calls {@code Bundle#getStringArray("readings")} to get the readings.</li>
     * <li>Calls {@code Bundle#getShortArray("wordclasses")} to get the attributes.</li>
     * </ul>
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
static final int TRANSACTION_disconnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_isConnected = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_init = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_setDictionary = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_searchCandidate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getCandidate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_memorizeCandidate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_addWord = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_searchWord = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_startInputDecoEmoji = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_isPseudoCandidate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getIMESettingInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_splitWord = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
}
/**
     * Disconnect from service.
     *
     * @return              Success:0  Failure:-1
     */
public int disconnect() throws android.os.RemoteException;
/**
     * Check the connector is still connecting the service.
     *
     * @return              Alive:true  NoAlive:false
     */
public boolean isConnected() throws android.os.RemoteException;
/**
     * Initialize the internal state of the VoiceEditor.
     * <BR>For initialization
     * <ul>
     * <li>Result of the candidate.</li>
     * <li>Result of the prediction.</li>
     * <li>Result of the consecutive clauses.</li>
     * </ul>
     * @return              Success:0  Failure:-1
     */
public int init() throws android.os.RemoteException;
/**
     * Setting the search condition candidate.
     *
     * @param dictionaryType     Type of used dictionary.
     * @param emojiFilter        true if an emoji filter will be enable.
     * @param decoemojiFilter    true if an decoemoji filter will be enable.
     * @return                   Success:0 Failure:-1
     */
public int setDictionary(int dictionaryType, boolean emojiFilter, boolean decoemojiFilter) throws android.os.RemoteException;
/**
     * Make the prediction candidates.
     *
     * You can get prediction candidates by the engine based on
     * given reading strings.
     * Gets the strings of prediction candidates
     * by calling {@link #getCandidate(int)}.
     * To realize the function of wildcard prediction,
     * specify the values of minLen and maxLen of the desired input strings.
     * <p>For example,
     * <ul>
     *   <li>To get strings of at least and no more than 5 characters, set minLen and maxLen to 5. </li>
     *   <li>To get strings of more than 3 characters, set minLen to 3 and maxLen to -1.</li>
     * </ul>
     *
     * @param phonetic      The phonetic (YOMI) of word.
     * @param minLen        The minimum length of a word to predict.
     *                      (minLen <= 0  : no limit)
     * @param maxLen        The maximum length of a word to predict.
     *                      (maxLen <= -1 : no limit)
     * @return              Plus value if there are candidates;
     *                      0 if there is no candidate;
     *                      -1 if an error occurs.
     */
public int searchCandidate(java.lang.String phonetic, int minLen, int maxLen) throws android.os.RemoteException;
/**
     * Gets candidate strings for either prediction or conversion.
     *
     * Only gets the number of candidate strings specified in the parameters.
     * By repeatedly calling this API, you can get
     * the all the available candidates.
     * If there are no more candidates left, the API returns {@code null}.
     * The "candidate" and "stroke" will both be stored
     * as an array in {@link Bundle}.
     * Calls {@code Bundle#getStringArray("candidate")} to get the "reading",
     * and  set "stroke" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the engine will only return the number of existent ones.
     *
     * @param maxCandidates         The maximum number of candidates
     *                                  returned by this method.
     * @return                      Candidate words.
     *                              If you want to get phonetic of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate".
     *                              If there is no candidate words result,
     *                              'Bundle # getStringArray ("candidate")' return zero-length array.
     */
public android.os.Bundle getCandidate(int maxCandidates) throws android.os.RemoteException;
/**
     * Learn prediction candidates and consecutive clauses.
     *
     * The index of the parameter starts at 0 using the order
     * in which candidates were obtained by {@link getCandidate(int)}.
     * For example, if the third candidate obtained by getCandidate()
     * is to be learned, it will be set to index 2.
     *
     * @param index                 The index of words by calling "getCandidate(int)".
     * @param learnFlag             true if learning predictions candidate will be enable.
     * @param connectFlag           true if learning consecutive clauses will be enable.
     * @return                      Success:0  Failure:-1
     */
public int memorizeCandidate(int index, boolean learnFlag, boolean connectFlag) throws android.os.RemoteException;
/**
     * Add a word to the learning dictionary.
     *
     * Registers reading and notation into the Learning dictionary.
     * Executes a word registration by specifying the reading string into
     * "phonetic" and the notation string into "candidate".
     *
     * @param candidate     Notation of words to learn.
     * @param phonetic      phonetic of words to learn.
     * @param attribute     Attribute value of the word. Index of the lexical category group.
     * @param relation      Relation learning flag
     *                      (learning relations with the previous registered word.
     *                       0:don't learn 1:do learn)
     * @return              Success:0  Failure:-1
     */
public int addWord(java.lang.String candidate, java.lang.String phonetic, int attribute, int relation) throws android.os.RemoteException;
/**
     * Search words on the user dictionary.
     *
     * Get search results by calling {@link #getCandidate(int)}.
     * Search method is a matching front.
     *
     * @param phonetic      phonetic of word to search from dictionary.
     * @return              found:1  Not found:0  Failure:-1
     */
public int searchWord(java.lang.String phonetic) throws android.os.RemoteException;
/**
     * Input start of "Deco Emoji".
     *
     * @return              Success:0  Failure:-1
     */
public int startInputDecoEmoji() throws android.os.RemoteException;
/**
     * Check a candidate word if it's on the pseudo dictionary.
     *
     * @param index         The index of words by {@link #getCandidate(int)}.
     * @return              true If the candidate is pseudo.
     *                      false If the candidate is not pseudo.
     */
public boolean isPseudoCandidate(int index) throws android.os.RemoteException;
/**
     * Get IMESetting Information.
     *
     * @return              IMESetting Information.
     *                      If you want to get IMESetting Information,
     *                      call Bundle#getBoolean("keySound") or 
     *                      call Bundle#getBoolean("keyVibration") or 
     *                      call Bundle#getBoolean("previewPopup").
     */
public android.os.Bundle getIMESettingInfo() throws android.os.RemoteException;
/**
     * Get the result of Morphological analysis.
     *
     * @param input         String to morphological analysis.
     * @param readingsMax   Maximum count of getting readings.
     * @return              The result of Morphological analysis.
     * <BR>For values
     * <ul>
     * <li>Calls {@code Bundle#getInt("result")} to get the Result of the processing.</li>
     * <li>Calls {@code Bundle#getStringArray("strings")} to get the Notation of words.</li>
     * <li>Calls {@code Bundle#getStringArray("readings")} to get the readings.</li>
     * <li>Calls {@code Bundle#getShortArray("wordclasses")} to get the attributes.</li>
     * </ul>
     */
public android.os.Bundle splitWord(java.lang.String input, int readingsMax) throws android.os.RemoteException;
}
