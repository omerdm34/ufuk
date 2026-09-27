---
name: Ufuk
description: Her yerin havası, masa üstü bir hava istasyonunun kadranı gibi okunur.
colors:
  lacquer-0: "#0E1013"
  lacquer-1: "#14171B"
  lacquer-2: "#1A1E23"
  lacquer-3: "#21262C"
  lacquer-4: "#2A3037"
  bone: "#ECE7DB"
  bone-muted: "#A9A396"
  engraving-dark: "#454C55"
  silver-0: "#E6E9EB"
  silver-1: "#F3F5F6"
  silver-2: "#E9ECEE"
  silver-3: "#DEE2E5"
  silver-4: "#D2D7DB"
  ink: "#15181B"
  ink-muted: "#545B62"
  engraving-light: "#AEB5BB"
  gilt-on-dark: "#C9A45C"
  gilt-on-light: "#8A6A24"
  vermilion-on-dark: "#E2583F"
  vermilion-on-light: "#B8321F"
  steel-on-dark: "#86ABD9"
  steel-on-light: "#2F5E96"
  error-on-dark: "#F2B8B5"
  error-on-light: "#B3261E"
typography:
  display:
    fontFamily: "Barlow Condensed"
    fontSize: "104sp"
    fontWeight: 300
    lineHeight: "104sp"
    letterSpacing: "-0.02em"
    fontFeature: "tnum"
  display-small:
    fontFamily: "Barlow Condensed"
    fontSize: "40sp"
    fontWeight: 300
    lineHeight: "44sp"
    fontFeature: "tnum"
  headline:
    fontFamily: "Barlow Condensed"
    fontSize: "24sp"
    fontWeight: 400
    lineHeight: "30sp"
    fontFeature: "tnum"
  title:
    fontFamily: "Barlow Condensed"
    fontSize: "24sp"
    fontWeight: 500
    lineHeight: "30sp"
  body:
    fontFamily: "Roboto"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
  label:
    fontFamily: "Barlow Condensed"
    fontSize: "13sp"
    fontWeight: 500
    lineHeight: "16sp"
    letterSpacing: "0.1em"
    fontFeature: "tnum"
rounded:
  xs: "4dp"
  sm: "6dp"
  md: "10dp"
  lg: "14dp"
  xl: "20dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "16dp"
  lg: "24dp"
  xl: "36dp"
components:
  button-tonal:
    backgroundColor: "{colors.silver-3}"
    textColor: "{colors.ink}"
    typography: "{typography.label}"
    rounded: "{rounded.xl}"
    height: "40dp"
  chip-filter-selected:
    backgroundColor: "{colors.silver-3}"
    textColor: "{colors.ink}"
    rounded: "{rounded.sm}"
    height: "32dp"
  input-search:
    backgroundColor: "{colors.silver-0}"
    textColor: "{colors.ink}"
    rounded: "{rounded.md}"
    height: "56dp"
  navigation-bar:
    backgroundColor: "{colors.silver-2}"
    textColor: "{colors.ink-muted}"
    height: "80dp"
  place-row:
    backgroundColor: "{colors.silver-0}"
    textColor: "{colors.ink}"
    padding: "10dp 16dp"
    height: "76dp"
---

# Design System: Ufuk

## Overview

**Creative North Star: "Masa üstü hava istasyonu"**

Ufuk, evlerde duvara asılan pirinç barometre, termometre ve nemölçer takımının ekrandaki karşılığıdır. Her yer bir alet kadranıyla okunur:

- Ölçeğin üzerinde canlı ibre şu anki değeri gösterir.
- İnce pirinç ayar ibresi dün aynı saatteki değeri gösterir.
- Yaldız yay günün aralığını işaretler.

Değişim, sayı okunmadan görülür. Kadranın altındaki kısa cümleler ne yapılacağını söyler.

İki tema vardır ve ikisi de tasarlanmıştır:

- **Siyah lake kadran (koyu):** sabah karanlıkta bakılan telefon için.
- **Gümüş kadran (açık):** gün ışığında dışarıda bakılan telefon için.

Yüzey sakindir. Degrade, buzlu cam ve gölge yoktur; derinlik yalnızca kadran yüzü ile zemin arasındaki ton farkından gelir. Kategorinin varsayılanı olan "hava durumuna göre renk değiştiren gökyüzü degradesi ve cam kartlar" bilinçli olarak reddedilmiştir.

**Key Characteristics:**
- Yuvarlak olan yalnızca kadranlardır; geri kalan her şey düz çizgi ve düz yüzeydir.
- Tek sıcak renk (vermilyon) yalnızca canlı okumaya aittir.
- Rakamlar dar ve ince (Barlow Condensed Light), tablo gibi hizalı.
- Etiketler alet üzerine kazınmış gibi: küçük, dar, geniş aralıklı, büyük harf.
- Bayat veri soluklaşır ve yaşını söyler.

## Colors

Nötr bir alet gövdesi, iki metal (yaldız, mavi çelik) ve tek bir canlı vurgu (vermilyon).

