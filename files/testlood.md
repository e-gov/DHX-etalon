![](../img/EL_struktuuri-_ja_investeerimisfondid_horisontaalne.jpg)

# Etalonteostuse testilood
## Ülevaade
Käesolev dokument määratleb DHX etalonteostuse testimise ulatuse, korralduse ja üksikasjad.
Testitakse vastavust ["Etalonteostuse spetsifikatsioonile"](https://github.com/e-gov/DHX-etalon/blob/master/files/spekk.md).
Testimise eripäraks on testimise ja kasutamise ühtimine - etalonteostuse kasutamine seisneb testide läbitegemises erinevate inimeste poolt.
Testandmed on määratletud eraldi dokumendis:  ["Etalonteostuse testandmed"](https://github.com/e-gov/DHX-etalon/blob/master/files/testandmed.md).

## Testilood

##### Tähistused:
* korrektselt kapseldatud fail - Elektroonilise andmevahetuse metaandmete loendile 2.1 vastavalt korrektselt kapseldatud fail. (Varasema toetus?)
* DHS 1 - DHS makett 1; DHS 2 - DHS makett 2.

### 1. PÕHIJUHT: Õige kapsli saatmine, adressaadil on DHX otsevõimekus

&nbsp;&nbsp;**Saatev süsteem**: DHS 1

&nbsp;&nbsp;**Saatja**: Ministeerium X

&nbsp;&nbsp;**Vastuvõttev süsteem**: DHS 2

&nbsp;&nbsp;**Adressaat**: Hõbekuuli OÜ

&nbsp;&nbsp;**Saadetis**: korrektselt kapseldatud fail

&nbsp;&nbsp;**Verifitseerija toimimine (samm-sammuline)**:

* Inimene valib Dokumendi saatmine tab-i Tegevused regioonis
* Valib rippmenüüst Vali dokument väärtuse korrektselt kapseldatud
* Valib rippmenüüst Vali adressaat väärtuse Hõbekuuli OÜ, registrikood 30000001
* Vajutab nupule Saada dokument

&nbsp;&nbsp;**Oodatav väljund**:
* dokument on vastu võetud
* saatvale süsteemile on saadetud õige vastuskood
* kajastatud nii saatva süsteemi kui ka vastuvõtva süsteemi sündmuste logis


### 2. Vale kapsli saatmine	

&nbsp;&nbsp;**Saatev süsteem**: DHS 1

&nbsp;&nbsp;**Saatja**: Ministeerium X

&nbsp;&nbsp;**Vastuvõttev süsteem**: DHS 2

&nbsp;&nbsp;**Adressaat**: Hõbekuuli OÜ

&nbsp;&nbsp;**Saadetis**: kapsli fail, mis ei vasta Elektroonilise andmevahetuse metaandmete loendile 2.1 (nt puudu kohustuslik väli), aga on XML fail õige XML vorminguga

&nbsp;&nbsp;**Verifitseerija toimimine (samm-sammuline)**:

* Inimene valib Dokumendi saatmine tab-i Tegevused regioonis
* Valib rippmenüüst Vali dokument väärtus valesti kapseldatud
* Valib rippmenüüst Vali adressaat väärtus Hõbekuuli OÜ, registrikood 30000001
* Vajutab nupule Saada dokument

&nbsp;&nbsp;**Oodatav väljund**:
* dokumendi saatmine ebaõnnestus.
* vastuses on DHX.Validation koodiga fault 
* kajastatud sündmuste logis

### 3. Faili saatmine (fail ei ole kapsel)

&nbsp;&nbsp;**Saatev süsteem**: DHS 1

&nbsp;&nbsp;**Saatja**: Ministeerium X

&nbsp;&nbsp;**Vastuvõttev süsteem**: DHS 2

&nbsp;&nbsp;**Adressaat**: Hõbekuuli OÜ

&nbsp;&nbsp;**Saadetis**: fail mis ei ole XML või XML vale vorminguga.

&nbsp;&nbsp;**Verifitseerija toimimine (samm-sammuline)**:

* Inimene valib Dokumendi saatmine tab-i Tegevused regioonis
* Valib rippmenüüst Vali dokument väärtus vale XML või mitte XML fail
* Valib rippmenüüst Vali adressaat väärtus Hõbekuuli OÜ, registrikood 30000001
* Vajutab nupule Saada dokument

&nbsp;&nbsp;**Oodatav väljund**:
* dokumendi saatmine ebaõnnestus
* vastuses on DHX.Validation koodiga fault
* kajastatud sündmuste logis

### 4. Duplikaadi kontroll

&nbsp;&nbsp;**Saatev süsteem**: DHS 1

&nbsp;&nbsp;**Saatja**: Ministeerium X

&nbsp;&nbsp;**Vastuvõttev süsteem**: DHS 2

&nbsp;&nbsp;**Adressaat**: Hõbekuuli OÜ

&nbsp;&nbsp;**Saadetis**: korrektselt kapseldatud fail

&nbsp;&nbsp;**Verifitseerija toimimine (samm-sammuline)**:

* Inimene valib Dokumendi saatmine tab-i Tegevused regioonis
* Eemaldab  linnukese märkeruudust  'Genereeri saadetise ID automaatselt'
* Sisestab välja Saadetise ID väärtuse, millega on eelnevalt dokument juba saadetud (väärtuse saab sündmuste logist, sündmuse logist tuleb kopeerida õnnestunud saatmise internalConsignmentId. Sündmuse logi näide: Sending document to: addressee: 30000001, X-road member: ee-dev/COM/30000001/DHX, is representee: false internalConsignmentId: **7e8d0dbc-8a04-48c6-a509-6ef25eb38c7b**)
* Valib rippmenüüst Vali dokument väärtus korrektselt kapseldatud
* Valib rippmenüüst Vali adressaat väärtus Hõbekuuli OÜ, registrikood 30000001
* Vajutab nupule Saada dokument

*DHS 2 tunneb ära, et on sama saadetise juba edukalt vastu võtnud ja tagastab vastava veateate.*

&nbsp;&nbsp;**Oodatav väljund**:
* dokument on tagasi lükatud
* vastuses on DHX.Duplicate koodiga fault
* kajastatud sündmuste logis


### 5. Adressaat "ei ole üleval"

&nbsp;&nbsp;**Saatev süsteem**: DHS 1

&nbsp;&nbsp;**Saatja**: Ministeerium X

&nbsp;&nbsp;**Vastuvõttev süsteem**: DHS 3

&nbsp;&nbsp;**Adressaat**: Asutus Y

&nbsp;&nbsp;**Saadetis**: korrektselt kapseldatud fail

&nbsp;&nbsp;**Verifitseerija toimimine (samm-sammuline)**:

* Inimene valib Dokumendi saatmine tab-i Tegevused regioonis
* Valib rippmenüüst Vali dokument väärtuse korrektselt kapseldatud
* Valib rippmenüüst Vali adressaat väärtuse Asutus Y, registrikood 70000004
* Vajutab nupule Saada dokument

*Asutusel Y on DHX-i võimekus (otse) s.t. globaalses konf-s on tema alamsüsteemi DHX kohta kirje, kuid tema DHS ei ole "üleval".*

&nbsp;&nbsp;**Oodatav väljund**:
* saatmine ebaõnnestub
* kajastatud sündmuste logis

### 6. Vahendatavale saatmine

&nbsp;&nbsp;**Saatev süsteem**: DHS 1

&nbsp;&nbsp;**Saatja**: Ministeerium X

&nbsp;&nbsp;**Vastuvõttev süsteem**: DHS 2

&nbsp;&nbsp;**Adressaat**: Vallavalitsus A, registrikood 70000001 (üks Hõbekuuli OÜ vahendatavatest asutustest)

&nbsp;&nbsp;**Saadetis**: korrektselt kapseldatud fail

&nbsp;&nbsp;**Verifitseerija toimimine (samm-sammuline)**:

* Inimene valib Dokumendi saatmine tab-i Tegevused regioonis
* Valib rippmenüüst Vali dokument väärtuse korrektselt kapseldatud
* Valib rippmenüüst Vali adressaat väärtuse Vallavalitsus A, registrikood 70000001
* Vajutab nupule Saada dokument

&nbsp;&nbsp;**Oodatav väljund**:
* dokument on vastu võetud
* saatvale süsteemile on saadetud õige vastuskood
* kajastatud sündmuste logis

### 7. Sisse tulnud valesti adresseeritud dokument

&nbsp;&nbsp;**Saatev süsteem**: DHS 2

&nbsp;&nbsp;**Saatja**: Hõbekuuli OÜ

&nbsp;&nbsp;**Vastuvõttev süsteem**: DHS 1

&nbsp;&nbsp;**Adressaat**: kapslis on adressaat, mis ei ole Ministeerium X

&nbsp;&nbsp;**Saadetis**: korrektselt kapseldatud fail

&nbsp;&nbsp;**Verifitseerija toimimine (samm-sammuline)**:

* Inimene valib Dokumendi saatmine tab-i Tegevused regioonis
* Valib rippmenüüst Vali dokument väärtuse kapslis vale adressaat
* Valib rippmenüüst Vali adressaat väärtuse Vallavalitsus A, registrikood 70000001
* Vajutab nupule Saada dokument

*Ministeeriumi X süsteem (makett 1) peab ära tundma, et dokument on valesti adresseeritud.*

&nbsp;&nbsp;**Oodatav väljund**:
* dokumendi saatmine ebaõnnestus.
* vastuses on DHX.InvalidAddressee koodiga fault.

### 8. Automaatne dokumendi saatmine

&nbsp;&nbsp;**Saatev süsteem**: DHS 1

&nbsp;&nbsp;**Saatja**: Ministeerium X

&nbsp;&nbsp;**Vastuvõttev süsteem**: DHS 2

&nbsp;&nbsp;**Adressaat**: Hõbekuuli OÜ

&nbsp;&nbsp;**Saadetis**: korrektselt kapseldatud fail

&nbsp;&nbsp;**Verifitseerija toimimine (samm-sammuline)**:

* Dokumendi saadetakse automaatselt seadistatud perioodilisusega. Inimene kontrollib automaatse dokumentide saatmiste olemasolu ja saatmiste staatusi sündmuste logis.

&nbsp;&nbsp;**Oodatav väljund**:
* dokument on vastu võetud
* saatvale süsteemile on saadetud õige vastuskood
* kajastatud sündmuste logis