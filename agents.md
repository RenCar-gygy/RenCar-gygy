# RenCarApp - Araç Kiralama Uygulaması

> Bu repository RenCarApp adında bir araç kiralama uygulamasının kaynak kodlarını içermektedir. Bu agents.md dosyası bu projede çalışan insan/AI herkesin uyması ZORUNDA olduğu genel kuralları içerir.

---

## 1) TEKNOLOJİ YIĞINI

- Mobil: Android Jetpack Compose, Kotlin
- Mimari: MVI (Model-View-Intent)
- DI: Hilt (KSP)
- Navigasyon: Compose Navigation (iç içe grafikler)
- Harita: MapLibre + OpenStreetMap
- Backend: `https://rencar.halitkalayci.com/` REST API (OpenAPI: `/api/docs`)

---

## 2) GENEL ÇALIŞMA PRENSİPLERİ

### 2.1) TEK SEFERDE DOSYA LİMİTİ

Hangi işlem olursa olsun tek seferde maksimum (birbiriyle alakalı) 5 dosyalar halinde çalışmak zorundasın.
İşlemi birbiriyle bağlantılı batchlere bölmek zorundasın. Eğer bunun aksi talep edilirse DUR ve EK ONAY iste.

### 2.2) UYDURMAK YASAK (NO INVENTING)

Eğer herhangi bir operasyonda bilgi ya da referans eksikliği/hatası yaşıyorsan buradaki eksik/hatalı bilgiyi uydurman yasak. Böyle bir durumda operasyonu durdur ve kullanıcıya sorarak ilerle.

API sözleşmesi için tek kaynak: `https://rencar.halitkalayci.com/api/docs` (JSON: `/api/docs-json`).
Tasarımda olup API'da karşılığı olmayan ekranlar (OTP, cüzdan, teslim fotoğrafı vb.) stub/mock olarak işaretlenir; davranış uydurulmaz.

### 2.3) ÖNCE PLANLA, SONRA KODLA

Kod üretmeden önce şunları yapmak zorundasın;

- Bir dosya dökümü hazırla (hangi dosyalar değişecek/eklenecek/silinecek + neden)
- Eğer varsa yeni bağımlılıklar matrisi (Hangi kütüphane, versiyon + neden)
- Planı sun, onay almadan asla implementasyona başlama.

### 2.4) MİMARİ KURALLAR (GOVERNANCE)

Mimari/teknik kararlar `docs/decisions.md` dosyasında tutulur; bir karar verirken veya
değiştirirken bu dosya güncellenmek ZORUNDADIR.

Sunum katmanı **MVI** ile yazılır. Yeni bir ekran/feature MVI ile kodlanırken aşağıdaki
dökümanlar BAĞLAYICI referanstır ve bunlara uymak ZORUNLUDUR:

- `docs/architecture/mvi-overview.md` — genel prensipler, veri akışı, katman/paket yapısı.
- `docs/architecture/mvi-contracts.md` — State + Intent + Effect kuralları.
- `docs/architecture/mvi-viewmodel-rules.md` — ViewModel, UI bağlama (Route/Screen) ve DI kuralları.

Ekran envanteri: `docs/screens.md`. Klasör yapısı: `docs/folder-structure.md`.

Referans implementasyon henüz atanmadı; ilk tamamlanan Auth ekranı (Login) referans alınacaktır.
Bu referansı bozacak bir sapma gerekiyorsa DUR ve EK ONAY iste (bkz. §2.2).

---

## 3) ÇIKTI FORMATI

Her implementasyon ya da plan sonrası aşağıdaki çıktı formatına uymak zorundasın.

- Dosya Dökümü vermek zorundasın.
- Resmi bir dil kullanmak zorundasın, emoji kullanman yasak.
- Her implementasyon sonrası eğer mümkünse "Happy-Path Test" ver.
- Bu implementasyon ile alaklı varsa sık yapılan hatalar ve senin implementasyon sırasında aklına gelen önerilerini listelemek zorundasın.
