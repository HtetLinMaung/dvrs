package com.dvc.constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dvc.models.ComboData;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonConstants {
        // public static List<String> HEADERS = Arrays.asList("serialno",
        // "recipientsname", "nameicpp", "gender",
        //
        // ob", "nric", "passport", "nationality", "organization",
        //
        //
        // "division", "township", "address1", "remark");
        //
        public static List<String> STATE = Arrays.asList(
                        "\u1000\u1001\u103B\u1004\u103A\u1015\u103C\u100A\u103A\u1014\u101A\u103A",
                        "\u1000\u101A\u102C\u1038\u1015\u103C\u100A\u103A\u1014\u101A\u103A",
                        "\u1000\u101B\u1004\u103A\u1015\u103C\u100A\u103A\u1014\u101A\u103A",
                        "\u1001\u103B\u1004\u103A\u1038\u1015\u103C\u100A\u103A\u1014\u101A\u103A",
                        "\u1005\u1005\u103A\u1000\u102D\u102F\u1004\u103A\u1038\u1010\u102D\u102F\u1004\u103A\u1038",
                        "\u1010\u1014\u1004\u103A\u1039\u101E\u102C\u101B\u102E\u1010\u102D\u102F\u1004\u103A\u1038",
                        "\u1015\u1032\u1001\u1030\u1038\u1010\u102D\u102F\u1004\u103A\u1038",
                        "\u1019\u1000\u103D\u1031\u1038\u1010\u102D\u102F\u1004\u103A\u1038",
                        "\u1019\u1014\u1039\u1010\u101C\u1031\u1038\u1010\u102D\u102F\u1004\u103A\u1038",
                        "\u1019\u103D\u1014\u103A\u1015\u103C\u100A\u103A\u1014\u101A\u103A",
                        "\u101B\u1001\u102D\u102F\u1004\u103A\u1015\u103C\u100A\u103A\u1014\u101A\u103A",
                        "\u101B\u1014\u103A\u1000\u102F\u1014\u103A\u1010\u102D\u102F\u1004\u103A\u1038",
                        "\u101B\u103E\u1019\u103A\u1038\u1015\u103C\u100A\u103A\u1014\u101A\u103A",
                        "\u1027\u101B\u102C\u101D\u1010\u102E\u1010\u102D\u102F\u1004\u103A\u1038",

                        "\u1014\u1031\u1015\u103c\u100a\u103a\u1010\u1031\u102c\u103a",
                        "\u101B\u103E\u1019\u103A\u1038\u1015\u103C\u100A\u103A\u1014\u101A\u103A (\u1010\u1031\u102c\u1004\u103a\u1015\u102d\u102f\u1004\u103a\u1038)",
                        "\u101B\u103E\u1019\u103A\u1038\u1015\u103C\u100A\u103A\u1014\u101A\u103A (\u1019\u103c\u1031\u102c\u1000\u103a\u1015\u102d\u102f\u1004\u103a\u1038)");
        public static List<String> HEADERS = Arrays.asList("serialno", "recipientname", "gener", "fathename", "dob",
                        "nricorpassport", "nationality", "organization", "mobilephone", "division", "township",
                        "address1", "remark");
        public static List<String> HEADERS_M = Arrays.asList("serialno", "recipientsname", "gender", "fathername",
                        "dob", "nric", "organization", "mobilephone", "division", "township", "address1", "remark");
        public static List<String> HEADERS_M_V2 = Arrays.asList("serialno", "recipientsname", "gender", "fathername",
                        "dob", "nric", "passport", "organization", "mobilephone", "division", "township", "address1",
                        "remark");
        public static List<String> HEADERS_F = Arrays.asList("serialno", "recipientsname", "gender", "fathername",
                        "dob", "passport", "nationality", "organization", "mobilephone", "division", "township",
                        "address1", "remark");

        public static List<ComboData> APPROVAL_LIST = Arrays.asList(

                        new ComboData("Uploaded", 1),

                        new ComboData("Verified", 10),

                        new ComboData("Submitted", 20),

                        new ComboData("Approved", 30),

                        new ComboData("Approved", 35)

        );

        public static List<ComboData> PAYMENT_LIST = Arrays.asList(

                        new ComboData("-", 0),

                        new ComboData("Processing", 1),

                        new ComboData("Paid", 10)

        );

        public static List<ComboData> VOID_LIST = Arrays.asList(

                        new ComboData("Unvoid", 1),

                        new ComboData("Void", 0)

        );

        public static List<String> HEADERS_MOHS = Arrays.asList("serialno", "recipientsname", "fathername", "dob",
                        "gender", "prefixnrc", "nrccode", "nrctype", "nrcno", "passport", "organization", "mobilephone",
                        "division", "township", "ward", "street", "occupation", "vaccinationcenter", "nationality");

        public static List<String> PREFIX_NRCS = Arrays.asList("ကချင်_၁", "ကယား_၂", "ကရင်_၃", "ချင်း_၄", "စစ်ကိုင်း_၅",
                        "တနင်္သာရီ_၆", "ပဲခူး_၇", "မကွေး_၈", "မန္တလေး_၉", "မွန်_၁၀", "ရခိုင်_၁၁", "ရန်ကုန်_၁၂",
                        "ရှမ်း_၁၃", "ဧရာဝတီ_၁၄");

        public static String NRC_CODE_JSON = "{\"ကချင်_၁\":[\"မကန\",\"‌ဆဘတ\",\"ဝမန\",\"ဆဒန\",\"အဂယ\",\"တနန\",\"ရဗယ\",\"ခဖန\",\"ဆလန\",\"ကပတ\",\"ပဝန\",\"မညန\",\"ဟပန\",\"မကတ\",\"ကမန\",\"ဖကန\",\"ကမတ\",\"ဗမန\",\"ရကန\",\"မလန\",\"မမန\",\"လဂန\",\"ဒဖယ\",\"မစန\",\"ပတအ\",\"မခဘ\",\"နမန\",\"ပနဒ\",\"ခဘဒ\",\"ခလဖ\",\"ဆပဘ\",\"WMW\",\"MKA\",\"MCBW/PND\",\"PTO\",\"CBE\",\"TNI\",\"SPM\",\"SGU\",\"BMO\",\"MNN\",\"MGG\",\"MSI\",\"MMUK\",\"SLK\",\"IGN\",\"PKT\",\"TPO\",\"NMG/PND\",\"AN\",\"MNA\",\"MTK\",\"AP\",\"SDN\",\"HKG\",\"AO\",\"LKG\",\"AR\",\"EN\",\"3.SGU\",\"AS\",\"SLM\",\"AQ\",\"SBM\"],\"ကယား_၂\":[\"လကန\",\"ဒမဆ\",\"ဖရဆ\",\"ရတန\",\"ဘလခ\",\"ဖဆန\",\"မစန\",\"ရသန\",\"LKW\",\"PSG\",\"BKE\",\"PKN\",\"DMO\",\"HSO\",\"STW\",\"BD\",\"BE\"],\"ကရင်_၃\":[\"ဘအန\",\"လဘန\",\"ပကန\",\"ရရသ\",\"သတက\",\"သတန\",\"လသန\",\"ဘဂလ\",\"ဖပန\",\"ကမမ\",\"ကကရ\",\"ကဆက\",\"ကဒန\",\"ဘသဆ\",\"မဝတ\",\"စကလ\",\"ဝလမ\",\"MME\",\"HBE\",\"PUN\",\"PAN\",\"KKK\",\"KIN\",\"TDG\",\"KDN\",\"BN\",\"BO\",\"EK\",\"2.PPN\",\"EL\",\"BP\",\"EM\"],\"ချင်း_၄\":[\"ဟခန\",\"ထတလ\",\"ဖလန\",\"ရခဒ\",\"တတန\",\"တဇန\",\"ကခန\",\"မတန\",\"မတပ\",\"ရဇန\",\"ကပလ\",\"ပလဝ\",\"ဆမန\",\"KPT\",\"HTLM\",\"TDM\",\"TZN\",\"HKA\",\"FLM\",\"MDT\",\"MPI\",\"PWA\",\"MTI\"],\"စစ်ကိုင်း_၅\":[\"စကန\",\"မမတ\",\"မမန\",\"ရဘန\",\"ဝလန\",\"ခဥန\",\"ကဘလ\",\"ကလန\",\"ရဥန\",\"ဒပယ\",\"တဆန\",\"ကမန\",\"မရန\",\"ခဥတ\",\"အရတ\",\"ဘတလ\",\"ယမပ\",\"ပလန\",\"ကနန\",\"ဆလက\",\"ကသန\",\"ထခန\",\"အတန\",\"ဗမန\",\"ကလတ\",\"ပလဘ\",\"ဝသန\",\"ကလထ\",\"ကလဝ\",\"မကန\",\"တမန\",\"ခပန\",\"မသန\",\"မလန\",\"ဖပန\",\"ခတန\",\"ဟမလ\",\"လရန\",\"လဟန\",\"နယန\",\"ပဆန\",\"ဒဟန\",\"မပလ\",\"ထပခ\",\"ဆမရ\",\"ငဇန\",\"CHU\",\"BDN\",\"MWA\",\"AYW\",\"KNI\",\"SLI\",\"PLE\",\"SGG\",\"MMU\",\"YMN\",\"MUG\",\"YEU\",\"TZE\",\"KHA\",\"KBU\",\"WLT\",\"SBO\",\"KNU\",\"DPN/TBN\",\"TMU\",\"KLN\",\"KLA\",\"MKN\",\"KLO\",\"HGG\",\"WTO\",\"BMK\",\"PLU\",\"KTA\",\"IDW\",\"KTI\",\"HMN\",\"LSE\",\"LHE\",\"NYN\",\"MLK\",\"PPN\",\"CT\",\"2.MYG\",\"BY\",\"DM\",\"DN\",\"DO\",\"DP\",\"KILA\",\"DU\",\"DQ\",\"TRX\",\"DR\",\"CS\",\"CU\",\"CV\",\"CX\",\"CW\",\"CY\",\"AL\",\"1.PPN\",\"AM\",\"BNK\"],\"တနင်္သာရီ_၆\":[\"ထဝန\",\"လလန\",\"သရခ\",\"ရဖန\",\"မတန\",\"ကလအ\",\"မအရ\",\"မအန\",\"ကစန\",\"ပလန\",\"တသရ\",\"ပလတ\",\"ကသန\",\"ဘပန\",\"ခမက\",\"ပကမ\",\"ကရရ\",\"မမန\",\"MYK\",\"TVY\",\"TNM\",\"KTHG\",\"BPN\",\"MGIW\",\"LLN\",\"PLW\",\"TYG\",\"YBU\",\"BG\",\"BH\",\"BI\",\"E/MGI\",\"W/MGK\",\"MGIE\",\"BL\",\"BJ\",\"BK\",\"BM\",\"VPT\",\"MIT\"],\"ပဲခူး_၇\":[\"တငန\",\"ရတရ\",\"ထတပ\",\"အတန\",\"ဖမန\",\"ကကန\",\"ပခန\",\"ကတခ\",\"ရကန\",\"ညလပ\",\"ဒဥန\",\"ဝမန\",\"သနပ\",\"ကဝန\",\"သဝတ\",\"လပတ\",\"မလန\",\"မညန\",\"အဖန\",\"ကပက\",\"ဇကန\",\"နတလ\",\"ပမန\",\"သကန\",\"ရတန\",\"ပတတ\",\"ပတန\",\"ပခတ\",\"KKI/KYI\",\"TGO\",\"HTN\",\"PYU\",\"YSE\",\"OTN\",\"PKG\",\"PDE\",\"PDG\",\"SDG\",\"TGN\",\"PGU\",\"ZGN\",\"DKU\",\"WAW\",\"GBK\",\"NBN\",\"NTN\",\"TRY\",\"MHA\",\"PME\",\"KTGA\",\"SGN\",\"TPN\",\"KWA\",\"LPN\",\"OPO\",\"MNO\",\"BR\",\"DC\",\"PRE-PME\",\"DD\",\"PKHG\",\"DH\",\"DF\",\"1.MHA\",\"BV\",\"GC\",\"YSE-YDE\",\"GH\",\"1.KKI\",\"GD\",\"GE\",\"GF\",\"2.TBN-TTN\",\"DG\"],\"မကွေး_၈\":[\"မကန\",\"ရနခ\",\"ခမန\",\"နမန\",\"မသန\",\"တတက\",\"မဘန\",\"ပဖန\",\"စလန\",\"စတရ\",\"ငဖန\",\"သရန\",\"မထန\",\"အလန\",\"ကမန\",\"မတန\",\"ဆပဝ\",\"မလန\",\"ပခက\",\"ရစက\",\"မမန\",\"ပမန\",\"ဆဖန\",\"ဂဂန\",\"ဆမန\",\"ထလန\",\"ကထန\",\"MGE\",\"YSO\",\"CHK\",\"YNG\",\"NMK\",\"SPU\",\"PAK\",\"TDI\",\"ALN\",\"TYT\",\"SWE\",\"KMA\",\"MLA\",\"MTT\",\"MYG\",\"PKU\",\"MBU\",\"PBU\",\"NPE\",\"SDA\",\"SLN\",\"MDN\",\"GGW\",\"SAW\",\"TLN\",\"1.SGU/MBU\",\"SRA\",\"2.MHA\",\"MDN\",\"SBWD\",\"BX\",\"1.MYG\",\"MBU\"],\"မန္တလေး_၉\":[\"မနမ\",\"အမဇ\",\"မရမ\",\"ခအဇ\",\"မနတ\",\"ခမစ\",\"မရတ\",\"မဟမ\",\"ပကခ\",\"အမရ\",\"ပသက\",\"မမန\",\"ပဥလ\",\"မတရ\",\"စကန\",\"သပက\",\"မကန\",\"တကတ\",\"ကဆန\",\"စကတ\",\"မသန\",\"တတဥ\",\"မခန\",\"တသန\",\"ကပတ\",\"နထက\",\"ညဥန\",\"ငဇန\",\"ငသရ\",\"မထလ\",\"မလန\",\"သစန\",\"ဝတန\",\"ရမသ\",\"ပဘန\",\"ဥတသ\",\"ဇယသ\",\"ပဗသ\",\"တကန\",\"ဒခသ\",\"ဇဘသ\",\"ပမန\",\"လဝန\",\"YMN\",\"PBE\",\"TZI\",\"KSE \",\"SKG\",\"TDU\",\"MTTA\",\"CMTSI\",\"CATZN\",\"PGDGN\",\"MDYNW\",\"PTI\",\"AMA\",\"KPG\",\"MHLG\",\"SKU\",\"MMO\",\"TBKN\",\"NZN\",\"MGN\",\"TTA\",\"NYU \",\"MDA\",\"MGK\",\"WDN\",\"MTLA\",\"MHAMY\",\"NTI\",\"MDYSW\",\"MDYSE\",\"MDYNE\",\"AE\",\"E-MDY\",\"W-MDY\",\"DI\",\"APA_AMA\",\"DIDJ\",\"AM_AMD_APTI\",\"DJ\",\"MYA_MDA\",\"DK\",\"2.SGU\",\"DL\",\"DS\",\"DT\",\"TKN\",\"CZ\",\"DA\",\"SGG\",\"DB\",\"1.MTA\",\"PND\",\"DZ\",\"YMN/TKN\",\"BW\",\"PME\",\"EB\",\"DV\",\"MHG\",\"DX\",\"DY\",\"TAK\",\"EA\",\"PMA\",\"LWE\",\"DW\"],\"မွန်_၁၀\":[\"မလမ\",\"ကမရ\",\"ခဆန\",\"သဖရ\",\"မဒန\",\"ရမန\",\"လမန\",\"ခဇန\",\"သထန\",\"ပမန\",\"ကထန\",\"ဘလန\",\"MME\",\"KMW\",\"MDN\",\"YE\",\"CZN\",\"TPZT\",\"TTN\",\"PAG\",\"KTO\",\"BLN\",\"AG\",\"MME-MMN\",\"EF\",\"KRW-KMW\",\"EG\",\"EH\",\"AHT\",\"EI\",\"2.MDN\",\"EJ\",\"BF\",\"EC\",\"ED\",\"EE\"],\"ရခိုင်_၁၁\":[\"စတန\",\"ရသတ\",\"ပဏက\",\"ပတန\",\"မဥန\",\"မပန\",\"ကတန\",\"မပန\",\"မတန\",\"ဘသတ\",\"တပဝ\",\"ကဖန\",\"ရဗန\",\"မအန\",\"အမန\",\"သတန\",\"တကန\",\"မအတ\",\"ဂမန\",\"ကတလ\",\"ရဗန\",\"AKB\",\"PTW\",\"PNGN\",\"RTG\",\"KPU\",\"MAG\",\"ANN\",\"RRE\",\"MPN\",\"MBA\",\"KTW\",\"GWA\",\"TGK\",\"SDY\",\"MDW\",\"BTG\",\"MHG\",\"AH\",\"CR\",\"PNN\",\"AJ\",\"CQ\",\"CP\",\"CO\",\"CN\",\"PRW\",\"AK\",\"AI\",\"1.MAG\",\"IP\"],\"ရန်ကုန်_၁၂\":[\"သဃက\",\"ရကန\",\"ဥကတ\",\"ဥကမ\",\"သကတ\",\"ဒပန\",\"တမန\",\"ပဇတ\",\"ဗတထ\",\"မဂတ\",\"ဒဂမ\",\"ဒဂရ\",\"ဒဂတ\",\"ဒဂဆ\",\"ကတတ\",\"ပဘတ\",\"လမတ\",\"လသန\",\"အလန\",\"ကမတ\",\"စခန\",\"လမန\",\"ကမရ\",\"မရက\",\"ဒဂန\",\"ဗဟန\",\"ဆကန\",\"သလန\",\"ကတန\",\"တတတ\",\"သခန\",\"ခရန\",\"တတန\",\"ကမန\",\"ကခက\",\"ဒလန\",\"ဆကခ\",\"ကကက\",\"အစန\",\"မဂဒ\",\"မဘန\",\"လကန\",\"တကန\",\"ထတပ\",\"ရပသ\",\"လသယ\",\"တတထ\",\"RGN\",\"MGDN\",\"ISN\",\"TME\",\"SOKA\",\"SKM\",\"DBN\",\"PZDG\",\"BTHG\",\"MTNT\",\"NOKA\",\"YKN\",\"TKA\",\"TGKN\",\"SKI\",\"DLA\",\"KTDA\",\"KMYT\",\"KMDE\",\"SCG\",\"DGN\",\"PBDN\",\"BHN\",\"MYGN\",\"LTA\",\"LMDW\",\"HLG\",\"ALE\",\"COI\",\"SRM\",\"KTN\",\"TGA\",\"KYN\",\"TTE\",\"KCGN\",\"KHU\",\"HBI\",\"HGU\",\"TKI\",\"NKGN\",\"FU\",\"FV\",\"FW\",\"FS\",\"FT\",\"N/RGV\",\"FY\",\"S/RGN\",\"TBN\",\"1.TBN\",\"BQ\",\"N/OKA\",\"A-B\",\"B/RGN\",\"U/RGN\",\"T/RGN\",\"W/RGN\",\"V/RGN\",\"S/OKA\",\"AA\",\"A/RGN\",\"X/RGN\",\"AD\",\"D/RGN\",\"CA\",\"H/RGN\",\"CB\",\"G/RGN\",\"CC\",\"F/RGN\",\"CJ\",\"N/RGN\",\"CI\",\"O/RGN\",\"CD\",\"E/RGN\",\"AC\",\"C/RGN\",\"CE\",\"J/RGN\",\"CG\",\"L/RGN\",\"CF/CH\",\"K/RGN\",\"CM\",\"P/RGN\",\"JE\",\"I/RGN\",\"CK\",\"Q/RGN\",\"CL\",\"R/RGN\",\"SKN\"],\"ရှမ်း_၁၃\":[\"တကန\",\"ဟပန\",\"ညရန\",\"ဆဆန\",\"ကလန\",\"ပတယ\",\"ရငန\",\"ရစန\",\"ပလန\",\"ဖခန\",\"ကတလ\",\"နတရ\",\"အတန\",\"လလန\",\"လခန\",\"နစန\",\"ကဟန\",\"ကသန\",\"မကန\",\"မရန\",\"ပလတ\",\"မနတ\",\"ခလန\",\"ကလဒ\",\"မစန\",\"လခတ\",\"မနန\",\"မမန\",\"မပန\",\"ဟမန\",\"ကတတ\",\"လရန\",\"သနန\",\"တယန\",\"မရတ\",\"ကလတ\",\"ပယန\",\"နဖန\",\"မဖတ\",\"ပဆန\",\"မကထ\",\"မဆတ\",\"နခန\",\"ကခန\",\"တမည\",\"မဟရ\",\"မကတ\",\"ပဆတ\",\"ကမန\",\"သပန\",\"နခတ\",\"နမတ\",\"နဆန\",\"မတတ\",\"မငန\",\"မလတ\",\"မမတ\",\"မဘန\",\"ဟပတ\",\"ပဝန\",\"မမထ\",\"နတန\",\"ပလထ\",\"လကန\",\"ကကန\",\"ခရဟ\",\"မထတ\",\"ကတန\",\"မလန\",\"မယန\",\"မပထ\",\"မခန\",\"မပတ\",\"တတန\",\"မဆန\",\"မခတ\",\"မတန\",\"ပပက\",\"မထန\",\"မဖန\",\"မယတ\",\"မယထ\",\"တခလ\",\"တလန\",\"ကလထ\",\"TGI\",\"SSG\",\"KLW\",\"LSK\",\"NYHE\",\"PTA\",\"PLG\",\"YNN\",\"PKN\",\"HPE\",\"MST\",\"MTN\",\"KME\",\"HPW\",\"NTU\",\"NSN\",\"NTU\",\"NKO\",\"MMK\",\"MAB\",\"PYG\",\"PWG\",\"NPN\",\"MSE\",\"KKI\",\"NKM\",\"KTG\",\"MKT\",\"MYAG\",\"MPG\",\"TCK\",\"MPK\",\"MYNG\",\"LLM\",\"KHG\",\"LKA\",\"MKG\",\"MSU\",\"KSI\",\"NSG\",\"LSO\",\"TMN/HWI\",\"KLG\",\"MYI\",\"TYN\",\"LKO\",\"MNI\",\"MPN\",\"MMI\",\"HPG\",\"MMW\",\"LKI\",\"KKM\",\"LLG\",\"AY\",\"N.YHE\",\"BB/BC\",\"2.HPG\",\"S.YHE\",\"WYN\",\"AZ\",\"ABN\",\"LSK-BHO\",\"EU\",\"LLN-LLM\",\"EW\",\"MOPN\",\"KSM\",\"KHM-KSU\",\"MHU\",\"AT\",\"AU\",\"ET\",\"ES\",\"EQ\",\"2.KKI\",\"AW\",\"AV\",\"EO\",\"EV\",\"AX\",\"1.HPG\",\"ET\",\"MKO\",\"BA\",\"PSA\",\"MPHG\",\"EKLG\"],\"ဧရာဝတီ_၁၄\":[\"ပသန\",\"ရသယ\",\"ငဆန\",\"ပသရ\",\"ကကထ\",\"ငပတ\",\"ဟကက\",\"ငရက\",\"သပန\",\"ကကန\",\"ရကန\",\"ငသခ\",\"ကပန\",\"ဟသတ\",\"ဇလန\",\"လမန\",\"အဂပ\",\"မအန\",\"ကခန\",\"မအပ\",\"ပတန\",\"ညတန\",\"ဓနဖ\",\"ဖပန\",\"အမတ\",\"ဘကလ\",\"ကလန\",\"ဒဒရ\",\"လပတ\",\"ပစလ\",\"မမက\",\"မမန\",\"အမန\",\"ဝခမ\",\"MAG\",\"KNK\",\"MUN\",\"PTNW\",\"YDN\",\"KLT\",\"DDE\",\"MYA\",\"EME\",\"WKA\",\"LPA\",\"MLGN\",\"BSNW/BSNE\",\"BSNE\",\"NPW\",\"TPG\",\"KGN\",\"YKI\",\"KPW\",\"PPN\",\"BGLE\",\"HZA\",\"DNU\",\"LMA\",\"IGU\",\"ZLN\",\"YKI-NTG\",\"RGN\",\"MMA\",\"MMN\",\"PTW\",\"BGE\",\"AF\",\"BSN-E\",\"BSN-W\",\"KGE\",\"RU\",\"2.MAG\",\"BT\",\"BS\"]}";

        public static List<String> DIVISIONS = Arrays.asList("ကချင်ပြည်နယ်", "ကယားပြည်နယ်", "ကရင်ပြည်နယ်",
                        "ချင်းပြည်နယ်", "စစ်ကိုင်းတိုင်းဒေသကြီး", "တနင်္သာရီတိုင်းဒေသကြီး", "ပဲခူးတိုင်းဒေသကြီး",
                        "မကွေးတိုင်းဒေသကြီး", "မန္တလေးတိုင်းဒေသကြီး", "မွန်ပြည်နယ်", "ရခိုင်ပြည်နယ်",
                        "ရန်ကုန်တိုင်းဒေသကြီး", "ဧရာဝတီတိုင်းဒေသကြီး", "နေပြည်တော်");

        public static String getAddressJson() {
                List<String> datalist = new ArrayList<>();
                datalist.addAll(AddressConstant1.ADDRESS_DATA);
                datalist.addAll(AddressConstant2.ADDRESS_DATA);
                return String.join("},", datalist);
        }

        public static List<String> getTownships(String division) throws IOException {

                Map<String, Object> address = new ObjectMapper().readValue(getAddressJson(), Map.class);
                Map<String, Object> data = (Map<String, Object>) address.get(division);
                return data.entrySet().stream().map(p -> p.getKey()).collect(Collectors.toList());
        }

        public static List<String> getWards(String division, String township) throws IOException {
                Map<String, Object> address = new ObjectMapper().readValue(getAddressJson(), Map.class);
                Map<String, Object> data = (Map<String, Object>) address.get(division);
                return (List<String>) data.get(township);
        }

        public static List<String> getNrcCodes(String prefixnrc) throws IOException {
                Map<String, Object> data = new ObjectMapper().readValue(NRC_CODE_JSON, Map.class);
                return (List<String>) data.get(prefixnrc);
        }

        public static List<String> NATIONALITY_CODES = Arrays.asList("နိုင်", "ပြု", "ဧည့်", "သ", "သီ", "စ");

}
