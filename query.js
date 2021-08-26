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