### Primary
- **Yaldız** (gilt-on-dark / gilt-on-light):
  - Kazıma vurgusu: bugünün aralık yayı ve çubukları, pirinç ayar ibresi, güneş yayı.
  - Kaydedilmiş yer yıldızı ve güncel veri noktası.
  - Material `primary` rolü.

### Secondary
- **Mavi çelik** (steel-on-dark / steel-on-light): yalnızca yağış. Saatlik yağış çubukları, günlük yağış yüzdesi, yağmur ve çisenti damlaları.

### Tertiary
- **Vermilyon** (vermilion-on-dark / vermilion-on-light): canlı okuma ibresi, saatlik grafikte "Şimdi" noktası ve etiketi, günlük ve satır çubuklarında şimdiki değer noktası, alet ibreleri.

### Neutral
- **Siyah lake** (lacquer-0…4): koyu tema zemini (`lacquer-1`), kadran yüzü (`lacquer-2`), kapsayıcılar ve çerçeve (`lacquer-3/4`).
- **Kemik** (bone, bone-muted): koyu temada metin ve ikincil metin, kazıma etiketler.
- **Gümüş** (silver-0…4): açık tema zemini (`silver-0`), kadran yüzü (`silver-1`), kapsayıcılar ve çerçeve.
- **Mürekkep** (ink, ink-muted): açık temada metin ve ikincil metin.
- **Kazıma çizgisi** (engraving-dark / engraving-light): ince çentikler ve ayırıcılar.

### Named Rules
**The Live Reading Rule.** Vermilyon yalnızca "şu an" ölçülen değeri gösteren öğeye verilir. Başlık, düğme, bildirim ya da süs için kullanılmaz.

**The No Cream Rule.** Açık tema soğuk gümüştür (#E6E9EB). Sıcak krem ya da kâğıt tonu kullanılmaz.

**The Stale Dims Rule.** Önbellekten gelen eski veride canlı ibre %40 opaklığa iner ve tazelik notu içi boş halka ile hata rengine döner. Renk tek başına anlam taşımaz; yaş her zaman yazıyla da söylenir.

## Typography

**Display Font:** Barlow Condensed (res/font, OFL; Light, Regular, Medium, SemiBold)
**Body Font:** Roboto (sistem)
**Label Font:** Barlow Condensed Medium

**Character:** Dar grotesk rakamlar 60'lar–70'ler barometre ve termometre kadranlarının ölçek yazısıdır. Uzun metinler (özet cümleleri, açıklamalar) okunaklı sistem yazısında kalır.

### Hierarchy
- **Display** (Light, 104sp, -0.02em, tnum): tek bir yerde kullanılır, kadrandaki şimdiki sıcaklık. Kadran 300dp'den darsa 56sp'ye iner.
- **Display small** (Light, 40sp): liste satırlarındaki sıcaklık; satırın en büyük öğesi.
- **Headline** (Regular, 24sp, tnum): günlük satırlarda en düşük/en yüksek, okuma değerleri (Hissedilen, Rüzgâr).
- **Title** (Medium, 24sp): ekran başlıkları, yer adları, alet değerleri.
- **Body** (Roboto 14–16sp): özet cümleleri (ilki titleMedium kalın), açıklamalar, ayarlar.
- **Label** (Medium, 11–16sp, 0.04–0.12em): kazıma etiketler (`SAATLİK`, `NEM`), saatler, kadran rakamları. Büyük harfe cihaz diliyle çevrilir.

### Named Rules
**The Small Degree Rule.** Derece işareti (°) Barlow'da değil sistem yazısında çizilir (`degreesText`); iri Barlow halkası rakamdan kopuk durur.

**The Tabular Rule.** Tüm rakamlar `tnum` ile yazılır. Alt alta gelen sıcaklıklar ve saatler kaymaz.

## Layout

Tek sütunlu dikey akış, 16–20dp kenar boşluğu. Bölümler kazıma etiketle açılır: başlığın üstünde 36dp, altında 14dp.

- Kutu yoktur; satırlar ve bölümler ince ayırıcı çizgilerle ayrılır.
- Liste hiyerarşisini yazı boyutu kurar: ikon (32dp), ad ve durum, sıcaklık ve bugünün aralık çubuğu.
- Tahmin ekranı yukarıdan aşağı şu sırayla akar: kadran (en fazla 340dp), gösterge açıklaması, özet cümleleri, üç okuma (dikey ayırıcılarla), 24 saatlik yatay şerit (sütun 56dp), 10 günlük aralık çubukları (hepsi aynı ölçekte), 2 sütunlu alet levhası (96dp kadranlar), atıf.
- 600dp ve üstü genişlikte alt çubuk yerine yan ray kullanılır ve içerik ortada en fazla 720dp genişliğinde kalır.

## Elevation & Depth

Gölge yoktur. Derinlik iki yolla verilir: kadran yüzünün (`lacquer-2` / `silver-1`) zeminden bir ton farklı olması ve 1–1,5dp'lik çerçeve halkası. İbreler yüzeyin üstünde çizilir; ibre göbeğinin içindeki yüz rengi nokta mili gösterir.

### Named Rules
**The Flat Instrument Rule.** Kabartma, iç gölge, parlama ya da metal dokusu taklit edilmez. Alet, çizgi ve ton ile anlatılır.

## Shapes

Alet gövdesi gibi küçük köşeler: 4 / 6 / 10 / 14 / 20dp. Yuvarlak olan yalnızca kadranlar, ibre göbekleri ve çubuk uçlarıdır.

- Ölçekler 270°'dir: 135°'den başlar, altta 90°'lik boşluk kalır. Dijital okuma bu boşluğa yazılır.
- Pusula 360° döner, kuzey yukarıdadır.
- Güneş yayı yarım dairedir.

## Components

### Temperature Dial (imza bileşen)
- Sırasıyla çizilen katmanlar:
  1. yüz ve çerçeve;
  2. 1° (geniş ölçekte 2°) çentikler, 5'lik (ya da 10'luk) uzun çentikler ve rakamlar;
  3. yaldız aralık yayı;
  4. alt boşlukta dijital okuma ve durum etiketi;
  5. pirinç ayar ibresi (baklava uçlu, 1,5dp);
  6. vermilyon canlı ibre (5dp, kısa kuyruk, göbek).
