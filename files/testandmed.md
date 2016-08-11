![](../img/EL_struktuuri-_ja_investeerimisfondid_horisontaalne.jpg)

# Etalonteostuse testandmed

### Organisatsioonid
|**nimetus** |**registrikood** |**kirjeldus** |
|--------|-------------|----------|
|Ministeerium X	|40000001 |omab DHX otsevõimekust omab X-teel alamsüsteemi  SUBSYSTEM:ee-dev:GOV:40000001:DHX|
|Hõbekuuli OÜ |30000001 |DHX vahendaja omab alamsüsteemi SUBSYSTEM:ee-dev:COM:30000001:DHX |
|Vallavalitsus A |70000001 |Hõbekuuli OÜ poolt vahendatav asutus, ilma iseseiva DHX võimekuseta|
|Muusem B |70000002	| Hõbekuuli OÜ poolt vahendatav asutus, ilma iseseiva DHX võimekuseta |
|Põhikool C	|70000003 |Hõbekuuli OÜ poolt vahendatav asutus, ilma iseseiva DHX võimekuseta |
|Asutus Y |70000004 | Asutus, mis omab DHX otsevõimekust, kuid kelle DHS (DHS 3) on pidevalt "maas". Kasutatakse "maasolevale" asutusele saatmise testimisel. |
### DHX vahendajate grupp
**Grupi nimetus**: DHX vahendajad

DHX vahendajate grupp, hoitakse X-tee globaalses konfiguratsioonis.
Sisaldab ühte vahendajat:

> SUBSYSTEM:ee-dev/COM/30000001/DHX

