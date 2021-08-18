package com.dvc.constants;

import java.util.Arrays;
import java.util.List;

import com.dvc.models.ComboData;

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

}
