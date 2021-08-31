const list = [
  {
    centerid: "YGN1",
  },
  {
    centerid: "YGN2",
  },
  {
    centerid: "YGN3",
  },
  {
    centerid: "YGN4",
  },
  {
    centerid: "YGN5",
  },
  {
    centerid: "YGN6",
  },
  {
    centerid: "YGN7",
  },
  {
    centerid: "MDY1",
  },
  {
    centerid: "MDY2",
  },
  {
    centerid: "MDY3",
  },
  {
    centerid: "NPW1",
  },
  {
    centerid: "TGI1",
  },
  {
    centerid: "TGI2",
  },
  {
    centerid: "LSO1",
  },
  {
    centerid: "LSO2",
  },
  {
    centerid: "LSO3",
  },
  {
    centerid: "LSO4",
  },
  {
    centerid: "LSO5",
  },
  {
    centerid: "MKN1",
  },
  {
    centerid: "MKN2",
  },
  {
    centerid: "MKN3",
  },
  {
    centerid: "MKN4",
  },
  {
    centerid: "MKN5",
  },
  {
    centerid: "KOK1",
  },
  {
    centerid: "KOK2",
  },
  {
    centerid: "KOK3",
  },
  {
    centerid: "KOK4",
  },
  {
    centerid: "KOK5",
  },
  {
    centerid: "KOK6",
  },
  {
    centerid: "KOK7",
  },
  {
    centerid: "TMN1",
  },
  {
    centerid: "TYN1",
  },
  {
    centerid: "TCK1",
  },
  {
    centerid: "PSN1",
  },
  {
    centerid: "PSN2",
  },
  {
    centerid: "MSE1",
  },
  {
    centerid: "PTN1",
  },
  {
    centerid: "MLM1",
  },
  {
    centerid: "MYK1",
  },
  {
    centerid: "SGN1",
  },
  {
    centerid: "DWI1",
  },
  {
    centerid: "YGN",
  },
  {
    centerid: "YGN10",
  },
  {
    centerid: "YGN11",
  },
  {
    centerid: "YGN8",
  },
  {
    centerid: "YGN9",
  },
];
const query = list
  .map(
    (m) =>
      `update Recipients set centerid = '${m.centerid}' where cid like '${
        m.centerid
      }%' and len(cid) = ${m.centerid.length + 7}`
  )
  .join("; ");
console.log(query);

[
  {
    "NO.": "1",
    "CID no.": "YGN20030479",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "2",
    "CID no.": "YGN20030472",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "3",
    "CID no.": "YGN20030474",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "4",
    "CID no.": "YGN20030462",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "5",
    "CID no.": "YGN20030464",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "6",
    "CID no.": "YGN20030451",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "7",
    "CID no.": "YGN20030435",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "8",
    "CID no.": "YGN20030461",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "9",
    "CID no.": "YGN20030466",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "10",
    "CID no.": "YGN20030463",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "11",
    "CID no.": "YGN20030444",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "12",
    "CID no.": "YGN20030437",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "13",
    "CID no.": "YGN20030445",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "14",
    "CID no.": "YGN20030438",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "15",
    "CID no.": "YGN20030439",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "16",
    "CID no.": "YGN20030443",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "17",
    "CID no.": "YGN20030442",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "18",
    "CID no.": "YGN20030441",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "19",
    "CID no.": "YGN20030447",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "20",
    "CID no.": "YGN20030452",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "21",
    "CID no.": "YGN20030446",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "22",
    "CID no.": "YGN20030449",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "23",
    "CID no.": "YGN20030448",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "24",
    "CID no.": "YGN20030453",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "25",
    "CID no.": "YGN20030454",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "26",
    "CID no.": "YGN20030434",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "27",
    "CID no.": "YGN20030432",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "28",
    "CID no.": "YGN20030433",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "29",
    "CID no.": "YGN20030422",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "30",
    "CID no.": "YGN20030421",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "31",
    "CID no.": "YGN20030395",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "32",
    "CID no.": "YGN20030401",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "33",
    "CID no.": "YGN20030399",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "34",
    "CID no.": "YGN20030402",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "35",
    "CID no.": "YGN20030407",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "36",
    "CID no.": "YGN20030398",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "37",
    "CID no.": "YGN20030409",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "38",
    "CID no.": "YGN20030430",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "39",
    "CID no.": "YGN20030429",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "40",
    "CID no.": "YGN20030424",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "41",
    "CID no.": "YGN20030410",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "42",
    "CID no.": "YGN20030397",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "43",
    "CID no.": "YGN20030403",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "44",
    "CID no.": "YGN20030426",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "45",
    "CID no.": "YGN20030396",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "46",
    "CID no.": "YGN20030412",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "47",
    "CID no.": "YGN20030423",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "48",
    "CID no.": "YGN20030420",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "49",
    "CID no.": "YGN20030400",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "50",
    "CID no.": "YGN20030406",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "51",
    "CID no.": "YGN20030417",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "52",
    "CID no.": "YGN20030411",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "53",
    "CID no.": "YGN20030404",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "54",
    "CID no.": "YGN20030405",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "55",
    "CID no.": "YGN20030418",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "56",
    "CID no.": "YGN20030413",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "57",
    "CID no.": "YGN20030428",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "58",
    "CID no.": "YGN20030408",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "59",
    "CID no.": "YGN20030419",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "60",
    "CID no.": "YGN20030431",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "61",
    "CID no.": "YGN20030415",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "62",
    "CID no.": "YGN20030423",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "63",
    "CID no.": "YGN20030420",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "64",
    "CID no.": "YGN20030400",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "65",
    "CID no.": "YGN20030406",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "66",
    "CID no.": "YGN20030417",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "67",
    "CID no.": "YGN20030411",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "68",
    "CID no.": "YGN20030404",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "69",
    "CID no.": "YGN20030405",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "70",
    "CID no.": "YGN20030418",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "71",
    "CID no.": "YGN20030413",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "72",
    "CID no.": "YGN20030428",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "73",
    "CID no.": "YGN20030408",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "74",
    "CID no.": "YGN20030419",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "75",
    "CID no.": "YGN20030431",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "76",
    "CID no.": "YGN20030415",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "77",
    "CID no.": "YGN20030427",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
  {
    "NO.": "78",
    "CID no.": "YGN20030416",
    "Wrong Lot no.": "202107B1936",
    "Correct Lot.no": "202107B1938",
  },
]
  .map((m) => `'${m["CID no."]}'`)
  .join(", ");
// .map(
//   (m) =>
//     `update DoseRecords set lot = '${m["Correct Lot.no"]}' where cid = '${m["CID no."]}'`
// )
// .join("; ");