- Açılışta ibre yaylı salınımla yerine oturur (damping 0,42, stiffness 55). "Animasyonları kaldır" açıksa doğrudan yerindedir.
- Ekran okuyucu kadranı tek cümle olarak okur.

### Instrument Gauges
- 96dp küçük kadranlar: rüzgâr pusulası, nem (0–100), barometre (960–1060 hPa, ayar ibresi 3 saat önce), UV (0–12), hava kalitesi (0–100), güneş yayı.
- Ölçeğin uç değerleri alt boşluğa kazınır.
- Altında sırasıyla kazıma etiket, değer (title) ve not (bodySmall) yer alır.

### Buttons
- **Shape:** Material varsayılanı tam yuvarlak.
- **Tonal:** birincil eylem ("Konumumu kullan"), ikonlu.
- **Outlined:** hata ekranında "Tekrar dene".
- **Text:** ikincil yönlendirme ("Keşfet'e git").

### Chips
- **Style:** FilterChip; Keşfet sıralaması (A–Z / En sıcak / En serin). Seçili olanın zemini `primaryContainer`.

### Inputs / Fields
- **Style:** OutlinedTextField, 10dp köşe, arama ikonu ve temizle düğmesi. Klavyede "Ara" eylemi odağı kapatır.

### Navigation
- Alt çubuk (üç hedef), 600dp ve üstünde yan ray.
- Seçili hedef dolu ikon, `primaryContainer` gösterge ve `onSurface` yazı. Seçili olmayan hedef çizgi ikon ve `onSurfaceVariant` yazı.

### Place Row
- Kutusuz satır, en az 76dp, hairline ayırıcı.
- Solda 32dp çizgi hava ikonu. Ortada yer adı (title) ile "durum · yerel saat". Sağda sıcaklık (display small), altında 56dp bugünün aralık çubuğu ve "en yüksek / en düşük".
- Keşfet'te sağda yıldız düğmesi vardır (boş çizgi / dolu yaldız).
- Yerlerim'de satır sola kaydırılarak kaldırılır; aynı eylem TalkBack özel eylemlerinde de bulunur.

### Weather Glyphs
- 24 birimlik ızgarada, 1,6 birimlik tek çizgi kalınlığında elle çizilmiş ikonlar: güneş/ay, parçalı, bulut, sis, çisenti, yağmur, kar, fırtına.
- Renkler: güneş ve şimşek yaldız, damlalar mavi çelik, geri kalan metin rengi.

## Do's and Don'ts

### Do:
- **Do** her yeni ölçümü kendi kadranına ya da ölçeğine koy: ibre şimdiki değer, varsa pirinç ibre önceki okuma.
- **Do** eski veriyi yaşıyla göster ("Güncellenemedi · 25 dk önceki veri") ve canlı ibreyi soluklaştır.
- **Do** sıcaklıkları `degreesText` ile yaz ve rakamlarda `tnum` kullan.
- **Do** etiketleri `EngravedLabel` ile yaz; büyük harfe cihaz diliyle çevrilir ("İNDEKSİ").
- **Do** her satırı ve aleti TalkBack için tek cümle yap (`clearAndSetSemantics`).

### Don't:
- **Don't** gökyüzü degradesi, buzlu cam kart ya da hava durumuna göre renk değiştiren zemin kullanma.
- **Don't** vermilyonu canlı okuma dışında bir şeye verme.
- **Don't** emoji ya da ikon fontu kullanma; hava ikonları `WeatherGlyph` ile çizilir.
- **Don't** Material You dinamik rengini açma; duvar kâğıdı rengi tek vurgu kuralını bozar.
- **Don't** listeleri kartlara koyma; hiyerarşiyi yazı boyutu ve ayırıcılar kurar.
