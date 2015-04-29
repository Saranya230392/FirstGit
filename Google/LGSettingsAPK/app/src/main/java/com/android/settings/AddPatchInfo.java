package com.android.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AddPatchInfo {
    private ArrayList<Addinfo> mList;

    public AddPatchInfo() {
        // TODO Auto-generated constructor stub
        mList = new ArrayList<Addinfo>();
    }

    public void sortList() {
        Collections.sort(mList, new Comparator<Addinfo>() {
            @Override
            public int compare(Addinfo patch1, Addinfo patch2) {
                // TODO Auto-generated method stub
                String id1 = String.valueOf((patch1).getDay());
                String id2 = String.valueOf((patch2).getDay());

                // ascending order
                return id1.compareTo(id2);

                // descending order
                //return id2.compareTo(id1);
            }
        });
    }

    public StringBuffer getPatchInfo() {
        int length = mList.size();
        StringBuffer sb = new StringBuffer();
        if (0 >= length) {
            return sb;
        }
        String day = mList.get(0).mDay;
        for (int i = 0; i < length; i++) {
            if (!day.equals(mList.get(i).mDay)) {
                sb.append("\n");
            }
            sb.append(mList.get(i).getAllinfo());
            day = mList.get(i).mDay;
        }
        return sb;
    }

    public StringBuffer getPatchInfo_user(String user) {
        int length = mList.size();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            if (user.equals(mList.get(i).getDeveloper())) {
                sb.append(mList.get(i).getAllinfo());
            }
        }
        return sb;
    }

    class Addinfo implements Comparable<Addinfo> {
        private String mNumber = null;
        private String mDevName = null;
        private String mDay = null;
        private String mSubject = null;

        public Addinfo(String _num, String _devname, String _day, String _subject) {
            // TODO Auto-generated constructor stub
            mNumber = _num;
            mDevName = _devname;
            mDay = _day;
            mSubject = _subject;
        }

        public String getAllinfo() {
            StringBuffer sb = new StringBuffer();
            sb.append("[" + mDay + "] ");
            sb.append("[" + mNumber + "] ");
            sb.append("[" + mDevName + "] ");
            sb.append(" *** " + mSubject);
            sb.append("\n");
            return sb.toString();
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return getAllinfo();
        }

        public String getSubinfo() {
            return mSubject;
        }

        public int getNumber() {
            return Integer.parseInt(mNumber);
        }

        public int getDay() {
            return Integer.parseInt(mDay);
        }

        public String getDeveloper() {
            return mDevName;
        }

        @Override
        public int compareTo(Addinfo another) {
            // TODO Auto-generated method stub
            int compareDay = another.getDay();
            return (int)(compareDay - Integer.parseInt(this.mDay));
        }
    }

    public class Sound {

        @SuppressWarnings("unchecked")
        public Sound() {
            // TODO Auto-generated constructor stub
            addInfo(mList);
            sortList();
        }

        private void addInfo(ArrayList<Addinfo> mSound) {
            addDeveloper_1_1(mSound);
            addDeveloper_1_2(mSound);
            addDeveloper_1_3(mSound);
            addDeveloper_1_4(mSound);
            addDeveloper_1_5(mSound);

            addDeveloper_2_1(mSound);
            addDeveloper_2_2(mSound);
            addDeveloper_2_3(mSound);
            addDeveloper_2_4(mSound);
            addDeveloper_2_5(mSound);

            addDeveloper_3_1(mSound);
            addDeveloper_3_2(mSound);
            addDeveloper_3_3(mSound);
            addDeveloper_3_4(mSound);
            addDeveloper_3_5(mSound);

        }

        private void addDeveloper_1_1(ArrayList<Addinfo> mSound) {
            //mSound.add(new Addinfo("##################DEV1##################"));
            mSound.add(new Addinfo("001", "hakgyu98.kim", "20130805",
                    "sprint vibrate pattern modify for A1"));
            mSound.add(new Addinfo("002", "hakgyu98.kim", "20130821",
                    "Emergency call support error fixed"));
            mSound.add(new Addinfo("003", "hakgyu98.kim", "20130823",
                    "VibrateCreation's icon changed(shortcut)"));
            mSound.add(new Addinfo("004", "hakgyu98.kim", "20130827",
                    "Emergency tone default set error fixed"));
            mSound.add(new Addinfo("005", "hakgyu98.kim", "20130829",
                    "Ringtone add button for BreadCrumb"));
            mSound.add(new Addinfo("010", "hakgyu98.kim", "20130910",
                    "vibrate creation issue fixed for spr"));
            mSound.add(new Addinfo("015", "hakgyu98.kim", "20130924",
                    "quiettime doublue touch issue fixed"));
            mSound.add(new Addinfo("020", "hakgyu98.kim", "20131010",
                    "voice notification ui moving issue fixed"));
            mSound.add(new Addinfo("025", "hakgyu98.kim", "20131021",
                    "multi delete do not support upkey"));
            mSound.add(new Addinfo("030", "hakgyu98.kim", "20131107",
                    "vibratepatterninfo memory issue"));
            mSound.add(new Addinfo("035", "hakgyu98.kim", "20131119",
                    "quietmode on, volume popup back key issue fixed"));
            mSound.add(new Addinfo("040", "hakgyu98.kim", "20131125",
                    "ringtone 1 delete item scenario applied"));
            mSound.add(new Addinfo("045", "hakgyu98.kim", "20131127",
                    "ringtone position error fixed"));
            mSound.add(new Addinfo("050", "hakgyu98.kim", "20131209",
                    "ringtone 1 item delete scenario remove"));
            mSound.add(new Addinfo("051", "hakgyu98.kim", "20131216",
                    "quietmode help token issue for VZW"));
            mSound.add(new Addinfo("060", "hakgyu98.kim", "20140107",
                    "quietmode recent app remove"));
            mSound.add(new Addinfo("065", "hakgyu98.kim", "20140115",
                    "vibrate rename issue fixed"));
        }

        /* Dev 1*/
        private void addDeveloper_1_2(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo("006", "hakgyu98.kim", "20130829",
                    "Ringtone volume support for A070"));
            mSound.add(new Addinfo("011", "hakgyu98.kim", "20130911",
                    "Ringtone/Notification sound/Vibration delete added"));
            mSound.add(new Addinfo("016", "hakgyu98.kim", "20131004",
                    "quietmode noti error fixed"));
            mSound.add(new Addinfo("021", "hakgyu98.kim", "20131010",
                    "quietmode help menu changed"));
            mSound.add(new Addinfo("026", "hakgyu98.kim", "20131023",
                    "smart ringtone menu issue fixed"));
            mSound.add(new Addinfo("031", "hakgyu98.kim", "20131106",
                    "quietmode help land layout not merged"));
            mSound.add(new Addinfo("036", "hakgyu98.kim", "20131119",
                    "mpcs power-up tone menu delete"));
            mSound.add(new Addinfo("041", "hakgyu98.kim", "20131126",
                    "vibration delete icon issue fixed"));
            mSound.add(new Addinfo("046", "hakgyu98.kim", "20131128",
                    "sd encryption build error fixed"));
            mSound.add(new Addinfo("051", "hakgyu98.kim", "20131210",
                    "talkback can read quietmode alertactivity"));
            mSound.add(new Addinfo("056", "hakgyu98.kim", "20131217",
                    "sound breadcrumb merged and 2 image button issue"));
            mSound.add(new Addinfo("061", "hakgyu98.kim", "20140108",
                    "vibrate touch issue fixed in vibrate picker"));
            mSound.add(new Addinfo("066", "hakgyu98.kim", "20140115",
                    "x5 overlay res quietmode help image issue fixed"));
        }

        private void addDeveloper_1_3(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo("007", "hakgyu98.kim", "20130830",
                    "QuietMode delete windows's text color issue gray -> black"));
            mSound.add(new Addinfo("012", "hakgyu98.kim", "20130912",
                    "vibrate strength incoming call menu for 070"));
            mSound.add(new Addinfo("017", "hakgyu98.kim", "20131007",
                    "ringtone/notification/vibration delete activated"));
            mSound.add(new Addinfo("022", "hakgyu98.kim", "20131015",
                    "quietmode help declare missed in manifest"));
            mSound.add(new Addinfo("027", "hakgyu98.kim", "20131028",
                    "delete activity title bar gap issue fixed"));
            mSound.add(new Addinfo("032", "hakgyu98.kim", "20131114",
                    "use smart ringtone flag from internal"));
            mSound.add(new Addinfo("037", "hakgyu98.kim", "20131121",
                    "quietmode help talkback issue fixed"));
            mSound.add(new Addinfo("042", "hakgyu98.kim", "20131126",
                    "ringtone delete icon issue fixed"));
            mSound.add(new Addinfo("047", "hakgyu98.kim", "20131129",
                    "ringtone default seleck and ok click error fixed"));
            mSound.add(new Addinfo("052", "hakgyu98.kim", "20131212",
                    "ringtone storage null check added"));
            mSound.add(new Addinfo("057", "hakgyu98.kim", "20131217",
                    "resetSoundSetting vibrate issue fixed"));
            mSound.add(new Addinfo("062", "hakgyu98.kim", "20140109",
                    "quietmode help land image issue fixed for x5"));
            mSound.add(new Addinfo("067", "hakgyu98.kim", "20140117",
                    "vibrate rename issue #2"));
        }

        private void addDeveloper_1_4(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo("008", "hakgyu98.kim", "201300906",
                    "vibrate create button issue with sound profile"));
            mSound.add(new Addinfo("013", "hakgyu98.kim", "20130916",
                    "action bar side effect fixed for tablet"));
            mSound.add(new Addinfo("018", "hakgyu98.kim", "20131008",
                    "drm issue temp blocked. not solved from LGSF"));
            mSound.add(new Addinfo("023", "hakgyu98.kim", "20131016",
                    "ringtone summary not changing issue fixed"));
            mSound.add(new Addinfo("028", "hakgyu98.kim", "20131101",
                    "volumepreferneceEx issue fixed"));
            mSound.add(new Addinfo("033", "hakgyu98.kim", "20131114",
                    "ringtonemanagerEx applied"));
            mSound.add(new Addinfo("038", "hakgyu98.kim", "20131122",
                    "vibrate delete issue fixed"));
            mSound.add(new Addinfo("043", "hakgyu98.kim", "20131127",
                    "volume thumb icon changed"));
            mSound.add(new Addinfo("048", "hakgyu98.kim", "20131202",
                    "default ringtone/notficaion setting when delete"));
            mSound.add(new Addinfo("053", "hakgyu98.kim", "20131213",
                    "timedsilent fragment issue fixed"));
            mSound.add(new Addinfo("058", "hakgyu98.kim", "20131218",
                    "quietmode no contact, scroll view added"));
            mSound.add(new Addinfo("063", "hakgyu98.kim", "20140109",
                    "encryption battery&plug text changed"));
        }

        private void addDeveloper_1_5(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo("009", "hakgyu98.kim", "20130909",
                    "quietmode no contact text position issue"));
            mSound.add(new Addinfo("014", "hakgyu98.kim", "20130923",
                    "quietmode contact delete sync issue fixed"));
            mSound.add(new Addinfo("019", "hakgyu98.kim", "20131008",
                    "delete activity's color issue fixed"));
            mSound.add(new Addinfo("024", "hakgyu98.kim", "20131021",
                    "QuietMode Contact null pointer exception fixed"));
            mSound.add(new Addinfo("029", "hakgyu98.kim", "20131105",
                    "vibrate longclick issue fixed by native limitation"));
            mSound.add(new Addinfo("034", "hakgyu98.kim", "20131114",
                    "quietmode help RTL issue fixed"));
            mSound.add(new Addinfo("039", "hakgyu98.kim", "20131125",
                    "quietmode help image wrong with xxhdpi"));
            mSound.add(new Addinfo("044", "hakgyu98.kim", "20131127",
                    "ringtone/notification 1 delete side effect fixed"));
            mSound.add(new Addinfo("049", "hakgyu98.kim", "20131203",
                    "silent uri setting error fixed for att"));
            mSound.add(new Addinfo("054", "hakgyu98.kim", "20131216",
                    "quietmode hlep ok button added"));
            mSound.add(new Addinfo("059", "hakgyu98.kim", "20131220",
                    "sd card eject sorry popup for w3_tim_br"));
            mSound.add(new Addinfo("064", "hakgyu98.kim", "20140113",
                    "quietmode allowed list layout issue fixed"));
        }

        /* Dev 2*/
        private void addDeveloper_2_1(ArrayList<Addinfo> mSound) {
            //mSound.add(new Addinfo("##################DEV2##################"));
            mSound.add(new Addinfo("001", "hyunjeong.shin", "20130805",
                    "Quiet mode - allowed contact list text color change - Z"));
            mSound.add(new Addinfo("002", "hyunjeong.shin", "20130807",
                    "Voice Notification - not support SMS for KDDI"));
            mSound.add(new Addinfo("003", "hyunjeong.shin", "20130822",
                    "not support RTL icon in voice notification"));
            mSound.add(new Addinfo("004", "hyunjeong.shin", "20130902",
                    "Quiet mode time picker issue - when SUI double touch"));
            mSound.add(new Addinfo("005", "hyunjeong.shin", "20130902",
                    "contact list item text color issue - black"));
            mSound.add(new Addinfo("006", "hyunjeong.shin", "20130905",
                    "add vibrate volume summary for a wifi"));
            mSound.add(new Addinfo("007", "hyunjeong.shin", "20130912",
                    "add vibrate function in quiet mode"));
            mSound.add(new Addinfo("008", "hyunjeong.shin", "20130913",
                    "add vibrate feedback for quiet mode"));
            mSound.add(new Addinfo("009", "hyunjeong.shin", "20130925",
                    "fixed null exception for ringervolume - key event"));
        }

        private void addDeveloper_2_2(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo("010", "hyunjeong.shin", "20131230",
                    "allowedcallsetting/scheduledmode - back issue"));
        }

        private void addDeveloper_2_3(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo("011", "hyunjeong.shin", "20131230",
                    "quiet time - switch padding issue"));
        }

        private void addDeveloper_2_4(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo("012", "hyunjeong.shin", "20140103",
                    "quiet time - action bar icon issue"));
        }

        private void addDeveloper_2_5(ArrayList<Addinfo> mSound) {
        }

        /* Dev 3*/
        private void addDeveloper_3_1(ArrayList<Addinfo> mSound) {
            //mSound.add(new Addinfo("##################DEV3##################"));
            mSound.add(new Addinfo(
                    "001",
                    "susin.park",
                    "20130806",
                    "Quiet mode - notification icon & string changed"));
            mSound.add(new Addinfo(
                    "002",
                    "susin.park",
                    "20130808",
                    "Incoming call vibration create - ringer mode change issue fixed"));
            mSound.add(new Addinfo(
                    "003",
                    "susin.park",
                    "20130809",
                    "LG service - vibrate play code change - pattern use"));
            mSound.add(new Addinfo(
                    "004",
                    "susin.park",
                    "20130816",
                    "Incoming call vibration create - power on/off key issue fixed"));
            mSound.add(new Addinfo(
                    "005",
                    "susin.park",
                    "20130820",
                    "Quiet mode switch update defence code add"));
            mSound.add(new Addinfo(
                    "006",
                    "susin.park",
                    "20130819",
                    "070 model sound menu update & Quiet time fragment modified"));
            mSound.add(new Addinfo(
                    "007",
                    "susin.park",
                    "20130822",
                    "Quiet time - BreadCrumb switch padding modified"));
            mSound.add(new Addinfo(
                    "008",
                    "susin.park",
                    "20130822",
                    "Reset Setting for Sound add"));
            mSound.add(new Addinfo(
                    "009",
                    "susin.park",
                    "20130823",
                    "Reset Setting for Quiet mode status"));
            mSound.add(new Addinfo(
                    "010",
                    "susin.park",
                    "20130827",
                    "ResetSettings.java - add sound Reset code"));
            mSound.add(new Addinfo(
                    "011",
                    "susin.park",
                    "20130827",
                    "add code Sound profile popup dismiss for Quiet mode on"));
            mSound.add(new Addinfo(
                    "012",
                    "susin.park",
                    "20130829",
                    "Z model - Quiet mode old icon issue fixed"));
            mSound.add(new Addinfo(
                    "013",
                    "susin.park",
                    "20130902",
                    "Quiet mode - Volume popup side key issue fixed"));
            mSound.add(new Addinfo(
                    "014",
                    "susin.park",
                    "20130902",
                    "A070 - internet call support method modified"));
            mSound.add(new Addinfo(
                    "015",
                    "susin.park",
                    "20130904",
                    "Quiet mode - IW language set layout modified"));
            mSound.add(new Addinfo(
                    "020",
                    "susin.park",
                    "20130923",
                    "ByteLengthFilter - coding rule modified"));
            mSound.add(new Addinfo(
                    "025",
                    "susin.park",
                    "20131024",
                    "Quiet mode switch prefernence hide line issue fixed"));
            mSound.add(new Addinfo(
                    "030",
                    "susin.park",
                    "20131101",
                    "Vibrate Creation delay issue fixed"));
            mSound.add(new Addinfo(
                    "035",
                    "susin.park",
                    "20131125",
                    "A070 - dialpad touch tone db sync issue fixed"));
            mSound.add(new Addinfo(
                    "040",
                    "susin.park",
                    "20131227",
                    "Quiet mode Service Refactoring"));
        }

        private void addDeveloper_3_2(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo(
                    "016",
                    "susin.park",
                    "20130904",
                    "QuietmodeSwitchPreference coding rule modified"));
            mSound.add(new Addinfo(
                    "021",
                    "susin.park",
                    "20130925",
                    "Quiet mode All files - coding rule modified"));
            mSound.add(new Addinfo(
                    "026",
                    "susin.park",
                    "20131025",
                    "vibrate creation layout name changed"));
            mSound.add(new Addinfo(
                    "031",
                    "susin.park",
                    "20131112",
                    "Quiet time setRepeating issue fixed"));
            mSound.add(new Addinfo(
                    "036",
                    "susin.park",
                    "20131202",
                    "Quiet mode time button text bold style modified"));
            mSound.add(new Addinfo(
                    "041",
                    "susin.park",
                    "20140107",
                    "Quiet mode switch drag issue fixed"));
        }

        private void addDeveloper_3_3(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo(
                    "017",
                    "susin.park",
                    "20130911",
                    "Quiet mode main activity upkey & back key issue fixed"));
            mSound.add(new Addinfo(
                    "022",
                    "susin.park",
                    "20130925",
                    "SoundSettings - coding rule modified"));
            mSound.add(new Addinfo(
                    "027",
                    "susin.park",
                    "20131025",
                    "Quiet mode Switch on delay issue fixed"));
            mSound.add(new Addinfo(
                    "032",
                    "susin.park",
                    "20131113",
                    "MPCS Power up tone menu summary issue fixed"));
            mSound.add(new Addinfo(
                    "037",
                    "susin.park",
                    "20131209",
                    "Quiet mode Scheduled off issue fixed"));
            mSound.add(new Addinfo(
                    "042",
                    "susin.park",
                    "20140113",
                    "Quiet mode SwitchPreference touch sound issue fixed"));
        }

        private void addDeveloper_3_4(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo(
                    "018",
                    "susin.park",
                    "20130916",
                    "Quiet mode Contact allowed init issue fixed & coding rule modified"));
            mSound.add(new Addinfo(
                    "023",
                    "susin.park",
                    "20131021",
                    "Vibrate creation vibrate issue fixed"));
            mSound.add(new Addinfo(
                    "028",
                    "susin.park",
                    "20131025",
                    "Vibrate Creation bulid error fixed"));
            mSound.add(new Addinfo(
                    "033",
                    "susin.park",
                    "20131121",
                    "Quiet mode setRepeating method reuse pach modified"));

            mSound.add(new Addinfo(
                    "038",
                    "susin.park",
                    "20131211",
                    "Quiet mode AlertActivity dissmiss issue fixed"));
            mSound.add(new Addinfo(
                    "043",
                    "susin.park",
                    "20140117",
                    "Quiet mode scheduled switch on/off issue fixed"));
        }

        private void addDeveloper_3_5(ArrayList<Addinfo> mSound) {
            mSound.add(new Addinfo(
                    "019",
                    "susin.park",
                    "20130923",
                    "QuietModeInfo - coding rule modified"));
            mSound.add(new Addinfo(
                    "024",
                    "susin.park",
                    "20131022",
                    "Vibrate creation delay issue fixed"));
            mSound.add(new Addinfo(
                    "029",
                    "susin.park",
                    "20131029",
                    "Quiet mode - set Quiet time switch sync issue fixed"));
            mSound.add(new Addinfo(
                    "034",
                    "susin.park",
                    "20131121",
                    "Quiet mode help activity upkey issue fixed"));
            mSound.add(new Addinfo(
                    "039",
                    "susin.park",
                    "20131217",
                    "Quiet mode talkback issue fixed"));
            mSound.add(new Addinfo(
                    "044",
                    "susin.park",
                    "20140120",
                    "Quiet mode vibrate option on/off issue fixed"));
        }
    }

    public class QuickButton {
        public QuickButton() {
            // TODO Auto-generated constructor stub
            addInfo(mList);
            sortList();
        }

        private void addInfo(ArrayList<Addinfo> mQuickButton) {
            addDeveloper_1_1(mQuickButton);
            addDeveloper_1_2(mQuickButton);
            addDeveloper_1_3(mQuickButton);
            addDeveloper_1_4(mQuickButton);
            addDeveloper_1_5(mQuickButton);

            addDeveloper_2_1(mQuickButton);
            addDeveloper_2_2(mQuickButton);
            addDeveloper_2_3(mQuickButton);
            addDeveloper_2_4(mQuickButton);
            addDeveloper_2_5(mQuickButton);

            addDeveloper_3_1(mQuickButton);
            addDeveloper_3_2(mQuickButton);
            addDeveloper_3_3(mQuickButton);
            addDeveloper_3_4(mQuickButton);
            addDeveloper_3_5(mQuickButton);
        }

        /* Dev 1*/
        private void addDeveloper_1_1(ArrayList<Addinfo> mQuickButton) {
            mQuickButton.add(new Addinfo("001", "hakgyu98.kim", "20140110",
                    "quickbutton allapslist title issue fixed"));
        }

        private void addDeveloper_1_2(ArrayList<Addinfo> mQuickButton) {
        }

        private void addDeveloper_1_3(ArrayList<Addinfo> mQuickButton) {
        }

        private void addDeveloper_1_4(ArrayList<Addinfo> mQuickButton) {
        }

        private void addDeveloper_1_5(ArrayList<Addinfo> mQuickButton) {
        }

        /* Dev 2*/
        private void addDeveloper_2_1(ArrayList<Addinfo> mQuickButton) {

        }

        private void addDeveloper_2_2(ArrayList<Addinfo> mQuickButton) {
        }

        private void addDeveloper_2_3(ArrayList<Addinfo> mQuickButton) {
        }

        private void addDeveloper_2_4(ArrayList<Addinfo> mQuickButton) {
        }

        private void addDeveloper_2_5(ArrayList<Addinfo> mQuickButton) {
        }

        /* Dev 3*/
        private void addDeveloper_3_1(ArrayList<Addinfo> mQuickButton) {
            mQuickButton.add(new Addinfo("001", "susin.park", "20130823",
                    "Reset setting for Quick button"));
        }

        private void addDeveloper_3_2(ArrayList<Addinfo> mQuickButton) {
        }

        private void addDeveloper_3_3(ArrayList<Addinfo> mQuickButton) {
        }

        private void addDeveloper_3_4(ArrayList<Addinfo> mQuickButton) {
        }

        private void addDeveloper_3_5(ArrayList<Addinfo> mQuickButton) {
        }
    }

    public class ShortCut {
        public ShortCut() {
            // TODO Auto-generated constructor stub
            addInfo(mList);
            sortList();
        }

        private void addInfo(ArrayList<Addinfo> mShortCut) {
            addDeveloper_1_1(mShortCut);
            addDeveloper_1_2(mShortCut);
            addDeveloper_1_3(mShortCut);
            addDeveloper_1_4(mShortCut);
            addDeveloper_1_5(mShortCut);

            addDeveloper_2_1(mShortCut);
            addDeveloper_2_2(mShortCut);
            addDeveloper_2_3(mShortCut);
            addDeveloper_2_4(mShortCut);
            addDeveloper_2_5(mShortCut);

            addDeveloper_3_1(mShortCut);
            addDeveloper_3_2(mShortCut);
            addDeveloper_3_3(mShortCut);
            addDeveloper_3_4(mShortCut);
            addDeveloper_3_5(mShortCut);
        }

        /* Dev 1*/
        private void addDeveloper_1_1(ArrayList<Addinfo> mShortCut) {

        }

        private void addDeveloper_1_2(ArrayList<Addinfo> mShortCut) {
        }

        private void addDeveloper_1_3(ArrayList<Addinfo> mShortCut) {
        }

        private void addDeveloper_1_4(ArrayList<Addinfo> mShortCut) {
        }

        private void addDeveloper_1_5(ArrayList<Addinfo> mShortCut) {
        }

        /* Dev 2*/
        private void addDeveloper_2_1(ArrayList<Addinfo> mShortCut) {

        }

        private void addDeveloper_2_2(ArrayList<Addinfo> mShortCut) {
        }

        private void addDeveloper_2_3(ArrayList<Addinfo> mShortCut) {
        }

        private void addDeveloper_2_4(ArrayList<Addinfo> mShortCut) {
        }

        private void addDeveloper_2_5(ArrayList<Addinfo> mShortCut) {
        }

        /* Dev 3*/
        private void addDeveloper_3_1(ArrayList<Addinfo> mShortCut) {

        }

        private void addDeveloper_3_2(ArrayList<Addinfo> mShortCut) {
        }

        private void addDeveloper_3_3(ArrayList<Addinfo> mShortCut) {
        }

        private void addDeveloper_3_4(ArrayList<Addinfo> mShortCut) {
        }

        private void addDeveloper_3_5(ArrayList<Addinfo> mShortCut) {
        }
    }

    public class Acccessibility {
        public Acccessibility() {
            // TODO Auto-generated constructor stub
            addInfo(mList);
            sortList();
        }

        private void addInfo(ArrayList<Addinfo> mAcccessibility) {
            addDeveloper_1_1(mAcccessibility);
            addDeveloper_1_2(mAcccessibility);
            addDeveloper_1_3(mAcccessibility);
            addDeveloper_1_4(mAcccessibility);
            addDeveloper_1_5(mAcccessibility);

            addDeveloper_2_1(mAcccessibility);
            addDeveloper_2_2(mAcccessibility);
            addDeveloper_2_3(mAcccessibility);
            addDeveloper_2_4(mAcccessibility);
            addDeveloper_2_5(mAcccessibility);

            addDeveloper_3_1(mAcccessibility);
            addDeveloper_3_2(mAcccessibility);
            addDeveloper_3_3(mAcccessibility);
            addDeveloper_3_4(mAcccessibility);
            addDeveloper_3_5(mAcccessibility);
        }

        /* Dev 1*/
        private void addDeveloper_1_1(ArrayList<Addinfo> mAcccessibility) {
            mAcccessibility.add(new Addinfo("001", "hakgyu98.kim", "20131022",
                    "native accessibility code merge"));
            mAcccessibility.add(new Addinfo("006", "hakgyu98.kim", "20140117",
                    "caption style grid bottom issue fixed"));
        }

        private void addDeveloper_1_2(ArrayList<Addinfo> mAcccessibility) {
            mAcccessibility.add(new Addinfo("002", "hakgyu98.kim", "20131103",
                    "native accessibility KK public merged"));
        }

        private void addDeveloper_1_3(ArrayList<Addinfo> mAcccessibility) {
            mAcccessibility.add(new Addinfo("003", "hakgyu98.kim", "20131119",
                    "native accessibility action name delete to enter"));
        }

        private void addDeveloper_1_4(ArrayList<Addinfo> mAcccessibility) {
            mAcccessibility.add(new Addinfo("004", "hakgyu98.kim", "20131120",
                    "accessibility shortcut issue fixed"));
        }

        private void addDeveloper_1_5(ArrayList<Addinfo> mAcccessibility) {
            mAcccessibility.add(new Addinfo("005", "hakgyu98.kim", "20131209",
                    "native caption jump to LG accessibility"));
        }

        /* Dev 2*/
        private void addDeveloper_2_1(ArrayList<Addinfo> mAcccessibility) {

        }

        private void addDeveloper_2_2(ArrayList<Addinfo> mAcccessibility) {
        }

        private void addDeveloper_2_3(ArrayList<Addinfo> mAcccessibility) {
        }

        private void addDeveloper_2_4(ArrayList<Addinfo> mAcccessibility) {
        }

        private void addDeveloper_2_5(ArrayList<Addinfo> mAcccessibility) {
        }

        /* Dev 3*/
        private void addDeveloper_3_1(ArrayList<Addinfo> mAcccessibility) {

        }

        private void addDeveloper_3_2(ArrayList<Addinfo> mAcccessibility) {
        }

        private void addDeveloper_3_3(ArrayList<Addinfo> mAcccessibility) {
        }

        private void addDeveloper_3_4(ArrayList<Addinfo> mAcccessibility) {
        }

        private void addDeveloper_3_5(ArrayList<Addinfo> mAcccessibility) {
        }
    }

    public class Security {
        public Security() {
            // TODO Auto-generated constructor stub
            addInfo(mList);
            sortList();
        }

        private void addInfo(ArrayList<Addinfo> mSecurity) {
            addDeveloper_1_1(mSecurity);
            addDeveloper_1_2(mSecurity);
            addDeveloper_1_3(mSecurity);
            addDeveloper_1_4(mSecurity);
            addDeveloper_1_5(mSecurity);

            addDeveloper_2_1(mSecurity);
            addDeveloper_2_2(mSecurity);
            addDeveloper_2_3(mSecurity);
            addDeveloper_2_4(mSecurity);
            addDeveloper_2_5(mSecurity);

            addDeveloper_3_1(mSecurity);
            addDeveloper_3_2(mSecurity);
            addDeveloper_3_3(mSecurity);
            addDeveloper_3_4(mSecurity);
            addDeveloper_3_5(mSecurity);
        }

        /* Dev 1*/
        private void addDeveloper_1_1(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo("001", "hakgyu98.kim", "20131008",
                    "encryption menu issue fixed"));
            mSecurity.add(new Addinfo("006", "hakgyu98.kim", "20131029",
                    "encrypt/decrypt api change"));
            mSecurity.add(new Addinfo("011", "hakgyu98.kim", "20131105",
                    "sd card encryption enable/disable by sd slot existing"));
            mSecurity.add(new Addinfo("016", "hakgyu98.kim", "20131125",
                    "hide navigation option changed at cryptkeeperconfig"));
            mSecurity.add(new Addinfo("021", "hakgyu98.kim", "20131130",
                    "cryptkeeper fail count code added"));
            mSecurity.add(new Addinfo("026", "hakgyu98.kim", "20131212",
                    "sim lock not support for SPR operator"));
            mSecurity.add(new Addinfo("031", "hakgyu98.kim", "20131224",
                    "confirm encyrpt phone menu text size up"));
            mSecurity.add(new Addinfo("032", "hakgyu98.kim", "20140109",
                    "sd encryption handler side effect fixed"));
            mSecurity.add(new Addinfo("041", "hakgyu98.kim", "20140116",
                    "wifi model quickencryption file added"));

        }

        private void addDeveloper_1_2(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo("002", "hakgyu98.kim", "20131024",
                    "security sd encryption enable"));
            mSecurity.add(new Addinfo("007", "hakgyu98.kim", "20131031",
                    "security system property api changed"));
            mSecurity.add(new Addinfo("012", "hakgyu98.kim", "20131112",
                    "cryptkeeper navigation hide issue fixed"));
            mSecurity.add(new Addinfo("017", "hakgyu98.kim", "20131125",
                    "PLATSEC234 patch for VZW"));
            mSecurity.add(new Addinfo("022", "hakgyu98.kim", "20131202",
                    "deviceadmin active/deactive delay added"));
            mSecurity.add(new Addinfo("027", "hakgyu98.kim", "20131213",
                    "encryptphone bold issue for VZW(overlay)"));
            mSecurity.add(new Addinfo("032", "hakgyu98.kim", "20131230",
                    "encryptphone menu flag issue fixed"));
            mSecurity.add(new Addinfo("037", "hakgyu98.kim", "20140110",
                    "sd encryption full exception popup orientation issue fixed"));
        }

        private void addDeveloper_1_3(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo("003", "hakgyu98.kim", "20131024",
                    "sim lock count issue fixed"));
            mSecurity.add(new Addinfo("008", "hakgyu98.kim", "20131031",
                    "changed property for checking sd encryption"));
            mSecurity.add(new Addinfo("013", "hakgyu98.kim", "20131113",
                    "sim lock menu display issue fixed with recovering native code "));
            mSecurity.add(new Addinfo("018", "hakgyu98.kim", "20131125",
                    "unencrypt hide navigation issue fixed"));
            mSecurity.add(new Addinfo("023", "hakgyu98.kim", "20131203",
                    "monitoringcert info to trustedcertificate action error fixed"));
            mSecurity.add(new Addinfo("028", "hakgyu98.kim", "20131213",
                    "localwipe token changed for VZW"));
            mSecurity.add(new Addinfo("033", "hakgyu98.kim", "20140103",
                    "sd encryption max file size init"));
            mSecurity.add(new Addinfo("038", "hakgyu98.kim", "20140114",
                    "sd encryption sd full exception handler issue fixed"));
        }

        private void addDeveloper_1_4(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo("004", "hakgyu98.kim", "20131025",
                    "security build error fixed"));
            mSecurity.add(new Addinfo("009", "hakgyu98.kim", "20131031",
                    "security cryptkeeper backbutton issue fixed"));
            mSecurity.add(new Addinfo("014", "hakgyu98.kim", "20131120",
                    "sd encyrption popup exception issue fixed"));
            mSecurity.add(new Addinfo("019", "hakgyu98.kim", "20131130",
                    "cryptkeeper icon for mdpi, ldpi"));
            mSecurity.add(new Addinfo("024", "hakgyu98.kim", "20131210",
                    "bill sim for VDF pin change message error fixed"));
            mSecurity.add(new Addinfo("029", "hakgyu98.kim", "20131224",
                    "sd encyrption other device popup bold issue fixed"));
            mSecurity.add(new Addinfo("034", "hakgyu98.kim", "20140106",
                    "sd encyrption batter&plug status display issue fixed"));
            mSecurity.add(new Addinfo("039", "hakgyu98.kim", "20140114",
                    "cryptkeeper navigation bar sim switch issue fixed"));
        }

        private void addDeveloper_1_5(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo("005", "hakgyu98.kim", "20131028",
                    "security hide navation for menu, memo, notifiation"));
            mSecurity.add(new Addinfo("010", "hakgyu98.kim", "20131103",
                    "security KK public merged"));
            mSecurity.add(new Addinfo("015", "hakgyu98.kim", "20131120",
                    "security setradiopower code merge"));
            mSecurity.add(new Addinfo("020", "hakgyu98.kim", "20131130",
                    "back key enable after pin code failed"));
            mSecurity.add(new Addinfo("025", "hakgyu98.kim", "20131212",
                    "security encrpyt get flag from package"));
            mSecurity.add(new Addinfo("030", "hakgyu98.kim", "20131224",
                    "sd encryption menu optimized"));
            mSecurity.add(new Addinfo("035", "hakgyu98.kim", "20140107",
                    "doing sd encrypt home key and enter setting ck error issue fixed"));
            mSecurity.add(new Addinfo("040", "hakgyu98.kim", "20140115",
                    "sd encryption exception case text layout issue fixed"));
        }

        /* Dev 2*/
        private void addDeveloper_2_1(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo("001", "hyunjeong.shin", "20131226",
                    "security illegal exception clear credential"));
        }

        private void addDeveloper_2_2(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo("002", "hyunjeong.shin", "20131226",
                    "security illegal exception when pressing noti setting"));
        }

        private void addDeveloper_2_3(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo("003", "hyunjeong.shin", "20140106",
                    "change security button fit in bottom"));
        }

        private void addDeveloper_2_4(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo("004", "hyunjeong.shin", "20140106",
                    "delete center image in crypt keeper/sdencryption"));
        }

        private void addDeveloper_2_5(ArrayList<Addinfo> mSecurity) {
        }

        /* Dev 3*/
        private void addDeveloper_3_1(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo(
                    "001",
                    "susin.park",
                    "20131016",
                    "Security-TrustedCredentialsSettings sorry popup issue fixed"));
            mSecurity.add(new Addinfo(
                    "006",
                    "susin.park",
                    "20140120",
                    "Security - Unknown Source popup time string modified"));
        }

        private void addDeveloper_3_2(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo(
                    "002",
                    "susin.park",
                    "20131209",
                    "Security - SD Encription layout issue fixed"));
            mSecurity.add(new Addinfo(
                    "007",
                    "susin.park",
                    "20140120",
                    "Security - Unknown apps remove just once checkbox for not kr(skt,lgu)"));
        }

        private void addDeveloper_3_3(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo(
                    "003",
                    "susin.park",
                    "20131213",
                    "Security - SD Encription description deleted"));
        }

        private void addDeveloper_3_4(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo(
                    "004",
                    "susin.park",
                    "20140109",
                    "Security - add to SKT Unknown apps"));
        }

        private void addDeveloper_3_5(ArrayList<Addinfo> mSecurity) {
            mSecurity.add(new Addinfo(
                    "005",
                    "susin.park",
                    "20140115",
                    "Security - add to LGU Unknown apps"));
        }
    }
}
