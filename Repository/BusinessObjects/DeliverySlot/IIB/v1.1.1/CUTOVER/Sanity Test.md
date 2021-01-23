# Input Message
```
{
  "slotId": "0dc86063-99e0-479d-a1f0-d7f5019b8018",
  "storeId": "1623",
  "dayOfWeek": "THURSDAY",
  "slotStartTS": "2019-10-31T09:02:00.000-07:00",
  "slotEndTS": "2019-10-31T10:00:00.000-07:00",
  "slotExpiryTS": "2019-10-31T01:00:00.000-07:00",
  "maxDeliveries": 3,
  "serviceType": "DELIVERY",
  "deliveryType": [
    "ATTENDED",
    "UNATTENDED"
  ],
  "b2bCharge": {
    "userType": "BUSINESS",
    "deliveryCharge": 5.95,
    "minimumBasketSize": 150,
    "alertBasketSize": 9999,
    "reducedDeliveryCharge": 5.95,
    "deliveryChargeUPC": "0000000022151",
    "reducedDeliveryChargeUPC": "0000000022151"
  },
  "b2cCharge": {
    "userType": "RESIDENTIAL",
    "deliveryCharge": 5.95,
    "minimumBasketSize": 150,
    "alertBasketSize": 9999,
    "reducedDeliveryCharge": 5.95,
    "deliveryChargeUPC": "0000000022151",
    "reducedDeliveryChargeUPC": "0000000022151"
  },
  "slotType": "ONEHR",
  "slotPlan": "STANDARD",
  "reservedByCurrentUser": false,
  "bookings": [
    {
      "userGuid": "300-090-1404680433134",
      "storeId": "1623",
      "orderId": "4860379",
      "versionNumber": "1",
      "deliveryType": "ATTENDED",
      "expiryTime": "2019-10-31T01:00:00.000-07:00"
    },
    {
      "userGuid": "520-000-0290087547378",
      "storeId": "1623",
      "orderId": "12016946",
      "cartId": "3060470",
      "deliveryType": "ATTENDED",
      "expiryTime": "2019-10-29T11:00:23.000-07:00",
      "bookingCategory": "Rebooking"
    },
    {
      "userGuid": "300-090-1546398103054",
      "storeId": "1623",
      "cartId": "3260544",
      "deliveryType": "ATTENDED",
      "expiryTime": "2019-10-30T15:06:59.000-07:00",
      "bookingCategory": "Subscribed"
    }
  ],
  "createdDate": "2019-10-23T22:19:02.525-07:00",
  "lastModifiedDate": "2019-10-23T22:19:02.525-07:00"
}

```