### Saatmiseks ettevalmistatud dokumendid
 ```XML
<DecContainer xmlns="http://www.riik.ee/schemas/deccontainer/vers_2_1/">
    <Transport>
        <DecSender>
            <OrganisationCode>30000001</OrganisationCode>
        </DecSender>
        <DecRecipient>
            <OrganisationCode>40000001</OrganisationCode>
        </DecRecipient>
    </Transport>
    <RecordCreator>
    	<Organisation>
     	<Name>Hõbekuuli OÜ</Name>
      	<OrganisationCode>30000001</OrganisationCode>
     	<StructuralUnit>Strateegia ja eelarve osakond </StructuralUnit>
      	<PositionTitle>finantspeaspetsialist</PositionTitle>
    	</Organisation>
       <Person>
            <Name>Kertu Riatesti</Name>
        </Person>
        <ContactData/>
    </RecordCreator>
  <RecordSenderToDec>
    <Organisation>
      <Name>Hõbekuuli OÜ</Name>
      <OrganisationCode>30000001</OrganisationCode>
      <StructuralUnit>Strateegia ja eelarve osakond</StructuralUnit>
    </Organisation>
    <Person>
      <Name>Kertu Riatesti</Name>
    </Person>
    <ContactData/>
  </RecordSenderToDec>
    <Recipient>
	<MessageForRecipient>Dokumendile lisatud kommentaar, kaaskiri saajale.</MessageForRecipient>
        <Organisation>
            <Name>Sotsiaalse Sidususe Ministeerium</Name>
            <OrganisationCode>40000001</OrganisationCode>
        </Organisation>
        <ContactData/>
    </Recipient>
    <RecordMetadata>
        <RecordGuid>56a80d98-0847-41dc-9271-cf98ad24f338</RecordGuid>
        <RecordType>Avaldus / Taotlus</RecordType>
        <RecordOriginalIdentifier>38546</RecordOriginalIdentifier>
        <RecordDateRegistered>2016-02-03T14:35:37.000+02:00</RecordDateRegistered>
        <RecordTitle>Testimiseks bacon</RecordTitle>
	<RecordAbstract>Dokumendi sisu lühiesitus vaba tekstina. Bacon ipsum dolor amet tongue filet mignon pig, spare ribs pork belly alcatra jowl tri-tip. Filet mignon pastrami leberkas cow corned beef landjaeger shankle pork chop. Kielbasa sausage alcatra pastrami salami, ham biltong rump meatloaf porchetta. Sausage ground round short loin, ribeye kielbasa capicola t-bone jowl shank pork loin tail fatback boudin picanha chicken. Short ribs filet mignon tongue flank, tail strip steak boudin swine beef ribs kevin t-bone rump chicken ribeye andouille.</RecordAbstract>
    </RecordMetadata>
    <Access>
        <AccessConditionsCode>AK</AccessConditionsCode>
    </Access>
    <SignatureMetadata>
        <SignatureType>Digitaalallkiri</SignatureType>
        <Signer>HIIRE, KERTU</Signer>
        <SignatureVerificationDate>2016-02-03T14:35:37.000+02:00</SignatureVerificationDate>
    </SignatureMetadata>
    <File>
        <FileGuid>92be817c-e69c-4fd8-9892-ea23907e41a5</FileGuid>
        <FileName>Testimiseks bacon.bdoc</FileName>
        <MimeType>application/octet-stream</MimeType>
        <FileSize>7319</FileSize>
        <ZipBase64Content>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
30f9BOtvJsy/bttfre37GxXwvwDC1im2lxwAAA==
</ZipBase64Content>
    </File>
</DecContainer>
 ```
 
 ```XML
<DecContainer xmlns="http://www.riik.ee/schemas/deccontainer/vers_2_1/">
    <Transport>
        <DecSender>
            <OrganisationCode>40000001</OrganisationCode>
        </DecSender>
        <DecRecipient>
            <OrganisationCode>70000001</OrganisationCode>
        </DecRecipient>
    </Transport>
    <RecordCreator>
    	<Organisation>
     	<Name>Sotsiaalse Sidususe Ministeerium</Name>
      	<OrganisationCode>40000001</OrganisationCode>
     	<StructuralUnit>Administratiivosakond</StructuralUnit>
      	<PositionTitle>Sekretär</PositionTitle>
    	</Organisation>
       <Person>
            <Name>Kertu Riatesti</Name>
        </Person>
        <ContactData/>
    </RecordCreator>
  <RecordSenderToDec>
    <Organisation>
        <Name>Sotsiaalse Sidususe Ministeerium</Name>
        <OrganisationCode>40000001</OrganisationCode>
        <StructuralUnit>Administratiivosakond</StructuralUnit>
    </Organisation>
    <Person>
      <Name>Kertu Riatesti</Name>
    </Person>
    <ContactData/>
  </RecordSenderToDec>
    <Recipient>
	<MessageForRecipient>Dokumendile lisatud kommentaar, kaaskiri saajale.</MessageForRecipient>
        <Organisation>
            <Name>Vallavalitsus A</Name>
            <OrganisationCode>70000001</OrganisationCode>
        </Organisation>
        <ContactData/>
    </Recipient>
    <RecordMetadata>
        <RecordGuid>56a80d98-0847-41dc-9271-cf98ad24f338</RecordGuid>
        <RecordType>Avaldus / Taotlus</RecordType>
        <RecordOriginalIdentifier>38546</RecordOriginalIdentifier>
        <RecordDateRegistered>2016-02-03T14:35:37.000+02:00</RecordDateRegistered>
        <RecordTitle>Testimiseks bacon</RecordTitle>
	<RecordAbstract>Dokumendi sisu lühiesitus vaba tekstina. Bacon ipsum dolor amet tongue filet mignon pig, spare ribs pork belly alcatra jowl tri-tip. Filet mignon pastrami leberkas cow corned beef landjaeger shankle pork chop. Kielbasa sausage alcatra pastrami salami, ham biltong rump meatloaf porchetta. Sausage ground round short loin, ribeye kielbasa capicola t-bone jowl shank pork loin tail fatback boudin picanha chicken. Short ribs filet mignon tongue flank, tail strip steak boudin swine beef ribs kevin t-bone rump chicken ribeye andouille.</RecordAbstract>
    </RecordMetadata>
    <Access>
        <AccessConditionsCode>AK</AccessConditionsCode>
    </Access>
    <SignatureMetadata>
        <SignatureType>Digitaalallkiri</SignatureType>
        <Signer>HIIRE, KERTU</Signer>
        <SignatureVerificationDate>2016-02-03T14:35:37.000+02:00</SignatureVerificationDate>
    </SignatureMetadata>
    <File>
        <FileGuid>92be817c-e69c-4fd8-9892-ea23907e41a5</FileGuid>
        <FileName>Testimiseks bacon.bdoc</FileName>
        <MimeType>application/octet-stream</MimeType>
        <FileSize>7319</FileSize>
        <ZipBase64Content>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
30f9BOtvJsy/bttfre37GxXwvwDC1im2lxwAAA==
</ZipBase64Content>
    </File>
</DecContainer>
 ```