# Output Message
```
<?xml version="1.0" encoding="UTF-8"?>
<GetDeliverySlot xmlns:Abs="https://collab.safeway.com/it/architecture/info/default.aspx">
    <DocumentData>
        <Document SystemEnvironmentCd="PROD" VersionId="1.2.1.022">
            <Abs:DocumentID>DeliverySlot</Abs:DocumentID>
            <Abs:AlternateDocumentID>DeliverySlot_20201005090024716415</Abs:AlternateDocumentID>
            <Abs:DocumentNm>DeliverySlot</Abs:DocumentNm>
            <Abs:CreationDt>2020-10-05T09:00:24.716415-07:00</Abs:CreationDt>
            <Abs:Description>Time window slots for an online customer to place an order</Abs:Description>
            <Abs:SourceApplicationCd>OSMS</Abs:SourceApplicationCd>
            <Abs:TargetApplicationCd>EDIS</Abs:TargetApplicationCd>
            <Abs:InternalFileTransferInd>Y</Abs:InternalFileTransferInd>
            <Abs:DataClassification>
                <Abs:DataClassificationLevel>
                    <Abs:Code>Internal</Abs:Code>
                </Abs:DataClassificationLevel>
                <Abs:BusinessSensitivityLevel>
                    <Abs:Code>Low</Abs:Code>
                </Abs:BusinessSensitivityLevel>
                <Abs:PHIdataInd>N</Abs:PHIdataInd>
                <Abs:PCIdataInd>N</Abs:PCIdataInd>
                <Abs:PIIdataInd>N</Abs:PIIdataInd>
            </Abs:DataClassification>
        </Document>
        <DocumentAction>
            <Abs:ActionTypeCd>UPDATE</Abs:ActionTypeCd>
            <Abs:RecordTypeCd>CHANGE</Abs:RecordTypeCd>
        </DocumentAction>
    </DocumentData>
    <DeliverySlotData>
        <Abs:DeliveryAreaType>
            <Abs:DeliveryAreaValueTxt>1623</Abs:DeliveryAreaValueTxt>
        </Abs:DeliveryAreaType>
        <Abs:SlotId>0dc86063-99e0-479d-a1f0-d7f5019b8018</Abs:SlotId>
        <Abs:SlotWindow>
            <Abs:StartTs>2019-10-31T09:02:00.000-07:00</Abs:StartTs>
            <Abs:EndTs>2019-10-31T10:00:00.000-07:00</Abs:EndTs>
            <Abs:ExpiryTs>2019-10-31T01:00:00.000-07:00</Abs:ExpiryTs>
            <Abs:DayOfWeek>THURSDAY</Abs:DayOfWeek>
        </Abs:SlotWindow>
        <Abs:SlotType>
            <Abs:Code>ONEHR</Abs:Code>
        </Abs:SlotType>
        <Abs:SlotPlanType>
            <Abs:Code>STANDARD</Abs:Code>
        </Abs:SlotPlanType>
        <Abs:MaxDeliveryCnt>3</Abs:MaxDeliveryCnt>
        <Abs:DeliveryServiceType>
            <Abs:Code>DELIVERY</Abs:Code>
        </Abs:DeliveryServiceType>
        <Abs:DeliveryReceiveType>
            <Abs:Code>ATTENDED</Abs:Code>
        </Abs:DeliveryReceiveType>
        <Abs:DeliveryReceiveType>
            <Abs:Code>UNATTENDED</Abs:Code>
        </Abs:DeliveryReceiveType>
        <Abs:DeliveryChargeType>
            <Abs:CustomerType>
                <Abs:Code>BUSINESS</Abs:Code>
            </Abs:CustomerType>
            <Abs:DeliveryChargeAmt>5.95</Abs:DeliveryChargeAmt>
            <Abs:DiscountedDeliveryChargeAmt>5.95</Abs:DiscountedDeliveryChargeAmt>
            <Abs:DeliveryChargeId>0000000022151</Abs:DeliveryChargeId>
            <Abs:DiscountedDeliveryChargeId>0000000022151</Abs:DiscountedDeliveryChargeId>
            <Abs:BasketSizeCnt>150</Abs:BasketSizeCnt>
            <Abs:AlertBasketSizeCnt>9999</Abs:AlertBasketSizeCnt>
        </Abs:DeliveryChargeType>
        <Abs:DeliveryChargeType>
            <Abs:CustomerType>
                <Abs:Code>RESIDENTIAL</Abs:Code>
            </Abs:CustomerType>
            <Abs:DeliveryChargeAmt>5.95</Abs:DeliveryChargeAmt>
            <Abs:DiscountedDeliveryChargeAmt>5.95</Abs:DiscountedDeliveryChargeAmt>
            <Abs:DeliveryChargeId>0000000022151</Abs:DeliveryChargeId>
            <Abs:DiscountedDeliveryChargeId>0000000022151</Abs:DiscountedDeliveryChargeId>
            <Abs:BasketSizeCnt>150</Abs:BasketSizeCnt>
            <Abs:AlertBasketSizeCnt>9999</Abs:AlertBasketSizeCnt>
        </Abs:DeliveryChargeType>
        <Abs:ReservedInd>false</Abs:ReservedInd>
        <Abs:SlotBookingType>
            <Abs:Customer>
                <Abs:GUID>300-090-1404680433134</Abs:GUID>
            </Abs:Customer>
            <Abs:ExpiryTs>2019-10-31T01:00:00.000-07:00</Abs:ExpiryTs>
            <Abs:BookingNbr>4860379</Abs:BookingNbr>
            <Abs:VersionNbr>1</Abs:VersionNbr>
            <Abs:OrderId>4860379</Abs:OrderId>
            <Abs:DeliveryServiceType>
                <Abs:Code>ATTENDED</Abs:Code>
            </Abs:DeliveryServiceType>
            <Abs:BookingCategoryType/>
        </Abs:SlotBookingType>
        <Abs:SlotBookingType>
            <Abs:Customer>
                <Abs:GUID>520-000-0290087547378</Abs:GUID>
            </Abs:Customer>
            <Abs:CartNbr>3060470</Abs:CartNbr>
            <Abs:ExpiryTs>2019-10-29T11:00:23.000-07:00</Abs:ExpiryTs>
            <Abs:BookingNbr>12016946</Abs:BookingNbr>
            <Abs:OrderId>12016946</Abs:OrderId>
            <Abs:DeliveryServiceType>
                <Abs:Code>ATTENDED</Abs:Code>
            </Abs:DeliveryServiceType>
            <Abs:BookingCategoryType>
                <Abs:Code>Rebooking</Abs:Code>
            </Abs:BookingCategoryType>
        </Abs:SlotBookingType>
        <Abs:SlotBookingType>
            <Abs:Customer>
                <Abs:GUID>300-090-1546398103054</Abs:GUID>
            </Abs:Customer>
            <Abs:CartNbr>3260544</Abs:CartNbr>
            <Abs:ExpiryTs>2019-10-30T15:06:59.000-07:00</Abs:ExpiryTs>
            <Abs:DeliveryServiceType>
                <Abs:Code>ATTENDED</Abs:Code>
            </Abs:DeliveryServiceType>
            <Abs:BookingCategoryType>
                <Abs:Code>Subscribed</Abs:Code>
            </Abs:BookingCategoryType>
        </Abs:SlotBookingType>
        <Abs:Auditdata>
            <Abs:CreateDtTm>2019-10-23T22:19:02.525-07:00</Abs:CreateDtTm>
            <Abs:UpdateDtTm>2019-10-23T22:19:02.525-07:00</Abs:UpdateDtTm>
        </Abs:Auditdata>
    </DeliverySlotData>
</GetDeliverySlot>
```